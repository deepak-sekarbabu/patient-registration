package com.deepak.patient.registration.repository;

import com.deepak.patient.registration.entity.BlacklistedAccessToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedAccessTokenRepository
    extends JpaRepository<BlacklistedAccessToken, Long> {
  Optional<BlacklistedAccessToken> findByToken(String token);

  void deleteByExpiryDateBefore(Instant now);
}
