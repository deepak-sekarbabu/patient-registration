package com.deepak.registration.model.patient.converter;

import com.deepak.registration.model.patient.MedicalInfo;

public class MedicalInfoConverter extends JsonConverter<MedicalInfo> {
  public MedicalInfoConverter() {
    super(MedicalInfo.class);
  }
}
