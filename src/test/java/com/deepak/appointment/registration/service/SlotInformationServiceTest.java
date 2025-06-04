package com.deepak.appointment.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deepak.appointment.registration.entity.SlotInformation;
import com.deepak.appointment.registration.repository.SlotInformationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlotInformationServiceTest {

  @Mock private SlotInformationRepository slotInformationRepository;

  @InjectMocks private SlotInformationService slotInformationService;

  @Test
  void getAvailableDates_shouldReturnListOfDates_whenDatesAvailable() {
    // Arrange
    Integer clinicId = 1;
    String doctorId = "doc1";
    LocalDate date = LocalDate.now();
    when(slotInformationRepository.findAvailableDatesByClinicAndDoctor(clinicId, doctorId))
        .thenReturn(Collections.singletonList(date));

    // Act
    List<LocalDate> dates = slotInformationService.getAvailableDates(clinicId, doctorId);

    // Assert
    assertNotNull(dates);
    assertEquals(1, dates.size());
    assertEquals(date, dates.get(0));
    verify(slotInformationRepository, times(1))
        .findAvailableDatesByClinicAndDoctor(clinicId, doctorId);
  }

  @Test
  void getAvailableDates_shouldReturnEmptyList_whenNoDatesAvailable() {
    // Arrange
    Integer clinicId = 1;
    String doctorId = "doc1";
    when(slotInformationRepository.findAvailableDatesByClinicAndDoctor(clinicId, doctorId))
        .thenReturn(Collections.emptyList());

    // Act
    List<LocalDate> dates = slotInformationService.getAvailableDates(clinicId, doctorId);

    // Assert
    assertNotNull(dates);
    assertTrue(dates.isEmpty());
    verify(slotInformationRepository, times(1))
        .findAvailableDatesByClinicAndDoctor(clinicId, doctorId);
  }

  @Test
  void getAvailableSlots_shouldReturnMapOfSlots_whenSlotsAvailable() {
    // Arrange
    Integer clinicId = 1;
    String doctorId = "doc1";
    LocalDate date = LocalDate.now();
    SlotInformation slot = new SlotInformation();
    slot.setSlotId(1L);
    slot.setShiftTime("Morning");
    slot.setSlotTime(LocalTime.of(10, 0));
    slot.setIsAvailable(true); // Assuming SlotInformation has an 'isAvailable' field

    when(slotInformationRepository.findAvailableSlotsByClinicDoctorAndDate(
            clinicId, doctorId, date))
        .thenReturn(Collections.singletonList(slot));

    // Act
    Map<String, List<Map<String, String>>> slotsMap =
        slotInformationService.getAvailableSlots(clinicId, doctorId, date);

    // Assert
    assertNotNull(slotsMap);
    assertFalse(slotsMap.isEmpty());
    assertTrue(slotsMap.containsKey("Morning"));
    assertEquals(1, slotsMap.get("Morning").size());
    assertEquals("10:00", slotsMap.get("Morning").get(0).get("time"));
    assertEquals("1", slotsMap.get("Morning").get(0).get("slotId"));
    // Depending on your SlotInformationService logic, you might also want to assert the "available"
    // status
    // assertEquals("true", slotsMap.get("Morning").get(0).get("available"));
    verify(slotInformationRepository, times(1))
        .findAvailableSlotsByClinicDoctorAndDate(clinicId, doctorId, date);
  }

  @Test
  void getAvailableSlots_shouldReturnEmptyMap_whenNoSlotsAvailable() {
    // Arrange
    Integer clinicId = 1;
    String doctorId = "doc1";
    LocalDate date = LocalDate.now();
    when(slotInformationRepository.findAvailableSlotsByClinicDoctorAndDate(
            clinicId, doctorId, date))
        .thenReturn(Collections.emptyList());

    // Act
    Map<String, List<Map<String, String>>> slotsMap =
        slotInformationService.getAvailableSlots(clinicId, doctorId, date);

    // Assert
    assertNotNull(slotsMap);
    assertTrue(slotsMap.isEmpty());
    verify(slotInformationRepository, times(1))
        .findAvailableSlotsByClinicDoctorAndDate(clinicId, doctorId, date);
  }
}
