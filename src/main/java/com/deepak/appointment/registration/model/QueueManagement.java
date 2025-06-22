package com.deepak.appointment.registration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;
import lombok.Data;

@Entity
@Table(name = "queue_management")
@Data
public class QueueManagement {
  @Id
  @Column(name = "queue_management_id")
  private Long queueManagementId;

  @Column(name = "appointment_id")
  private Long appointmentId;

  @Column(name = "slot_id")
  private Integer slotId;

  @Column(name = "clinic_id")
  private Integer clinicId;

  @Column(name = "doctor_id")
  private String doctorId;

  @Column(name = "initial_queue_no")
  private Integer initialQueueNo;

  @Column(name = "current_queue_no")
  private Integer currentQueueNo;

  @Column(name = "advance_paid")
  private Boolean advancePaid;

  @Column(name = "cancelled")
  private Boolean cancelled;

  @Column(name = "advance_revert_if_paid")
  private Boolean advanceRevertIfPaid;

  @Column(name = "patient_reached")
  private Boolean patientReached;

  @Column(name = "visit_status")
  private String visitStatus;

  @Column(name = "consultation_fee_paid")
  private Boolean consultationFeePaid;

  @Column(name = "consultation_fee_amount")
  private Double consultationFeeAmount;

  @Column(name = "transaction_id_advance_fee")
  private String transactionIdAdvanceFee;

  @Column(name = "transaction_id_consultation_fee")
  private String transactionIdConsultationFee;

  @Column(name = "transaction_id_advance_revert")
  private String transactionIdAdvanceRevert;

  @Column(name = "queue_date")
  private Date date;
}
