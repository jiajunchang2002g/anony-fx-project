package com.example.paymentplatform.service;

import com.example.paymentplatform.config.FinancialProperties;
import com.example.paymentplatform.config.FxProperties;
import com.example.paymentplatform.dto.fx.FxRateResponse;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.FxRate;
import com.example.paymentplatform.exception.NotFoundException;
import com.example.paymentplatform.repository.FxRateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FxRateService {

  private final FxRateProvider provider;
  private final FxRateCache cache;
  private final FxRateRepository fxRateRepository;
  private final FxProperties fxProperties;
  private final FinancialProperties financialProperties;

  public FxRateService(FxRateProvider provider, FxRateCache cache,
                       FxRateRepository fxRateRepository,
                       FxProperties fxProperties,
                       FinancialProperties financialProperties) {
    this.provider = provider;
    this.cache = cache;
    this.fxRateRepository = fxRateRepository;
    this.fxProperties = fxProperties;
    this.financialProperties = financialProperties;
  }

  @Transactional(readOnly = true)
  public BigDecimal getRate(Currency baseCurrency, Currency quoteCurrency) {
    if (baseCurrency == quoteCurrency) {
      return BigDecimal.ONE.setScale(financialProperties.scale(),
                                     financialProperties.roundingMode());
    }
    Optional<BigDecimal> cached = cache.getRate(baseCurrency, quoteCurrency);
    if (cached.isPresent()) {
      return cached.get();
    }
    Optional<FxRate> latest =
        fxRateRepository
            .findTopByBaseCurrencyAndQuoteCurrencyOrderByFetchedAtDesc(
                baseCurrency, quoteCurrency);
    if (latest.isPresent() &&
        latest.get().getExpiresAt().isAfter(Instant.now())) {
      BigDecimal rate = latest.get().getRate();
      cache.putRate(baseCurrency, quoteCurrency, rate,
                    fxProperties.cacheTtlSeconds());
      return rate;
    }
    return refreshRates(baseCurrency).get(quoteCurrency);
  }

  @Transactional
  public Map<Currency, BigDecimal> refreshRates(Currency baseCurrency) {
    Map<Currency, BigDecimal> baseRates = provider.fetchBaseRates(baseCurrency);
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(fxProperties.cacheTtlSeconds());
    cache.evictBase(baseCurrency);

    for (Map.Entry<Currency, BigDecimal> entry : baseRates.entrySet()) {
      cache.putRate(baseCurrency, entry.getKey(), entry.getValue(),
                    fxProperties.cacheTtlSeconds());
      FxRate fxRate =
          fxRateRepository
              .findTopByBaseCurrencyAndQuoteCurrencyOrderByFetchedAtDesc(
                  baseCurrency, entry.getKey())
              .orElseGet(FxRate::new);
      fxRate.setBaseCurrency(baseCurrency);
      fxRate.setQuoteCurrency(entry.getKey());
      fxRate.setRate(entry.getValue());
      fxRate.setSource("static-provider");
      fxRate.setFetchedAt(now);
      fxRate.setExpiresAt(expiresAt);
      fxRateRepository.save(fxRate);
    }
    return baseRates;
  }

  @Transactional(readOnly = true)
  public List<FxRateResponse> getRates(Currency baseCurrency) {
    Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
    for (Currency quoteCurrency : Currency.values()) {
      rates.put(quoteCurrency, getRate(baseCurrency, quoteCurrency));
    }
    Instant now = Instant.now();
    return rates.entrySet()
        .stream()
        .map(entry
             -> new FxRateResponse(
                 baseCurrency.name(), entry.getKey().name(), entry.getValue(),
                 now, now.plusSeconds(fxProperties.cacheTtlSeconds())))
        .toList();
  }

  @Transactional(readOnly = true)
  public BigDecimal requireRate(String sourceCurrency, String targetCurrency) {
    Currency source = Currency.fromCode(sourceCurrency);
    Currency target = Currency.fromCode(targetCurrency);
    BigDecimal rate = getRate(source, target);
    if (rate == null) {
      throw new NotFoundException("FX rate not available");
    }
    return rate;
  }
}
