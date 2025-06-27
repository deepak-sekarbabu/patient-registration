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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity representing a patient in the system. Maps to the 'patients' table in the database. */
@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Patient entity containing all registration details")
public class Patient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the patient", example = "1")
  @Hidden
  private Long id;

  @Column(name = "phone_number", nullable = false, length = 10)
  @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
  @Schema(description = "Patient's phone number", example = "9876543210")
  private String phoneNumber;

  @Convert(converter = PersonalDetailsConverter.class)
  @Column(columnDefinition = "json", name = "personal_details")
  @Schema(description = "Personal details of the patient")
  private PersonalDetails personalDetails;

  @Convert(converter = MedicalInfoConverter.class)
  @Column(columnDefinition = "json", name = "medical_info")
  @Schema(description = "Medical information of the patient")
  private MedicalInfo medicalInfo;

  @Convert(converter = EmergencyContactConverter.class)
  @Column(columnDefinition = "json", name = "emergency_contact")
  @Schema(description = "Emergency contact details")
  private EmergencyContact emergencyContact;

  @Convert(converter = InsuranceDetailsConverter.class)
  @Column(columnDefinition = "json", name = "insurance_details")
  @Schema(description = "Insurance details of the patient")
  private InsuranceDetails insuranceDetails;

  @Convert(converter = ClinicPreferencesConverter.class)
  @Column(columnDefinition = "json", name = "clinic_preferences")
  @Schema(description = "Clinic communication preferences")
  private ClinicPreferences clinicPreferences;

  @Column(name = "password_hash", nullable = false, length = 255)
  @Schema(description = "Hashed password of the patient")
  private String passwordHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Schema(description = "Timestamp when the patient record was created")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  @Schema(description = "Timestamp when the patient record was last updated")
  private LocalDateTime updatedAt;

  @Builder.Default
  @Column(name = "using_default_password", nullable = false)
  @Schema(description = "Flag indicating if the patient is using default password")
  private boolean usingDefaultPassword = true;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  @Schema(description = "Flag indicating if the patient account is active")
  private boolean active = true;

  @Builder.Default
  @Column(name = "failed_login_attempts", nullable = false)
  @Schema(description = "Number of failed login attempts")
  private int failedLoginAttempts = 0;

  @Column(name = "locked_until")
  @Schema(description = "Timestamp until the account is locked")
  private OffsetDateTime lockedUntil;

  @Column(name = "last_login_at")
  @Schema(description = "Timestamp of the last successful login")
  private OffsetDateTime lastLoginAt;

  /** Sets the createdAt timestamp before persisting a new entity. */
  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  /** Updates the updatedAt timestamp before updating an existing entity. */
  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return "Patient{"
        + "id="
        + id
        + ", phoneNumber='"
        + (phoneNumber != null ? "[MASKED]" : "null")
        + '\''
        + ", personalDetails="
        + personalDetails
        + ", medicalInfo="
        + medicalInfo
        + ", emergencyContact="
        + emergencyContact
        + ", insuranceDetails="
        + insuranceDetails
        + ", clinicPreferences="
        + clinicPreferences
        + ", passwordHash='[MASKED]'"
        + ", createdAt="
        + createdAt
        + ", updatedAt="
        + updatedAt
        + ", usingDefaultPassword="
        + usingDefaultPassword
        + ", active="
        + active
        + ", failedLoginAttempts="
        + failedLoginAttempts
        + ", lockedUntil="
        + lockedUntil
        + ", lastLoginAt="
        + lastLoginAt
        + '}';
  }
}
