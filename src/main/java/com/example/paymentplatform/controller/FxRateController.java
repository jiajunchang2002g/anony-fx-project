package com.example.paymentplatform.controller;

import com.example.paymentplatform.config.FxProperties;
import com.example.paymentplatform.dto.ApiResponse;
import com.example.paymentplatform.dto.fx.FxRateResponse;
import com.example.paymentplatform.entity.Currency;
import com.example.paymentplatform.service.FxRateService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fx-rates")
public class FxRateController {

  private final FxRateService fxRateService;
  private final FxProperties fxProperties;

  public FxRateController(FxRateService fxRateService,
                          FxProperties fxProperties) {
    this.fxRateService = fxRateService;
    this.fxProperties = fxProperties;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<FxRateResponse>>>
  currentRates(@RequestParam(required = false) String base) {
    Currency baseCurrency = base == null || base.isBlank()
                                ? Currency.fromCode(fxProperties.baseCurrency())
                                : Currency.fromCode(base);
    return ResponseEntity.ok(
        ApiResponse.ok("FX rates", fxRateService.getRates(baseCurrency)));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<List<FxRateResponse>>>
  refresh(@RequestParam(required = false) String base) {
    Currency baseCurrency = base == null || base.isBlank()
                                ? Currency.fromCode(fxProperties.baseCurrency())
                                : Currency.fromCode(base);
    fxRateService.refreshRates(baseCurrency);
    return ResponseEntity.ok(ApiResponse.ok(
        "FX rates refreshed", fxRateService.getRates(baseCurrency)));
  }
}
