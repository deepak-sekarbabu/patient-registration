package com.deepak.registration.model.patient;

import java.time.LocalDate;
import java.time.Period;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonalDetails {
    private String name;
    private String phoneNumber;
    private String email;
    private LocalDate birthdate;
    private String sex;
    private Address address;
    private String occupation;

    public Integer getAge() {
        return (birthdate != null) ? Period.between(birthdate, LocalDate.now()).getYears() : null;
    }
}