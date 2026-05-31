package com.example.paymentplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transfer_id", nullable = false)
  private Transfer transfer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LedgerDirection direction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency;

  @Column(nullable = false, precision = 24, scale = 6)
  private BigDecimal amount;

  @Column(name = "balance_after", nullable = false, precision = 24, scale = 6)
  private BigDecimal balanceAfter;

  @Column(nullable = false, length = 500) private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }
}
