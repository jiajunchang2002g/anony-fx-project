package com.example.paymentplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MultiCurrencyPaymentPlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(MultiCurrencyPaymentPlatformApplication.class, args);
  }
}
