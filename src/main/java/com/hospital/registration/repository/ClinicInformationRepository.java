package com.hospital.registration.repository;

import com.hospital.registration.dto.ClinicBasicInfoDTO;
import com.hospital.registration.model.ClinicInformation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicInformationRepository extends JpaRepository<ClinicInformation, Integer> {
  @Query(
      "SELECT new com.hospital.registration.dto.ClinicBasicInfoDTO(c.clinicId, c.clinicName) FROM ClinicInformation c")
  List<ClinicBasicInfoDTO> findAllBasicInfo();
}
