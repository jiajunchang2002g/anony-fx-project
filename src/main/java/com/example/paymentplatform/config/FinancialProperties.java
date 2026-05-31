package com.example.paymentplatform.config;

import java.math.RoundingMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.financial")
public record FinancialProperties(int scale, RoundingMode roundingMode) {}
