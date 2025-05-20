package com.deepak.registration.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deepak.registration.model.patient.Patient;
import com.deepak.registration.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Patients", description = "Operations related to patient registration and management")
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @Operation(summary = "Create a new patient", description = "Registers a new patient in the system and returns the saved patient details.", requestBody = @RequestBody(required = true, description = "Patient object to be created", content = @Content(schema = @Schema(implementation = Patient.class))), responses = {
            @ApiResponse(responseCode = "200", description = "Patient created successfully", content = @Content(schema = @Schema(implementation = Patient.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Patient> createPatient(@org.springframework.web.bind.annotation.RequestBody Patient patient) {
        Patient savedPatient = patientService.createPatient(patient);
        return ResponseEntity.ok(savedPatient);
    }

    @Operation(summary = "Get patient by phone number", description = "Retrieves patient information using the provided phone number.", parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "phoneNumber", description = "Phone number of the patient", required = true, example = "+919789801844")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Patient found", content = @Content(schema = @Schema(implementation = Patient.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
    })
    @GetMapping("/by-phone")
    public ResponseEntity<Patient> getPatientByPhoneNumber(@RequestParam String phoneNumber) {
        Patient patient = patientService.getPatientByPhoneNumber(phoneNumber);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patient);
    }
}