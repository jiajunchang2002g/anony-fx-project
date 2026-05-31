package com.example.paymentplatform.repository;

import com.example.paymentplatform.entity.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository
    extends JpaRepository<LedgerEntry, UUID> {

  List<LedgerEntry> findByWallet_IdOrderByCreatedAtDesc(UUID walletId);

  List<LedgerEntry> findByTransfer_IdOrderByCreatedAtAsc(UUID transferId);
}
