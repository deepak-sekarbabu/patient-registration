package com.deepak.appointment.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AppointmentRequest {
  @NotNull(message = "Patient ID is required")
  private Long patientId;

  @NotBlank(message = "Appointment type is required")
  private String appointmentType;

  @NotBlank(message = "Appointment for is required")
  private String appointmentFor;

  @NotBlank(message = "Name is required")
  private String appointmentForName;

  private String appointmentForAge;

  @NotBlank(message = "Symptom is required")
  private String symptom;

  private String otherSymptoms;

  @NotNull(message = "Appointment date is required")
  private LocalDate appointmentDate;

  @NotBlank(message = "Clinic ID is required")
  private String clinicId;

  @NotBlank(message = "Doctor ID is required")
  private String doctorId;

  @NotBlank(message = "Slot ID is required")
  private String slotId;
}
