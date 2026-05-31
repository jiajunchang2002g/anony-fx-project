package com.example.paymentplatform.service;

import com.example.paymentplatform.entity.Currency;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisFxRateCache implements FxRateCache {

  private final StringRedisTemplate redisTemplate;

  public RedisFxRateCache(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Optional<BigDecimal> getRate(Currency baseCurrency,
                                      Currency quoteCurrency) {
    String value =
        redisTemplate.opsForValue().get(key(baseCurrency, quoteCurrency));
    return value == null ? Optional.empty()
                         : Optional.of(new BigDecimal(value));
  }

  @Override
  public void putRate(Currency baseCurrency, Currency quoteCurrency,
                      BigDecimal rate, long ttlSeconds) {
    redisTemplate.opsForValue().set(key(baseCurrency, quoteCurrency),
                                    rate.toPlainString(),
                                    Duration.ofSeconds(ttlSeconds));
  }

  @Override
  public void evictBase(Currency baseCurrency) {
    for (Currency quoteCurrency : Currency.values()) {
      redisTemplate.delete(key(baseCurrency, quoteCurrency));
    }
  }

  private String key(Currency baseCurrency, Currency quoteCurrency) {
    return "fx-rate:" + baseCurrency.name() + ':' + quoteCurrency.name();
  }
}
