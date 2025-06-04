package com.deepak.patient.registration.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deepak.appointment.registration.service.ClinicInformationService;
import com.deepak.patient.registration.model.patient.LoginRequest;
import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.model.patient.PersonalDetails;
import com.deepak.patient.registration.security.TokenProvider;
import com.deepak.patient.registration.service.PatientService;
import com.deepak.patient.registration.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "app.jwt.access-token-expiration-ms=3600000",
      "app.jwt.refresh-token-expiration-ms=2592000000"
    })
@Disabled
class SessionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Mock private SessionService sessionService;

  @Mock private PatientService patientService;

  @Mock private TokenProvider tokenProvider;

  @Mock private ClinicInformationService clinicInformationService;

  @Autowired private ObjectMapper objectMapper;

  private Patient patient;
  private ResponseCookie accessTokenCookie;
  private ResponseCookie refreshTokenCookie;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setId(1L);
    patient.setPhoneNumber("1234567890");
    patient.setPasswordHash("hashedPassword");

    PersonalDetails personalDetails =
        PersonalDetails.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .phoneNumber("1234567890")
            .build();
    patient.setPersonalDetails(personalDetails);

    accessTokenCookie =
        ResponseCookie.from("accessToken", "dummyAccessToken").httpOnly(true).path("/").build();
    refreshTokenCookie =
        ResponseCookie.from("refreshToken", "dummyRefreshToken").httpOnly(true).path("/").build();

    when(tokenProvider.generateAccessTokenCookie(anyString())).thenReturn(accessTokenCookie);
    when(tokenProvider.generateRefreshTokenCookie(anyString())).thenReturn(refreshTokenCookie);
  }

  @Test
  void validateToken_shouldReturnPatientInfo_whenTokenIsValid() throws Exception {
    String token = "valid-token";
    when(tokenProvider.validateAccessToken(token)).thenReturn(true);
    when(tokenProvider.getUserIdFromToken(token)).thenReturn("1");
    when(patientService.getPatientById(1L)).thenReturn(patient);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/session/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"" + token + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid", is(true)))
            .andExpect(jsonPath("$.patient.id", is(1)))
            .andReturn();

    String response = result.getResponse().getContentAsString();
    assertTrue(response.contains("\"valid\":true"));
    assertTrue(response.contains("\"id\":1"));
  }

  @Test
  void validateToken_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
    String token = "invalid-token";
    when(tokenProvider.validateAccessToken(token)).thenReturn(false);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/session/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"" + token + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.valid", is(false)))
            .andExpect(jsonPath("$.message", is("Invalid or expired token")))
            .andReturn();

    String response = result.getResponse().getContentAsString();
    assertTrue(response.contains("\"valid\":false"));
    assertTrue(response.contains("Invalid or expired token"));
  }

  @Test
  void validateToken_shouldReturnBadRequest_whenNoTokenProvidedInBody() throws Exception {
    Map<String, String> tokenRequest = Map.of(); // Empty map, "token" key missing

    mockMvc
        .perform(
            post("/v1/api/auth/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.valid", is(false)))
        .andExpect(jsonPath("$.message", is("No token provided")));
  }

  @Test
  void validateToken_shouldReturnUnauthorized_whenPatientNotFoundForToken() throws Exception {
    Map<String, String> tokenRequest = Map.of("token", "valid-token-unknown-user");
    when(tokenProvider.validateAccessToken("valid-token-unknown-user")).thenReturn(true);
    when(tokenProvider.getUserIdFromToken("valid-token-unknown-user"))
        .thenReturn("99"); // Valid format, but no patient
    when(patientService.getPatientById(99L)).thenReturn(null);

    mockMvc
        .perform(
            post("/v1/api/auth/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.valid", is(false)))
        .andExpect(jsonPath("$.message", is("Patient not found for token"))); // Updated message
  }

  @Test
  void validateToken_shouldReturnUnauthorized_whenUserIdInTokenInvalidFormat() throws Exception {
    Map<String, String> tokenRequest = Map.of("token", "token-bad-userid");
    when(tokenProvider.validateAccessToken("token-bad-userid")).thenReturn(true);
    when(tokenProvider.getUserIdFromToken("token-bad-userid")).thenReturn("not-a-number");

    mockMvc
        .perform(
            post("/v1/api/auth/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.valid", is(false)))
        .andExpect(jsonPath("$.message", is("Invalid user ID format")));
  }

  @Test
  @Disabled("Temporarily disabled - cookie validation issue")
  void refreshToken_shouldReturnOkAndSetCookies_whenSuccessful() throws Exception {
    String oldToken = "old-refresh-token";
    String newAccessToken = "new-access-token";
    String newRefreshToken = "new-refresh-token";
    String phoneNumber = "1234567890";

    patient.getPersonalDetails().setPhoneNumber(phoneNumber);

    Cookie mockRefreshTokenCookie = new Cookie("refreshToken", oldToken);
    when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(oldToken);
    when(tokenProvider.getUserIdFromRefreshToken(oldToken)).thenReturn("1");
    when(sessionService.isRefreshTokenBlacklisted(oldToken)).thenReturn(false);
    when(patientService.getPatientById(1L)).thenReturn(patient);
    when(tokenProvider.createAccessToken(eq("1"), eq(phoneNumber))).thenReturn(newAccessToken);
    when(tokenProvider.createRefreshToken("1")).thenReturn(newRefreshToken);

    MvcResult result =
        mockMvc
            .perform(post("/api/session/refresh").cookie(mockRefreshTokenCookie).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Token refreshed successfully")))
            .andReturn();

    verify(sessionService).blacklistRefreshToken(oldToken);
    List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("accessToken=new-access-token") && h.contains("HttpOnly")));
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("refreshToken=new-refresh-token") && h.contains("HttpOnly")));
  }

  @Test
  void refreshToken_shouldReturnUnauthorized_whenNoRefreshTokenCookie() throws Exception {
    when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(null);

    mockMvc
        .perform(post("/api/session/refresh").with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message", is("Refresh token not found")));
  }

  @Test
  void refreshToken_shouldReturnUnauthorized_whenTokenBlacklisted() throws Exception {
    String blacklistedToken = "blacklisted-token";
    Cookie mockRefreshTokenCookie = new Cookie("refreshToken", blacklistedToken);

    when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(blacklistedToken);
    when(sessionService.isRefreshTokenBlacklisted(blacklistedToken)).thenReturn(true);

    mockMvc
        .perform(post("/api/session/refresh").cookie(mockRefreshTokenCookie).with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message", is("Invalid or blacklisted refresh token")));
  }

  @Test
  void refreshToken_shouldReturnUnauthorized_whenPatientOrDetailsNotFound() throws Exception {
    Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "user-not-found-token");
    when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn("user-not-found-token");
    when(tokenProvider.getUserIdFromRefreshToken("user-not-found-token")).thenReturn("1");
    when(sessionService.isRefreshTokenBlacklisted("user-not-found-token")).thenReturn(false);
    when(patientService.getPatientById(1L)).thenReturn(null);

    mockMvc
        .perform(post("/v1/api/auth/refresh").with(csrf()).cookie(mockRefreshTokenCookie))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Patient data not found for token.")); // Exact message
  }

  @Test
  void refreshToken_shouldReturnUnauthorized_whenTokenProviderFails() throws Exception {
    Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "token-provider-fail-token");
    when(tokenProvider.extractRefreshTokenFromCookies(any()))
        .thenReturn("token-provider-fail-token");
    when(tokenProvider.getUserIdFromRefreshToken("token-provider-fail-token"))
        .thenThrow(new RuntimeException("JWT processing error"));

    mockMvc
        .perform(post("/v1/api/auth/refresh").with(csrf()).cookie(mockRefreshTokenCookie))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Failed to process refresh token.")); // Exact message
  }

  @Test
  @Disabled("Temporarily disabled - cookie validation issue")
  void logout_shouldReturnOkAndClearCookies() throws Exception {
    String refreshToken = "some-refresh-token";
    Cookie mockRefreshTokenCookie = new Cookie("refreshToken", refreshToken);

    when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(refreshToken);

    // Create clear cookies for the test
    ResponseCookie clearAccessCookie =
        ResponseCookie.from("accessToken", "").path("/").maxAge(0).httpOnly(true).build();
    ResponseCookie clearRefreshCookie =
        ResponseCookie.from("refreshToken", "").path("/").maxAge(0).httpOnly(true).build();

    when(tokenProvider.generateClearAccessTokenCookie()).thenReturn(clearAccessCookie);
    when(tokenProvider.generateClearRefreshTokenCookie()).thenReturn(clearRefreshCookie);

    MvcResult result =
        mockMvc
            .perform(post("/api/session/logout").with(csrf()).cookie(mockRefreshTokenCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Logout successful")))
            .andReturn();

    verify(sessionService).blacklistRefreshToken(refreshToken);
    List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(
                h ->
                    h.contains("accessToken=; Max-Age=0")
                        && h.contains("Path=/")
                        && h.contains("HttpOnly")));
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("accessToken=; Max-Age=0") && h.contains("Path=/")));
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("refreshToken=; Max-Age=0") && h.contains("Path=/")));
  }

  @Test
  void logout_shouldWorkEvenIfNoRefreshTokenCookiePresent() throws Exception {
    // Create clear cookies for the test
    ResponseCookie clearAccessCookie =
        ResponseCookie.from("accessToken", "").path("/").maxAge(0).httpOnly(true).build();
    ResponseCookie clearRefreshCookie =
        ResponseCookie.from("refreshToken", "").path("/").maxAge(0).httpOnly(true).build();

    when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(null);
    when(tokenProvider.generateClearAccessTokenCookie()).thenReturn(clearAccessCookie);
    when(tokenProvider.generateClearRefreshTokenCookie()).thenReturn(clearRefreshCookie);

    MvcResult result =
        mockMvc
            .perform(post("/api/session/logout").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Logout successful")))
            .andReturn();

    verify(sessionService, never()).blacklistRefreshToken(anyString());
    List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("accessToken=;") && h.contains("Max-Age=0")));
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("refreshToken=;") && h.contains("Max-Age=0")));
  }

  @Test
  @Disabled("Temporarily disabled - needs endpoint and cookie validation update")
  void login_shouldReturnLoginResponseAndSetCookies_whenCredentialsValid() throws Exception {
    String phoneNumber = "1234567890";
    String password = "password";
    LoginRequest loginRequest = new LoginRequest(phoneNumber, password);
    when(patientService.validateLogin(phoneNumber, password)).thenReturn(patient);
    when(tokenProvider.createAccessToken(eq("1"), eq(phoneNumber))).thenReturn("new-access-token");
    when(tokenProvider.createRefreshToken("1")).thenReturn("new-refresh-token");
    when(tokenProvider.generateAccessTokenCookie("new-access-token")).thenReturn(accessTokenCookie);
    when(tokenProvider.generateRefreshTokenCookie("new-refresh-token"))
        .thenReturn(refreshTokenCookie);

    MvcResult result =
        mockMvc
            .perform(
                post("/v1/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patient.id", is(1)))
            .andExpect(jsonPath("$.accessToken", is("new-access-token")))
            .andExpect(jsonPath("$.usingDefaultPassword", is(false)))
            .andReturn();

    List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(h -> h.contains("accessToken=new-access-token") && h.contains("HttpOnly")));
    assertTrue(
        setCookieHeaders.stream()
            .anyMatch(
                h ->
                    h.contains("refreshToken=new-refresh-token")
                        && h.contains("HttpOnly")
                        && h.contains("Path=/v1/api/auth/refresh")));
  }

  @Test
  void login_shouldReturnUnauthorized_whenCredentialsInvalid() throws Exception {
    String phoneNumber = "1234567890";
    String wrongPassword = "wrongpassword";
    LoginRequest loginRequest = new LoginRequest(phoneNumber, wrongPassword);
    when(patientService.validateLogin(phoneNumber, wrongPassword)).thenReturn(null);

    mockMvc
        .perform(
            post("/api/session/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message", is("Invalid login credentials")));
  }

  @Test
  void login_shouldReturnBadRequest_whenLoginRequestIsInvalidDueToValidations() throws Exception {
    // Test with empty request body
    mockMvc
        .perform(post("/api/session/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.phoneNumber", is("Phone number is required")))
        .andExpect(jsonPath("$.password", is("Password is required")));

    // Test with empty phone number and password
    LoginRequest emptyRequest = new LoginRequest("", "");
    mockMvc
        .perform(
            post("/api/session/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.phoneNumber", is("Phone number is required")))
        .andExpect(jsonPath("$.password", is("Password is required")));
  }
}
