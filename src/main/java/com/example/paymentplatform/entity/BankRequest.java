package com.example.paymentplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "bank_requests")
public class BankRequest {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BankPartner partner;

  @Column(name = "request_type", nullable = false, length = 100)
  private String requestType;

  @Column(nullable = false, columnDefinition = "text") private String payload;

  @Column(nullable = false) private String signature;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BankRequestStatus status;

  @Column(name = "response_body", columnDefinition = "text")
  private String responseBody;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (status == null) {
      status = BankRequestStatus.PENDING;
    }
  }
}
