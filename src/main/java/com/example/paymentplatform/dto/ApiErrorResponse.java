package com.example.paymentplatform.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(String errorCode, String message,
                               List<String> details, String path,
                               Instant timestamp) {}
