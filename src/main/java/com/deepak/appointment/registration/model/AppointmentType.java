package com.deepak.appointment.registration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/** Represents the type of appointment. */
public enum AppointmentType {
  CONSULTATION("Consultation"),
  FOLLOW_UP("Follow-up"),
  ROUTINE_CHECKUP("Routine Checkup"),
  EMERGENCY("Emergency"),
  VACCINATION("Vaccination"),
  DIAGNOSTIC_TEST("Diagnostic Test"),
  PROCEDURE("Procedure"),
  OTHER("Other");

  @Getter private final String displayName;

  AppointmentType(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getValue() {
    return name();
  }

  @JsonCreator
  public static AppointmentType fromString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return AppointmentType.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to match by display name as a fallback
      for (AppointmentType type : values()) {
        if (type.getDisplayName().equalsIgnoreCase(value)) {
          return type;
        }
      }
      throw new IllegalArgumentException(
          String.format(
              "Invalid appointment type: %s. Must be one of: %s",
              value, String.join(", ", getNames())));
    }
  }

  private static String[] getNames() {
    AppointmentType[] types = values();
    String[] names = new String[types.length];
    for (int i = 0; i < types.length; i++) {
      names[i] = types[i].name();
    }
    return names;
  }
}
