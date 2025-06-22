package com.deepak.appointment.registration.repository;

import com.deepak.appointment.registration.model.QueueManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueManagementRepository extends JpaRepository<QueueManagement, Integer> {
  // Add custom query methods if needed
}
