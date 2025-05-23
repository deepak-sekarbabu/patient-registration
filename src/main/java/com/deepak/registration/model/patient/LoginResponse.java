package com.deepak.registration.model.patient;

public class LoginResponse {
  private Patient patient;
  private String token;

  public LoginResponse(Patient patient, String token) {
    this.patient = patient;
    this.token = token;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
