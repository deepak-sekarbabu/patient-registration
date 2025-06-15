package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.entity.Appointment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
  // Custom query methods can be added here if needed
  boolean existsBySlotIdAndActiveTrue(Long slotId);

  /**
   * Finds all active appointments for a given patient ID.
   *
   * @param patientId the ID of the patient
   * @return list of active appointments for the patient
   */
  List<Appointment> findByPatientIdAndActiveTrue(Long patientId);
}
