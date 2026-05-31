package com.example.paymentplatform.repository;

import com.example.paymentplatform.entity.AppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

  Optional<AppUser> findByEmailIgnoreCase(String email);
}
