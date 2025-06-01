package com.deepak.registration.controller;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.model.patient.UpdatePasswordRequest;
import com.deepak.registration.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Patients", description = "Operations related to patient registration and management")
@RestController
@RequestMapping("v1/api/patients")
public class PatientController {
  private final PatientService patientService;
  private static final org.slf4j.Logger logger =
      org.slf4j.LoggerFactory.getLogger(PatientController.class);

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  @Operation(
      summary = "Create a new patient",
      description = "Registers a new patient in the system and returns the saved patient details.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Patient object to be created",
              content = @Content(schema = @Schema(implementation = Patient.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient created successfully",
            content = @Content(schema = @Schema(implementation = Patient.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
      })
  @PostMapping
  public ResponseEntity<Patient> createPatient(
      @org.springframework.web.bind.annotation.RequestBody Patient patient) {
    logger.info("Received request: Create new patient");
    logger.info("Creating new patient with phone number: {}", patient.getPhoneNumber());
    if (patient.getUpdatedAt() == null) {
      patient.setUpdatedAt(java.time.LocalDateTime.now());
    }
    Patient savedPatient = patientService.createPatient(patient);
    logger.info("Successfully created patient with id: {}", savedPatient.getId());
    return ResponseEntity.ok(savedPatient);
  }

  @Operation(
      summary = "Get patient by phone number",
      description = "Retrieves patient information using the provided phone number.",
      parameters =
          @io.swagger.v3.oas.annotations.Parameter(
              name = "phoneNumber",
              description = "Phone number of the patient",
              required = true,
              example = "+919789801844"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient found",
            content = @Content(schema = @Schema(implementation = Patient.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
      })
  @GetMapping("/by-phone")
  public ResponseEntity<Patient> getPatientByPhoneNumber(@RequestParam String phoneNumber) {
    logger.info("Received request: Get patient by phone number: {}", phoneNumber);
    Patient patient = patientService.getPatientByPhoneNumber(phoneNumber);
    if (patient == null) {
      logger.warn("Patient not found for phone number: {}", phoneNumber);
      return ResponseEntity.notFound().build();
    }
    logger.info("Patient found for phone number: {}", phoneNumber);
    return ResponseEntity.ok(patient);
  }

  @Operation(
      summary = "Check if user exists by phone number",
      description =
          "Returns true if a patient exists with the given phone number, false otherwise.",
      parameters =
          @io.swagger.v3.oas.annotations.Parameter(
              name = "phoneNumber",
              description = "Phone number to check existence",
              required = true,
              example = "9876543210"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Existence result",
            content = @Content(schema = @Schema(implementation = Boolean.class)))
      })
  @GetMapping("/exists-by-phone")
  public ResponseEntity<Boolean> existsByPhoneNumber(@RequestParam String phoneNumber) {
    logger.info("Received request: Check if patient exists by phone number: {}", phoneNumber);
    boolean exists = patientService.existsByPhoneNumber(phoneNumber);
    logger.info("Existence check for phone number {}: {}", phoneNumber, exists);
    return ResponseEntity.ok(exists);
  }

  @Operation(
      summary = "Get patient by id",
      description = "Retrieves patient information using the provided id.",
      parameters =
          @io.swagger.v3.oas.annotations.Parameter(
              name = "id",
              description = "ID of the patient",
              required = true,
              example = "1"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient found",
            content = @Content(schema = @Schema(implementation = Patient.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
      })
  @GetMapping("/by-id")
  public ResponseEntity<Patient> getPatientById(@RequestParam Long id) {
    logger.info("Received request: Get patient by id: {}", id);
    // Get the authenticated user's ID from the security context
    Long authenticatedUserId =
        Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

    // Check if the requested ID matches the authenticated user's ID
    if (!authenticatedUserId.equals(id)) {
      logger.warn(
          "Access denied: Authenticated user {} attempted to access patient data for id: {}",
          authenticatedUserId,
          id);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Patient patient = patientService.getPatientById(id);
    if (patient == null) {
      logger.warn("Patient not found for id: {}", id);
      return ResponseEntity.notFound().build();
    }
    logger.info("Patient found for id: {}", id);
    return ResponseEntity.ok(patient);
  }

  @Operation(
      summary = "Update patient information",
      description = "Updates an existing patient's information in the system.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Updated patient object (only include fields to update)",
              content = @Content(schema = @Schema(implementation = Patient.class))),
      parameters =
          @io.swagger.v3.oas.annotations.Parameter(
              name = "id",
              description = "ID of the patient to update",
              required = true,
              example = "1"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient updated successfully",
            content = @Content(schema = @Schema(implementation = Patient.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
      })
  @PutMapping("/{id}")
  public ResponseEntity<Patient> updatePatient(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Patient object with fields to update")
          @org.springframework.web.bind.annotation.RequestBody
          @Valid
          Patient patient) {
    logger.info("Received request: Update patient with id: {}", id);
    // Get the authenticated user's ID from the security context
    Long authenticatedUserId =
        Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

    // Check if the requested ID matches the authenticated user's ID
    if (!authenticatedUserId.equals(id)) {
      logger.warn(
          "Access denied: Authenticated user {} attempted to update patient data for id: {}",
          authenticatedUserId,
          id);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    logger.debug("Received update request for patient id: {}", id);
    if (patient == null) {
      logger.warn("Update request body is null for patient id: {}", id);
      return ResponseEntity.badRequest().build();
    }
    logger.debug("Update request data: {}", patient);

    Patient updatedPatient = patientService.updatePatient(id, patient);
    if (updatedPatient == null) {
      logger.warn("Patient not found for update with id: {}", id);
      return ResponseEntity.notFound().build();
    }
    logger.info("Successfully updated patient with id: {}", id);
    logger.debug("Successfully updated patient with id: {}", id);
    return ResponseEntity.ok(updatedPatient);
  }

  @Operation(
      summary = "Delete patient by id",
      description = "Deletes a patient from the system using the provided id.",
      parameters =
          @io.swagger.v3.oas.annotations.Parameter(
              name = "id",
              description = "ID of the patient to delete",
              required = true,
              example = "1"),
      responses = {
        @ApiResponse(
            responseCode = "204",
            description = "Patient deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePatientById(@PathVariable Long id) {
    logger.info("Received request: Delete patient by id: {}", id);
    // Get the authenticated user's ID from the security context
    Long authenticatedUserId =
        Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

    // Check if the requested ID matches the authenticated user's ID
    if (!authenticatedUserId.equals(id)) {
      logger.warn(
          "Access denied: Authenticated user {} attempted to delete patient data for id: {}",
          authenticatedUserId,
          id);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    logger.info("Deleting patient with id: {}", id);
    logger.debug("Received delete request for patient id: {}", id);
    try {
      patientService.deletePatient(id);
      logger.info("Successfully deleted patient with id: {}", id);
      logger.debug("Successfully deleted patient with id: {}", id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      logger.warn("Patient not found for deletion with id: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(
      summary = "Change patient password",
      description = "Updates the password for a patient with the given id.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "New password request",
              content = @Content(schema = @Schema(implementation = UpdatePasswordRequest.class))),
      parameters =
          @io.swagger.v3.oas.annotations.Parameter(
              name = "id",
              description = "ID of the patient whose password is to be updated",
              required = true,
              example = "1"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Password updated successfully.",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
      })
  @PostMapping("/{id}/password")
  public ResponseEntity<String> updatePassword(
      @PathVariable Long id, @RequestBody UpdatePasswordRequest request) {
    logger.info("Received request: Update password for patient id: {}", id);
    // Get the authenticated user's ID from the security context
    Long authenticatedUserId =
        Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

    // Check if the requested ID matches the authenticated user's ID
    if (!authenticatedUserId.equals(id)) {
      logger.warn(
          "Access denied: Authenticated user {} attempted to update password for patient id: {}",
          authenticatedUserId,
          id);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    patientService.updatePassword(id, request.getNewPassword());
    logger.info("Password updated successfully for patient id: {}", id);
    return ResponseEntity.ok("Password updated successfully.");
  }
}
