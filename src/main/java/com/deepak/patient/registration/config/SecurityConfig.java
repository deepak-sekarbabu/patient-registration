package com.deepak.patient.registration.config;

import com.deepak.patient.registration.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/v1/api/patients")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/v1/api/patients/*/password")
                    .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/v1/api/patients/**")
                    .permitAll()
                    .requestMatchers("/v1/api/auth/login")
                    .permitAll()
                    .requestMatchers("/v1/api/patients/")
                    .permitAll()
                    .requestMatchers("/v1/api/patients/register")
                    .permitAll()
                    .requestMatchers("/v1/api/patients/exists-by-phone")
                    .permitAll()
                    .requestMatchers("/v1/api/auth/validate")
                    .permitAll()
                    .requestMatchers("/v1/api/auth/refresh")
                    .permitAll()
                    .requestMatchers("/v1/api/auth/logout")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/v1/api/patients/by-id")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/v1/api/patients/{id}")
                    .authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/v1/api/patients/{id}")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/v1/api/patients/{id}/password")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/v1/api/clinics/*/doctors/*/available-dates")
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
    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // Add
    // your
    // frontend
    // URL
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
