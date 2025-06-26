package com.deepak.patient.registration.service;

import com.deepak.patient.registration.model.patient.auth.RefreshToken;
import com.deepak.patient.registration.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${app.jwtRefreshExpirationMs:604800000}") // 7 days default
  private Long refreshTokenDurationMs;

  private static final SecureRandom secureRandom = new SecureRandom();
  private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

  public RefreshToken createRefreshToken(Long userId) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUserId(userId.intValue());
    refreshToken.setToken(generateRandomToken());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
    refreshToken.setRevoked(false);
    return refreshTokenRepository.save(refreshToken);
  }

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().isBefore(Instant.now()) || token.isRevoked()) {
      refreshTokenRepository.delete(token);
      throw new RuntimeException("Refresh token was expired or revoked. Please login again.");
    }
    return token;
  }

  @Transactional
  public void deleteByUserId(Long userId) {
    refreshTokenRepository.revokeAllUserTokens(userId);
  }

  @Transactional
  public void deleteExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens();
  }

  private String generateRandomToken() {
    byte[] randomBytes = new byte[64];
    secureRandom.nextBytes(randomBytes);
    return base64Encoder.encodeToString(randomBytes);
  }
}
