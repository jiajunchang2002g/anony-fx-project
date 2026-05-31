package com.example.paymentplatform.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile("!test")
public class RedisConfig {

  @Bean
  public RedisConnectionFactory
  redisConnectionFactory(RedisProperties properties) {
    RedisStandaloneConfiguration configuration =
        new RedisStandaloneConfiguration(properties.getHost(),
                                         properties.getPort());
    if (properties.getPassword() != null &&
        !properties.getPassword().isEmpty()) {
      configuration.setPassword(properties.getPassword());
    }
    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  public StringRedisTemplate
  stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }
}
