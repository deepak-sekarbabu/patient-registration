package com.deepak.registration.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {

  private final RedisTemplate<String, String> redisTemplate;

  @Value("${app.jwt.refresh-token-expiration-ms}")
  private long refreshTokenExpirationMs;

  /**
   * Blacklists a refresh token to prevent its reuse
   *
   * @param token The refresh token to blacklist
   */
  public void blacklistRefreshToken(String token) {
    // Store token in Redis blacklist
    // Key format: "blacklisted_refresh_token:{token}"
    String key = "blacklisted_refresh_token:" + token;

    // Store with an expiration time matching the original token expiration
    redisTemplate
        .opsForValue()
        .set(key, "blacklisted", refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
  }

  /**
   * Check if a refresh token is blacklisted
   *
   * @param token The refresh token to check
   * @return true if blacklisted, false otherwise
   */
  public boolean isRefreshTokenBlacklisted(String token) {
    String key = "blacklisted_refresh_token:" + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  /**
   * Store user's session data with expiry
   *
   * @param userId The user ID
   * @param sessionData The session data to store
   * @param expiryTimeMs Expiry time in milliseconds
   */
  public void storeSessionData(String userId, String sessionData, long expiryTimeMs) {
    String key = "user_session:" + userId;
    redisTemplate.opsForValue().set(key, sessionData, expiryTimeMs, TimeUnit.MILLISECONDS);
  }

  /**
   * Get user's session data
   *
   * @param userId The user ID
   * @return The session data or null if not found
   */
  public String getSessionData(String userId) {
    String key = "user_session:" + userId;
    return redisTemplate.opsForValue().get(key);
  }

  /**
   * Invalidate user's session
   *
   * @param userId The user ID
   */
  public void invalidateSession(String userId) {
    String key = "user_session:" + userId;
    redisTemplate.delete(key);
  }
}
