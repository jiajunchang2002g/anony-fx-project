package com.example.paymentplatform.controller;

import com.example.paymentplatform.dto.ApiResponse;
import com.example.paymentplatform.dto.transfer.TransferRequest;
import com.example.paymentplatform.dto.transfer.TransferResponse;
import com.example.paymentplatform.security.UserPrincipal;
import com.example.paymentplatform.service.TransferService;
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
@RequestMapping("/api/transfers")
public class TransferController {

  private final TransferService transferService;

  public TransferController(TransferService transferService) {
    this.transferService = transferService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<TransferResponse>>
  transfer(@AuthenticationPrincipal UserPrincipal principal,
           @Valid @RequestBody TransferRequest request) {
    TransferResponse response =
        transferService.transfer(principal.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("Transfer completed", response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<TransferResponse>>>
  list(@AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok(ApiResponse.ok(
        "Transfer history", transferService.getTransfers(principal.getId())));
  }

  @GetMapping("/{transferId}")
  public ResponseEntity<ApiResponse<TransferResponse>>
  get(@AuthenticationPrincipal UserPrincipal principal,
      @PathVariable UUID transferId) {
    return ResponseEntity.ok(ApiResponse.ok(
        "Transfer details",
        transferService.getTransfer(principal.getId(), transferId)));
  }
}
