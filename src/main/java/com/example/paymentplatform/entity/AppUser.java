package com.example.paymentplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "users")
public class AppUser {

  @Id private UUID id;

  @Column(nullable = false, unique = true) private String email;

  @Column(name = "password_hash", nullable = false) private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role = Role.USER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status = UserStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private Set<Wallet> wallets = new LinkedHashSet<>();

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (role == null) {
      role = Role.USER;
    }
    if (status == null) {
      status = UserStatus.ACTIVE;
    }
  }
}
