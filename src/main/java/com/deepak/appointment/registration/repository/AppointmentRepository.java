package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.entity.Appointment;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  /**
   * Counts active appointments for a patient on a specific date.
   *
   * @param patientId the ID of the patient
   * @param date the date to check for appointments
   * @return count of active appointments for the patient on the given date
   */
  @Query(
      "SELECT COUNT(a) FROM Appointment a JOIN SlotInformation s ON a.slotId = s.slotId "
          + "WHERE a.patientId = :patientId AND a.active = true AND s.slotDate = :date")
  int countActiveAppointmentsByPatientAndDate(
      @Param("patientId") Long patientId, @Param("date") LocalDate date);
}
