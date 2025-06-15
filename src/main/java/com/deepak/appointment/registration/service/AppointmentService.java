package com.deepak.appointment.registration.service;

import com.deepak.appointment.registration.converter.AppointmentConverter;
import com.deepak.appointment.registration.dto.AppointmentRequest;
import com.deepak.appointment.registration.dto.AppointmentResponse;
import com.deepak.appointment.registration.entity.Appointment;
import com.deepak.appointment.registration.exception.ConflictException;
import com.deepak.appointment.registration.exception.NotFoundException;
import com.deepak.appointment.registration.repository.AppointmentRepository;
import com.deepak.patient.registration.service.PatientService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing appointment operations. */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final AppointmentConverter appointmentConverter;
  private final PatientService patientService;

  /**
   * Creates a new appointment.
   *
   * @param request the appointment request containing appointment details
   * @return the created appointment response
   * @throws ConflictException if the requested slot is already booked
   */
  public AppointmentResponse createAppointment(AppointmentRequest request) {
    log.info(
        "Creating new appointment for patient ID: {}, slot ID: {}",
        request.getPatientId(),
        request.getSlotId());

    // Validate patient exists
    Long patientId = request.getPatientId();
    if (patientService.getPatientById(patientId) == null) {
      log.warn("Patient not found with ID: {}", patientId);
      throw new NotFoundException("Patient not found with ID: " + patientId);
    }

    // Check if slot is already booked
    Long slotId = Long.parseLong(request.getSlotId());
    log.debug("Checking availability for slot ID: {}", slotId);

    if (appointmentRepository.existsBySlotIdAndActiveTrue(slotId)) {
      log.warn("Slot already booked - slot ID: {}", slotId);
      throw new ConflictException("The selected slot is already booked");
    }

    // Convert DTO to entity and save
    log.debug("Converting request to appointment entity");
    Appointment appointment = appointmentConverter.toEntity(request);

    log.debug("Saving appointment for patient ID: {}, slot ID: {}", patientId, slotId);
    Appointment savedAppointment = appointmentRepository.save(appointment);
    log.info(
        "Successfully created appointment with ID: {} for patient ID: {}",
        savedAppointment.getAppointmentId(),
        patientId);

    // Convert saved entity back to response DTO
    return appointmentConverter.toResponse(savedAppointment);
  }

  /**
   * Retrieves all active appointments for a specific patient.
   *
   * @param patientId the ID of the patient
   * @return list of appointment responses for the patient
   * @throws NotFoundException if the patient is not found
   */
  @Transactional(readOnly = true)
  public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
    log.info("Fetching appointments for patient ID: {}", patientId);

    // Validate patient exists
    if (patientService.getPatientById(patientId) == null) {
      log.warn("Patient not found with ID: {}", patientId);
      throw new NotFoundException("Patient not found with ID: " + patientId);
    }

    // Find all active appointments for the patient
    List<Appointment> appointments = appointmentRepository.findByPatientIdAndActiveTrue(patientId);
    log.debug("Found {} active appointments for patient ID: {}", appointments.size(), patientId);

    // Convert entities to DTOs
    return appointments.stream().map(appointmentConverter::toResponse).collect(Collectors.toList());
  }
}
