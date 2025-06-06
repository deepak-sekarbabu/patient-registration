package com.deepak.appointment.registration.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DoctorInfoDropDown {
  // Getters and setters
  private String doctorId;
  private String doctorName;

  public DoctorInfoDropDown() {}

  public DoctorInfoDropDown(String doctorId, String doctorName) {
    this.doctorId = doctorId;
    this.doctorName = doctorName;
  }
}
