package com.deepak.registration.model.patient;

import com.deepak.registration.model.patient.converter.ClinicPreferencesConverter;
import com.deepak.registration.model.patient.converter.EmergencyContactConverter;
import com.deepak.registration.model.patient.converter.InsuranceDetailsConverter;
import com.deepak.registration.model.patient.converter.MedicalInfoConverter;
import com.deepak.registration.model.patient.converter.PersonalDetailsConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(columnNames = "phoneNumber"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Patient entity containing all registration details")
public class Patient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the patient", example = "1")
  @Hidden
  private Long id;

  @Column(name = "phone_number")
  @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
  @Schema(description = "Patient's phone number", example = "9876543210")
  private String phoneNumber;

  @Convert(converter = PersonalDetailsConverter.class)
  @Column(columnDefinition = "json")
  @Schema(description = "Personal details of the patient")
  private PersonalDetails personalDetails;

  @Convert(converter = MedicalInfoConverter.class)
  @Column(columnDefinition = "json")
  @Schema(description = "Medical information of the patient")
  private MedicalInfo medicalInfo;

  @Convert(converter = EmergencyContactConverter.class)
  @Column(columnDefinition = "json")
  @Schema(description = "Emergency contact details")
  private EmergencyContact emergencyContact;

  @Convert(converter = InsuranceDetailsConverter.class)
  @Column(columnDefinition = "json")
  @Schema(description = "Insurance details of the patient")
  private InsuranceDetails insuranceDetails;

  @Convert(converter = ClinicPreferencesConverter.class)
  @Column(columnDefinition = "json")
  @Schema(description = "Clinic communication preferences")
  private ClinicPreferences clinicPreferences;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Builder.Default
  @Column(name = "using_default_password", nullable = false)
  private boolean usingDefaultPassword = true;

  // Add missing method for setting default password
  public void setUsingDefaultPassword(boolean usingDefaultPassword) {
    this.usingDefaultPassword = usingDefaultPassword;
  }
}
