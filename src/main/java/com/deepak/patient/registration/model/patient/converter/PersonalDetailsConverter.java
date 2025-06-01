package com.deepak.patient.registration.model.patient.converter;

import com.deepak.patient.registration.model.patient.PersonalDetails;

public class PersonalDetailsConverter extends JsonConverter<PersonalDetails> {
  public PersonalDetailsConverter() {
    super(PersonalDetails.class);
  }
}
