package com.deepak.registration.service;

import org.springframework.stereotype.Service;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }
}