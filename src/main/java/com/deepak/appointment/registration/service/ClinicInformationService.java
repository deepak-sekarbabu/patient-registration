package com.deepak.appointment.registration.service;

import com.deepak.appointment.registration.dto.ClinicBasicInfoDTO;
import com.deepak.appointment.registration.model.ClinicInformation;
import com.deepak.appointment.registration.repository.ClinicInformationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ClinicInformationService {

  private final ClinicInformationRepository clinicInformationRepository;

  @Autowired
  public ClinicInformationService(ClinicInformationRepository clinicInformationRepository) {
    this.clinicInformationRepository = clinicInformationRepository;
  }

  @Cacheable(value = "clinics", unless = "#result == null || #result.isEmpty()")
  public List<ClinicInformation> getAllClinics() {
    return clinicInformationRepository.findAll();
  }

  @Cacheable(value = "clinic", key = "#id", unless = "#result == null")
  public ClinicInformation getClinicById(Integer id) {
    return clinicInformationRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Clinic not found with id: " + id));
  }

  @Cacheable(value = "clinicBasicInfo", unless = "#result == null || #result.isEmpty()")
  public List<ClinicBasicInfoDTO> getBasicClinicInfo() {
    return clinicInformationRepository.findAllBasicInfo();
  }
}
