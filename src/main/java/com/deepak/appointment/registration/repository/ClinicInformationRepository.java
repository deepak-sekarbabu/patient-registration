package com.deepak.appointment.registration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.deepak.appointment.registration.dto.ClinicBasicInfoDTO;
import com.deepak.appointment.registration.model.ClinicInformation;

@Repository
public interface ClinicInformationRepository extends JpaRepository<ClinicInformation, Integer> {
  @Query("SELECT new com.deepak.appointment.registration.dto.ClinicBasicInfoDTO(c.clinicId, c.clinicName) FROM ClinicInformation c")
  List<ClinicBasicInfoDTO> findAllBasicInfo();
}
