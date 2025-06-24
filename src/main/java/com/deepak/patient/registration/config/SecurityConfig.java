package com.deepak.patient.registration.config;

import com.deepak.patient.registration.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Value("${app.cors.allowed-origins}")
  private String[] allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Unauthorized: Authentication token was either missing or invalid."))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            response.sendError(
                                HttpServletResponse.SC_FORBIDDEN,
                                "Access denied: You don't have permission to access this resource.")))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // Public endpoints
                    .requestMatchers(
                        "/v1/api/auth/**",
                        "/v1/api/patients/register",
                        "/v1/api/patients/exists-by-phone",
                        "/v1/api/patients/forgot-password")
                    .permitAll()
                    // Allow preflight requests for all endpoints
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // Allow appointment creation without authentication for testing
                    .requestMatchers(HttpMethod.POST, "/api/v1/appointments")
                    .permitAll()
                    // Swagger UI
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    // Actuator
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    // Patient management
                    .requestMatchers(HttpMethod.POST, "/v1/api/patients")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/v1/api/patients/*/password")
                    .permitAll()
                    // Protected endpoints
                    .requestMatchers("/v1/api/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated());

    // Add JWT filter
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-XSRF-TOKEN",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
    return new ShallowEtagHeaderFilter();
  }
}
