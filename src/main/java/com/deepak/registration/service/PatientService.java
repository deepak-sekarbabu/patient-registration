package com.deepak.registration.service;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.repository.PatientRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing patient-related operations.
 * This includes creating, retrieving, updating, deleting patients,
 * as well as validating login credentials and updating passwords.
 * It interacts with the {@link PatientRepository} for data persistence.
 */
@Service
public class PatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

  private final PatientRepository patientRepository;

  public PatientService(PatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  /**
   * Creates a new patient record.
   * If a phone number is provided, it is used as the default password,
   * which is then hashed. The {@code usingDefaultPassword} flag is set to true.
   *
   * @param patient The patient object to be created.
   * @return The saved patient object with its generated ID.
   */
  public Patient createPatient(Patient patient) {
    logger.debug("Creating patient: {}", patient);
    // Set default password as phone number if provided, then hash it.
    // This is a temporary measure for initial account setup.
    // The user is expected to change this default password.
    if (patient.getPhoneNumber() != null) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      patient.setPasswordHash(encoder.encode(patient.getPhoneNumber()));
      patient.setUsingDefaultPassword(true); // Mark that the patient is using the default password
    }
    return patientRepository.save(patient);
  }

  public Patient getPatientByPhoneNumber(String phoneNumber) {
    logger.debug("Fetching patient by phone number: {}", phoneNumber);
    return patientRepository.findByPhoneNumber(phoneNumber).orElse(null);
  }

  public boolean existsByPhoneNumber(String phoneNumber) {
    return patientRepository.findByPhoneNumber(phoneNumber).isPresent();
  }

  public Patient getPatientById(Long id) {
    logger.debug("Fetching patient by id: {}", id);
    return patientRepository.findById(id).orElse(null);
  }

  /**
   * Updates an existing patient's details.
   * Retrieves the patient by ID and updates fields if new values are provided in {@code updatedPatient}.
   *
   * @param id The ID of the patient to update.
   * @param updatedPatient A patient object containing the fields to update.
   * @return The updated patient object, or {@code null} if the patient was not found.
   */
  public Patient updatePatient(Long id, Patient updatedPatient) {
    logger.debug("Updating patient with id: {}", id);
    return patientRepository
        .findById(id)
        .map(
            existingPatient -> {
              // Update personal details if provided
              if (updatedPatient.getPersonalDetails() != null) {
                existingPatient.setPersonalDetails(updatedPatient.getPersonalDetails());
              }
              // Update medical information if provided
              if (updatedPatient.getMedicalInfo() != null) {
                existingPatient.setMedicalInfo(updatedPatient.getMedicalInfo());
              }
              // Update emergency contact if provided
              if (updatedPatient.getEmergencyContact() != null) {
                existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());
              }
              // Update insurance details if provided
              if (updatedPatient.getInsuranceDetails() != null) {
                existingPatient.setInsuranceDetails(updatedPatient.getInsuranceDetails());
              }
              // Update clinic preferences if provided
              if (updatedPatient.getClinicPreferences() != null) {
                existingPatient.setClinicPreferences(updatedPatient.getClinicPreferences());
              }
              // Note: Other fields like phone number and password are not updated here.
              // Specific methods should be used for those operations (e.g., updatePassword).
              existingPatient.setUpdatedAt(LocalDateTime.now()); // Update the timestamp
              return patientRepository.save(existingPatient);
            })
        .orElse(null); // Return null if patient with the given ID is not found
  }

  public void deletePatient(Long id) {
    logger.debug("Deleting patient with id: {}", id);
    if (patientRepository.existsById(id)) {
      patientRepository.deleteById(id);
    } else {
      logger.warn("Patient not found for deletion with id: {}", id);
      throw new RuntimeException("Patient not found"); // Or a more specific exception
    }
  }

  /**
   * Validates patient login credentials.
   * Fetches the patient by phone number and compares the provided password
   * with the stored hashed password using BCrypt.
   *
   * @param phoneNumber The patient's phone number.
   * @param password The plain text password to validate.
   * @return The {@link Patient} object if login is successful, {@code null} otherwise.
   */
  public Patient validateLogin(String phoneNumber, String password) {
    // Retrieve the patient by phone number
    Patient patient = getPatientByPhoneNumber(phoneNumber);
    if (patient == null) {
      logger.warn("Login failed: patient not found for phone number {}", phoneNumber);
      return null; // Patient not found
    }

    // Verify the provided password against the stored hash
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    if (encoder.matches(password, patient.getPasswordHash())) {
      // Password matches
      return patient;
    } else {
      // Password does not match
      logger.warn("Login failed: invalid password for phone number {}", phoneNumber);
      return null;
    }
  }

  /**
   * Updates the password for a given patient.
   * The new password is required and will be hashed using BCrypt.
   * The {@code usingDefaultPassword} flag is set to false after a successful password update.
   *
   * @param patientId The ID of the patient whose password is to be updated.
   * @param newPassword The new plain text password.
   * @throws RuntimeException if the patient is not found.
   * @throws IllegalArgumentException if the new password is null or empty.
   */
  public void updatePassword(Long patientId, String newPassword) {
    // Retrieve the patient by ID, or throw an exception if not found
    Patient patient =
        patientRepository
            .findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

    // Validate that the new password is not null or empty
    if (newPassword == null || newPassword.isEmpty()) {
      throw new IllegalArgumentException("New password cannot be null or empty");
    }

    // Hash the new password using BCrypt
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    patient.setPasswordHash(encoder.encode(newPassword));

    // Update the 'updatedAt' timestamp
    patient.setUpdatedAt(LocalDateTime.now());

    // Mark that the patient is no longer using the default password
    patient.setUsingDefaultPassword(false);

    patientRepository.save(patient);
    logger.info("Password updated successfully for patient id: {}", patientId);
  }
}
