package com.example.paymentplatform.service;

import com.example.paymentplatform.entity.Currency;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StaticFxRateProvider implements FxRateProvider {

  private static final Map<Currency, BigDecimal> SGD_BASE_RATES =
      Map.of(Currency.SGD, new BigDecimal("1.0000000000"), Currency.USD,
             new BigDecimal("0.7400000000"), Currency.EUR,
             new BigDecimal("0.6800000000"), Currency.JPY,
             new BigDecimal("109.1500000000"));

  @Override
  public Map<Currency, BigDecimal> fetchBaseRates(Currency baseCurrency) {
    BigDecimal baseRate = SGD_BASE_RATES.get(baseCurrency);
    if (baseRate == null) {
      throw new IllegalArgumentException("Unsupported FX base currency: " +
                                         baseCurrency);
    }
    Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
    for (Currency quoteCurrency : Currency.values()) {
      BigDecimal quoteBaseRate = SGD_BASE_RATES.get(quoteCurrency);
      BigDecimal derivedRate =
          quoteBaseRate.divide(baseRate, 10, RoundingMode.HALF_UP);
      rates.put(quoteCurrency, derivedRate);
    }
    return rates;
  }
}
