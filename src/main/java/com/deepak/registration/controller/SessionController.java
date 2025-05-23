package com.deepak.registration.controller;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.security.TokenProvider;
import com.deepak.registration.service.PatientService;
import com.deepak.registration.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class SessionController {

  private final SessionService sessionService;
  private final PatientService patientService;
  private final TokenProvider tokenProvider;

  @GetMapping("/validate")
  public ResponseEntity<?> validateSession(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Check if the user is authenticated
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {

      Map<String, Object> response = new HashMap<>();
      response.put("valid", false);
      response.put("message", "Session is invalid or expired");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Get user ID from authentication
    String userId = authentication.getName();
    // Patient patient = patientService.getPatientById(userId);
    Patient patient = patientService.getPatientById(1L);

    Map<String, Object> response = new HashMap<>();
    response.put("valid", true);
    response.put("patient", patient);

    // No need to send the actual token since we're using HttpOnly cookies
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = tokenProvider.extractRefreshTokenFromCookies(request);
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
    }

    try {
      String userId = tokenProvider.getUserIdFromRefreshToken(refreshToken);

      // Check if the token is blacklisted (logged out)
      if (sessionService.isRefreshTokenBlacklisted(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
      }

      // Create new tokens
      String newAccessToken = tokenProvider.createAccessToken(userId);
      String newRefreshToken = tokenProvider.createRefreshToken(userId);

      // Blacklist old refresh token
      sessionService.blacklistRefreshToken(refreshToken);

      // Create and set cookies
      ResponseCookie accessTokenCookie = tokenProvider.generateAccessTokenCookie(newAccessToken);
      ResponseCookie refreshTokenCookie = tokenProvider.generateRefreshTokenCookie(newRefreshToken);

      response.addHeader("Set-Cookie", accessTokenCookie.toString());
      response.addHeader("Set-Cookie", refreshTokenCookie.toString());

      return ResponseEntity.ok().body("Token refreshed successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to refresh token");
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = tokenProvider.extractRefreshTokenFromCookies(request);

    // Blacklist the refresh token if it exists
    if (refreshToken != null) {
      sessionService.blacklistRefreshToken(refreshToken);
    }

    // Clear cookies
    ResponseCookie accessTokenCookie = tokenProvider.generateClearAccessTokenCookie();
    ResponseCookie refreshTokenCookie = tokenProvider.generateClearRefreshTokenCookie();

    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());

    return ResponseEntity.ok().body("Logged out successfully");
  }
}
