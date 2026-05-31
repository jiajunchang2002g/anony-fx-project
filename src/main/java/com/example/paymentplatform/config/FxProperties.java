package com.example.paymentplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.fx")
public record FxProperties(String baseCurrency, long cacheTtlSeconds) {}
