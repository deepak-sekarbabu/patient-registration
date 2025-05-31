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
      // Extract token from either header or cookies
      String accessToken = tokenProvider.extractAccessToken(request);

      if (accessToken != null && tokenProvider.validateAccessToken(accessToken)) {
        String userId = tokenProvider.getUserIdFromToken(accessToken);

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        // Create authentication token
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      logger.error("Could not set user authentication in security context", e);
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    // List all public endpoints that should skip JWT authentication
    return path.equals("/v1/api/auth/validate")
        || path.equals("/v1/api/auth/refresh")
        || path.equals("/v1/api/auth/logout")
        || path.equals("/v1/api/patients/login")
        || path.equals("/v1/api/patients/register")
        || path.equals("/v1/api/patients/exists-by-phone")
        || (path.equals("/v1/api/patients") && request.getMethod().equals("POST"))
        || (path.matches("/v1/api/patients/\\d+/password") && request.getMethod().equals("POST"))
        || (path.matches("/v1/api/patients/\\d+") && request.getMethod().equals("PUT"))
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-resources")
        || path.startsWith("/webjars");
  }
}
