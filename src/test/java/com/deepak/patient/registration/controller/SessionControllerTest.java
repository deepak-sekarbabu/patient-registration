package com.deepak.patient.registration.controller;

import com.deepak.patient.registration.model.patient.LoginRequest;
import com.deepak.patient.registration.model.patient.LoginResponse;
import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.model.patient.PersonalDetails;
import com.deepak.patient.registration.security.TokenProvider;
import com.deepak.patient.registration.service.PatientService;
import com.deepak.patient.registration.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient patient;
    private ResponseCookie accessTokenCookieHttpOnly;
    private ResponseCookie refreshTokenCookieHttpOnly;
    private ResponseCookie clearAccessCookieHttpOnly;
    private ResponseCookie clearRefreshCookieHttpOnly;


    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setPhoneNumber("1234567890");
        PersonalDetails pd = new PersonalDetails();
        pd.setFirstName("Test");
        pd.setLastName("User");
        patient.setPersonalDetails(pd);
        patient.setUsingDefaultPassword(false); // Important for some logic paths

        // HttpOnly cookies
        accessTokenCookieHttpOnly = ResponseCookie.from("accessToken", "new-access-token").path("/").httpOnly(true).secure(false).build(); // secure false for http test
        refreshTokenCookieHttpOnly = ResponseCookie.from("refreshToken", "new-refresh-token").path("/v1/api/auth/refresh").httpOnly(true).secure(false).build();
        clearAccessCookieHttpOnly = ResponseCookie.from("accessToken", "").path("/").maxAge(0).httpOnly(true).secure(false).build();
        clearRefreshCookieHttpOnly = ResponseCookie.from("refreshToken", "").path("/v1/api/auth/refresh").maxAge(0).httpOnly(true).secure(false).build();
    }

    @Test
    void validateToken_shouldReturnPatientInfo_whenTokenIsValid() throws Exception {
        Map<String, String> tokenRequest = Map.of("token", "valid-token");
        when(tokenProvider.validateAccessToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid-token")).thenReturn("1");
        when(patientService.getPatientById(1L)).thenReturn(patient);

        mockMvc.perform(post("/v1/api/auth/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.patient.id", is(1)));
    }

    @Test
    void validateToken_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        Map<String, String> tokenRequest = Map.of("token", "invalid-token");
        when(tokenProvider.validateAccessToken("invalid-token")).thenReturn(false);

        mockMvc.perform(post("/v1/api/auth/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid or expired token")));
    }

    @Test
    void validateToken_shouldReturnBadRequest_whenNoTokenProvidedInBody() throws Exception {
        Map<String, String> tokenRequest = Map.of(); // Empty map, "token" key missing

        mockMvc.perform(post("/v1/api/auth/validate")
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
        when(tokenProvider.getUserIdFromToken("valid-token-unknown-user")).thenReturn("99"); // Valid format, but no patient
        when(patientService.getPatientById(99L)).thenReturn(null);

        mockMvc.perform(post("/v1/api/auth/validate")
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

        mockMvc.perform(post("/v1/api/auth/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid user ID format in token"))); // Updated message
    }


    @Test
    void refreshToken_shouldReturnOkAndSetCookies_whenSuccessful() throws Exception {
        Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "old-refresh-token");
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn("old-refresh-token");
        when(tokenProvider.getUserIdFromRefreshToken("old-refresh-token")).thenReturn("1");
        when(sessionService.isRefreshTokenBlacklisted("old-refresh-token")).thenReturn(false);
        when(patientService.getPatientById(1L)).thenReturn(patient);
        when(tokenProvider.createAccessToken(eq("1"), eq("1234567890"))).thenReturn("new-access-token");
        when(tokenProvider.createRefreshToken("1")).thenReturn("new-refresh-token");
        when(tokenProvider.generateAccessTokenCookie("new-access-token")).thenReturn(accessTokenCookieHttpOnly);
        when(tokenProvider.generateRefreshTokenCookie("new-refresh-token")).thenReturn(refreshTokenCookieHttpOnly);

        MvcResult result = mockMvc.perform(post("/v1/api/auth/refresh")
                .with(csrf())
                .cookie(mockRefreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("Token refreshed successfully.")) // Exact message
                .andReturn();

        verify(sessionService).blacklistRefreshToken("old-refresh-token");
        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("accessToken=new-access-token") && h.contains("HttpOnly")));
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("refreshToken=new-refresh-token") && h.contains("HttpOnly") && h.contains("Path=/v1/api/auth/refresh")));
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenNoRefreshTokenCookie() throws Exception {
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(null);
        mockMvc.perform(post("/v1/api/auth/refresh").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Refresh token not found in cookies.")); // Exact message
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenTokenBlacklisted() throws Exception {
        Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "blacklisted-token");
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn("blacklisted-token");
        // UserIdFromRefreshToken might not be called if blacklisted check is first, but good to mock defensively
        when(tokenProvider.getUserIdFromRefreshToken("blacklisted-token")).thenReturn("1");
        when(sessionService.isRefreshTokenBlacklisted("blacklisted-token")).thenReturn(true);

        mockMvc.perform(post("/v1/api/auth/refresh").with(csrf()).cookie(mockRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid or blacklisted refresh token.")); // Exact message
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenPatientOrDetailsNotFound() throws Exception {
        Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "user-not-found-token");
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn("user-not-found-token");
        when(tokenProvider.getUserIdFromRefreshToken("user-not-found-token")).thenReturn("1");
        when(sessionService.isRefreshTokenBlacklisted("user-not-found-token")).thenReturn(false);
        when(patientService.getPatientById(1L)).thenReturn(null);

        mockMvc.perform(post("/v1/api/auth/refresh").with(csrf()).cookie(mockRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Patient data not found for token.")); // Exact message
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenTokenProviderFails() throws Exception {
        Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "token-provider-fail-token");
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn("token-provider-fail-token");
        when(tokenProvider.getUserIdFromRefreshToken("token-provider-fail-token")).thenThrow(new RuntimeException("JWT processing error"));

        mockMvc.perform(post("/v1/api/auth/refresh").with(csrf()).cookie(mockRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Failed to process refresh token.")); // Exact message
    }


    @Test
    void logout_shouldReturnOkAndClearCookies() throws Exception {
        Cookie mockRefreshTokenCookie = new Cookie("refreshToken", "some-refresh-token");
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn("some-refresh-token");
        when(tokenProvider.generateClearAccessTokenCookie()).thenReturn(clearAccessCookieHttpOnly);
        when(tokenProvider.generateClearRefreshTokenCookie()).thenReturn(clearRefreshCookieHttpOnly);

        MvcResult result = mockMvc.perform(post("/v1/api/auth/logout").with(csrf()).cookie(mockRefreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully.")) // Exact message
                .andReturn();

        verify(sessionService).blacklistRefreshToken("some-refresh-token");
        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("accessToken=;") && h.contains("Max-Age=0") && h.contains("HttpOnly")));
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("refreshToken=;") && h.contains("Max-Age=0") && h.contains("HttpOnly") && h.contains("Path=/v1/api/auth/refresh")));
    }

    @Test
    void logout_shouldWorkEvenIfNoRefreshTokenCookiePresent() throws Exception {
        when(tokenProvider.extractRefreshTokenFromCookies(any())).thenReturn(null);
        when(tokenProvider.generateClearAccessTokenCookie()).thenReturn(clearAccessCookieHttpOnly);
        when(tokenProvider.generateClearRefreshTokenCookie()).thenReturn(clearRefreshCookieHttpOnly);

        MvcResult result = mockMvc.perform(post("/v1/api/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully."))
                .andReturn();

        verify(sessionService, never()).blacklistRefreshToken(anyString());
        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("accessToken=;") && h.contains("Max-Age=0")));
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("refreshToken=;") && h.contains("Max-Age=0")));
    }


    @Test
    void login_shouldReturnLoginResponseAndSetCookies_whenCredentialsValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("1234567890", "password");
        when(patientService.validateLogin("1234567890", "password")).thenReturn(patient);
        when(tokenProvider.createAccessToken(eq("1"), eq("1234567890"))).thenReturn("new-access-token");
        when(tokenProvider.createRefreshToken("1")).thenReturn("new-refresh-token");
        when(tokenProvider.generateAccessTokenCookie("new-access-token")).thenReturn(accessTokenCookieHttpOnly);
        when(tokenProvider.generateRefreshTokenCookie("new-refresh-token")).thenReturn(refreshTokenCookieHttpOnly);

        MvcResult result = mockMvc.perform(post("/v1/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.id", is(1)))
                .andExpect(jsonPath("$.accessToken", is("new-access-token")))
                .andExpect(jsonPath("$.usingDefaultPassword", is(false)))
                .andReturn();

        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("accessToken=new-access-token") && h.contains("HttpOnly")));
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.contains("refreshToken=new-refresh-token") && h.contains("HttpOnly") && h.contains("Path=/v1/api/auth/refresh")));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("1234567890", "wrongpassword");
        when(patientService.validateLogin("1234567890", "wrongpassword")).thenReturn(null);

        mockMvc.perform(post("/v1/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid login credentials.")); // Exact message
    }

    @Test
    void login_shouldReturnBadRequest_whenLoginRequestIsInvalidDueToValidations() throws Exception {
        // LoginRequest has @NotBlank on phoneNumber and password.
        // Sending an empty JSON object should trigger validation failures.
        mockMvc.perform(post("/v1/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
