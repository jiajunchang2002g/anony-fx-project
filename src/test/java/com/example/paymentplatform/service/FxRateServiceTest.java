package com.example.paymentplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.paymentplatform.config.FinancialProperties;
import com.example.paymentplatform.config.FxProperties;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.repository.FxRateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FxRateServiceTest {

  @Mock private FxRateRepository fxRateRepository;

  private FxRateService fxRateService;

  @BeforeEach
  void setUp() {
    fxRateService =
        new FxRateService(new StaticFxRateProvider(), new InMemoryFxRateCache(),
                          fxRateRepository, new FxProperties("SGD", 3600),
                          new FinancialProperties(6, RoundingMode.HALF_UP));
  }

  @Test
  void returnsOneForSameCurrency() {
    assertThat(fxRateService.getRate(Currency.SGD, Currency.SGD))
        .isEqualByComparingTo("1.000000");
  }

  @Test
  void derivesCrossCurrencyRateThroughBaseCurrency() {
    when(fxRateRepository
             .findTopByBaseCurrencyAndQuoteCurrencyOrderByFetchedAtDesc(
                 Currency.SGD, Currency.USD))
        .thenReturn(Optional.empty());

    assertThat(fxRateService.getRate(Currency.SGD, Currency.USD))
        .isEqualByComparingTo("0.7400000000");
  }
}
