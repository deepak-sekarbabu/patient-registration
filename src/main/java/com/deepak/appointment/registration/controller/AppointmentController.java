package com.deepak.appointment.registration.controller;

import com.deepak.appointment.registration.dto.AppointmentRequest;
import com.deepak.appointment.registration.dto.AppointmentResponse;
import com.deepak.appointment.registration.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing patient appointments. */
@Tag(name = "Appointment", description = "APIs for managing patient appointments")
@RestController
@RequestMapping(value = "v1/api/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentController {

  private final AppointmentService appointmentService;

  public AppointmentController(AppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  /**
   * Creates a new appointment for a patient.
   *
   * @param appointmentRequest The appointment details
   * @return The created appointment details
   */
  @Operation(
      summary = "Create a new appointment",
      description = "Creates a new appointment with the provided details")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Appointment created successfully",
        content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AppointmentResponse> createAppointment(
      @Valid
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Appointment details",
              required = true,
              content = @Content(schema = @Schema(implementation = AppointmentRequest.class)))
          @RequestBody
          AppointmentRequest appointmentRequest) {

    AppointmentResponse response = appointmentService.createAppointment(appointmentRequest);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Retrieves all active appointments for a specific patient.
   *
   * @param patientId the ID of the patient
   * @return list of active appointments for the patient
   */
  @Operation(
      summary = "Get appointments by patient ID",
      description = "Retrieves all active appointments for a specific patient",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved appointments",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponse.class, type = "array"))),
        @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
      })
  @GetMapping("/patient/{patientId}")
  public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatientId(
      @Parameter(description = "ID of the patient", required = true) @PathVariable Long patientId) {
    List<AppointmentResponse> appointments =
        appointmentService.getAppointmentsByPatientId(patientId);
    return ResponseEntity.ok(appointments);
  }
}
