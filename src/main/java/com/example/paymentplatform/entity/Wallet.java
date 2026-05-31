package com.example.paymentplatform.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wallets", uniqueConstraints = @UniqueConstraint(
                             columnNames = {"user_id", "currency"}))
public class Wallet {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency;

  @Column(nullable = false, precision = 24, scale = 6)
  private BigDecimal balance = BigDecimal.ZERO;

  @Version private Long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL,
             orphanRemoval = false)
  private Set<LedgerEntry> ledgerEntries = new LinkedHashSet<>();

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (balance == null) {
      balance = BigDecimal.ZERO;
    }
    if (version == null) {
      version = 0L;
    }
  }

  public void debit(BigDecimal amount) { balance = balance.subtract(amount); }

  public void credit(BigDecimal amount) { balance = balance.add(amount); }
}
