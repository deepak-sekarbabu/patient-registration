package com.deepak.registration.model.patient.converter;

import com.deepak.registration.model.patient.EmergencyContact;

public class EmergencyContactConverter extends JsonConverter<EmergencyContact> {
    public EmergencyContactConverter() {
        super(EmergencyContact.class);
    }
}
