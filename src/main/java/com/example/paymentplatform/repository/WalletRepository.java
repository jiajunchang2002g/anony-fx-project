package com.example.paymentplatform.repository;

import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.Wallet;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

  List<Wallet> findByUser_IdOrderByCurrencyAsc(UUID userId);

  Optional<Wallet> findByUser_IdAndCurrency(UUID userId, Currency currency);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select w from Wallet w join fetch w.user where w.id = :id")
  Optional<Wallet> findLockedById(@Param("id") UUID id);
}
