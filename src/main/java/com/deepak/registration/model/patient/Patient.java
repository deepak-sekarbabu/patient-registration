package com.deepak.registration.model.patient;

import com.deepak.registration.model.patient.converter.ClinicPreferencesConverter;
import com.deepak.registration.model.patient.converter.EmergencyContactConverter;
import com.deepak.registration.model.patient.converter.InsuranceDetailsConverter;
import com.deepak.registration.model.patient.converter.MedicalInfoConverter;
import com.deepak.registration.model.patient.converter.PersonalDetailsConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(columnNames = "phoneNumber"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="phone_number")
    private String phoneNumber;

    @Convert(converter = PersonalDetailsConverter.class)
    @Column(columnDefinition = "json")
    private PersonalDetails personalDetails;

    @Convert(converter = MedicalInfoConverter.class)
    @Column(columnDefinition = "json")
    private MedicalInfo medicalInfo;

    @Convert(converter = EmergencyContactConverter.class)
    @Column(columnDefinition = "json")
    private EmergencyContact emergencyContact;

    @Convert(converter = InsuranceDetailsConverter.class)
    @Column(columnDefinition = "json")
    private InsuranceDetails insuranceDetails;

    @Convert(converter = ClinicPreferencesConverter.class)
    @Column(columnDefinition = "json")
    private ClinicPreferences clinicPreferences;
}