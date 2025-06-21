package com.deepak.appointment.registration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/** Represents who the appointment is for. */
public enum AppointmentFor {
  SELF("Self"),
  SPOUSE("Spouse"),
  CHILD("Child"),
  PARENT("Parent"),
  SIBLING("Sibling"),
  RELATIVE("Relative"),
  FRIEND("Friend"),
  OTHER("Other");

  @Getter private final String displayName;

  AppointmentFor(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue
  public String getValue() {
    return name();
  }

  @JsonCreator
  public static AppointmentFor fromString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return AppointmentFor.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to match by display name as a fallback
      for (AppointmentFor type : values()) {
        if (type.getDisplayName().equalsIgnoreCase(value)) {
          return type;
        }
      }
      throw new IllegalArgumentException(
          String.format(
              "Invalid 'appointment for' value: %s. Must be one of: %s",
              value, String.join(", ", getNames())));
    }
  }

  private static String[] getNames() {
    AppointmentFor[] types = values();
    String[] names = new String[types.length];
    for (int i = 0; i < types.length; i++) {
      names[i] = types[i].name();
    }
    return names;
  }
}
