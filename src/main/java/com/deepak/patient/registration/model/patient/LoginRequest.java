package com.deepak.patient.registration.model.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  @Schema(description = "Phone number of the patient", example = "9789801844")
  @NotBlank
  private String phoneNumber;

  @Schema(description = "Password for login", example = "9789801844")
  @NotBlank
  private String password;

  @Override
  public String toString() {
    return "LoginRequest("
        + "phoneNumber='"
        + (getPhoneNumber() != null ? "[MASKED]" : "null")
        + '\''
        + ", password='[MASKED]'"
        + ')';
  }
}
