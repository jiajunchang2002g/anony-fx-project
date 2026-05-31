package com.example.paymentplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bank")
public record BankProperties(String sharedSecret) {}
