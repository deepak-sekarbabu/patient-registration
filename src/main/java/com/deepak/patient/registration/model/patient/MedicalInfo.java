package com.deepak.patient.registration.model.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
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
public class MedicalInfo {
  @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood group")
  @Schema(description = "Blood group of the patient", example = "B+")
  private String bloodGroup;

  @Schema(description = "List of allergies", example = "[\"Penicillin\", \"Dust\"]")
  private List<String> allergies;

  @Schema(description = "List of existing medical conditions", example = "[\"Asthma\"]")
  private List<String> existingConditions;

  @Schema(description = "List of current medications", example = "[\"Salbutamol inhaler\"]")
  private List<String> currentMedications;

  @Valid
  @Schema(description = "Family medical history")
  private FamilyHistory familyHistory;

  @Override
  public String toString() {
    return "MedicalInfo("
        + "bloodGroup='"
        + (getBloodGroup() != null ? "[MASKED]" : "null")
        + '\''
        +
        // allergies, existingConditions, currentMedications are intentionally excluded as per user
        // feedback
        ", familyHistory="
        + (getFamilyHistory() != null ? getFamilyHistory().toString() : "null")
        + // Delegates to FamilyHistory.toString()
        ')';
  }
}
