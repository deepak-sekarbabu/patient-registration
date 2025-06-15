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
  private String slotTime;
  private String doctorId;
  private String doctorName;
  private Integer clinicId;
  private String clinicName;
  private boolean active;
}
