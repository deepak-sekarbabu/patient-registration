package com.deepak.patient.registration.model.patient;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class Address {

  @Size(min = 2, max = 100)
  @Schema(description = "Street address", example = "123 Medical Lane")
  private String street;

  @Size(min = 2, max = 50)
  @Schema(description = "City name", example = "Bangalore")
  private String city;

  @Size(min = 2, max = 50)
  @Schema(description = "State name", example = "Karnataka")
  private String state;

  @Size(min = 4, max = 10)
  @Schema(description = "Postal code", example = "560001")
  private String postalCode;

  @Size(min = 2, max = 50)
  @Schema(description = "Country name", example = "India")
  private String country;

  @Override
  public String toString() {
    return "Address(street='[MASKED]', city='[MASKED]', state='[MASKED]', postalCode='[MASKED]', country='[MASKED]')";
  }
}
