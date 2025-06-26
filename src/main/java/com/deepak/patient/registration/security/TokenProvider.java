package com.deepak.patient.registration.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Provides utility methods for JWT (JSON Web Token) creation, validation, and cookie management.
 * This class handles the generation of access and refresh tokens, validation of these tokens, and
 * the creation of HTTP cookies to store them. Configuration values for JWT secret, expiration
 * times, and cookie properties are injected from application properties.
 */
@Component
public class TokenProvider {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  public SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  @Value("${app.jwt.access-token-expiration-ms}")
  private long accessTokenExpirationMs;

  @Value("${app.jwt.refresh-token-expiration-ms}")
  private long refreshTokenExpirationMs;

  @Value("${app.cookies.domain}")
  private String cookieDomain;

  @Value("${app.cookies.secure}")
  private boolean cookieSecure;

  private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  /**
   * Creates a JWT access token for the given user ID and phone number. The token includes the user
   * ID as the subject and phone number as a custom claim. It is signed using the HS512 algorithm
   * and has a configured expiration time.
   *
   * @param userId The unique identifier of the user.
   * @param phoneNumber The user's phone number.
   * @return A JWT access token string.
   */
  public String createAccessToken(String userId, String phoneNumber) {
    return Jwts.builder()
        .subject(userId) // Standard claim: Subject (user ID)
        .claim("phoneNumber", phoneNumber) // Custom claim: phoneNumber
        .issuedAt(new Date()) // Standard claim: Issued At
        .expiration(
            new Date(
                System.currentTimeMillis()
                    + accessTokenExpirationMs)) // Standard claim: Expiration Date
        .signWith(getSigningKey()) // Sign with the signing key
        .compact();
  }

  /**
   * Creates a JWT refresh token for the given user ID. The token includes the user ID as the
   * subject and a unique token ID (jti). It is signed using the HS512 algorithm and has a
   * configured expiration time (typically longer than access tokens).
   *
   * @param userId The unique identifier of the user.
   * @return A JWT refresh token string.
   */
  public String createRefreshToken(String userId) {
    String tokenId = UUID.randomUUID().toString(); // Generate a unique ID for the refresh token

    return Jwts.builder()
        .subject(userId) // Standard claim: Subject (user ID)
        .id(tokenId) // Standard claim: JWT ID (jti)
        .issuedAt(new Date()) // Standard claim: Issued At
        .expiration(
            new Date(
                System.currentTimeMillis()
                    + refreshTokenExpirationMs)) // Standard claim: Expiration Date
        .signWith(getSigningKey()) // Sign with the signing key
        .compact();
  }

  /**
   * Validates an access token. Checks if the token is correctly signed, not malformed, not expired,
   * and supported.
   *
   * @param token The JWT access token string.
   * @return {@code true} if the token is valid, {@code false} otherwise.
   */
  public boolean validateAccessToken(String token) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
      return true;
    } catch (SignatureException e) {
      // logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      // logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      // logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      // logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      // logger.error("JWT claims string is empty: {}", e.getMessage());
    } catch (JwtException e) {
      // logger.error("JWT validation failed: {}", e.getMessage());
    }
    return false;
  }

  /**
   * Validates a refresh token. Similar to access token validation, checks signature, format,
   * expiration, and support.
   *
   * @param token The JWT refresh token string.
   * @return {@code true} if the token is valid, {@code false} otherwise.
   */
  public boolean validateRefreshToken(String token) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      // Optionally log these exceptions if needed for debugging, but typically
      // returning false is
      // sufficient for validation logic.
      // logger.warn("Refresh token validation failed: " + e.getMessage());
      return false;
    }
  }

  /** Extracts user ID from a token */
  public String getUserIdFromToken(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  /** Extracts user ID specifically from a refresh token */
  public String getUserIdFromRefreshToken(String refreshToken) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(refreshToken)
        .getPayload()
        .getSubject();
  }

  /**
   * Generates an HTTP-only cookie for the access token. Configures properties like domain, path,
   * HttpOnly, Secure, SameSite, and MaxAge. - HttpOnly: Prevents client-side JavaScript access,
   * mitigating XSS. - Secure: Transmits cookie only over HTTPS (if {@code app.cookies.secure} is
   * true). - SameSite="Lax": Provides some CSRF protection.
   *
   * @param accessToken The JWT access token string.
   * @return A {@link ResponseCookie} object for the access token.
   */
  public ResponseCookie generateAccessTokenCookie(String accessToken) {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
        .domain(cookieDomain) // Set cookie domain (e.g., ".example.com" or null for current host)
        .path("/") // Cookie is valid for all paths
        .httpOnly(false) // Protects against XSS attacks
        .secure(cookieSecure) // Send cookie only over HTTPS if true
        .sameSite(
            "Lax") // CSRF protection: cookie sent on top-level navigation's and GET requests from
        // other sites
        .maxAge(accessTokenExpirationMs / 1000) // Max age in seconds
        .build();
  }

  /**
   * Generates an HTTP-only cookie for the refresh token. Similar configuration as the access token
   * cookie, but typically with a longer MaxAge.
   *
   * @param refreshToken The JWT refresh token string.
   * @return A {@link ResponseCookie} object for the refresh token.
   */
  public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .domain(cookieDomain)
        .path("/")
        .httpOnly(false)
        .secure(cookieSecure)
        .sameSite("Lax") // Consider "Strict" if refresh token is only used on same-site requests
        .maxAge(refreshTokenExpirationMs / 1000) // Max age in seconds
        .build();
  }

  /**
   * Generates a cookie to clear/invalidate the access token cookie. This is achieved by setting an
   * empty value and MaxAge to 0.
   *
   * @return A {@link ResponseCookie} object that clears the access token cookie.
   */
  public ResponseCookie generateClearAccessTokenCookie() {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "") // Empty value
        .domain(cookieDomain)
        .path("/")
        .httpOnly(false)
        .secure(cookieSecure)
        .sameSite("Lax")
        .maxAge(0) // Expire immediately
        .build();
  }

  /**
   * Generates a cookie to clear/invalidate the refresh token cookie.
   *
   * @return A {@link ResponseCookie} object that clears the refresh token cookie.
   */
  public ResponseCookie generateClearRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .domain(cookieDomain)
        .path("/")
        .httpOnly(false)
        .secure(cookieSecure)
        .sameSite("Lax")
        .maxAge(0)
        .build();
  }

  /**
   * Extracts the JWT access token from the HttpServletRequest. It first attempts to retrieve the
   * token from the "Authorization" header (Bearer token). If not found in the header, it tries to
   * extract it from an HTTP cookie named "accessToken".
   *
   * @param request The incoming HttpServletRequest.
   * @return The JWT access token string if found, or {@code null} otherwise.
   */
  public String extractAccessToken(HttpServletRequest request) {
    // Priority 1: Extract token from 'Authorization: Bearer <token>' header
    String headerToken = extractTokenFromHeader(request);
    if (headerToken != null) {
      return headerToken;
    }

    // Priority 2: Extract token from 'accessToken' cookie
    return extractAccessTokenFromCookies(request);
  }

  /**
   * Extracts the token from the "Authorization" HTTP header. Expects the header in the format
   * "Bearer <token>".
   *
   * @param request The HttpServletRequest.
   * @return The token string if found and correctly formatted, otherwise {@code null}.
   */
  private String extractTokenFromHeader(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // Remove "Bearer " prefix
    }
    return null;
  }

  /**
   * Extracts the access token value from the "accessToken" cookie.
   *
   * @param request The HttpServletRequest.
   * @return The access token string from the cookie, or {@code null} if not found.
   */
  public String extractAccessTokenFromCookies(HttpServletRequest request) {
    return extractCookieValue(request, ACCESS_TOKEN_COOKIE_NAME).orElse(null);
  }

  /**
   * Extracts the refresh token value from the "refreshToken" cookie.
   *
   * @param request The HttpServletRequest.
   * @return The refresh token string from the cookie, or {@code null} if not found.
   */
  public String extractRefreshTokenFromCookies(HttpServletRequest request) {
    return extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME).orElse(null);
  }

  /**
   * Helper method to extract a specific cookie's value by its name.
   *
   * @param request The HttpServletRequest.
   * @param cookieName The name of the cookie to extract.
   * @return An {@link Optional} containing the cookie value if found, otherwise {@link
   *     Optional#empty()}.
   */
  private Optional<String> extractCookieValue(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookieName.equals(cookie.getName())) {
          return Optional.of(cookie.getValue());
        }
      }
    }
    return Optional.empty();
  }
}
