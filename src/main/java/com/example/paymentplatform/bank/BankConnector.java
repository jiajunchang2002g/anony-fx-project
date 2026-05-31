package com.example.paymentplatform.bank;

import com.example.paymentplatform.dto.bank.BankSimulationRequest;
import com.example.paymentplatform.dto.bank.BankSimulationResponse;
import com.example.paymentplatform.entity.BankPartner;

public interface BankConnector {

  BankPartner partner();

  BankSimulationResponse simulate(BankSimulationRequest request);
}
