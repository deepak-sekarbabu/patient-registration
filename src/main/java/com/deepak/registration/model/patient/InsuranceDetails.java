package com.deepak.registration.model.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
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
public class InsuranceDetails {

  @Size(min = 2, max = 50)
  @Schema(description = "Insurance provider name", example = "Star Health")
  private String provider;

  @Size(min = 5, max = 30)
  @Schema(description = "Insurance policy number", example = "STAR123456")
  private String policyNumber;

  @Future
  @Schema(description = "Insurance validity date", example = "2025-12-31")
  private LocalDate validTill;
}
