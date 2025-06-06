package com.deepak.appointment.registration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "slot_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotInformation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "slot_id")
  private Long slotId;

  @Column(name = "slot_no")
  private Integer slotNo;

  @Column(name = "shift_time")
  private String shiftTime;

  @Column(name = "slot_time", columnDefinition = "TIME")
  private LocalTime slotTime;

  @Column(name = "clinic_id")
  private Integer clinicId;

  @Column(name = "doctor_id")
  private String doctorId;

  @Column(name = "slot_date")
  private LocalDate slotDate;

  @Column(name = "is_available")
  private Boolean isAvailable;
}
