package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.dto.DoctorInfoDropDown;
import com.deepak.appointment.registration.entity.DoctorInformation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorInformationRepository extends JpaRepository<DoctorInformation, Integer> {

  @Query(
      "SELECT new com.deepak.appointment.registration.dto.DoctorInfoDropDown(d.doctorId, d.doctorName) FROM DoctorInformation d WHERE d.clinicId = ?1")
  List<DoctorInfoDropDown> findDoctorsByClinicId(Integer clinicId);
}
