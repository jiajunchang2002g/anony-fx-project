package com.example.paymentplatform.dto.bank;

import com.example.paymentplatform.entity.BankPartner;
import java.time.Instant;

public record BankSimulationResponse(BankPartner partner, String status,
                                     String signature, String message,
                                     Instant timestamp) {}
