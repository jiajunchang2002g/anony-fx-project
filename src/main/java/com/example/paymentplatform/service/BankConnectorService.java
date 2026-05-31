package com.example.paymentplatform.service;

import com.example.paymentplatform.bank.BankConnectorRegistry;
import com.example.paymentplatform.dto.bank.BankSimulationRequest;
import com.example.paymentplatform.dto.bank.BankSimulationResponse;
import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.entity.BankRequest;
import com.example.paymentplatform.entity.BankRequestStatus;
import com.example.paymentplatform.exception.UnauthorizedException;
import com.example.paymentplatform.repository.BankRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankConnectorService {

  private final BankConnectorRegistry registry;
  private final BankRequestRepository bankRequestRepository;
  private final BankRequestSigner signer;
  private final ObjectMapper objectMapper;

  public BankConnectorService(BankConnectorRegistry registry,
                              BankRequestRepository bankRequestRepository,
                              BankRequestSigner signer,
                              ObjectMapper objectMapper) {
    this.registry = registry;
    this.bankRequestRepository = bankRequestRepository;
    this.signer = signer;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public BankSimulationResponse simulate(BankPartner partner,
                                         BankSimulationRequest request) {
    String expectedToken = partner.name().toLowerCase() + "-token";
    if (!expectedToken.equals(request.accessToken())) {
      throw new UnauthorizedException("Invalid bank access token");
    }

    String requestSignature = signer.sign(
        partner.name() + "|" + request.requestType() + "|" + request.payload());
    BankRequest bankRequest = new BankRequest();
    bankRequest.setPartner(partner);
    bankRequest.setRequestType(request.requestType());
    bankRequest.setPayload(request.payload());
    bankRequest.setSignature(requestSignature);
    bankRequest.setStatus(BankRequestStatus.PENDING);
    bankRequestRepository.save(bankRequest);

    try {
      BankSimulationResponse response = registry.get(partner).simulate(request);
      bankRequest.setStatus(BankRequestStatus.SUCCESS);
      bankRequest.setResponseBody(objectMapper.writeValueAsString(response));
      bankRequestRepository.save(bankRequest);
      return response;
    } catch (Exception exception) {
      bankRequest.setStatus(BankRequestStatus.FAILED);
      bankRequest.setResponseBody(exception.getMessage());
      bankRequestRepository.save(bankRequest);
      throw new RuntimeException("Bank connector simulation failed", exception);
    }
  }
}
