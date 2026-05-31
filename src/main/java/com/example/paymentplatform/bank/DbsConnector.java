package com.example.paymentplatform.bank;

import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.service.BankRequestSigner;
import org.springframework.stereotype.Component;

@Component
public class DbsConnector extends AbstractMockBankConnector {

  public DbsConnector(BankRequestSigner bankRequestSigner) {
    super(bankRequestSigner);
  }

  @Override
  public BankPartner partner() {
    return BankPartner.DBS;
  }

  @Override
  protected String buildMessage(
      com.example.paymentplatform.dto.bank.BankSimulationRequest request) {
    return "DBS connector accepted " + request.requestType();
  }
}
