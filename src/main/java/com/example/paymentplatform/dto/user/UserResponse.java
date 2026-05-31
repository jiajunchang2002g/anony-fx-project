package com.example.paymentplatform.dto.user;

import com.example.paymentplatform.entity.AppUser;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String role,
                           Instant createdAt) {

  public static UserResponse from(AppUser user) {
    return new UserResponse(user.getId(), user.getEmail(),
                            user.getRole().name(), user.getCreatedAt());
  }
}
