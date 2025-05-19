package com.deepak.registration.model.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FamilyHistory {
    private boolean diabetes;
    private boolean hypertension;
    private boolean heartDisease;
}
