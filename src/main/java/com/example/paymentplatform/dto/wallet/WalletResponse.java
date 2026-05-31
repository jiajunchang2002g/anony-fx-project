package com.example.paymentplatform.dto.wallet;

import com.example.paymentplatform.entity.Wallet;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(UUID id, UUID userId, String currency,
                             BigDecimal balance, Instant createdAt,
                             Instant updatedAt) {

  public static WalletResponse from(Wallet wallet) {
    return new WalletResponse(wallet.getId(), wallet.getUser().getId(),
                              wallet.getCurrency().name(), wallet.getBalance(),
                              wallet.getCreatedAt(), wallet.getUpdatedAt());
  }
}
