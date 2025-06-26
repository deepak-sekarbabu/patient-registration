-- -------------------------------------------------------------------------------
-- -----------------------------Registration Tables-------------------------------
-- -------------------------------------------------------------------------------
-- Use database
USE QueueManagement;

-- Patient Registration Table
CREATE TABLE
    IF NOT EXISTS patients (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        phone_number CHAR(10) NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        personal_details JSON,
        medical_info JSON,
        insurance_details JSON,
        emergency_contact JSON,
        clinic_preferences JSON,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        using_default_password BOOLEAN NOT NULL DEFAULT TRUE,
        UNIQUE KEY uq_phone_number (phone_number)
    );

-- Appointments Table
CREATE TABLE
    IF NOT EXISTS appointments (
        appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        patient_id BIGINT NOT NULL,
        appointment_type VARCHAR(50) NOT NULL,
        appointment_for VARCHAR(10) NOT NULL,
        appointment_for_name VARCHAR(255) NOT NULL,
        appointment_for_age INTEGER,
        symptom VARCHAR(255),
        other_symptoms VARCHAR(255),
        appointment_date DATETIME NOT NULL,
        slot_id BIGINT,
        doctor_id VARCHAR(50) NOT NULL,
        clinic_id INTEGER NOT NULL,
        active BOOLEAN NOT NULL DEFAULT TRUE,
        CONSTRAINT patient_fk FOREIGN KEY (patient_id) REFERENCES patients (id),
        CONSTRAINT doctor_fk FOREIGN KEY (doctor_id) REFERENCES doctor_information (doctor_id),
        CONSTRAINT clinic_fk FOREIGN KEY (clinic_id) REFERENCES clinic_information (clinic_id),
        CONSTRAINT slot_fk FOREIGN KEY (slot_id) REFERENCES slot_information (slot_id),
        UNIQUE KEY uq_slot_id (slot_id)
    );

-- Queue Management Table
CREATE TABLE
    IF NOT EXISTS queue_management (
        queue_management_id INTEGER AUTO_INCREMENT PRIMARY KEY,
        appointment_id BIGINT,
        slot_id BIGINT,
        clinic_id INTEGER,
        doctor_id VARCHAR(50),
        initial_queue_no INTEGER,
        current_queue_no INTEGER,
        advance_paid BOOLEAN,
        cancelled BOOLEAN,
        deleted BOOLEAN DEFAULT FALSE,
        advance_revert_if_paid BOOLEAN,
        patient_reached BOOLEAN,
        visit_status VARCHAR(255),
        consultation_fee_paid BOOLEAN,
        consultation_fee_amount DECIMAL(10, 2),
        transaction_id_advance_fee VARCHAR(255),
        transaction_id_consultation_fee VARCHAR(255),
        transaction_id_advance_revert VARCHAR(255),
        queue_date DATE,
        FOREIGN KEY (slot_id) REFERENCES slot_information (slot_id),
        FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id),
        FOREIGN KEY (clinic_id) REFERENCES clinic_information (clinic_id),
        FOREIGN KEY (doctor_id) REFERENCES doctor_information (doctor_id)
    );

CREATE TABLE
    IF NOT EXISTS blacklisted_access_tokens (
        id BIGINT NOT NULL AUTO_INCREMENT,
        token VARCHAR(512) NOT NULL UNIQUE,
        expiry_date DATETIME(6) NULL,
        PRIMARY KEY (id)
);