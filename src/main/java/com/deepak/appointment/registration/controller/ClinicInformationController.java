package com.deepak.appointment.registration.controller;

import com.deepak.appointment.registration.dto.ClinicInfoDropDown;
import com.deepak.appointment.registration.dto.DoctorInfoDropDown;
import com.deepak.appointment.registration.model.ClinicInformation;
import com.deepak.appointment.registration.service.ClinicInformationService;
import com.deepak.appointment.registration.service.DoctorInformationService;
import com.deepak.appointment.registration.service.SlotInformationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "Clinic Information", description = "APIs for managing clinic information")
public class ClinicInformationController {

  private final ClinicInformationService clinicInformationService;
  private final DoctorInformationService doctorInformationService;
  private final SlotInformationService slotInformationService;

  public ClinicInformationController(
      ClinicInformationService clinicInformationService,
      DoctorInformationService doctorInformationService,
      SlotInformationService slotInformationService) {
    this.clinicInformationService = clinicInformationService;
    this.doctorInformationService = doctorInformationService;
    this.slotInformationService = slotInformationService;
  }

  @GetMapping("/get-clinic")
  @Hidden
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
  @Hidden
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
                    schema = @Schema(implementation = ClinicInfoDropDown.class)))
      })
  public ResponseEntity<List<ClinicInfoDropDown>> getBasicClinicInfo() {
    return ResponseEntity.ok(clinicInformationService.getBasicClinicInfo());
  }

  @GetMapping("/get-clinic/{clinicId}/doctors")
  @Operation(
      summary = "Get doctors for clinic",
      description =
          "Retrieves doctor IDs and names for a specific clinic ID, intended for dropdowns",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved doctor list for clinic",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DoctorInfoDropDown.class)))
      })
  public ResponseEntity<List<DoctorInfoDropDown>> getDoctorsForClinic(
      @PathVariable Integer clinicId) {
    List<DoctorInfoDropDown> doctors = doctorInformationService.getDoctorsForClinic(clinicId);
    return ResponseEntity.ok(doctors);
  }

  @GetMapping("/clinics/{clinicId}/doctors/{doctorId}/available-dates")
  @Operation(
      summary = "Get available dates for booking",
      description =
          "Retrieves a list of dates with available slots for a specific clinic and doctor",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved available dates",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<List<LocalDate>> getAvailableDates(
      @PathVariable Integer clinicId, @PathVariable String doctorId) {
    List<LocalDate> availableDates = slotInformationService.getAvailableDates(clinicId, doctorId);
    return ResponseEntity.ok(availableDates);
  }

  @GetMapping("/clinics/{clinicId}/doctors/{doctorId}/slots")
  @Operation(
      summary = "Get available time slots",
      description = "Retrieves available time slots for a specific clinic, doctor, and date",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved available slots",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<Map<String, Object>> getAvailableSlots(
      @PathVariable Integer clinicId,
      @PathVariable String doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    Map<String, List<Map<String, String>>> slots =
        slotInformationService.getAvailableSlots(clinicId, doctorId, date);

    return ResponseEntity.ok(
        Map.of(
            "clinicId", clinicId,
            "doctorId", doctorId,
            "date", date,
            "availableSlots", slots));
  }
}
