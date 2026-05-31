package com.example.paymentplatform.bank;

import com.example.paymentplatform.entity.BankPartner;
import com.example.paymentplatform.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BankConnectorRegistry {

  private final Map<BankPartner, BankConnector> connectors;

  public BankConnectorRegistry(List<BankConnector> connectorList) {
    this.connectors =
        connectorList.stream().collect(Collectors.toUnmodifiableMap(
            BankConnector::partner, connector -> connector));
  }

  public BankConnector get(BankPartner partner) {
    BankConnector connector = connectors.get(partner);
    if (connector == null) {
      throw new NotFoundException("Unsupported bank partner: " + partner);
    }
    return connector;
  }
}
