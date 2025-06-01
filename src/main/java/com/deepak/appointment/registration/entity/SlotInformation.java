package com.deepak.appointment.registration.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "slot_information")
public class SlotInformation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "slot_id")
  private Long slotId;

  @Column(name = "slot_no")
  private Integer slotNo;

  @Column(name = "shift_time")
  private String shiftTime;

  @Column(name = "slot_time")
  private LocalTime slotTime;

  @Column(name = "clinic_id")
  private Integer clinicId;

  @Column(name = "doctor_id")
  private String doctorId;

  @Column(name = "slot_date")
  private LocalDate slotDate;

  @Column(name = "is_available")
  private Boolean isAvailable;

  // Getters and Setters
  public Long getSlotId() {
    return slotId;
  }

  public void setSlotId(Long slotId) {
    this.slotId = slotId;
  }

  public Integer getSlotNo() {
    return slotNo;
  }

  public void setSlotNo(Integer slotNo) {
    this.slotNo = slotNo;
  }

  public String getShiftTime() {
    return shiftTime;
  }

  public void setShiftTime(String shiftTime) {
    this.shiftTime = shiftTime;
  }

  public LocalTime getSlotTime() {
    return slotTime;
  }

  public void setSlotTime(LocalTime slotTime) {
    this.slotTime = slotTime;
  }

  public Integer getClinicId() {
    return clinicId;
  }

  public void setClinicId(Integer clinicId) {
    this.clinicId = clinicId;
  }

  public String getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(String doctorId) {
    this.doctorId = doctorId;
  }

  public LocalDate getSlotDate() {
    return slotDate;
  }

  public void setSlotDate(LocalDate slotDate) {
    this.slotDate = slotDate;
  }

  public Boolean getIsAvailable() {
    return isAvailable;
  }

  public void setIsAvailable(Boolean available) {
    isAvailable = available;
  }
}
