package com.deepak.appointment.registration.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AppointmentResponse {
  private Long appointmentId;
  private Long patientId;
  private String appointmentType;
  private String appointmentFor;
  private String appointmentForName;
  private Integer appointmentForAge;
  private String symptom;
  private String otherSymptoms;
  private LocalDateTime appointmentDate;
  private Long slotId;
  private String doctorId;
  private Integer clinicId;
  private boolean active;
}
