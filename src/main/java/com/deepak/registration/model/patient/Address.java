package com.deepak.registration.model.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
