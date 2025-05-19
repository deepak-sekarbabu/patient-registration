package com.deepak.registration.model.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact {
    private String name;
    private String relationship;
    private String phoneNumber;
    private String address; // Simplified here
}
