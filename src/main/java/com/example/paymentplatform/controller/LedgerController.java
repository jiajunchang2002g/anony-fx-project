package com.example.paymentplatform.controller;

import com.example.paymentplatform.dto.ApiResponse;
import com.example.paymentplatform.dto.ledger.LedgerEntryResponse;
import com.example.paymentplatform.security.UserPrincipal;
import com.example.paymentplatform.service.LedgerService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

  private final LedgerService ledgerService;

  public LedgerController(LedgerService ledgerService) {
    this.ledgerService = ledgerService;
  }

  @GetMapping("/wallets/{walletId}")
  public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>>
  walletLedger(@AuthenticationPrincipal UserPrincipal principal,
               @PathVariable UUID walletId) {
    return ResponseEntity.ok(ApiResponse.ok(
        "Wallet ledger",
        ledgerService.getWalletLedger(principal.getId(), walletId)));
  }

  @GetMapping("/wallets/{walletId}/transfers/{transferId}")
  public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>>
  transferLedger(@AuthenticationPrincipal UserPrincipal principal,
                 @PathVariable UUID walletId, @PathVariable UUID transferId) {
    return ResponseEntity.ok(ApiResponse.ok(
        "Transfer ledger", ledgerService.getTransferLedger(
                               principal.getId(), walletId, transferId)));
  }
}
