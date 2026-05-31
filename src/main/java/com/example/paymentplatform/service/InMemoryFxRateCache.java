package com.example.paymentplatform.service;

import com.example.paymentplatform.entity.Currency;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryFxRateCache implements FxRateCache {

  private final Map<String, BigDecimal> cache = new ConcurrentHashMap<>();

  @Override
  public Optional<BigDecimal> getRate(Currency baseCurrency,
                                      Currency quoteCurrency) {
    return Optional.ofNullable(cache.get(key(baseCurrency, quoteCurrency)));
  }

  @Override
  public void putRate(Currency baseCurrency, Currency quoteCurrency,
                      BigDecimal rate, long ttlSeconds) {
    cache.put(key(baseCurrency, quoteCurrency), rate);
  }

  @Override
  public void evictBase(Currency baseCurrency) {
    cache.keySet().removeIf(key -> key.startsWith(baseCurrency.name() + ":"));
  }

  private String key(Currency baseCurrency, Currency quoteCurrency) {
    return baseCurrency.name() + ":" + quoteCurrency.name();
  }
}
