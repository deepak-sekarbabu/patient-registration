package com.deepak.patient.registration.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deepak.patient.registration.model.patient.Patient;
import com.deepak.patient.registration.model.patient.PersonalDetails;
import com.deepak.patient.registration.model.patient.UpdatePasswordRequest;
import com.deepak.patient.registration.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
@WebMvcTest(PatientController.class)
@Disabled
class PatientControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PatientService patientService;

  @MockBean
  private com.deepak.appointment.registration.service.ClinicInformationService
      clinicInformationService; // To prevent loading ClinicInformationController and its deps

  @MockBean
  private com.deepak.appointment.registration.controller.ClinicInformationController
      clinicInformationController; // Added to prevent its full initialization and further deps

  @MockBean
  private com.deepak.patient.registration.security.CustomUserDetailsService
      customUserDetailsService; // Standard mock for security in WebMvcTest

  @MockBean
  private com.deepak.patient.registration.controller.SessionController
      sessionController; // Prophylactic mock

  @Autowired private ObjectMapper objectMapper;

  private Patient patient;
  private Patient patientInput;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setId(1L);
    patient.setPhoneNumber("+911234567890");
    PersonalDetails pd = new PersonalDetails();
    pd.setName("Test");
    pd.setEmail("test@example.com");
    patient.setPersonalDetails(pd);
    patient.setUpdatedAt(LocalDateTime.now());
    patient.setUsingDefaultPassword(false);

    patientInput = new Patient();
    patientInput.setPhoneNumber("+911234567890");
    PersonalDetails pdInput = new PersonalDetails();
    pdInput.setName("Test");
    pdInput.setEmail("test@example.com");
    patientInput.setPersonalDetails(pdInput);
  }

  @Test
  @WithMockUser // Added for consistency / potential security
  void createPatient_shouldReturnCreatedPatient_OnSuccess() throws Exception {
    when(patientService.createPatient(any(Patient.class))).thenReturn(patient);

    mockMvc
        .perform(
            post("/v1/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientInput)))
        .andExpect(status().isOk()) // Controller returns OK for creation
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.phoneNumber", is("+911234567890")));
  }

  @Test
  @WithMockUser // Added for consistency / potential security
  void createPatient_shouldReturnInternalServerError_WhenServiceThrowsIllegalArgumentException()
      throws Exception {
    when(patientService.createPatient(any(Patient.class)))
        .thenThrow(new IllegalArgumentException("Invalid patient data"));

    mockMvc
        .perform(
            post("/v1/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Patient()))) // Send some data
        .andExpect(
            status().isInternalServerError()) // Default mapping for unhandled RuntimeExceptions
        .andExpect(content().string(containsString("Invalid patient data")));
  }

  @Test
  @WithMockUser // Added for consistency / potential security
  void getPatientByPhoneNumber_shouldReturnPatient_WhenFound() throws Exception {
    when(patientService.getPatientByPhoneNumber("+911234567890")).thenReturn(patient);

    mockMvc
        .perform(get("/v1/api/patients/by-phone").param("phoneNumber", "+911234567890"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phoneNumber", is("+911234567890")));
  }

  @Test
  @WithMockUser // Added for consistency / potential security
  void getPatientByPhoneNumber_shouldReturnNotFound_WhenMissing() throws Exception {
    when(patientService.getPatientByPhoneNumber("unknown")).thenReturn(null);

    mockMvc
        .perform(get("/v1/api/patients/by-phone").param("phoneNumber", "unknown"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser // Added for consistency / potential security
  void existsByPhoneNumber_shouldReturnTrue_WhenPatientExists() throws Exception {
    when(patientService.existsByPhoneNumber("+911234567890")).thenReturn(true);
    mockMvc
        .perform(get("/v1/api/patients/exists-by-phone").param("phoneNumber", "+911234567890"))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  @WithMockUser // Added for consistency / potential security
  void existsByPhoneNumber_shouldReturnFalse_WhenPatientDoesNotExist() throws Exception {
    when(patientService.existsByPhoneNumber("unknown")).thenReturn(false);
    mockMvc
        .perform(get("/v1/api/patients/exists-by-phone").param("phoneNumber", "unknown"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  // --- Tests requiring Authentication ---

  @Test
  @WithMockUser(username = "1") // Mock authenticated user with ID "1"
  void getPatientById_shouldReturnPatient_WhenIdMatchesAuthenticatedUser() throws Exception {
    when(patientService.getPatientById(1L)).thenReturn(patient);

    mockMvc
        .perform(get("/v1/api/patients/by-id").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  @WithMockUser(username = "2") // Authenticated user ID "2"
  void getPatientById_shouldReturnForbidden_WhenIdDoesNotMatchAuthenticatedUser() throws Exception {
    // No need to mock patientService.getPatientById for this specific test,
    // as the controller's security check should happen first.
    mockMvc
        .perform(get("/v1/api/patients/by-id").param("id", "1")) // Requesting patient 1 as user 2
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "1")
  void getPatientById_shouldReturnNotFound_WhenPatientMissingAndAuthorized() throws Exception {
    when(patientService.getPatientById(1L)).thenReturn(null);
    mockMvc
        .perform(get("/v1/api/patients/by-id").param("id", "1"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "1")
  void updatePatient_shouldUpdateAndReturnPatient_WhenAuthorized() throws Exception {
    Patient updatedPatientDetails = new Patient();
    PersonalDetails pdUpdated = new PersonalDetails();
    pdUpdated.setName("UpdatedFirstName");
    updatedPatientDetails.setPersonalDetails(pdUpdated);

    Patient resultPatient = new Patient(); // What the service returns
    resultPatient.setId(1L);
    resultPatient.setPersonalDetails(pdUpdated);

    when(patientService.updatePatient(eq(1L), any(Patient.class))).thenReturn(resultPatient);

    mockMvc
        .perform(
            put("/v1/api/patients/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPatientDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.personalDetails.firstName", is("UpdatedFirstName")));
  }

  @Test
  @WithMockUser(username = "1")
  void updatePatient_shouldReturnBadRequest_WhenRequestBodyNull() throws Exception {
    mockMvc
        .perform(
            put("/v1/api/patients/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null))) // Sending explicit null object
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Patient data cannot be null")));
  }

  @Test
  @WithMockUser(username = "2")
  void updatePatient_shouldReturnForbidden_WhenNotAuthorized() throws Exception {
    Patient updates = new Patient();
    mockMvc
        .perform(
            put("/v1/api/patients/1") // Trying to update patient 1 as user 2
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "1")
  void updatePatient_shouldReturnNotFound_WhenPatientMissingAndAuthorized() throws Exception {
    Patient updates = new Patient();
    updates.setId(1L); // This ID is part of the updates DTO but path param is primary
    when(patientService.updatePatient(eq(1L), any(Patient.class)))
        .thenReturn(null); // Service indicates not found

    mockMvc
        .perform(
            put("/v1/api/patients/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "1")
  void deletePatientById_shouldReturnNoContent_WhenAuthorizedAndSuccessful() throws Exception {
    doNothing().when(patientService).deletePatient(1L);
    mockMvc.perform(delete("/v1/api/patients/1").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "2")
  void deletePatientById_shouldReturnForbidden_WhenNotAuthorized() throws Exception {
    mockMvc
        .perform(delete("/v1/api/patients/1").with(csrf())) // User 2 trying to delete patient 1
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "1")
  void deletePatientById_shouldReturnNotFound_WhenPatientMissingAndAuthorized() throws Exception {
    // Controller catches RuntimeException from service and maps to NotFound
    doThrow(new RuntimeException("Patient not found with id: 1"))
        .when(patientService)
        .deletePatient(1L);
    mockMvc
        .perform(delete("/v1/api/patients/1").with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Patient not found with id: 1")));
  }

  @Test
  @WithMockUser(username = "1")
  void updatePassword_shouldReturnOk_WhenAuthorizedAndSuccessful() throws Exception {
    UpdatePasswordRequest request = new UpdatePasswordRequest();
    request.setNewPassword("newPass123");
    doNothing().when(patientService).updatePassword(1L, "newPass123");

    mockMvc
        .perform(
            post("/v1/api/patients/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("Password updated successfully."));
  }

  @Test
  @WithMockUser(username = "2")
  void updatePassword_shouldReturnForbidden_WhenNotAuthorized() throws Exception {
    UpdatePasswordRequest request = new UpdatePasswordRequest();
    request.setNewPassword("newPass123");
    mockMvc
        .perform(
            post("/v1/api/patients/1/password") // User 2 trying for patient 1
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "1")
  void updatePassword_shouldReturnNotFound_WhenPatientNotFound() throws Exception {
    UpdatePasswordRequest request = new UpdatePasswordRequest();
    request.setNewPassword("newPass123");
    doThrow(new RuntimeException("Patient not found with id: 1"))
        .when(patientService)
        .updatePassword(1L, "newPass123");

    mockMvc
        .perform(
            post("/v1/api/patients/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Patient not found with id: 1")));
  }

  @Test
  @WithMockUser(username = "1")
  void updatePassword_shouldReturnBadRequest_WhenPasswordRequestIsInvalid() throws Exception {
    UpdatePasswordRequest request = new UpdatePasswordRequest(); // newPassword is null
    // Controller's direct check for null or empty newPassword
    mockMvc
        .perform(
            post("/v1/api/patients/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("New password cannot be null or empty.")));
  }

  @Test
  @WithMockUser(username = "1")
  void updatePassword_shouldReturnBadRequest_WhenPasswordInRequestIsEmpty() throws Exception {
    UpdatePasswordRequest request = new UpdatePasswordRequest();
    request.setNewPassword(""); // Empty password
    mockMvc
        .perform(
            post("/v1/api/patients/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("New password cannot be null or empty.")));
  }
}
