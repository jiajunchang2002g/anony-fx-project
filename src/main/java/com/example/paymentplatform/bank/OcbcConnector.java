package com.example.paymentplatform.bank;

import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.service.BankRequestSigner;
import org.springframework.stereotype.Component;

@Component
public class OcbcConnector extends AbstractMockBankConnector {

  public OcbcConnector(BankRequestSigner bankRequestSigner) {
    super(bankRequestSigner);
  }

  @Override
  public BankPartner partner() {
    return BankPartner.OCBC;
  }

  @Override
  protected String buildMessage(
      com.example.paymentplatform.dto.bank.BankSimulationRequest request) {
    return "OCBC connector accepted " + request.requestType();
  }
}
