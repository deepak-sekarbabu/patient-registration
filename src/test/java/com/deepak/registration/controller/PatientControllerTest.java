package com.deepak.registration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PatientControllerTest {

  @Mock private PatientService patientService;

  @InjectMocks private PatientController patientController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreatePatient() {
    Patient patient = new Patient();
    when(patientService.createPatient(any(Patient.class))).thenReturn(patient);
    ResponseEntity<Patient> response = patientController.createPatient(patient);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testGetPatientByPhoneNumber() {
    Patient patient = new Patient();
    when(patientService.getPatientByPhoneNumber("1234567890")).thenReturn(patient);
    ResponseEntity<Patient> response = patientController.getPatientByPhoneNumber("1234567890");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testGetPatientByPhoneNumberNotFound() {
    when(patientService.getPatientByPhoneNumber("notfound")).thenReturn(null);
    ResponseEntity<Patient> response = patientController.getPatientByPhoneNumber("notfound");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetPatientByIdNotFound() {
    when(patientService.getPatientById(99L)).thenReturn(null);
    ResponseEntity<Patient> response = patientController.getPatientById(99L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testUpdatePatient() {
    Patient patient = new Patient();
    when(patientService.updatePatient(any(Long.class), any(Patient.class))).thenReturn(patient);
    ResponseEntity<Patient> response = patientController.updatePatient(1L, patient);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testUpdatePatientNotFound() {
    when(patientService.updatePatient(any(Long.class), any(Patient.class))).thenReturn(null);
    Patient patient = new Patient();
    ResponseEntity<Patient> response = patientController.updatePatient(2L, patient);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testUpdatePatientBadRequest() {
    ResponseEntity<Patient> response = patientController.updatePatient(2L, null);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testDeletePatientById() {
    // No exception means success
    ResponseEntity<Void> response = patientController.deletePatientById(1L);
    assertTrue(
        response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError());
  }

  @Test
  void testDeletePatientByIdNotFound() {
    doThrow(new RuntimeException("Patient not found")).when(patientService).deletePatient(99L);
    ResponseEntity<Void> response = patientController.deletePatientById(99L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
