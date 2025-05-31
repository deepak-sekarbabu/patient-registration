package com.deepak.registration.service;

import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Manages user session data and refresh token blacklisting in memory. This implementation uses
 * {@link ConcurrentHashMap} for storing session information and blacklisted refresh tokens.
 *
 * <p><b>Note:</b> This in-memory approach is suitable for single-instance deployments. For
 * distributed environments or applications requiring persistent session management and token
 * blacklisting across multiple instances, an external distributed cache like Redis (which is part
 * of this project's stack for other caching purposes) would be a more robust solution.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

  private final ConcurrentHashMap<String, SessionData> sessionMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> refreshTokenBlacklist = new ConcurrentHashMap<>();

  @Value("${app.jwt.refresh-token-expiration-ms}")
  private long refreshTokenExpirationMs;

  /**
   * Blacklists a refresh token to prevent its reuse, typically after it has been used to issue a
   * new access token or during logout. The token is stored with an expiration time based on {@code
   * app.jwt.refresh-token-expiration-ms}. This method also performs a cleanup of other expired
   * tokens from the blacklist.
   *
   * @param token The refresh token (JWT ID or the token itself) to blacklist.
   */
  public void blacklistRefreshToken(String token) {
    // Store the token with its calculated expiration time.
    // The value stored is the timestamp when this token should be considered expired from the
    // blacklist.
    refreshTokenBlacklist.put(token, System.currentTimeMillis() + refreshTokenExpirationMs);

    // Proactively clean up any tokens in the blacklist whose expiration time has passed.
    // This helps to keep the blacklist map from growing indefinitely.
    refreshTokenBlacklist
        .entrySet()
        .removeIf(entry -> entry.getValue() < System.currentTimeMillis());
  }

  /**
   * Checks if a given refresh token is currently blacklisted. A token is considered blacklisted if
   * it exists in the blacklist and its recorded expiration time has not yet passed. If a token is
   * found in the blacklist but its expiration time has passed, it is removed from the blacklist and
   * considered not blacklisted.
   *
   * @param token The refresh token (JWT ID or the token itself) to check.
   * @return {@code true} if the token is blacklisted and still active, {@code false} otherwise.
   */
  public boolean isRefreshTokenBlacklisted(String token) {
    Long expiryTime = refreshTokenBlacklist.get(token);
    if (expiryTime == null) {
      // Token not found in blacklist
      return false;
    }
    if (expiryTime < System.currentTimeMillis()) {
      // Token found, but its blacklist entry has expired; remove it.
      refreshTokenBlacklist.remove(token);
      return false;
    }
    // Token is in the blacklist and its blacklist entry is still active.
    return true;
  }

  /**
   * Stores arbitrary session data for a user, associated with an expiry time. This could be used
   * for simple session attributes. This method also performs a cleanup of other expired session
   * data.
   *
   * @param userId The user ID to associate with the session data.
   * @param sessionData The string representation of session data to store.
   * @param expiryTimeMs The duration in milliseconds for which the session data should be valid.
   */
  public void storeSessionData(String userId, String sessionData, long expiryTimeMs) {
    // Store the session data along with its calculated absolute expiration timestamp.
    sessionMap.put(userId, new SessionData(sessionData, System.currentTimeMillis() + expiryTimeMs));

    // Proactively clean up any session data in the map whose expiration time has passed.
    // This helps to keep the session map from growing indefinitely.
    sessionMap
        .entrySet()
        .removeIf(entry -> entry.getValue().getExpiryTime() < System.currentTimeMillis());
  }

  /**
   * Retrieves active session data for a user.
   *
   * @param userId The user ID whose session data is to be retrieved.
   * @return The session data string if found and not expired, or {@code null} otherwise.
   */
  public String getSessionData(String userId) {
    SessionData sessionDataWrapper = sessionMap.get(userId);
    if (sessionDataWrapper == null
        || sessionDataWrapper.getExpiryTime() < System.currentTimeMillis()) {
      // Session data not found or has expired
      if (sessionDataWrapper != null) {
        // Explicitly remove expired session data if encountered during retrieval
        sessionMap.remove(userId);
      }
      return null;
    }
    return sessionDataWrapper.getSessionData();
  }

  /**
   * Invalidates (removes) all session data associated with a user ID.
   *
   * @param userId The user ID whose session is to be invalidated.
   */
  public void invalidateSession(String userId) {
    sessionMap.remove(userId);
  }

  /** Inner class to hold session data along with its expiration timestamp. */
  private static class SessionData {
    private final String sessionData;
    private final long expiryTime; // Absolute timestamp (ms since epoch) when this data expires

    /**
     * Constructs a SessionData wrapper.
     *
     * @param sessionData The actual session data string.
     * @param expiryTime The absolute time (milliseconds since epoch) at which this data expires.
     */
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
