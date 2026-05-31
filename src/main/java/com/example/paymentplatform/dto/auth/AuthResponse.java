package com.example.paymentplatform.dto.auth;

import com.example.paymentplatform.dto.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {}
