package com.example.paymentplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.paymentplatform.dto.transfer.TransferRequest;
import com.example.paymentplatform.dto.transfer.TransferResponse;
import com.example.paymentplatform.entity.AppUser;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.Role;
import com.example.paymentplatform.entity.UserStatus;
import com.example.paymentplatform.entity.Wallet;
import com.example.paymentplatform.repository.LedgerEntryRepository;
import com.example.paymentplatform.repository.UserRepository;
import com.example.paymentplatform.repository.WalletRepository;
import com.example.paymentplatform.service.TransferService;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TransferConcurrencyIntegrationTest {

  @Autowired private TransferService transferService;

  @Autowired private UserRepository userRepository;

  @Autowired private WalletRepository walletRepository;

  @Autowired private LedgerEntryRepository ledgerEntryRepository;

  private UUID userId;
  private UUID sourceWalletId;
  private UUID targetWalletId;

  @BeforeEach
  void setUp() {
    ledgerEntryRepository.deleteAll();
    walletRepository.deleteAll();
    userRepository.deleteAll();

    AppUser user = new AppUser();
    user.setEmail("concurrency@example.com");
    user.setPasswordHash("hash");
    user.setRole(Role.USER);
    user.setStatus(UserStatus.ACTIVE);
    user = userRepository.saveAndFlush(user);
    userId = user.getId();

    Wallet sourceWallet = new Wallet();
    sourceWallet.setUser(user);
    sourceWallet.setCurrency(Currency.SGD);
    sourceWallet.setBalance(new BigDecimal("100.000000"));
    sourceWallet = walletRepository.saveAndFlush(sourceWallet);

    Wallet targetWallet = new Wallet();
    targetWallet.setUser(user);
    targetWallet.setCurrency(Currency.USD);
    targetWallet.setBalance(new BigDecimal("0.000000"));
    targetWallet = walletRepository.saveAndFlush(targetWallet);

    sourceWalletId = sourceWallet.getId();
    targetWalletId = targetWallet.getId();
  }

  @Test
  void concurrentIdempotentTransfersOnlyApplyOnce() throws Exception {
    TransferRequest request = new TransferRequest(
        sourceWalletId, targetWalletId, new BigDecimal("10.000000"),
        "concurrency-idempotency-key", "concurrent payment");
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);

    Callable<TransferResponse> task = () -> {
      ready.countDown();
      start.await(5, TimeUnit.SECONDS);
      return transferService.transfer(userId, request);
    };

    Future<TransferResponse> first = executor.submit(task);
    Future<TransferResponse> second = executor.submit(task);

    assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
    start.countDown();

    TransferResponse firstResponse = first.get(20, TimeUnit.SECONDS);
    TransferResponse secondResponse = second.get(20, TimeUnit.SECONDS);

    assertThat(firstResponse.id()).isEqualTo(secondResponse.id());
    assertThat(
        walletRepository.findById(sourceWalletId).orElseThrow().getBalance())
        .isEqualByComparingTo("90.000000");
    assertThat(
        walletRepository.findById(targetWalletId).orElseThrow().getBalance())
        .isEqualByComparingTo("7.400000");
    assertThat(ledgerEntryRepository.findByTransfer_IdOrderByCreatedAtAsc(
                   firstResponse.id()))
        .hasSize(2);

    executor.shutdownNow();
  }
}
