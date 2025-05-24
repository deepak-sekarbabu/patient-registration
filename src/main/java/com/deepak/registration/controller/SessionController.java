package com.deepak.registration.controller;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.security.TokenProvider;
import com.deepak.registration.service.PatientService;
import com.deepak.registration.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class SessionController {

  private final SessionService sessionService;
  private final PatientService patientService;
  private final TokenProvider tokenProvider;

  @Operation(
      summary = "Validate session",
      description = "Validates the current user session and returns patient info if valid.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Session is valid",
            content = @Content(schema = @Schema(implementation = Patient.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Session is invalid or expired",
            content = @Content)
      })
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

  @Operation(
      summary = "Refresh authentication token",
      description = "Refreshes the authentication tokens using the refresh token in cookies.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "401",
            description = "No refresh token found or invalid session",
            content = @Content)
      })
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

  @Operation(
      summary = "Logout user",
      description = "Logs out the user by blacklisting the refresh token and clearing cookies.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Logged out successfully",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
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
