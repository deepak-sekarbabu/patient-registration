package com.deepak.appointment.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deepak.appointment.registration.dto.ClinicInfoDropDown;
import com.deepak.appointment.registration.model.ClinicInformation;
import com.deepak.appointment.registration.repository.ClinicInformationRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClinicInformationServiceTest {

  @Mock private ClinicInformationRepository clinicInformationRepository;

  @InjectMocks private ClinicInformationService clinicInformationService;

  private ClinicInformation clinic;

  @BeforeEach
  void setUp() {
    clinic = new ClinicInformation();
    clinic.setClinicId(1);
    clinic.setClinicName("Test Clinic");
    // Add other necessary properties if your ClinicInformation class has them
    // For example:
  }

  @Test
  void getAllClinics_shouldReturnListOfClinics() {
    // Arrange
    when(clinicInformationRepository.findAll()).thenReturn(Collections.singletonList(clinic));

    // Act
    List<ClinicInformation> clinics = clinicInformationService.getAllClinics();

    // Assert
    assertNotNull(clinics);
    assertEquals(1, clinics.size());
    assertEquals("Test Clinic", clinics.getFirst().getClinicName());
    verify(clinicInformationRepository, times(1)).findAll();
  }

  @Test
  void getAllClinics_shouldReturnEmptyListWhenNoClinics() {
    // Arrange
    when(clinicInformationRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<ClinicInformation> clinics = clinicInformationService.getAllClinics();

    // Assert
    assertNotNull(clinics);
    assertTrue(clinics.isEmpty());
    verify(clinicInformationRepository, times(1)).findAll();
  }

  @Test
  void getClinicById_shouldReturnClinic_whenClinicExists() {
    // Arrange
    when(clinicInformationRepository.findById(1)).thenReturn(Optional.of(clinic));

    // Act
    ClinicInformation foundClinic = clinicInformationService.getClinicById(1);

    // Assert
    assertNotNull(foundClinic);
    assertEquals("Test Clinic", foundClinic.getClinicName());
    verify(clinicInformationRepository, times(1)).findById(1);
  }

  @Test
  void getClinicById_shouldThrowRuntimeException_whenClinicNotFound() {
    // Arrange
    when(clinicInformationRepository.findById(1)).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> clinicInformationService.getClinicById(1));
    assertEquals("Clinic not found with id: 1", exception.getMessage());
    verify(clinicInformationRepository, times(1)).findById(1);
  }

  @Test
  void getBasicClinicInfo_shouldReturnListOfDropDowns() {
    // Arrange
    ClinicInfoDropDown dropDown = new ClinicInfoDropDown(1, "Test Clinic");
    when(clinicInformationRepository.findAllBasicInfo())
        .thenReturn(Collections.singletonList(dropDown));

    // Act
    List<ClinicInfoDropDown> basicInfo = clinicInformationService.getBasicClinicInfo();

    // Assert
    assertNotNull(basicInfo);
    assertEquals(1, basicInfo.size());
    assertEquals("Test Clinic", basicInfo.getFirst().getClinicName());
    verify(clinicInformationRepository, times(1)).findAllBasicInfo();
  }

  @Test
  void getBasicClinicInfo_shouldReturnEmptyListWhenNoBasicInfo() {
    // Arrange
    when(clinicInformationRepository.findAllBasicInfo()).thenReturn(Collections.emptyList());

    // Act
    List<ClinicInfoDropDown> basicInfo = clinicInformationService.getBasicClinicInfo();

    // Assert
    assertNotNull(basicInfo);
    assertTrue(basicInfo.isEmpty());
    verify(clinicInformationRepository, times(1)).findAllBasicInfo();
  }
}
