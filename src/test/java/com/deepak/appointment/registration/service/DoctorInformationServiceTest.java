package com.deepak.appointment.registration.service;

import com.deepak.appointment.registration.dto.DoctorInfoDropDown;
import com.deepak.appointment.registration.repository.DoctorInformationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorInformationServiceTest {

    @Mock
    private DoctorInformationRepository doctorInformationRepository;

    @InjectMocks
    private DoctorInformationService doctorInformationService;

    @Test
    void getDoctorsForClinic_shouldReturnListOfDoctors_whenDoctorsExist() {
        // Arrange
        Integer clinicId = 1;
        DoctorInfoDropDown doctor = new DoctorInfoDropDown("doc1", "Dr. Smith");
        when(doctorInformationRepository.findDoctorsByClinicId(clinicId))
            .thenReturn(Collections.singletonList(doctor));

        // Act
        List<DoctorInfoDropDown> doctors = doctorInformationService.getDoctorsForClinic(clinicId);

        // Assert
        assertNotNull(doctors);
        assertEquals(1, doctors.size());
        assertEquals("Dr. Smith", doctors.get(0).getDoctorName());
        verify(doctorInformationRepository, times(1)).findDoctorsByClinicId(clinicId);
    }

    @Test
    void getDoctorsForClinic_shouldReturnEmptyList_whenNoDoctorsFound() {
        // Arrange
        Integer clinicId = 1;
        when(doctorInformationRepository.findDoctorsByClinicId(clinicId))
            .thenReturn(Collections.emptyList());

        // Act
        List<DoctorInfoDropDown> doctors = doctorInformationService.getDoctorsForClinic(clinicId);

        // Assert
        assertNotNull(doctors);
        assertTrue(doctors.isEmpty());
        verify(doctorInformationRepository, times(1)).findDoctorsByClinicId(clinicId);
    }
}
