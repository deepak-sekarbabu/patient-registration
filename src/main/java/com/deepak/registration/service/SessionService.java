package com.deepak.registration.service;

import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {

  private final ConcurrentHashMap<String, SessionData> sessionMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> refreshTokenBlacklist = new ConcurrentHashMap<>();

  @Value("${app.jwt.refresh-token-expiration-ms}")
  private long refreshTokenExpirationMs;

  /**
   * Blacklists a refresh token to prevent its reuse
   *
   * @param token The refresh token to blacklist
   */
  public void blacklistRefreshToken(String token) {
    // Store token with expiration time
    refreshTokenBlacklist.put(token, System.currentTimeMillis() + refreshTokenExpirationMs);

    // Clean up expired tokens
    refreshTokenBlacklist
        .entrySet()
        .removeIf(entry -> entry.getValue() < System.currentTimeMillis());
  }

  /**
   * Check if a refresh token is blacklisted
   *
   * @param token The refresh token to check
   * @return true if blacklisted, false otherwise
   */
  public boolean isRefreshTokenBlacklisted(String token) {
    Long expiryTime = refreshTokenBlacklist.get(token);
    if (expiryTime == null) {
      return false;
    }
    if (expiryTime < System.currentTimeMillis()) {
      // Token expired, remove it
      refreshTokenBlacklist.remove(token);
      return false;
    }
    return true;
  }

  /**
   * Store user's session data with expiry
   *
   * @param userId The user ID
   * @param sessionData The session data to store
   * @param expiryTimeMs Expiry time in milliseconds
   */
  public void storeSessionData(String userId, String sessionData, long expiryTimeMs) {
    sessionMap.put(userId, new SessionData(sessionData, System.currentTimeMillis() + expiryTimeMs));

    // Clean up expired sessions
    sessionMap
        .entrySet()
        .removeIf(entry -> entry.getValue().getExpiryTime() < System.currentTimeMillis());
  }

  /**
   * Get user's session data
   *
   * @param userId The user ID
   * @return The session data or null if not found or expired
   */
  public String getSessionData(String userId) {
    SessionData sessionData = sessionMap.get(userId);
    if (sessionData == null || sessionData.getExpiryTime() < System.currentTimeMillis()) {
      return null;
    }
    return sessionData.getSessionData();
  }

  /**
   * Invalidate user's session
   *
   * @param userId The user ID
   */
  public void invalidateSession(String userId) {
    sessionMap.remove(userId);
  }

  private static class SessionData {
    private final String sessionData;
    private final long expiryTime;

    public SessionData(String sessionData, long expiryTime) {
      this.sessionData = sessionData;
      this.expiryTime = expiryTime;
    }

    public String getSessionData() {
      return sessionData;
    }

    public long getExpiryTime() {
      return expiryTime;
    }
  }
}
