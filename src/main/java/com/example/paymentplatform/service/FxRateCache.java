package com.example.paymentplatform.service;

import com.example.paymentplatform.entity.Currency;
import java.math.BigDecimal;
import java.util.Optional;

public interface FxRateCache {

  Optional<BigDecimal> getRate(Currency baseCurrency, Currency quoteCurrency);

  void putRate(Currency baseCurrency, Currency quoteCurrency, BigDecimal rate,
               long ttlSeconds);

  void evictBase(Currency baseCurrency);
}
