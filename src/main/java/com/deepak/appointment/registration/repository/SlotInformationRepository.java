package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.entity.SlotInformation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotInformationRepository extends JpaRepository<SlotInformation, Long> {

  @Query(
      "SELECT DISTINCT s.slotDate FROM SlotInformation s "
          + "WHERE s.clinicId = :clinicId AND s.doctorId = :doctorId AND s.isAvailable = true "
          + "AND s.slotDate >= CURRENT_DATE "
          + "ORDER BY s.slotDate")
  List<LocalDate> findAvailableDatesByClinicAndDoctor(
      @Param("clinicId") Integer clinicId, @Param("doctorId") String doctorId);

  @Query(
      "SELECT s FROM SlotInformation s "
          + "WHERE s.clinicId = :clinicId "
          + "AND s.doctorId = :doctorId "
          + "AND s.slotDate = :date "
          + "AND s.isAvailable = true "
          + "ORDER BY s.slotTime")
  List<SlotInformation> findAvailableSlotsByClinicDoctorAndDate(
      @Param("clinicId") Integer clinicId,
      @Param("doctorId") String doctorId,
      @Param("date") LocalDate date);
}
