package com.example.paymentplatform.service;

import com.example.paymentplatform.dto.wallet.WalletCreateRequest;
import com.example.paymentplatform.dto.wallet.WalletResponse;
import com.example.paymentplatform.entity.AppUser;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.Wallet;
import com.example.paymentplatform.exception.ConflictException;
import com.example.paymentplatform.exception.NotFoundException;
import com.example.paymentplatform.repository.UserRepository;
import com.example.paymentplatform.repository.WalletRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;

  public WalletService(UserRepository userRepository,
                       WalletRepository walletRepository) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
  }

  @Transactional
  public WalletResponse createWallet(UUID userId, WalletCreateRequest request) {
    AppUser user = userRepository.findById(userId).orElseThrow(
        () -> new NotFoundException("User not found"));
    Currency currency = Currency.fromCode(request.currency());
    if (walletRepository.findByUser_IdAndCurrency(userId, currency)
            .isPresent()) {
      throw new ConflictException("Wallet already exists for currency " +
                                  currency.name());
    }

    Wallet wallet = new Wallet();
    wallet.setUser(user);
    wallet.setCurrency(currency);
    wallet.setBalance(java.math.BigDecimal.ZERO);
    walletRepository.saveAndFlush(wallet);
    return WalletResponse.from(wallet);
  }

  @Transactional(readOnly = true)
  public List<WalletResponse> getWallets(UUID userId) {
    return walletRepository.findByUser_IdOrderByCurrencyAsc(userId)
        .stream()
        .map(WalletResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public Wallet requireOwnedWallet(UUID userId, UUID walletId) {
    Wallet wallet = walletRepository.findById(walletId).orElseThrow(
        () -> new NotFoundException("Wallet not found"));
    if (!wallet.getUser().getId().equals(userId)) {
      throw new NotFoundException("Wallet not found");
    }
    return wallet;
  }
}
