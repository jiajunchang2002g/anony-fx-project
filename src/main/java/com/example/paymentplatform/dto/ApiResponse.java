package com.example.paymentplatform.dto;

import java.time.Instant;

public record ApiResponse<T>(boolean success, String message, T data,
                             Instant timestamp) {

  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, message, data, Instant.now());
  }

  public static <T> ApiResponse<T> created(String message, T data) {
    return new ApiResponse<>(true, message, data, Instant.now());
  }
}
