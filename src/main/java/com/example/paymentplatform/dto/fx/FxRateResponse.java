package com.example.paymentplatform.dto.fx;

import java.math.BigDecimal;
import java.time.Instant;

public record FxRateResponse(String baseCurrency, String quoteCurrency,
                             BigDecimal rate, Instant fetchedAt,
                             Instant expiresAt) {}
