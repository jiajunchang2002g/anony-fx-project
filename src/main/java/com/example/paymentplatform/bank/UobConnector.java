package com.example.paymentplatform.bank;

import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.service.BankRequestSigner;
import org.springframework.stereotype.Component;

@Component
public class UobConnector extends AbstractMockBankConnector {

  public UobConnector(BankRequestSigner bankRequestSigner) {
    super(bankRequestSigner);
  }

  @Override
  public BankPartner partner() {
    return BankPartner.UOB;
  }

  @Override
  protected String buildMessage(
      com.example.paymentplatform.dto.bank.BankSimulationRequest request) {
    return "UOB connector accepted " + request.requestType();
  }
}
