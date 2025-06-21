package com.deepak.appointment.registration.entity;

import com.deepak.appointment.registration.model.AppointmentFor;
import com.deepak.appointment.registration.model.AppointmentType;
import com.deepak.appointment.registration.model.Symptom;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "appointment_id")
  private Long appointmentId;

  @Column(name = "patient_id", nullable = false)
  private Long patientId;

  @Enumerated(EnumType.STRING)
  @Column(name = "appointment_type", nullable = false, length = 50)
  private AppointmentType appointmentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "appointment_for", nullable = false, length = 20)
  private AppointmentFor appointmentFor;

  @Column(name = "appointment_for_name", nullable = false, length = 255)
  private String appointmentForName;

  @Column(name = "appointment_for_age")
  private Integer appointmentForAge;

  @Enumerated(EnumType.STRING)
  @Column(name = "symptom", length = 50)
  private Symptom symptom;

  @Column(name = "other_symptoms", length = 255)
  private String otherSymptoms;

  @Column(name = "appointment_date", nullable = false)
  private LocalDateTime appointmentDate;

  @Column(name = "slot_id")
  private Long slotId;

  @Column(name = "doctor_id", nullable = false, length = 50)
  private String doctorId;

  @Column(name = "clinic_id", nullable = false)
  private Integer clinicId;

  @Column(name = "active", nullable = false)
  private boolean active = true;
}
