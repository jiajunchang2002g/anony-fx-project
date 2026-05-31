package com.example.paymentplatform.repository;

import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.entity.FxRate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, UUID> {

  Optional<FxRate> findTopByBaseCurrencyAndQuoteCurrencyOrderByFetchedAtDesc(
      Currency baseCurrency, Currency quoteCurrency);

  List<FxRate> findByBaseCurrencyAndFetchedAtAfterOrderByQuoteCurrencyAsc(
      Currency baseCurrency, java.time.Instant fetchedAfter);
}
