package com.deepak.patient.registration.model.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContact {

  @Size(min = 2, max = 50)
  @Schema(description = "Name of the emergency contact", example = "Priya Sharma")
  private String name;

  @Size(min = 2, max = 30)
  @Schema(description = "Relationship to the patient", example = "Spouse")
  private String relationship;

  @Pattern(
      regexp = "^\\+91[0-9]{10}$",
      message = "Phone number must start with +91 and be followed by 10 digits")
  @Schema(description = "Phone number of the emergency contact", example = "+919876543211")
  private String phoneNumber;

  @Size(min = 2, max = 100)
  @Schema(description = "Address of the emergency contact", example = "Same as patient")
  private String address;

  @Override
  public String toString() {
    return "EmergencyContact("
        + "name='[MASKED]'"
        + ", relationship='"
        + getRelationship()
        + '\''
        + // Assuming getRelationship() is safe
        ", phoneNumber='[MASKED]'"
        + ", address='[MASKED]'"
        + ')';
  }
}
