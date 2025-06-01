package com.hospital.registration.controller;

import com.hospital.registration.dto.ClinicBasicInfoDTO;
import com.hospital.registration.model.ClinicInformation;
import com.hospital.registration.service.ClinicInformationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Clinic Information", description = "APIs for managing clinic information")
public class ClinicInformationController {

  private final ClinicInformationService clinicInformationService;

  @Autowired
  public ClinicInformationController(ClinicInformationService clinicInformationService) {
    this.clinicInformationService = clinicInformationService;
  }

  @GetMapping("/get-clinic")
  @Operation(
      summary = "Get all clinics",
      description = "Retrieves information about all registered clinics",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved clinic list",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClinicInformation.class)))
      })
  public ResponseEntity<List<ClinicInformation>> getAllClinics() {
    return ResponseEntity.ok(clinicInformationService.getAllClinics());
  }

  @GetMapping("/get-clinic/{id}")
  @Operation(
      summary = "Get clinic by ID",
      description = "Retrieves information about a specific clinic by its ID",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved clinic information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClinicInformation.class))),
        @ApiResponse(responseCode = "404", description = "Clinic not found")
      })
  public ResponseEntity<ClinicInformation> getClinicById(@PathVariable Integer id) {
    return ResponseEntity.ok(clinicInformationService.getClinicById(id));
  }

  @GetMapping("/get-clinic-basic")
  @Operation(
      summary = "Get basic clinic information",
      description = "Retrieves only clinic IDs and names",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved basic clinic information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClinicBasicInfoDTO.class)))
      })
  public ResponseEntity<List<ClinicBasicInfoDTO>> getBasicClinicInfo() {
    return ResponseEntity.ok(clinicInformationService.getBasicClinicInfo());
  }
}
