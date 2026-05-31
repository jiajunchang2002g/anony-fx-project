package com.example.paymentplatform.service;

import com.example.paymentplatform.config.FinancialProperties;
import com.example.paymentplatform.dto.transfer.TransferRequest;
import com.example.paymentplatform.dto.transfer.TransferResponse;
import com.example.paymentplatform.entity.AppUser;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.LedgerDirection;
import com.example.paymentplatform.entity.LedgerEntry;
import com.example.paymentplatform.entity.Transfer;
import com.example.paymentplatform.entity.TransferStatus;
import com.example.paymentplatform.entity.Wallet;
import com.example.paymentplatform.exception.BadRequestException;
import com.example.paymentplatform.exception.ConflictException;
import com.example.paymentplatform.exception.InsufficientBalanceException;
import com.example.paymentplatform.exception.NotFoundException;
import com.example.paymentplatform.repository.LedgerEntryRepository;
import com.example.paymentplatform.repository.TransferRepository;
import com.example.paymentplatform.repository.UserRepository;
import com.example.paymentplatform.repository.WalletRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final TransferRepository transferRepository;
  private final LedgerEntryRepository ledgerEntryRepository;
  private final FxRateService fxRateService;
  private final FinancialProperties financialProperties;
  private final IdempotencyLockService idempotencyLockService;

  public TransferService(UserRepository userRepository,
                         WalletRepository walletRepository,
                         TransferRepository transferRepository,
                         LedgerEntryRepository ledgerEntryRepository,
                         FxRateService fxRateService,
                         FinancialProperties financialProperties,
                         IdempotencyLockService idempotencyLockService) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
    this.transferRepository = transferRepository;
    this.ledgerEntryRepository = ledgerEntryRepository;
    this.fxRateService = fxRateService;
    this.financialProperties = financialProperties;
    this.idempotencyLockService = idempotencyLockService;
  }

  @Transactional
  public TransferResponse transfer(UUID requesterUserId,
                                   TransferRequest request) {
    return idempotencyLockService.execute(
        request.idempotencyKey(), () -> doTransfer(requesterUserId, request));
  }

  private TransferResponse doTransfer(UUID requesterUserId,
                                      TransferRequest request) {
    if (request.amount() == null ||
        request.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("Transfer amount must be positive");
    }
    if (request.sourceWalletId().equals(request.targetWalletId())) {
      throw new BadRequestException(
          "Source and target wallets must be different");
    }
    if (transferRepository.findByIdempotencyKey(request.idempotencyKey())
            .isPresent()) {
      return TransferResponse.from(
          transferRepository.findByIdempotencyKey(request.idempotencyKey())
              .orElseThrow());
    }

    AppUser requester =
        userRepository.findById(requesterUserId)
            .orElseThrow(() -> new NotFoundException("User not found"));

    Wallet sourceWallet;
    Wallet targetWallet;
    if (request.sourceWalletId().compareTo(request.targetWalletId()) < 0) {
      sourceWallet =
          walletRepository.findLockedById(request.sourceWalletId())
              .orElseThrow(
                  () -> new NotFoundException("Source wallet not found"));
      targetWallet =
          walletRepository.findLockedById(request.targetWalletId())
              .orElseThrow(
                  () -> new NotFoundException("Target wallet not found"));
    } else {
      targetWallet =
          walletRepository.findLockedById(request.targetWalletId())
              .orElseThrow(
                  () -> new NotFoundException("Target wallet not found"));
      sourceWallet =
          walletRepository.findLockedById(request.sourceWalletId())
              .orElseThrow(
                  () -> new NotFoundException("Source wallet not found"));
    }

    if (!sourceWallet.getUser().getId().equals(requester.getId())) {
      throw new BadRequestException(
          "Transfers can only be initiated from your own wallet");
    }

    BigDecimal sourceAmount = normalize(request.amount());
    BigDecimal fxRate = fxRateService.getRate(sourceWallet.getCurrency(),
                                              targetWallet.getCurrency());
    BigDecimal targetAmount = normalize(sourceAmount.multiply(fxRate));

    if (sourceWallet.getBalance().compareTo(sourceAmount) < 0) {
      throw new InsufficientBalanceException("Insufficient wallet balance");
    }

    Transfer transfer = new Transfer();
    transfer.setIdempotencyKey(request.idempotencyKey());
    transfer.setRequestedBy(requester);
    transfer.setSourceWallet(sourceWallet);
    transfer.setTargetWallet(targetWallet);
    transfer.setSourceCurrency(sourceWallet.getCurrency());
    transfer.setTargetCurrency(targetWallet.getCurrency());
    transfer.setSourceAmount(sourceAmount);
    transfer.setTargetAmount(targetAmount);
    transfer.setFxRate(fxRate);
    transfer.setStatus(TransferStatus.PENDING);

    try {
      transfer = transferRepository.saveAndFlush(transfer);
    } catch (DataIntegrityViolationException exception) {
      return TransferResponse.from(
          transferRepository.findByIdempotencyKey(request.idempotencyKey())
              .orElseThrow(
                  () -> new ConflictException("Duplicate idempotency key")));
    }

    sourceWallet.debit(sourceAmount);
    targetWallet.credit(targetAmount);

    LedgerEntry debitEntry = new LedgerEntry();
    debitEntry.setTransfer(transfer);
    debitEntry.setWallet(sourceWallet);
    debitEntry.setDirection(LedgerDirection.DEBIT);
    debitEntry.setCurrency(sourceWallet.getCurrency());
    debitEntry.setAmount(sourceAmount);
    debitEntry.setBalanceAfter(sourceWallet.getBalance());
    debitEntry.setDescription(
        buildDebitDescription(request.note(), targetWallet.getId()));

    LedgerEntry creditEntry = new LedgerEntry();
    creditEntry.setTransfer(transfer);
    creditEntry.setWallet(targetWallet);
    creditEntry.setDirection(LedgerDirection.CREDIT);
    creditEntry.setCurrency(targetWallet.getCurrency());
    creditEntry.setAmount(targetAmount);
    creditEntry.setBalanceAfter(targetWallet.getBalance());
    creditEntry.setDescription(
        buildCreditDescription(request.note(), sourceWallet.getId()));

    walletRepository.saveAll(List.of(sourceWallet, targetWallet));
    ledgerEntryRepository.saveAll(List.of(debitEntry, creditEntry));

    transfer.setStatus(TransferStatus.COMPLETED);
    transfer.setCompletedAt(Instant.now());
    transferRepository.saveAndFlush(transfer);
    return TransferResponse.from(transfer);
  }

  @Transactional(readOnly = true)
  public TransferResponse getTransfer(UUID userId, UUID transferId) {
    Transfer transfer =
        transferRepository.findById(transferId)
            .orElseThrow(() -> new NotFoundException("Transfer not found"));
    if (!transfer.getRequestedBy().getId().equals(userId)) {
      throw new NotFoundException("Transfer not found");
    }
    return TransferResponse.from(transfer);
  }

  @Transactional(readOnly = true)
  public List<TransferResponse> getTransfers(UUID userId) {
    return transferRepository.findByRequestedBy_IdOrderByCreatedAtDesc(userId)
        .stream()
        .map(TransferResponse::from)
        .toList();
  }

  private BigDecimal normalize(BigDecimal amount) {
    return amount.setScale(financialProperties.scale(),
                           financialProperties.roundingMode());
  }

  private String buildDebitDescription(String note, UUID targetWalletId) {
    if (note == null || note.isBlank()) {
      return "Transfer debit to wallet " + targetWalletId;
    }
    return note + " | debit to wallet " + targetWalletId;
  }

  private String buildCreditDescription(String note, UUID sourceWalletId) {
    if (note == null || note.isBlank()) {
      return "Transfer credit from wallet " + sourceWalletId;
    }
    return note + " | credit from wallet " + sourceWalletId;
  }
}
