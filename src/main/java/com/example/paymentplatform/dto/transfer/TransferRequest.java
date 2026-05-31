package com.example.paymentplatform.dto.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record
TransferRequest(@NotNull UUID sourceWalletId, @NotNull UUID targetWalletId,
                @NotNull @DecimalMin(value = "0.000001",
                                     inclusive = true) BigDecimal amount,
                @NotBlank String idempotencyKey, String note) {}
