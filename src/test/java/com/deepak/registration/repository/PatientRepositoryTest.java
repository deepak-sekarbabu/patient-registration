package com.deepak.registration.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.deepak.registration.model.patient.Patient;

@DataJpaTest
public class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void testSaveAndFindByPhoneNumber() {
        Patient patient = new Patient();
        patient.setPhoneNumber("1234567890");
        patientRepository.save(patient);
        assertTrue(patientRepository.findByPhoneNumber("1234567890").isPresent());
    }
}
