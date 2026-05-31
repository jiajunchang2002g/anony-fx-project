package com.example.paymentplatform.dto.wallet;

import jakarta.validation.constraints.NotBlank;

public record WalletCreateRequest(@NotBlank String currency) {}
