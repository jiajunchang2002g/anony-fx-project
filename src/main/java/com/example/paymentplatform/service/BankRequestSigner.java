package com.example.paymentplatform.service;

import com.example.paymentplatform.config.BankProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class BankRequestSigner {

  private final BankProperties bankProperties;

  public BankRequestSigner(BankProperties bankProperties) {
    this.bankProperties = bankProperties;
  }

  public String sign(String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(
          bankProperties.sharedSecret().getBytes(StandardCharsets.UTF_8),
          "HmacSHA256"));
      return Base64.getEncoder().encodeToString(
          mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to sign bank request", exception);
    }
  }
}
