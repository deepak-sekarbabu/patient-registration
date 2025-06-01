package com.deepak.patient.registration.model.patient.converter;

import com.deepak.patient.registration.model.patient.EmergencyContact;

public class EmergencyContactConverter extends JsonConverter<EmergencyContact> {
  public EmergencyContactConverter() {
    super(EmergencyContact.class);
  }
}
