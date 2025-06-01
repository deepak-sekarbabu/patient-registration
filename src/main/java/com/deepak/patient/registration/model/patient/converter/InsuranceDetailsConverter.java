package com.deepak.patient.registration.model.patient.converter;

import com.deepak.patient.registration.model.patient.InsuranceDetails;

public class InsuranceDetailsConverter extends JsonConverter<InsuranceDetails> {
  public InsuranceDetailsConverter() {
    super(InsuranceDetails.class);
  }
}
