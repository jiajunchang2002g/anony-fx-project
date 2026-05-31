package com.example.paymentplatform.controller;

import com.example.paymentplatform.dto.ApiResponse;
import com.example.paymentplatform.dto.bank.BankSimulationRequest;
import com.example.paymentplatform.dto.bank.BankSimulationResponse;
import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.service.BankConnectorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank-connectors")
public class BankConnectorController {

  private final BankConnectorService bankConnectorService;

  public BankConnectorController(BankConnectorService bankConnectorService) {
    this.bankConnectorService = bankConnectorService;
  }

  @PostMapping("/{partner}/simulate")
  public ResponseEntity<ApiResponse<BankSimulationResponse>>
  simulate(@PathVariable BankPartner partner,
           @Valid @RequestBody BankSimulationRequest request) {
    BankSimulationResponse response =
        bankConnectorService.simulate(partner, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("Bank connector simulation completed",
                                  response));
  }
}
