package com.deepak.appointment.registration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "doctor_clinic_view")
public class DoctorInformation {

  @Id
  @Column(name = "doctor_id")
  private String doctorId;

  @Column(name = "doctor_name")
  private String doctorName;

  @Column(name = "clinic_id")
  private Integer clinicId;

  @Column(name = "clinic_name")
  private String clinicName;

  public DoctorInformation() {}

  public DoctorInformation(String doctorId, String doctorName) {
    this.doctorId = doctorId;
    this.doctorName = doctorName;
  }
}
