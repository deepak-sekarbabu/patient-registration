package com.deepak.registration.service;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

  private final PatientRepository patientRepository;

  public PatientService(PatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  public Patient createPatient(Patient patient) {
    logger.debug("Creating patient: {}", patient);
    // Set default password as phone number, hash it
    if (patient.getPhoneNumber() != null) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      patient.setPasswordHash(encoder.encode(patient.getPhoneNumber()));
      patient.setUsingDefaultPassword(true);
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

  public Patient updatePatient(Long id, Patient updatedPatient) {
    logger.debug("Updating patient with id: {}", id);
    return patientRepository
        .findById(id)
        .map(
            existingPatient -> {
              if (updatedPatient.getPersonalDetails() != null) {
                existingPatient.setPersonalDetails(updatedPatient.getPersonalDetails());
              }
              if (updatedPatient.getMedicalInfo() != null) {
                existingPatient.setMedicalInfo(updatedPatient.getMedicalInfo());
              }
              if (updatedPatient.getEmergencyContact() != null) {
                existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());
              }
              if (updatedPatient.getInsuranceDetails() != null) {
                existingPatient.setInsuranceDetails(updatedPatient.getInsuranceDetails());
              }
              if (updatedPatient.getClinicPreferences() != null) {
                existingPatient.setClinicPreferences(updatedPatient.getClinicPreferences());
              }
              return patientRepository.save(existingPatient);
            })
        .orElse(null);
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
}
