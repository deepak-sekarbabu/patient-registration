package com.deepak.registration.model.patient;

import java.time.LocalDate;
import java.time.Period;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDetails {
    @NotNull
    @Size(min = 2, max = 50)
    @Schema(description = "Full name of the patient", example = "Rohan")
    private String name;

    @NotNull
    @Size(min = 10, max = 13)
    @Schema(description = "Patient's phone number", example = "+919876543210")
    private String phoneNumber;

    @Email
    @Schema(description = "Patient's email address", example = "rohan@domain.com")
    private String email;


    @Past
    @Schema(description = "Birthdate in yyyy-MM-dd format", example = "1995-08-15")
    private LocalDate birthdate;

    @NotNull
    @Pattern(regexp = "[MFO]", message = "Sex must be M, F, or O")
    @Schema(description = "Sex of the patient (M/F/O)", example = "M")
    private String sex;


    @Schema(description = "Address details")
    private Address address;

    @Size(max = 100)
    @Schema(description = "Occupation of the patient", example = "Software Engineer")
    private String occupation;

    public Integer getAge() {
        return (birthdate != null) ? Period.between(birthdate, LocalDate.now()).getYears() : null;
    }
}