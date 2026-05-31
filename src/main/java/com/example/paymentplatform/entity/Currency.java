package com.example.paymentplatform.entity;

import java.util.Arrays;

public enum Currency {
  SGD,
  USD,
  EUR,
  JPY;

  public static Currency fromCode(String code) {
    return Arrays.stream(values())
        .filter(currency -> currency.name().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(()
                         -> new IllegalArgumentException(
                             "Unsupported currency: " + code));
  }
}
