package com.example.paymentplatform.service;

import com.example.paymentplatform.dto.auth.AuthResponse;
import com.example.paymentplatform.dto.auth.LoginRequest;
import com.example.paymentplatform.dto.auth.RegisterRequest;
import com.example.paymentplatform.dto.user.UserResponse;
import com.example.paymentplatform.entity.AppUser;
import com.example.paymentplatform.entity.Role;
import com.example.paymentplatform.entity.UserStatus;
import com.example.paymentplatform.exception.ConflictException;
import com.example.paymentplatform.exception.UnauthorizedException;
import com.example.paymentplatform.repository.UserRepository;
import com.example.paymentplatform.security.JwtService;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
    if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
      throw new ConflictException("Email is already registered");
    }

    AppUser user = new AppUser();
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER);
    user.setStatus(UserStatus.ACTIVE);
    userRepository.saveAndFlush(user);
    return new AuthResponse(jwtService.generateToken(user),
                            UserResponse.from(user));
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        request.email().trim().toLowerCase(Locale.ROOT), request.password()));
    AppUser user =
        userRepository
            .findByEmailIgnoreCase(
                request.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(
                () -> new UnauthorizedException("Invalid credentials"));
    return new AuthResponse(jwtService.generateToken(user),
                            UserResponse.from(user));
  }
}
