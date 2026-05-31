package com.example.paymentplatform.service;

import com.example.paymentplatform.dto.ledger.LedgerEntryResponse;
import com.example.paymentplatform.repository.LedgerEntryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

  private final WalletService walletService;
  private final LedgerEntryRepository ledgerEntryRepository;

  public LedgerService(WalletService walletService,
                       LedgerEntryRepository ledgerEntryRepository) {
    this.walletService = walletService;
    this.ledgerEntryRepository = ledgerEntryRepository;
  }

  @Transactional(readOnly = true)
  public List<LedgerEntryResponse> getWalletLedger(UUID userId, UUID walletId) {
    walletService.requireOwnedWallet(userId, walletId);
    return ledgerEntryRepository.findByWallet_IdOrderByCreatedAtDesc(walletId)
        .stream()
        .map(LedgerEntryResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<LedgerEntryResponse> getTransferLedger(UUID userId, UUID walletId,
                                                     UUID transferId) {
    walletService.requireOwnedWallet(userId, walletId);
    return ledgerEntryRepository
        .findByTransfer_IdOrderByCreatedAtAsc(transferId)
        .stream()
        .map(LedgerEntryResponse::from)
        .toList();
  }
}
