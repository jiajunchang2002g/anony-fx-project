package com.example.paymentplatform.security;

import com.example.paymentplatform.entity.AppUser;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

  private final UUID id;
  private final String email;
  private final String password;
  private final boolean enabled;
  private final List<GrantedAuthority> authorities;

  public UserPrincipal(AppUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.password = user.getPasswordHash();
    this.enabled =
        user.getStatus() == null || user.getStatus().name().equals("ACTIVE");
    this.authorities =
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  public UUID getId() { return id; }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return enabled;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
