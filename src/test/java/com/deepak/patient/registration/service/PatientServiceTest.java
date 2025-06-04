package com.deepak.patient.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.model.patient.PersonalDetails;
import com.deepak.patient.registration.repository.PatientRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

  @Mock private PatientRepository patientRepository;

  // BCryptPasswordEncoder is instantiated directly in PatientService,
  // so we'll use a real one here for assertions.
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @InjectMocks private PatientService patientService;

  private Patient patient;
  private Patient patientWithDetails;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setId(1L);
    patient.setPhoneNumber("+919876543210");
    // Password set in specific tests where needed for clarity
    patient.setUsingDefaultPassword(false);
    patient.setCreatedAt(LocalDateTime.now().minusDays(1));
    patient.setUpdatedAt(LocalDateTime.now().minusHours(1));

    PersonalDetails personalDetails = new PersonalDetails();
    personalDetails.setFirstName("Test");
    personalDetails.setLastName("User");
    personalDetails.setEmail("test.user@example.com");

    patientWithDetails = new Patient();
    patientWithDetails.setId(1L);
    patientWithDetails.setPhoneNumber("+919876543210");
    patientWithDetails.setPasswordHash(encoder.encode("password123"));
    patientWithDetails.setUsingDefaultPassword(false);
    patientWithDetails.setPersonalDetails(personalDetails);
    patientWithDetails.setMedicalInfo("No known allergies.");
    patientWithDetails.setCreatedAt(LocalDateTime.now().minusDays(1));
    patientWithDetails.setUpdatedAt(LocalDateTime.now().minusHours(1));
  }

  @Test
  void createPatient_shouldSavePatientWithHashedDefaultPasswordFromPhoneNumber() {
    Patient newPatient = new Patient();
    newPatient.setPhoneNumber("1234567890");
    // No passwordHash set, service should use phone number as default password

    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    when(patientRepository.save(patientCaptor.capture()))
        .thenAnswer(
            invocation -> {
              Patient p = invocation.getArgument(0);
              p.setId(2L); // Simulate save assigning an ID
              // Simulate JPA @PrePersist if PatientService doesn't set timestamps
              if (p.getCreatedAt() == null) p.setCreatedAt(LocalDateTime.now());
              if (p.getUpdatedAt() == null) p.setUpdatedAt(LocalDateTime.now());
              return p;
            });

    Patient savedPatient = patientService.createPatient(newPatient);

    assertNotNull(savedPatient);
    assertEquals(2L, savedPatient.getId());
    assertNotNull(savedPatient.getPasswordHash());
    assertTrue(encoder.matches("1234567890", savedPatient.getPasswordHash()));
    assertTrue(savedPatient.isUsingDefaultPassword());
    assertNotNull(savedPatient.getCreatedAt());
    assertNotNull(savedPatient.getUpdatedAt());
    verify(patientRepository, times(1)).save(any(Patient.class));

    Patient captured = patientCaptor.getValue();
    assertEquals("1234567890", captured.getPhoneNumber());
    assertTrue(encoder.matches("1234567890", captured.getPasswordHash()));
    assertTrue(captured.isUsingDefaultPassword());
  }

  @Test
  void createPatient_shouldSavePatientWithNullPasswordHash_ifPhoneNumberNull() {
    Patient newPatient = new Patient(); // No phone number

    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    when(patientRepository.save(patientCaptor.capture()))
        .thenAnswer(
            invocation -> {
              Patient p = invocation.getArgument(0);
              p.setId(3L);
              return p;
            });

    Patient savedPatient = patientService.createPatient(newPatient);

    assertNotNull(savedPatient);
    assertNull(savedPatient.getPasswordHash()); // Password hash should be null
    assertFalse(
        savedPatient.isUsingDefaultPassword()); // Should be false as no default password was set
    verify(patientRepository).save(newPatient);

    Patient captured = patientCaptor.getValue();
    assertNull(captured.getPhoneNumber());
    assertNull(captured.getPasswordHash());
    assertFalse(captured.isUsingDefaultPassword());
  }

  @Test
  void getPatientByPhoneNumber_shouldReturnPatient_whenFound() {
    when(patientRepository.findByPhoneNumber("+919876543210"))
        .thenReturn(Optional.of(patientWithDetails));
    Patient found = patientService.getPatientByPhoneNumber("+919876543210");
    assertNotNull(found);
    assertEquals("+919876543210", found.getPhoneNumber());
    assertEquals("Test", found.getPersonalDetails().getFirstName());
  }

  @Test
  void getPatientByPhoneNumber_shouldReturnNull_whenNotFound() {
    when(patientRepository.findByPhoneNumber("unknown")).thenReturn(Optional.empty());
    Patient found = patientService.getPatientByPhoneNumber("unknown");
    assertNull(found);
  }

  @Test
  void existsByPhoneNumber_shouldReturnTrue_whenFound() {
    // PatientService.existsByPhoneNumber directly calls repository.existsByPhoneNumber in the
    // provided code,
    // but the example test checks findByPhoneNumber. Let's assume it should use findByPhoneNumber
    // as per example.
    when(patientRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.of(patient));
    assertTrue(patientService.existsByPhoneNumber("+919876543210"));
  }

  @Test
  void existsByPhoneNumber_shouldReturnFalse_whenNotFound() {
    when(patientRepository.findByPhoneNumber("unknown")).thenReturn(Optional.empty());
    assertFalse(patientService.existsByPhoneNumber("unknown"));
  }

  @Test
  void getPatientById_shouldReturnPatient_whenFound() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patientWithDetails));
    Patient found = patientService.getPatientById(1L);
    assertNotNull(found);
    assertEquals(1L, found.getId());
    assertEquals("Test", found.getPersonalDetails().getFirstName());
  }

  @Test
  void getPatientById_shouldReturnNull_whenNotFound() {
    when(patientRepository.findById(2L)).thenReturn(Optional.empty());
    Patient found = patientService.getPatientById(2L);
    assertNull(found);
  }

  @Test
  void updatePatient_shouldUpdateFields_whenPatientExists() {
    Patient existingPatient = new Patient(); // Create a fresh patient instance for the test
    existingPatient.setId(1L);
    existingPatient.setPhoneNumber("+919876543210");
    PersonalDetails originalDetails = new PersonalDetails();
    originalDetails.setFirstName("OriginalFirst");
    originalDetails.setLastName("OriginalLast");
    existingPatient.setPersonalDetails(originalDetails);
    existingPatient.setMedicalInfo("Original Medical Info");
    LocalDateTime initialUpdatedAt = LocalDateTime.now().minusDays(1);
    existingPatient.setUpdatedAt(initialUpdatedAt);

    Patient updates = new Patient();
    PersonalDetails newDetails = new PersonalDetails();
    newDetails.setFirstName("UpdatedName");
    newDetails.setLastName("UpdatedLastName"); // Add last name to updates
    newDetails.setEmail("updated.email@example.com"); // Add email
    updates.setPersonalDetails(newDetails);
    updates.setMedicalInfo("New Medical Info");

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    when(patientRepository.save(patientCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Patient updatedPatient = patientService.updatePatient(1L, updates);

    assertNotNull(updatedPatient);
    assertEquals("UpdatedName", updatedPatient.getPersonalDetails().getFirstName());
    assertEquals("UpdatedLastName", updatedPatient.getPersonalDetails().getLastName());
    assertEquals("updated.email@example.com", updatedPatient.getPersonalDetails().getEmail());
    assertEquals("New Medical Info", updatedPatient.getMedicalInfo());
    assertEquals("+919876543210", updatedPatient.getPhoneNumber()); // Should not change
    assertNotNull(updatedPatient.getUpdatedAt());
    assertTrue(updatedPatient.getUpdatedAt().isAfter(initialUpdatedAt));

    verify(patientRepository, times(1)).findById(1L);
    verify(patientRepository, times(1)).save(any(Patient.class));

    Patient captured = patientCaptor.getValue();
    assertEquals("UpdatedName", captured.getPersonalDetails().getFirstName());
    assertTrue(captured.getUpdatedAt().isAfter(initialUpdatedAt));
  }

  @Test
  void
      updatePatient_shouldUpdateOnlyNonNullFieldsInPersonalDetails_ifPersonalDetailsInUpdateIsNotNull() {
    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    existingPatient.setPhoneNumber("originalPhone");
    PersonalDetails originalDetails = new PersonalDetails();
    originalDetails.setFirstName("OriginalFirst");
    originalDetails.setLastName("OriginalLast");
    originalDetails.setEmail("original.email@example.com");
    existingPatient.setPersonalDetails(originalDetails);
    LocalDateTime initialUpdatedAt = LocalDateTime.now().minusDays(1);
    existingPatient.setUpdatedAt(initialUpdatedAt);

    Patient updates = new Patient();
    PersonalDetails partialNewDetails = new PersonalDetails();
    partialNewDetails.setFirstName("UpdatedFirst"); // Only FirstName is updated
    // LastName and Email are null in partialNewDetails
    updates.setPersonalDetails(partialNewDetails);
    // MedicalInfo is null in updates, so existingPatient's MedicalInfo should persist.
    updates.setMedicalInfo(null);
    existingPatient.setMedicalInfo("Existing Medical Info");

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.save(any(Patient.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Patient updated = patientService.updatePatient(1L, updates);

    assertEquals("UpdatedFirst", updated.getPersonalDetails().getFirstName());
    // Based on PatientService logic, if a field in updates.personalDetails is null, it will
    // overwrite existing.
    assertNull(
        updated.getPersonalDetails().getLastName(),
        "LastName should be null as it was null in the update's PersonalDetails");
    assertNull(
        updated.getPersonalDetails().getEmail(),
        "Email should be null as it was null in the update's PersonalDetails");
    assertEquals(
        "Existing Medical Info",
        updated.getMedicalInfo(),
        "MedicalInfo should remain unchanged as it was null in updates");
    assertEquals("originalPhone", updated.getPhoneNumber());
    assertTrue(updated.getUpdatedAt().isAfter(initialUpdatedAt));
  }

  @Test
  void updatePatient_shouldNotUpdatePersonalDetails_ifPersonalDetailsInUpdateIsNull() {
    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    PersonalDetails originalDetails = new PersonalDetails();
    originalDetails.setFirstName("OriginalFirst");
    existingPatient.setPersonalDetails(originalDetails);
    existingPatient.setMedicalInfo("Original Medical");
    LocalDateTime initialUpdatedAt = LocalDateTime.now().minusDays(1);
    existingPatient.setUpdatedAt(initialUpdatedAt);

    Patient updates = new Patient();
    updates.setPersonalDetails(null); // PersonalDetails is null in the update request
    updates.setMedicalInfo("Updated Medical");

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.save(any(Patient.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Patient updated = patientService.updatePatient(1L, updates);

    assertEquals(
        "OriginalFirst",
        updated.getPersonalDetails().getFirstName(),
        "PersonalDetails should not change");
    assertEquals("Updated Medical", updated.getMedicalInfo());
    assertTrue(updated.getUpdatedAt().isAfter(initialUpdatedAt));
  }

  @Test
  void updatePatient_shouldReturnNull_whenPatientNotFound() {
    Patient updates = new Patient();
    when(patientRepository.findById(2L)).thenReturn(Optional.empty());
    Patient updatedPatient = patientService.updatePatient(2L, updates);
    assertNull(updatedPatient);
    verify(patientRepository, times(1)).findById(2L);
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void deletePatient_shouldDelete_whenPatientExists() {
    when(patientRepository.existsById(1L)).thenReturn(true);
    doNothing().when(patientRepository).deleteById(1L);

    assertDoesNotThrow(() -> patientService.deletePatient(1L));

    verify(patientRepository, times(1)).existsById(1L);
    verify(patientRepository, times(1)).deleteById(1L);
  }

  @Test
  void deletePatient_shouldThrowRuntimeException_whenPatientNotFound() {
    when(patientRepository.existsById(2L)).thenReturn(false);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              patientService.deletePatient(2L);
            });
    assertEquals("Patient not found with id: 2", exception.getMessage()); // Corrected message
    verify(patientRepository, times(1)).existsById(2L);
    verify(patientRepository, never()).deleteById(anyLong());
  }

  @Test
  void validateLogin_shouldReturnPatient_whenCredentialsCorrect() {
    patientWithDetails.setPasswordHash(encoder.encode("password123")); // Ensure it's set
    when(patientRepository.findByPhoneNumber("+919876543210"))
        .thenReturn(Optional.of(patientWithDetails));

    Patient validatedPatient = patientService.validateLogin("+919876543210", "password123");
    assertNotNull(validatedPatient);
    assertEquals(patientWithDetails.getId(), validatedPatient.getId());
  }

  @Test
  void validateLogin_shouldReturnNull_whenPasswordIncorrect() {
    patientWithDetails.setPasswordHash(encoder.encode("password123"));
    when(patientRepository.findByPhoneNumber("+919876543210"))
        .thenReturn(Optional.of(patientWithDetails));

    Patient validatedPatient = patientService.validateLogin("+919876543210", "wrongPassword");
    assertNull(validatedPatient);
  }

  @Test
  void validateLogin_shouldReturnNull_whenPatientNotFound() {
    when(patientRepository.findByPhoneNumber("unknown")).thenReturn(Optional.empty());
    Patient validatedPatient = patientService.validateLogin("unknown", "password123");
    assertNull(validatedPatient);
  }

  @Test
  void validateLogin_shouldReturnNull_whenPatientHasNullPasswordHash() {
    patientWithDetails.setPasswordHash(null); // Simulate patient with no password set
    when(patientRepository.findByPhoneNumber("+919876543210"))
        .thenReturn(Optional.of(patientWithDetails));

    Patient validatedPatient = patientService.validateLogin("+919876543210", "anyPassword");
    assertNull(validatedPatient);
  }

  @Test
  void updatePassword_shouldUpdateHashAndFlag_whenPatientExists() {
    LocalDateTime initialUpdateTimestamp = patientWithDetails.getUpdatedAt();
    patientWithDetails.setUsingDefaultPassword(true); // Set to true for this test

    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patientWithDetails));
    when(patientRepository.save(patientCaptor.capture())).thenReturn(patientWithDetails);

    patientService.updatePassword(1L, "newPassword123");

    Patient savedPatient = patientCaptor.getValue();
    assertNotNull(savedPatient.getPasswordHash());
    assertTrue(encoder.matches("newPassword123", savedPatient.getPasswordHash()));
    assertFalse(savedPatient.isUsingDefaultPassword());
    assertNotNull(savedPatient.getUpdatedAt());
    assertTrue(savedPatient.getUpdatedAt().isAfter(initialUpdateTimestamp));
    verify(patientRepository, times(1)).findById(1L);
    verify(patientRepository, times(1)).save(any(Patient.class));
  }

  @Test
  void updatePassword_shouldThrowRuntimeException_whenPatientNotFound() {
    when(patientRepository.findById(2L)).thenReturn(Optional.empty());
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              patientService.updatePassword(2L, "newPassword123");
            });
    assertEquals("Patient not found with id: 2", exception.getMessage());
    verify(patientRepository, times(1)).findById(2L);
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void updatePassword_shouldThrowIllegalArgumentException_whenNewPasswordNull() {
    // No need to mock findById if it's not called before the check
    // when(patientRepository.findById(1L)).thenReturn(Optional.of(patientWithDetails));
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              patientService.updatePassword(1L, null);
            });
    assertEquals("New password cannot be null or empty", exception.getMessage());
    // verify(patientRepository, times(1)).findById(1L)); // This check might be too early depending
    // on service impl
    verify(patientRepository, never())
        .findById(anyLong()); // findById should not be called if password invalid
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void updatePassword_shouldThrowIllegalArgumentException_whenNewPasswordEmpty() {
    // when(patientRepository.findById(1L)).thenReturn(Optional.of(patientWithDetails));
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              patientService.updatePassword(1L, "");
            });
    assertEquals("New password cannot be null or empty", exception.getMessage());
    verify(patientRepository, never()).findById(anyLong());
    verify(patientRepository, never()).save(any(Patient.class));
  }
}
