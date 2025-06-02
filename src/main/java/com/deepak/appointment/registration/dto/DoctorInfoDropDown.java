package com.deepak.appointment.registration.dto;

public class DoctorInfoDropDown {
  private String doctorId;
  private String doctorName;

  public DoctorInfoDropDown() {}

  public DoctorInfoDropDown(String doctorId, String doctorName) {
    this.doctorId = doctorId;
    this.doctorName = doctorName;
  }

  // Getters and setters
  public String getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(String doctorId) {
    this.doctorId = doctorId;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public void setDoctorName(String doctorName) {
    this.doctorName = doctorName;
  }
}
