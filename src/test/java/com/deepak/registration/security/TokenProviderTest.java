package com.deepak.registration.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

class TokenProviderTest {

  private TokenProvider tokenProvider;

  @BeforeEach
  void setUp() {
    tokenProvider = new TokenProvider();
    // Manually set the fields that would be injected by @Value
    ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "test-secret");
    ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationMs", 1000L);
    ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationMs", 2000L);
    ReflectionTestUtils.setField(tokenProvider, "cookieDomain", "localhost");
    ReflectionTestUtils.setField(tokenProvider, "cookieSecure", false);
  }

  @Test
  void testGenerateAccessTokenCookie_HttpOnlyFalse() {
    // Setup: Provide necessary values for @Value fields
    // Using ReflectionTestUtils from Spring Test module
    ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "test-secret-for-access");
    ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationMs", 3600000L); // 1 hour
    ReflectionTestUtils.setField(tokenProvider, "cookieDomain", "localhost");
    ReflectionTestUtils.setField(tokenProvider, "cookieSecure", false);

    ResponseCookie accessTokenCookie = tokenProvider.generateAccessTokenCookie("dummyAccessToken");
    assertFalse(accessTokenCookie.isHttpOnly(), "Access token cookie should not be HttpOnly");
    assertFalse(
        accessTokenCookie.toString().contains("HttpOnly"),
        "Access token cookie string should not contain HttpOnly");
  }

  @Test
  void testGenerateRefreshTokenCookie_HttpOnlyFalse() {
    // Setup: Provide necessary values for @Value fields
    ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "test-secret-for-refresh");
    ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationMs", 86400000L); // 1 day
    ReflectionTestUtils.setField(tokenProvider, "cookieDomain", "localhost");
    ReflectionTestUtils.setField(tokenProvider, "cookieSecure", false);

    ResponseCookie refreshTokenCookie =
        tokenProvider.generateRefreshTokenCookie("dummyRefreshToken");
    assertFalse(refreshTokenCookie.isHttpOnly(), "Refresh token cookie should not be HttpOnly");
    assertFalse(
        refreshTokenCookie.toString().contains("HttpOnly"),
        "Refresh token cookie string should not contain HttpOnly");
  }
}
