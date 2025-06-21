package com.deepak.appointment.registration.converter;

import com.deepak.appointment.registration.dto.AppointmentRequest;
import com.deepak.appointment.registration.dto.AppointmentResponse;
import com.deepak.appointment.registration.entity.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class AppointmentConverter {

  /**
   * Converts an AppointmentRequest to an Appointment entity.
   *
   * @param request the appointment request DTO
   * @return the converted Appointment entity
   */
  public Appointment toEntity(AppointmentRequest request) {
    if (request == null) {
      return null;
    }

    Appointment appointment = new Appointment();
    appointment.setPatientId(request.getPatientId());

    // Handle enum types
    appointment.setAppointmentType(request.getAppointmentType());
    appointment.setAppointmentFor(request.getAppointmentFor());
    appointment.setSymptom(request.getSymptom());

    // Set other fields
    appointment.setAppointmentForName(request.getAppointmentForName());
    appointment.setAppointmentForAge(parseAge(request.getAppointmentForAge()));
    appointment.setOtherSymptoms(request.getOtherSymptoms());
    appointment.setAppointmentDate(convertToLocalDateTime(request.getAppointmentDate()));
    appointment.setSlotId(parseLongSafely(request.getSlotId()));
    appointment.setDoctorId(request.getDoctorId());
    appointment.setClinicId(parseIntegerSafely(request.getClinicId()));
    appointment.setActive(true); // New appointments are active by default

    return appointment;
  }

  /**
   * Converts an Appointment entity to an AppointmentResponse DTO.
   *
   * @param appointment the appointment entity
   * @return the converted AppointmentResponse DTO
   */
  public AppointmentResponse toResponse(Appointment appointment) {
    if (appointment == null) {
      return null;
    }

    AppointmentResponse response = new AppointmentResponse();
    response.setAppointmentId(appointment.getAppointmentId());
    response.setPatientId(appointment.getPatientId());

    // Handle enum types
    response.setAppointmentType(appointment.getAppointmentType());
    response.setAppointmentFor(appointment.getAppointmentFor());
    response.setSymptom(appointment.getSymptom());

    // Set other fields
    response.setAppointmentForName(appointment.getAppointmentForName());
    response.setAppointmentForAge(appointment.getAppointmentForAge());
    response.setOtherSymptoms(appointment.getOtherSymptoms());
    response.setAppointmentDate(appointment.getAppointmentDate());
    response.setSlotId(appointment.getSlotId());
    response.setDoctorId(appointment.getDoctorId());
    response.setClinicId(appointment.getClinicId());
    response.setActive(appointment.isActive());

    return response;
  }

  /**
   * Converts a LocalDate to LocalDateTime at the start of the day.
   *
   * @param date the date to convert
   * @return the converted LocalDateTime or null if input is null
   */
  private LocalDateTime convertToLocalDateTime(LocalDate date) {
    return date != null ? date.atStartOfDay() : null;
  }

  /**
   * Safely parses an age string to Integer.
   *
   * @param ageStr the age string to parse
   * @return the parsed Integer or null if parsing fails
   */
  private Integer parseAge(String ageStr) {
    try {
      return ageStr != null && !ageStr.trim().isEmpty() ? Integer.parseInt(ageStr.trim()) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Safely parses a string to Long.
   *
   * @param value the string to parse
   * @return the parsed Long or null if parsing fails or input is null
   */
  private Long parseLongSafely(String value) {
    try {
      return value != null && !value.trim().isEmpty() ? Long.parseLong(value.trim()) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Safely parses a string to Integer.
   *
   * @param value the string to parse
   * @return the parsed Integer or null if parsing fails or input is null
   */
  private Integer parseIntegerSafely(String value) {
    try {
      return value != null && !value.trim().isEmpty() ? Integer.parseInt(value.trim()) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
