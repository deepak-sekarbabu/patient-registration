package com.deepak.registration.model.patient.converter;

import com.deepak.registration.model.patient.ClinicPreferences;

public class ClinicPreferencesConverter extends JsonConverter<ClinicPreferences> {
    public ClinicPreferencesConverter() {
        super(ClinicPreferences.class);
    }
}
