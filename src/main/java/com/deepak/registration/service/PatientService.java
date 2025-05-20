package com.deepak.registration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        logger.debug("Creating patient: {}", patient);
        return patientRepository.save(patient);
    }

    public Patient getPatientByPhoneNumber(String phoneNumber) {
        logger.debug("Fetching patient by phone number: {}", phoneNumber);
        return patientRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    public Patient getPatientById(Long id) {
        logger.debug("Fetching patient by id: {}", id);
        return patientRepository.findById(id).orElse(null);
    }
}