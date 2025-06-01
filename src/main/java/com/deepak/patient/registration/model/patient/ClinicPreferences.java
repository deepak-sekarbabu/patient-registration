package com.deepak.patient.registration.model.patient;

import com.deepak.patient.registration.validation.AllowedCommunicationMethods;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ClinicPreferences {

  @Size(min = 2, max = 30)
  @Schema(description = "Preferred language for communication", example = "English")
  private String preferredLanguage;

  @NotNull
  @AllowedCommunicationMethods
  @Schema(
      description = "Preferred communication methods",
      example = "[\"Email\", \"SMS\", \"Whatsapp\"]")
  private List<String> communicationMethod;
}
