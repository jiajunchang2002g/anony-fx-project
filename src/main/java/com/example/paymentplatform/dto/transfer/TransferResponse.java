package com.example.paymentplatform.dto.transfer;

import com.example.paymentplatform.entity.Transfer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(UUID id, String status, UUID sourceWalletId,
                               UUID targetWalletId, String sourceCurrency,
                               String targetCurrency, BigDecimal sourceAmount,
                               BigDecimal targetAmount, BigDecimal fxRate,
                               Instant createdAt, Instant completedAt,
                               String failureReason) {

  public static TransferResponse from(Transfer transfer) {
    return new TransferResponse(
        transfer.getId(), transfer.getStatus().name(),
        transfer.getSourceWallet().getId(), transfer.getTargetWallet().getId(),
        transfer.getSourceCurrency().name(),
        transfer.getTargetCurrency().name(), transfer.getSourceAmount(),
        transfer.getTargetAmount(), transfer.getFxRate(),
        transfer.getCreatedAt(), transfer.getCompletedAt(),
        transfer.getFailureReason());
  }
}
