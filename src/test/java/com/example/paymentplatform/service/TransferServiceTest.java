package com.example.paymentplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.paymentplatform.config.FinancialProperties;
import com.example.paymentplatform.dto.transfer.TransferRequest;
import com.example.paymentplatform.dto.transfer.TransferResponse;
import com.example.paymentplatform.entity.AppUser;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.Role;
import com.example.paymentplatform.entity.Transfer;
import com.example.paymentplatform.entity.UserStatus;
import com.example.paymentplatform.entity.Wallet;
import com.example.paymentplatform.repository.LedgerEntryRepository;
import com.example.paymentplatform.repository.TransferRepository;
import com.example.paymentplatform.repository.UserRepository;
import com.example.paymentplatform.repository.WalletRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransferServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private WalletRepository walletRepository;
  @Mock private TransferRepository transferRepository;
  @Mock private LedgerEntryRepository ledgerEntryRepository;
  @Mock private FxRateService fxRateService;

  private TransferService transferService;

  private UUID userId;
  private Wallet sourceWallet;
  private Wallet targetWallet;

  @BeforeEach
  void setUp() {
    transferService = new TransferService(
        userRepository, walletRepository, transferRepository,
        ledgerEntryRepository, fxRateService,
        new FinancialProperties(6, RoundingMode.HALF_UP),
        new IdempotencyLockService());

    userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    AppUser user = new AppUser();
    user.setId(userId);
    user.setEmail("tester@example.com");
    user.setPasswordHash("hash");
    user.setRole(Role.USER);
    user.setStatus(UserStatus.ACTIVE);

    sourceWallet = new Wallet();
    sourceWallet.setId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    sourceWallet.setUser(user);
    sourceWallet.setCurrency(Currency.SGD);
    sourceWallet.setBalance(new BigDecimal("100.000000"));

    targetWallet = new Wallet();
    targetWallet.setId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
    targetWallet.setUser(user);
    targetWallet.setCurrency(Currency.USD);
    targetWallet.setBalance(new BigDecimal("0.000000"));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(walletRepository.findLockedById(sourceWallet.getId()))
        .thenReturn(Optional.of(sourceWallet));
    when(walletRepository.findLockedById(targetWallet.getId()))
        .thenReturn(Optional.of(targetWallet));
    when(transferRepository.findByIdempotencyKey("idem-1"))
        .thenReturn(Optional.empty());
    when(transferRepository.saveAndFlush(any(Transfer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(walletRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(ledgerEntryRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void rejectsInsufficientBalance() {
    TransferRequest request =
        new TransferRequest(sourceWallet.getId(), targetWallet.getId(),
                            new BigDecimal("1000.000000"), "idem-1", "rent");

    when(fxRateService.getRate(Currency.SGD, Currency.USD))
        .thenReturn(new BigDecimal("0.5000000000"));
    assertThatThrownBy(() -> transferService.transfer(userId, request))
        .isInstanceOf(com.example.paymentplatform.exception
                          .InsufficientBalanceException.class);

    verify(ledgerEntryRepository, never()).saveAll(anyList());
  }

  @Test
  void convertsAndRoundsTransferAmounts() {
    when(fxRateService.getRate(Currency.SGD, Currency.USD))
        .thenReturn(new BigDecimal("0.5000000000"));
    TransferRequest request = new TransferRequest(
        sourceWallet.getId(), targetWallet.getId(),
        new BigDecimal("10.1234567"), "idem-1", "portfolio transfer");

    TransferResponse response = transferService.transfer(userId, request);

    assertThat(response.status()).isEqualTo("COMPLETED");
    assertThat(response.sourceAmount()).isEqualByComparingTo("10.123457");
    assertThat(response.targetAmount()).isEqualByComparingTo("5.061729");
    assertThat(sourceWallet.getBalance()).isEqualByComparingTo("89.876543");
    assertThat(targetWallet.getBalance()).isEqualByComparingTo("5.061729");
  }
}
