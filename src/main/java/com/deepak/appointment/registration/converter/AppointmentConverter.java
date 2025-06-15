package com.deepak.appointment.registration.converter;

import com.deepak.appointment.registration.dto.AppointmentRequest;
import com.deepak.appointment.registration.dto.AppointmentResponse;
import com.deepak.appointment.registration.entity.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class AppointmentConverter {

  public Appointment toEntity(AppointmentRequest request) {
    Appointment appointment = new Appointment();
    appointment.setPatientId(request.getPatientId());
    appointment.setAppointmentType(request.getAppointmentType());
    appointment.setAppointmentFor(request.getAppointmentFor());
    appointment.setAppointmentForName(request.getAppointmentForName());
    appointment.setAppointmentForAge(parseAge(request.getAppointmentForAge()));
    appointment.setSymptom(request.getSymptom());
    appointment.setOtherSymptoms(request.getOtherSymptoms());
    appointment.setAppointmentDate(convertToLocalDateTime(request.getAppointmentDate()));
    appointment.setSlotId(parseLongSafely(request.getSlotId()));
    appointment.setDoctorId(request.getDoctorId());
    appointment.setClinicId(Integer.parseInt(request.getClinicId()));
    return appointment;
  }

  public AppointmentResponse toResponse(Appointment appointment) {
    if (appointment == null) {
      return null;
    }

    AppointmentResponse response = new AppointmentResponse();
    response.setAppointmentId(appointment.getAppointmentId());
    response.setPatientId(appointment.getPatientId());
    response.setAppointmentType(appointment.getAppointmentType());
    response.setAppointmentFor(appointment.getAppointmentFor());
    response.setAppointmentForName(appointment.getAppointmentForName());
    response.setAppointmentForAge(appointment.getAppointmentForAge());
    response.setSymptom(appointment.getSymptom());
    response.setOtherSymptoms(appointment.getOtherSymptoms());
    response.setAppointmentDate(appointment.getAppointmentDate());
    response.setSlotId(appointment.getSlotId());
    response.setDoctorId(appointment.getDoctorId());
    response.setClinicId(appointment.getClinicId());
    response.setActive(appointment.isActive());
    return response;
  }

  private LocalDateTime convertToLocalDateTime(LocalDate date) {
    // Set default time to start of day, can be adjusted based on slot time if
    // needed
    return date != null ? date.atStartOfDay() : null;
  }

  private Integer parseAge(String ageStr) {
    try {
      return ageStr != null && !ageStr.trim().isEmpty() ? Integer.parseInt(ageStr) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Long parseLongSafely(String value) {
    try {
      return value != null ? Long.parseLong(value) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
