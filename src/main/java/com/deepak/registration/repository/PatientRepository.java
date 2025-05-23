package com.deepak.registration.repository;

import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deepak.registration.model.patient.Patient;

@Repository
@Lazy
public interface PatientRepository extends JpaRepository<Patient, Long> {
  Optional<Patient> findByPhoneNumber(String phoneNumber);
}
