package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
  // Custom query methods can be added here if needed
  boolean existsBySlotIdAndActiveTrue(Long slotId);
}
