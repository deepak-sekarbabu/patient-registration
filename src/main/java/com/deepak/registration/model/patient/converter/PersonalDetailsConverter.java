package com.deepak.registration.model.patient.converter;

import com.deepak.registration.model.patient.PersonalDetails;

public class PersonalDetailsConverter extends JsonConverter<PersonalDetails> {
    public PersonalDetailsConverter() {
        super(PersonalDetails.class);
    }
}
