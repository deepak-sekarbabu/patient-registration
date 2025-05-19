package com.deepak.registration.model.patient;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsuranceDetails {
    private String provider;
    private String policyNumber;
    private LocalDate validTill;
}

