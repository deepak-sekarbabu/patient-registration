package com.deepak.patient.registration.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.repository.PatientRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PatientServiceTest {

  @Mock private PatientRepository patientRepository;

  @InjectMocks private PatientService patientService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreatePatient() {
    Patient patient = new Patient();
    when(patientRepository.save(any(Patient.class))).thenReturn(patient);
    Patient result = patientService.createPatient(patient);
    assertNotNull(result);
  }

  @Test
  void testGetPatientById() {
    Patient patient = new Patient();
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    Patient result = patientService.getPatientById(1L);
    assertNotNull(result);
  }

  // Add more tests for update, delete, get by phone, etc.
}
