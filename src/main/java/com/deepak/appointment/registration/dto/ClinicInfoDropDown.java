package com.deepak.appointment.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClinicInfoDropDown {
  private Integer clinicId;
  private String clinicName;
}
