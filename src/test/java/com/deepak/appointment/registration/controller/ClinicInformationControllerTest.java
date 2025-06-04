package com.deepak.appointment.registration.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deepak.appointment.registration.dto.AvailableSlotsResponse;
import com.deepak.appointment.registration.dto.ClinicInfoDropDown;
import com.deepak.appointment.registration.dto.DoctorInfoDropDown;
import com.deepak.appointment.registration.model.ClinicInformation;
import com.deepak.appointment.registration.service.ClinicInformationService;
import com.deepak.appointment.registration.service.DoctorInformationService;
import com.deepak.appointment.registration.service.SlotInformationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ClinicInformationController.class)
class ClinicInformationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired
  private ObjectMapper
      objectMapper; // For converting objects to JSON strings if needed for request bodies (not for

  // GET)

  @MockBean private ClinicInformationService clinicInformationService;

  @MockBean private DoctorInformationService doctorInformationService;

  @MockBean private SlotInformationService slotInformationService;

  @MockBean
  private com.deepak.patient.registration.security.CustomUserDetailsService
      customUserDetailsService; // Added to mock security dependency

  @MockBean
  private com.deepak.patient.registration.controller.PatientController
      patientController; // Added to prevent its full initialization

  @MockBean
  private com.deepak.patient.registration.controller.SessionController
      sessionController; // Added to prevent its full initialization

  @MockBean
  private com.deepak.patient.registration.service.PatientService
      patientService; // Added because something in the context requires it

  private ClinicInformation clinic;
  private ClinicInfoDropDown clinicInfoDropDown;
  private DoctorInfoDropDown doctorInfoDropDown;
  private LocalDate testDate;

  @BeforeEach
  void setUp() {
    clinic = new ClinicInformation();
    clinic.setClinicId(1);
    clinic.setClinicName("Test Clinic");
    clinic.setClinicAddress("123 Test Street");
    clinic.setClinicPhoneNumbers("555-1234");
    clinic.setClinicAmenities("General Checkup, Vaccinations");

    clinicInfoDropDown = new ClinicInfoDropDown(1, "Test Clinic Basic");
    doctorInfoDropDown = new DoctorInfoDropDown("doc1", "Dr. Test");
    testDate = LocalDate.of(2024, 3, 15);
  }

  @Test
  @WithMockUser
  void getAllClinics_shouldReturnListOfClinics() throws Exception {
    when(clinicInformationService.getAllClinics()).thenReturn(Collections.singletonList(clinic));

    mockMvc
        .perform(get("/v1/api/get-clinic"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].clinicName", is("Test Clinic")));
  }

  @Test
  @WithMockUser
  void getAllClinics_shouldReturnEmptyList_whenNoClinics() throws Exception {
    when(clinicInformationService.getAllClinics()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/v1/api/get-clinic"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser
  void getClinicById_shouldReturnClinic_whenClinicExists() throws Exception {
    when(clinicInformationService.getClinicById(1)).thenReturn(clinic);

    mockMvc
        .perform(get("/v1/api/get-clinic/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.clinicName", is("Test Clinic")))
        .andExpect(jsonPath("$.clinicId", is(1)));
  }

  @Test
  @WithMockUser
  void getClinicById_shouldReturnNotFound_whenClinicDoesNotExist() throws Exception {
    when(clinicInformationService.getClinicById(1))
        .thenThrow(new RuntimeException("Clinic not found with id: 1"));

    mockMvc
        .perform(get("/v1/api/get-clinic/1"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            result -> {
              Throwable resolvedException = result.getResolvedException();
              assertNotNull(resolvedException, "Expected an exception to be resolved");
              // Check the cause of the ServletException
              Throwable cause = resolvedException.getCause();
              assertTrue(
                  cause instanceof RuntimeException,
                  "Expected cause to be RuntimeException, but got: "
                      + (cause != null ? cause.getClass().getName() : "null"));
              assertEquals(
                  "Clinic not found with id: 1", cause.getMessage(), "Exception message mismatch");
            });
  }

  @Test
  @WithMockUser
  void getBasicClinicInfo_shouldReturnBasicInfo() throws Exception {
    when(clinicInformationService.getBasicClinicInfo())
        .thenReturn(Collections.singletonList(clinicInfoDropDown));

    mockMvc
        .perform(get("/v1/api/get-clinic-basic"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].clinicName", is("Test Clinic Basic")));
  }

  @Test
  @WithMockUser
  void getBasicClinicInfo_shouldReturnEmptyList_whenNoBasicInfo() throws Exception {
    when(clinicInformationService.getBasicClinicInfo()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/v1/api/get-clinic-basic"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser
  void getDoctorsForClinic_shouldReturnDoctorList() throws Exception {
    when(doctorInformationService.getDoctorsForClinic(1))
        .thenReturn(Collections.singletonList(doctorInfoDropDown));

    mockMvc
        .perform(get("/v1/api/get-clinic/1/doctors"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].doctorName", is("Dr. Test")));
  }

  @Test
  @WithMockUser
  void getDoctorsForClinic_shouldReturnEmptyList_whenNoDoctors() throws Exception {
    when(doctorInformationService.getDoctorsForClinic(1)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/v1/api/get-clinic/1/doctors"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser
  void getAvailableDates_shouldReturnDatesList() throws Exception {
    LocalDate availableDate = LocalDate.of(2024, 1, 15);
    when(slotInformationService.getAvailableDates(1, "doc1"))
        .thenReturn(Collections.singletonList(availableDate));

    mockMvc
        .perform(get("/v1/api/clinics/1/doctors/doc1/available-dates"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is("2024-01-15")));
  }

  @Test
  @WithMockUser
  void getAvailableDates_shouldReturnEmptyList_whenNoDates() throws Exception {
    when(slotInformationService.getAvailableDates(1, "doc1")).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/v1/api/clinics/1/doctors/doc1/available-dates"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser
  void getAvailableSlots_shouldReturnSlotsMap() throws Exception {
    Map<String, List<Map<String, String>>> slotsData =
        Map.of("Morning", List.of(Map.of("time", "10:00", "slotId", "1", "available", "true")));

    AvailableSlotsResponse expectedResponse =
        new AvailableSlotsResponse(1, "doc1", testDate, slotsData);

    when(slotInformationService.getAvailableSlots(1, "doc1", testDate)).thenReturn(slotsData);

    mockMvc
        .perform(get("/v1/api/clinics/1/doctors/doc1/slots").param("date", "2024-03-15"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.clinicId", is(1)))
        .andExpect(jsonPath("$.doctorId", is("doc1")))
        .andExpect(jsonPath("$.date", is("2024-03-15")))
        .andExpect(jsonPath("$.availableSlots.Morning", hasSize(1)))
        .andExpect(jsonPath("$.availableSlots.Morning[0].time", is("10:00")))
        .andExpect(jsonPath("$.availableSlots.Morning[0].slotId", is("1")));
  }

  @Test
  @WithMockUser
  void getAvailableSlots_shouldReturnEmptySlotsMap_whenNoSlots() throws Exception {
    Map<String, List<Map<String, String>>> emptySlotsData = Collections.emptyMap();
    AvailableSlotsResponse expectedResponse =
        new AvailableSlotsResponse(1, "doc1", testDate, emptySlotsData);

    when(slotInformationService.getAvailableSlots(1, "doc1", testDate)).thenReturn(emptySlotsData);

    mockMvc
        .perform(get("/v1/api/clinics/1/doctors/doc1/slots").param("date", "2024-03-15"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.clinicId", is(1)))
        .andExpect(jsonPath("$.doctorId", is("doc1")))
        .andExpect(jsonPath("$.date", is("2024-03-15")))
        .andExpect(jsonPath("$.availableSlots", anEmptyMap()));
  }

  @Test
  @WithMockUser
  void getAvailableSlots_shouldReturnBadRequest_whenDateParamIsMissing() throws Exception {
    mockMvc.perform(get("/v1/api/clinics/1/doctors/doc1/slots")).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void getAvailableSlots_shouldReturnBadRequest_whenDateParamIsInvalidFormat() throws Exception {
    mockMvc
        .perform(get("/v1/api/clinics/1/doctors/doc1/slots").param("date", "invalid-date-format"))
        .andExpect(status().isBadRequest());
  }
}
