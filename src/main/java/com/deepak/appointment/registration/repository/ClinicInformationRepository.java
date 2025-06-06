package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.dto.ClinicInfoDropDown;
import com.deepak.appointment.registration.model.ClinicInformation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicInformationRepository extends JpaRepository<ClinicInformation, Integer> {
  @Query(
      "SELECT new com.deepak.appointment.registration.dto.ClinicInfoDropDown(c.clinicId, c.clinicName) FROM ClinicInformation c")
  List<ClinicInfoDropDown> findAllBasicInfo();
}
