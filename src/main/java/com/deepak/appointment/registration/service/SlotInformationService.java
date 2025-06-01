package com.deepak.appointment.registration.service;

import com.deepak.appointment.registration.entity.SlotInformation;
import com.deepak.appointment.registration.repository.SlotInformationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SlotInformationService {

  private final SlotInformationRepository slotInformationRepository;

  public SlotInformationService(SlotInformationRepository slotInformationRepository) {
    this.slotInformationRepository = slotInformationRepository;
  }

  public List<LocalDate> getAvailableDates(Integer clinicId, String doctorId) {
    return slotInformationRepository.findAvailableDatesByClinicAndDoctor(clinicId, doctorId);
  }

  public Map<String, List<Map<String, String>>> getAvailableSlots(
      Integer clinicId, String doctorId, LocalDate date) {
    List<SlotInformation> slots =
        slotInformationRepository.findAvailableSlotsByClinicDoctorAndDate(clinicId, doctorId, date);

    // Group slots by shift time
    return slots.stream()
        .collect(
            Collectors.groupingBy(
                SlotInformation::getShiftTime,
                Collectors.mapping(
                    slot ->
                        Map.of(
                            "time", slot.getSlotTime().toString(),
                            "slotId", slot.getSlotId().toString()),
                    Collectors.toList())));
  }
}
