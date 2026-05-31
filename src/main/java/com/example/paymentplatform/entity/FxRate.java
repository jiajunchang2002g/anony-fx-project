package com.example.paymentplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fx_rates",
       uniqueConstraints =
           @UniqueConstraint(columnNames = {"base_currency", "quote_currency"}))
public class FxRate {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "base_currency", nullable = false, length = 3)
  private Currency baseCurrency;

  @Enumerated(EnumType.STRING)
  @Column(name = "quote_currency", nullable = false, length = 3)
  private Currency quoteCurrency;

  @Column(nullable = false, precision = 24, scale = 10) private BigDecimal rate;

  @Column(nullable = false, length = 100) private String source;

  @Column(name = "fetched_at", nullable = false) private Instant fetchedAt;

  @Column(name = "expires_at", nullable = false) private Instant expiresAt;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }
}
