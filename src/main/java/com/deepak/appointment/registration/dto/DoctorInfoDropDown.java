package com.deepak.appointment.registration.dto;

public class DoctorInfoDropDown {
  private Integer doctorId;
  private String doctorName;

  public DoctorInfoDropDown(Integer doctorId, String doctorName) {
    this.doctorId = doctorId;
    this.doctorName = doctorName;
  }

  // Getters and setters
  public Integer getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(Integer doctorId) {
    this.doctorId = doctorId;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public void setDoctorName(String doctorName) {
    this.doctorName = doctorName;
  }
}
