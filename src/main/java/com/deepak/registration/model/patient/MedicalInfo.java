package com.deepak.registration.model.patient;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalInfo {
    private String bloodGroup;
    private List<String> allergies;
    private List<String> existingConditions;
    private List<String> currentMedications;
    private FamilyHistory familyHistory;
}

