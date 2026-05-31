package com.example.paymentplatform.security;

import com.example.paymentplatform.config.JwtProperties;
import com.example.paymentplatform.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.secretKey = Keys.hmacShaKeyFor(
        properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(AppUser user) {
    Instant now = Instant.now();
    Instant expiresAt =
        now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES);
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("uid", user.getId().toString())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(secretKey)
        .compact();
  }

  public String extractUsername(String token) {
    return claims(token).getSubject();
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(claims(token).get("uid", String.class));
  }

  public boolean isTokenValid(String token) {
    try {
      Claims claims = claims(token);
      return claims.getExpiration().after(new Date());
    } catch (RuntimeException exception) {
      return false;
    }
  }

  private Claims claims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
