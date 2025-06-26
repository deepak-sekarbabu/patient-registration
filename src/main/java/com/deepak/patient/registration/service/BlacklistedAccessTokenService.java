package com.deepak.patient.registration.service;

import com.deepak.patient.registration.entity.BlacklistedAccessToken;
import com.deepak.patient.registration.repository.BlacklistedAccessTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistedAccessTokenService {
  private final BlacklistedAccessTokenRepository repository;

  public void blacklistToken(String token, Instant expiryDate) {
    BlacklistedAccessToken entity = new BlacklistedAccessToken();
    entity.setToken(token);
    entity.setExpiryDate(expiryDate);
    repository.save(entity);
  }

  public boolean isTokenBlacklisted(String token) {
    repository.deleteByExpiryDateBefore(Instant.now()); // Clean up expired
    return repository.findByToken(token).isPresent();
  }
}
