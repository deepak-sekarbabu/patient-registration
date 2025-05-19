package com.deepak.registration.model.patient.converter;

import com.deepak.registration.model.patient.InsuranceDetails;

public class InsuranceDetailsConverter extends JsonConverter<InsuranceDetails> {
    public InsuranceDetailsConverter() {
        super(InsuranceDetails.class);
    }
}
