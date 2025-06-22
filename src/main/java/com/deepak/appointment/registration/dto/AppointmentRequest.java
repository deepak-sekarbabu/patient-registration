package com.deepak.appointment.registration.dto;

import com.deepak.appointment.registration.model.AppointmentFor;
import com.deepak.appointment.registration.model.AppointmentType;
import com.deepak.appointment.registration.model.Symptom;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import java.time.LocalDate;
import lombok.Data;

/** Represents a request to create a new appointment. */
@Data
public class AppointmentRequest {
  @NotNull(message = "Patient ID is required")
  private Long patientId;

  @NotNull(message = "Appointment type is required")
  private AppointmentType appointmentType;

  @NotNull(message = "Appointment for is required")
  private AppointmentFor appointmentFor;

  @NotBlank(message = "Name is required")
  @Pattern(
      regexp = "^[a-zA-Z\\s'-]+",
      message = "Name can only contain letters, spaces, hyphens, and apostrophes")
  private String appointmentForName;

  @Pattern(
      regexp = "^(0|[1-9]\\d?|1[01]\\d|110)$",
      message = "Age must be a number between 0 and 110",
      groups = {Default.class, NotNull.class})
  private String appointmentForAge;

  @NotNull(message = "Symptom is required")
  private Symptom symptom;

  private String otherSymptoms;

  @NotNull(message = "Appointment date is required")
  @FutureOrPresent(message = "Appointment date cannot be in the past")
  private LocalDate appointmentDate;

  @NotBlank(message = "Clinic ID is required")
  private String clinicId;

  @NotBlank(message = "Doctor ID is required")
  private String doctorId;

  @NotBlank(message = "Slot ID is required")
  private String slotId;
}
