package com.deepak.appointment.registration.dto;

import com.deepak.appointment.registration.model.AppointmentFor;
import com.deepak.appointment.registration.model.AppointmentType;
import com.deepak.appointment.registration.model.Symptom;
import java.time.LocalDateTime;
import lombok.Data;

/** DTO for appointment response data. */
@Data
public class AppointmentResponse {
  private Long appointmentId;
  private Long patientId;
  private AppointmentType appointmentType;
  private AppointmentFor appointmentFor;
  private String appointmentForName;
  private Integer appointmentForAge;
  private Symptom symptom;
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
