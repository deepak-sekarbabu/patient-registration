package com.deepak.registration.controller;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.security.TokenProvider;
import com.deepak.registration.service.PatientService;
import com.deepak.registration.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Session Controller", description = "Operations related to Session Management")
@RequiredArgsConstructor
public class SessionController {

  private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

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
    logger.info("Validating session for request: {}", request.getRequestURI());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Check if the user is authenticated
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      logger.warn("Session invalid or expired. Authentication: {}", authentication);
      Map<String, Object> response = new HashMap<>();
      response.put("valid", false);
      response.put("message", "Session is invalid or expired");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Get user ID from authentication
    String userId = authentication.getName();
    logger.info("Session valid for userId: {}", userId);
    Long id = Long.parseLong(userId);
    Patient patient = patientService.getPatientById(id);

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
    logger.info("Refreshing token for request: {}", request.getRequestURI());
    String refreshToken = tokenProvider.extractRefreshTokenFromCookies(request);
    if (refreshToken == null) {
      logger.warn("No refresh token found in cookies");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
    }

    try {
      String userId = tokenProvider.getUserIdFromRefreshToken(refreshToken);
      logger.info("Refresh token belongs to userId: {}", userId);

      // Check if the token is blacklisted (logged out)
      if (sessionService.isRefreshTokenBlacklisted(refreshToken)) {
        logger.warn("Refresh token is blacklisted for userId: {}", userId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
      }

      // Create new tokens
      String newAccessToken = tokenProvider.createAccessToken(userId);
      String newRefreshToken = tokenProvider.createRefreshToken(userId);
      logger.info("Generated new access and refresh tokens for userId: {}", userId);

      // Blacklist old refresh token
      sessionService.blacklistRefreshToken(refreshToken);
      logger.info("Blacklisted old refresh token for userId: {}", userId);

      // Create and set cookies
      ResponseCookie accessTokenCookie = tokenProvider.generateAccessTokenCookie(newAccessToken);
      ResponseCookie refreshTokenCookie = tokenProvider.generateRefreshTokenCookie(newRefreshToken);

      response.addHeader("Set-Cookie", accessTokenCookie.toString());
      response.addHeader("Set-Cookie", refreshTokenCookie.toString());

      logger.info("Set new access and refresh token cookies for userId: {}", userId);
      return ResponseEntity.ok().body("Token refreshed successfully");
    } catch (Exception e) {
      logger.error("Failed to refresh token", e);
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
    logger.info("Logging out user for request: {}", request.getRequestURI());
    String refreshToken = tokenProvider.extractRefreshTokenFromCookies(request);

    // Blacklist the refresh token if it exists
    if (refreshToken != null) {
      sessionService.blacklistRefreshToken(refreshToken);
      logger.info("Blacklisted refresh token during logout");
    } else {
      logger.warn("No refresh token found during logout");
    }

    // Clear cookies
    ResponseCookie accessTokenCookie = tokenProvider.generateClearAccessTokenCookie();
    ResponseCookie refreshTokenCookie = tokenProvider.generateClearRefreshTokenCookie();

    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());

    logger.info("Cleared access and refresh token cookies during logout");
    return ResponseEntity.ok().body("Logged out successfully");
  }
}
