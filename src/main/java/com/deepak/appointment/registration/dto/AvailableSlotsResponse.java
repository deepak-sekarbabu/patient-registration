package com.deepak.appointment.registration.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableSlotsResponse {
  private Integer clinicId;
  private String doctorId;
  private LocalDate date;
  private Map<String, List<Map<String, String>>> availableSlots;
}
