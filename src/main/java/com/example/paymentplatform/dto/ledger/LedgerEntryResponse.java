package com.example.paymentplatform.dto.ledger;

import com.example.paymentplatform.entity.LedgerEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(UUID id, UUID transferId, UUID walletId,
                                  String direction, String currency,
                                  BigDecimal amount, BigDecimal balanceAfter,
                                  String description, Instant createdAt) {

  public static LedgerEntryResponse from(LedgerEntry entry) {
    return new LedgerEntryResponse(
        entry.getId(), entry.getTransfer().getId(), entry.getWallet().getId(),
        entry.getDirection().name(), entry.getCurrency().name(),
        entry.getAmount(), entry.getBalanceAfter(), entry.getDescription(),
        entry.getCreatedAt());
  }
}
