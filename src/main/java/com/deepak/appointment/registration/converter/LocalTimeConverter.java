package com.deepak.appointment.registration.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Converter
public class LocalTimeConverter implements AttributeConverter<LocalTime, Object> {
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  @Override
  public Object convertToDatabaseColumn(LocalTime localTime) {
    return localTime != null ? Time.valueOf(localTime) : null;
  }

  @Override
  public LocalTime convertToEntityAttribute(Object dbData) {
    switch (dbData) {
      case null -> {
        return null;
      }
      case Time time -> {
        return time.toLocalTime();
      }
      case String timeStr -> {
        try {
          return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception ex) {
          throw new IllegalArgumentException("Cannot convert to LocalTime: " + dbData, ex);
        }
      }
      default -> throw new IllegalArgumentException(
          "Unsupported type for LocalTime conversion: " + dbData.getClass());
    }
  }
}
