package com.deepak.patient.registration.security;

import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.repository.PatientRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final PatientRepository patientRepository;

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    // Since we're using phone number as username in our system
    // But here userId could be the actual userId (from JWT) or phone number
    Patient patient;

    try {
      // Try to parse as long (patient ID)
      Long id = Long.parseLong(userId);
      patient =
          patientRepository
              .findById(id)
              .orElseThrow(
                  () -> new UsernameNotFoundException("User not found with id: " + userId));
    } catch (NumberFormatException e) {
      // If not a number, treat as phone number
      patient =
          patientRepository
              .findByPhoneNumber(userId)
              .orElseThrow(
                  () -> new UsernameNotFoundException("User not found with phone: " + userId));
    }

    // Create a UserDetails object with the patient's ID as the username
    // The password hash is not used for token authentication, but is required by UserDetails
    return User.builder()
        .username(patient.getId().toString())
        .password(patient.getPasswordHash())
        .authorities(Collections.emptyList()) // No
        // special
        // roles
        // for now
        .build();
  }
}
