package com.deepak.patient.registration.service;

import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.model.patient.PersonalDetails;
import com.deepak.patient.registration.repository.PatientRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing patient-related operations. This includes creating, retrieving,
 * updating, deleting patients, as well as validating login credentials and updating passwords. It
 * interacts with the {@link PatientRepository} for data persistence.
 */
@Service
public class PatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

  private final PatientRepository patientRepository;

  public PatientService(PatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  /**
   * Creates a new patient record. If a phone number is provided, it is used as the default
   * password, which is then hashed. The {@code usingDefaultPassword} flag is set to true.
   *
   * @param patient The patient object to be created.
   * @return The saved patient object with its generated ID.
   */
  public Patient createPatient(Patient patient) {
    logger.debug("Creating patient: {}", patient);
    // Set default password as phone number if provided, then hash it.
    // This is a temporary measure for initial account setup.
    // The user is expected to change this default password.
    if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().trim().isEmpty()) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      patient.setPasswordHash(encoder.encode(patient.getPhoneNumber()));
      patient.setUsingDefaultPassword(true); // Mark that the patient is using the default password
    } else {
      patient.setPasswordHash(null);
      patient.setUsingDefaultPassword(false);
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
   * Updates an existing patient's details. Retrieves the patient by ID and updates fields if new
   * values are provided in {@code updatedPatient}.
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
              // Update only the fields that are not null in updatedPatient
              if (updatedPatient.getPersonalDetails() != null) {
                // If existing personal details is null, set the new one directly
                if (existingPatient.getPersonalDetails() == null) {
                  existingPatient.setPersonalDetails(updatedPatient.getPersonalDetails());
                } else {
                  // Otherwise, update only non-null fields from the updated details
                  PersonalDetails existingDetails = existingPatient.getPersonalDetails();
                  PersonalDetails newDetails = updatedPatient.getPersonalDetails();

                  if (newDetails.getName() != null) {
                    existingDetails.setName(newDetails.getName());
                  }
                  if (newDetails.getEmail() != null) {
                    existingDetails.setEmail(newDetails.getEmail());
                  }
                  if (newDetails.getPhoneNumber() != null) {
                    existingDetails.setPhoneNumber(newDetails.getPhoneNumber());
                  }
                  if (newDetails.getBirthdate() != null) {
                    existingDetails.setBirthdate(newDetails.getBirthdate());
                  }
                  if (newDetails.getSex() != null) {
                    existingDetails.setSex(newDetails.getSex());
                  }
                  if (newDetails.getAddress() != null) {
                    existingDetails.setAddress(newDetails.getAddress());
                  }
                  if (newDetails.getOccupation() != null) {
                    existingDetails.setOccupation(newDetails.getOccupation());
                  }
                }
              }

              // Update other fields if they are not null in the updated patient
              if (updatedPatient.getMedicalInfo() != null) {
                existingPatient.setMedicalInfo(updatedPatient.getMedicalInfo());
              }
              if (updatedPatient.getInsuranceDetails() != null) {
                existingPatient.setInsuranceDetails(updatedPatient.getInsuranceDetails());
              }
              if (updatedPatient.getEmergencyContact() != null) {
                existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());
              }
              if (updatedPatient.getClinicPreferences() != null) {
                existingPatient.setClinicPreferences(updatedPatient.getClinicPreferences());
              }

              // Update the timestamp
              existingPatient.setUpdatedAt(LocalDateTime.now());
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
      throw new RuntimeException("Patient not found with id: " + id);
    }
  }

  /**
   * Validates patient login credentials. Fetches the patient by phone number and compares the
   * provided password with the stored hashed password using BCrypt.
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
      patient.setLastLoginAt(OffsetDateTime.now());
      patientRepository.save(patient);
      return patient;
    } else {
      // Password does not match
      logger.warn("Login failed: invalid password for phone number {}", phoneNumber);
      return null;
    }
  }

  /**
   * Updates the password for a given patient. The new password is required and will be hashed using
   * BCrypt. The {@code usingDefaultPassword} flag is set to false after a successful password
   * update.
   *
   * @param patientId The ID of the patient whose password is to be updated.
   * @param newPassword The new plain text password.
   * @throws RuntimeException if the patient is not found.
   * @throws IllegalArgumentException if the new password is null or empty.
   */
  public void updatePassword(Long patientId, String newPassword) {
    // Validate the new password is not null or empty first
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("New password cannot be null or empty");
    }

    // Then retrieve the patient by ID, or throw an exception if not found
    Patient patient =
        patientRepository
            .findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

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
