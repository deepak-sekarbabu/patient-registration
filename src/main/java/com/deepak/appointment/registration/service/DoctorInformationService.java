package com.deepak.appointment.registration.service;

import com.deepak.appointment.registration.dto.DoctorInfoDropDown;
import com.deepak.appointment.registration.repository.DoctorInformationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorInformationService {

  private final DoctorInformationRepository doctorInformationRepository;

  @Cacheable(
      value = "doctorsByClinic",
      key = "#clinicId",
      unless = "#result == null || #result.isEmpty()")
  public List<DoctorInfoDropDown> getDoctorsForClinic(Integer clinicId) {
    return doctorInformationRepository.findDoctorsByClinicId(clinicId);
  }
}
