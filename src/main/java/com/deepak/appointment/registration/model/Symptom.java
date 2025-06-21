package com.deepak.appointment.registration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

/** Represents common symptoms for which patients book appointments. */
public enum Symptom {
  FEVER("Fever"),
  COUGH("Cough"),
  HEADACHE("Headache"),
  SORE_THROAT("Sore Throat"),
  BODY_ACHE("Body Ache"),
  FATIGUE("Fatigue"),
  NAUSEA("Nausea"),
  VOMITING("Vomiting"),
  DIARRHEA("Diarrhea"),
  CHEST_PAIN("Chest Pain"),
  SHORTNESS_OF_BREATH("Shortness of Breath"),
  DIZZINESS("Dizziness"),
  JOINT_PAIN("Joint Pain"),
  RASH("Rash"),
  ALLERGIES("Allergies"),
  COLD("Cold"),
  FLU("Flu"),
  OTHER("Other");

  @Getter private final String displayName;

  Symptom(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getValue() {
    return name();
  }

  @JsonCreator
  public static Symptom fromString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return Symptom.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to match by display name as a fallback
      for (Symptom symptom : values()) {
        if (symptom.getDisplayName().equalsIgnoreCase(value)) {
          return symptom;
        }
      }
      throw new IllegalArgumentException(
          String.format(
              "Invalid symptom: %s. Must be one of: %s", value, String.join(", ", getNames())));
    }
  }

  private static String[] getNames() {
    Symptom[] symptoms = values();
    String[] names = new String[symptoms.length];
    for (int i = 0; i < symptoms.length; i++) {
      names[i] = symptoms[i].name();
    }
    return names;
  }

  public static List<Symptom> getCommonSymptoms() {
    return Arrays.asList(
        FEVER,
        COUGH,
        HEADACHE,
        SORE_THROAT,
        BODY_ACHE,
        FATIGUE,
        NAUSEA,
        VOMITING,
        DIARRHEA,
        CHEST_PAIN,
        SHORTNESS_OF_BREATH,
        DIZZINESS,
        JOINT_PAIN,
        RASH,
        ALLERGIES,
        COLD,
        FLU,
        OTHER);
  }
}
