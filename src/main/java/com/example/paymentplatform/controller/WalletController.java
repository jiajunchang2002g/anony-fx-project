package com.example.paymentplatform.controller;

import com.example.paymentplatform.dto.ApiResponse;
import com.example.paymentplatform.dto.wallet.WalletCreateRequest;
import com.example.paymentplatform.dto.wallet.WalletResponse;
import com.example.paymentplatform.security.UserPrincipal;
import com.example.paymentplatform.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<WalletResponse>>
  create(@AuthenticationPrincipal UserPrincipal principal,
         @Valid @RequestBody WalletCreateRequest request) {
    WalletResponse response =
        walletService.createWallet(principal.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("Wallet created", response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<WalletResponse>>>
  list(@AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok(ApiResponse.ok(
        "Wallet list", walletService.getWallets(principal.getId())));
  }

  @GetMapping("/{walletId}")
  public ResponseEntity<ApiResponse<WalletResponse>>
  get(@AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID walletId) {
    return ResponseEntity.ok(ApiResponse.ok(
        "Wallet details", WalletResponse.from(walletService.requireOwnedWallet(
                              principal.getId(), walletId))));
  }
}
