package com.deepak.registration.model.patient;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class FamilyHistory {

  @Schema(description = "Family history of diabetes", example = "true")
  private Boolean diabetes;

  @Schema(description = "Family history of hypertension", example = "false")
  private Boolean hypertension;

  @Schema(description = "Family history of heart disease", example = "true")
  private Boolean heartDisease;

  @Override
  public String toString() {
    return "FamilyHistory[PHI MASKED]";
  }
}
