package com.example.paymentplatform.config;

import com.example.paymentplatform.security.CustomUserDetailsService;
import com.example.paymentplatform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider
  authenticationProvider(CustomUserDetailsService userDetailsService,
                         PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager
  authenticationManager(DaoAuthenticationProvider authenticationProvider) {
    return new ProviderManager(authenticationProvider);
  }

  @Bean
  public SecurityFilterChain
  securityFilterChain(HttpSecurity http,
                      JwtAuthenticationFilter jwtAuthenticationFilter,
                      DaoAuthenticationProvider provider) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session
            -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(provider)
        .authorizeHttpRequests(auth
                               -> auth.requestMatchers("/api/auth/**",
                                                       "/actuator/health",
                                                       "/error")
                                      .permitAll()
                                      .requestMatchers("/api/admin/**")
                                      .hasRole("ADMIN")
                                      .anyRequest()
                                      .authenticated())
        .addFilterBefore(jwtAuthenticationFilter,
                         UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
