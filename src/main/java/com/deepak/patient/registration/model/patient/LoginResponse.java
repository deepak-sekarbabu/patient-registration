package com.deepak.patient.registration.model.patient;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
  private Patient patient;
  private String token;

  public LoginResponse(Patient patient, String token) {
    this.patient = patient;
    this.token = token;
  }

  @Override
  public String toString() {
    return "LoginResponse("
        + "patient="
        + (getPatient() != null ? getPatient().toString() : "null")
        + // Delegates to Patient.toString()
        ", token='[MASKED_JWT]'"
        + ')';
  }
}
