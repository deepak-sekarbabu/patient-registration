package com.deepak.appointment.registration.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "doctor_clinic_view")
public class DoctorInformation {

  @Id private Integer doctorId;
  private String doctorName;
  private Integer clinicId;
  private String clinicName;

  public DoctorInformation() {}

  public DoctorInformation(Integer doctorId, String doctorName) {
    this.doctorId = doctorId;
    this.doctorName = doctorName;
  }
}
