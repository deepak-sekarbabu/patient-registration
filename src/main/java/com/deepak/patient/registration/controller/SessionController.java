package com.deepak.patient.registration.controller;

import com.deepak.patient.registration.model.patient.LoginRequest;
import com.deepak.patient.registration.model.patient.LoginResponse;
import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.model.patient.auth.RefreshToken;
import com.deepak.patient.registration.security.TokenProvider;
import com.deepak.patient.registration.service.BlacklistedAccessTokenService;
import com.deepak.patient.registration.service.PatientService;
import com.deepak.patient.registration.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth")
@Tag(name = "Session Controller", description = "Operations related to Session Management")
@RequiredArgsConstructor
public class SessionController {

  private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

  private final PatientService patientService;
  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final BlacklistedAccessTokenService blacklistedAccessTokenService;

  @Operation(
      summary = "Validate JWT token",
      description =
          "Validates a JWT token and returns patient info if valid. The token should be passed in the request body.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema =
                          @Schema(
                              type = "object",
                              example = "{\"token\": \"your-jwt-token-here\"}",
                              description = "Request body containing the JWT token to validate"))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Token is valid",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            type = "object",
                            example = "{\"valid\": true, \"patient\": { ... }}",
                            description = "Response when token is valid"))),
        @ApiResponse(
            responseCode = "401",
            description = "Token is invalid or expired",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            type = "object",
                            example =
                                "{\"valid\": false, \"message\": \"Invalid or expired token\"}",
                            description = "Response when token is invalid or expired"))),
        @ApiResponse(
            responseCode = "400",
            description = "No token provided",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            type = "object",
                            example = "{\"valid\": false, \"message\": \"No token provided\"}",
                            description = "Response when no token is provided")))
      })
  @PostMapping("/validate")
  public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
    try {
      String token = tokenRequest.get("token");
      if (token == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("valid", false, "message", "No token provided"));
      }
      if (blacklistedAccessTokenService.isTokenBlacklisted(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("valid", false, "message", "Token is blacklisted (logged out)"));
      }
      if (!tokenProvider.validateAccessToken(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("valid", false, "message", "Invalid or expired token"));
      }
      String userId = tokenProvider.getUserIdFromToken(token);
      Long patientId = Long.valueOf(userId);
      Patient patient = patientService.getPatientById(patientId);
      if (patient == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("valid", false, "message", "Patient not found"));
      }
      return ResponseEntity.ok(Map.of("valid", true, "patient", patient));
    } catch (NumberFormatException e) {
      logger.error("Invalid user ID format in token: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("valid", false, "message", "Invalid user ID format"));
    } catch (Exception e) {
      logger.error("Token validation error: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("valid", false, "message", "Token validation failed: " + e.getMessage()));
    }
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
  @Transactional
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    logger.info("Refreshing token for request: {}", request.getRequestURI());
    String requestRefreshToken = tokenProvider.extractRefreshTokenFromCookies(request);
    if (requestRefreshToken == null) {
      logger.warn("No refresh token found in cookies");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
    }
    try {
      return refreshTokenService
          .findByToken(requestRefreshToken)
          .map(
              refreshToken -> {
                RefreshToken verifiedToken = refreshTokenService.verifyExpiration(refreshToken);
                Long userId = verifiedToken.getUserId().longValue();
                Patient patient = patientService.getPatientById(userId);
                if (patient == null || patient.getPersonalDetails() == null) {
                  logger.warn("Patient or personal details not found for userId: {}", userId);
                  return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User data not found");
                }
                String phoneNumber = patient.getPersonalDetails().getPhoneNumber();
                // Generate new tokens
                String newAccessToken =
                    tokenProvider.createAccessToken(String.valueOf(userId), phoneNumber);
                RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userId);
                // Revoke old refresh tokens
                refreshTokenService.deleteByUserId(userId);
                // Set cookies
                ResponseCookie accessTokenCookie =
                    tokenProvider.generateAccessTokenCookie(newAccessToken);
                ResponseCookie refreshTokenCookie =
                    tokenProvider.generateRefreshTokenCookie(newRefreshToken.getToken());
                response.addHeader("Set-Cookie", accessTokenCookie.toString());
                response.addHeader("Set-Cookie", refreshTokenCookie.toString());
                logger.info("Set new access and refresh token cookies for userId: {}", userId);
                return ResponseEntity.ok().body("Token refreshed successfully");
              })
          .orElseGet(
              () ->
                  ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                      .body("Refresh token not found or expired"));
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
  @Transactional
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    logger.info("Logging out user for request: {}", request.getRequestURI());
    String refreshToken = tokenProvider.extractRefreshTokenFromCookies(request);
    // Blacklist the access token
    String accessToken = tokenProvider.extractAccessToken(request);
    if (accessToken != null && tokenProvider.validateAccessToken(accessToken)) {
      Date expiry =
          Jwts.parser()
              .verifyWith(tokenProvider.getSigningKey())
              .build()
              .parseSignedClaims(accessToken)
              .getPayload()
              .getExpiration();
      blacklistedAccessTokenService.blacklistToken(accessToken, expiry.toInstant());
    }
    if (refreshToken != null) {
      refreshTokenService
          .findByToken(refreshToken)
          .ifPresent(
              token -> {
                token.setRevoked(true);
                refreshTokenService.deleteByUserId(token.getUserId().longValue());
              });
      logger.info("Revoked refresh token during logout");
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

  @Operation(
      summary = "Patient Login",
      description =
          "Validates login credentials and returns patient info and JWT token if successful.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Login request with phone number and password",
              content = @Content(schema = @Schema(implementation = LoginRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
      })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    logger.info("Received request: Login for phone number: {}", loginRequest.getPhoneNumber());
    Patient patient =
        patientService.validateLogin(loginRequest.getPhoneNumber(), loginRequest.getPassword());
    if (patient == null) {
      logger.warn("Login failed for phone number: {}", loginRequest.getPhoneNumber());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    logger.info("Login successful for phone number: {}", loginRequest.getPhoneNumber());
    String userId = String.valueOf(patient.getId());
    String accessToken = tokenProvider.createAccessToken(userId, patient.getPhoneNumber());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(patient.getId());
    // Set cookies
    ResponseCookie accessTokenCookie = tokenProvider.generateAccessTokenCookie(accessToken);
    ResponseCookie refreshTokenCookie =
        tokenProvider.generateRefreshTokenCookie(refreshToken.getToken());
    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    LoginResponse loginResponse = new LoginResponse(patient, accessToken);
    return ResponseEntity.ok(loginResponse);
  }
}
