package com.example.paymentplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transfers",
       uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
public class Transfer {

  @Id private UUID id;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requested_by_user_id", nullable = false)
  private AppUser requestedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_wallet_id", nullable = false)
  private Wallet sourceWallet;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_wallet_id", nullable = false)
  private Wallet targetWallet;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_currency", nullable = false, length = 3)
  private Currency sourceCurrency;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_currency", nullable = false, length = 3)
  private Currency targetCurrency;

  @Column(name = "source_amount", nullable = false, precision = 24, scale = 6)
  private BigDecimal sourceAmount;

  @Column(name = "target_amount", nullable = false, precision = 24, scale = 6)
  private BigDecimal targetAmount;

  @Column(nullable = false, precision = 24, scale = 10)
  private BigDecimal fxRate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferStatus status = TransferStatus.PENDING;

  @Column(name = "failure_reason", length = 500) private String failureReason;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "completed_at") private Instant completedAt;

  @OneToMany(mappedBy = "transfer")
  private Set<LedgerEntry> ledgerEntries = new LinkedHashSet<>();

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (status == null) {
      status = TransferStatus.PENDING;
    }
  }
}
