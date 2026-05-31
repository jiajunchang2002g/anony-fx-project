package com.example.paymentplatform.dto.bank;

import jakarta.validation.constraints.NotBlank;

public record BankSimulationRequest(@NotBlank String requestType,
                                    @NotBlank String payload,
                                    @NotBlank String accessToken) {}
