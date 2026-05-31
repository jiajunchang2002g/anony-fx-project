package com.example.paymentplatform.service;

import com.example.paymentplatform.entity.Currency;
import java.math.BigDecimal;
import java.util.Map;

public interface FxRateProvider {

  Map<Currency, BigDecimal> fetchBaseRates(Currency baseCurrency);
}
