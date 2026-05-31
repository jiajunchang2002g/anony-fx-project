package com.example.paymentplatform.bank;

import com.example.paymentplatform.dto.bank.BankSimulationRequest;
import com.example.paymentplatform.dto.bank.BankSimulationResponse;
import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.service.BankRequestSigner;
import java.time.Instant;

public abstract class AbstractMockBankConnector implements BankConnector {

  private final BankRequestSigner bankRequestSigner;

  protected AbstractMockBankConnector(BankRequestSigner bankRequestSigner) {
    this.bankRequestSigner = bankRequestSigner;
  }

  @Override
  public BankSimulationResponse simulate(BankSimulationRequest request) {
    String responseMaterial = partner().name() + "|" + request.requestType() +
                              "|" + request.payload();
    String signature = bankRequestSigner.sign(responseMaterial);
    return new BankSimulationResponse(partner(), "SUCCESS", signature,
                                      buildMessage(request), Instant.now());
  }

  protected String buildMessage(BankSimulationRequest request) {
    return partner().name() + " processed " + request.requestType();
  }

  protected BankRequestSigner signer() { return bankRequestSigner; }
}
