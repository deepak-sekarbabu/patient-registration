package com.deepak.registration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter.
 * This filter intercepts incoming HTTP requests, extracts the JWT access token,
 * validates it, and sets up Spring Security's authentication context if the token is valid.
 * It extends OncePerRequestFilter to ensure it's executed once per request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // Attempt to extract the JWT access token from the request (from header or cookies)
      String accessToken = tokenProvider.extractAccessToken(request);

      // Validate the token if it exists
      if (accessToken != null && tokenProvider.validateAccessToken(accessToken)) {
        // Extract user ID from the token
        String userId = tokenProvider.getUserIdFromToken(accessToken);

        // Load user details from the UserDetailsService
        // Note: Spring Security's UserDetailsService is typically used here,
        // which might load user details from a database or other sources.
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        // Create an authentication token (UsernamePasswordAuthenticationToken)
        // This token represents the authenticated user.
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Set additional details for the authentication, such as IP address and browser agent
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Set the authentication object in Spring Security's context
        // This makes the user principal available throughout the application
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      // Log any exception that occurs during authentication processing
      // It's important not to let exceptions propagate from the filter,
      // as this could expose security details or break the filter chain.
      logger.error("Could not set user authentication in security context", e);
    }

    // Continue the filter chain, allowing the request to proceed to the next filter or servlet
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    // This method determines whether the JWT authentication filter should be skipped
    // for certain request paths. It's crucial for public endpoints that don't require authentication.

    // Authentication-related public endpoints:
    // - /v1/api/auth/validate: Endpoint for validating tokens (might be public or require specific handling)
    // - /v1/api/auth/refresh: Endpoint for refreshing JWT access tokens using a refresh token
    // - /v1/api/auth/logout: Endpoint for user logout
    // - /v1/api/patients/login: Patient login endpoint
    // - /v1/api/patients/register: Patient registration endpoint (same as POST to /v1/api/patients)
    // - /v1/api/patients/exists-by-phone: Publicly check if a phone number is already registered

    // Public patient actions (specific operations allowed without full JWT auth):
    // - POST /v1/api/patients: Patient registration (creating a new patient)
    // - POST /v1/api/patients/{id}/password: Setting or updating password for a patient (e.g., after registration or for password reset)
    // - PUT /v1/api/patients/{id}: Updating patient details (might be restricted by ownership in service layer)

    // API Documentation and Swagger UI endpoints:
    // These paths are for accessing API documentation and should be publicly accessible.
    return path.equals("/v1/api/auth/validate")
        || path.equals("/v1/api/auth/refresh")
        || path.equals("/v1/api/auth/logout")
        || path.equals("/v1/api/patients/login")
        || path.equals("/v1/api/patients/register") // Redundant if POST /v1/api/patients is covered
        || path.equals("/v1/api/patients/exists-by-phone")
        || (path.equals("/v1/api/patients") && request.getMethod().equals("POST"))
        || (path.matches("/v1/api/patients/\\d+/password") && request.getMethod().equals("POST"))
        || (path.matches("/v1/api/patients/\\d+") && request.getMethod().equals("PUT")) // Consider if PUT should always be public or if some auth is needed
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-resources")
        || path.startsWith("/webjars");
  }
}
