package com.example.paymentplatform.repository;

import com.example.paymentplatform.entity.BankRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRequestRepository
    extends JpaRepository<BankRequest, UUID> {}
