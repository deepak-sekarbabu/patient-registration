package com.deepak.registration.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.deepak.registration.model.patient.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class PatientRepositoryTest {

  @Autowired private PatientRepository patientRepository;

  @Test
  void testSaveAndFindByPhoneNumber() {
    Patient patient = new Patient();
    patient.setPhoneNumber("1234567890");
    patient.setPasswordHash("dummyhash");
    patientRepository.save(patient);
    assertTrue(patientRepository.findByPhoneNumber("1234567890").isPresent());
  }
}
