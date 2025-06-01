package com.hospital.registration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clinic_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicInformation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "clinic_id")
  private Integer clinicId;

  @Column(name = "clinic_name")
  private String clinicName;

  @Column(name = "clinic_address")
  private String clinicAddress;

  @Column(name = "clinic_pin_code")
  private String clinicPinCode;

  @Column(name = "map_geo_location")
  private String mapGeoLocation;

  @Column(name = "clinic_amenities")
  private String clinicAmenities;

  @Column(name = "clinic_email")
  private String clinicEmail;

  @Column(name = "clinic_timing")
  private String clinicTiming;

  @Column(name = "clinic_website")
  private String clinicWebsite;

  @Column(name = "clinic_phone_numbers", columnDefinition = "json")
  private String clinicPhoneNumbers;

  @Column(name = "no_of_doctors")
  private Integer noOfDoctors;
}
