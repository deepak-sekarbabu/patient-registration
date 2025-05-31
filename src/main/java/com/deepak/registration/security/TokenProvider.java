package com.deepak.registration.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

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

  /** Creates a JWT access token for the given user ID */
  public String createAccessToken(String userId, String phoneNumber) {
    return Jwts.builder()
        .setSubject(userId)
        .claim("phoneNumber", phoneNumber)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  /** Creates a JWT refresh token for the given user ID */
  public String createRefreshToken(String userId) {
    String tokenId = UUID.randomUUID().toString();

    return Jwts.builder()
        .setSubject(userId)
        .setId(tokenId) // Use jti claim for token ID
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  /** Validates an access token */
  public boolean validateAccessToken(String token) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
      return true;
    } catch (SignatureException
        | MalformedJwtException
        | ExpiredJwtException
        | UnsupportedJwtException
        | IllegalArgumentException e) {
      return false;
    }
  }

  /** Validates a refresh token */
  public boolean validateRefreshToken(String token) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
      return true;
    } catch (SignatureException
        | MalformedJwtException
        | ExpiredJwtException
        | UnsupportedJwtException
        | IllegalArgumentException e) {
      return false;
    }
  }

  /** Extracts user ID from a token */
  public String getUserIdFromToken(String token) {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
  }

  /** Extracts user ID specifically from a refresh token */
  public String getUserIdFromRefreshToken(String refreshToken) {
    return Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(refreshToken)
        .getBody()
        .getSubject();
  }

  /** Generate a cookie containing the access token */
  public ResponseCookie generateAccessTokenCookie(String accessToken) {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
        .domain(cookieDomain)
        .path("/")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .maxAge(accessTokenExpirationMs / 1000) // Convert
        // to
        // seconds
        .build();
  }

  /** Generate a cookie containing the refresh token */
  public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .domain(cookieDomain)
        .path("/")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .maxAge(refreshTokenExpirationMs / 1000) // Convert to seconds
        .build();
  }

  /** Generate a cookie to clear the access token */
  public ResponseCookie generateClearAccessTokenCookie() {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
        .domain(cookieDomain)
        .path("/")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .maxAge(0)
        .build();
  }

  /** Generate a cookie to clear the refresh token */
  public ResponseCookie generateClearRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .domain(cookieDomain)
        .path("/")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .maxAge(0)
        .build();
  }

  /** Extract token from header or cookies */
  public String extractAccessToken(HttpServletRequest request) {
    // First try to get token from header
    String headerToken = extractTokenFromHeader(request);
    if (headerToken != null) {
      return headerToken;
    }

    // If not in header, try to get from cookies
    return extractAccessTokenFromCookies(request);
  }

  /** Extract token from header */
  private String extractTokenFromHeader(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /** Extract token from cookie */
  public String extractAccessTokenFromCookies(HttpServletRequest request) {
    return extractCookieValue(request, ACCESS_TOKEN_COOKIE_NAME).orElse(null);
  }

  /** Extracts refresh token from cookies */
  public String extractRefreshTokenFromCookies(HttpServletRequest request) {
    return extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME).orElse(null);
  }

  /** Helper method to extract cookie value */
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
