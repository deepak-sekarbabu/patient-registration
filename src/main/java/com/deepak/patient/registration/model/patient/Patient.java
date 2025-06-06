package com.deepak.patient.registration.model.patient;

import com.deepak.patient.registration.model.patient.converter.ClinicPreferencesConverter;
import com.deepak.patient.registration.model.patient.converter.EmergencyContactConverter;
import com.deepak.patient.registration.model.patient.converter.InsuranceDetailsConverter;
import com.deepak.patient.registration.model.patient.converter.MedicalInfoConverter;
import com.deepak.patient.registration.model.patient.converter.PersonalDetailsConverter;
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
import java.time.LocalDateTime;
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

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Add missing method for setting default password
  @Builder.Default
  @Column(name = "using_default_password", nullable = false)
  private boolean usingDefaultPassword = true;

  public static class PatientBuilder {
    private Long id;
    private String phoneNumber;
    private PersonalDetails personalDetails;
    private MedicalInfo medicalInfo;
    private EmergencyContact emergencyContact;
    private InsuranceDetails insuranceDetails;
    private ClinicPreferences clinicPreferences;
    private String passwordHash;
    private LocalDateTime updatedAt;

    public Patient build() {
      if (this.updatedAt == null) {
        this.updatedAt = java.time.LocalDateTime.now();
      }
      boolean usingDefaultPassword = true;
      return new Patient(
          id,
          phoneNumber,
          personalDetails,
          medicalInfo,
          emergencyContact,
          insuranceDetails,
          clinicPreferences,
          passwordHash,
          updatedAt,
          usingDefaultPassword);
    }
  }

  @Override
  public String toString() {
    return "Patient("
        + "id="
        + getId()
        + ", phoneNumber='"
        + (getPhoneNumber() != null ? "[MASKED]" : "null")
        + '\''
        + ", personalDetails="
        + (getPersonalDetails() != null ? getPersonalDetails().toString() : "null")
        + ", medicalInfo="
        + (getMedicalInfo() != null ? getMedicalInfo().toString() : "null")
        + ", emergencyContact="
        + (getEmergencyContact() != null ? getEmergencyContact().toString() : "null")
        + ", insuranceDetails="
        + (getInsuranceDetails() != null ? getInsuranceDetails().toString() : "null")
        + ", clinicPreferences="
        + (getClinicPreferences() != null ? getClinicPreferences().toString() : "null")
        + ", passwordHash='[MASKED]'"
        + // Explicitly mask passwordHash
        ", updatedAt="
        + getUpdatedAt()
        + ", usingDefaultPassword="
        + isUsingDefaultPassword()
        + // Assuming getter is isUsingDefaultPassword
        ')';
  }
}
