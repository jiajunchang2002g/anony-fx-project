package com.example.paymentplatform.repository;

import com.example.paymentplatform.entity.Transfer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

  Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

  List<Transfer> findByRequestedBy_IdOrderByCreatedAtDesc(UUID userId);
}
