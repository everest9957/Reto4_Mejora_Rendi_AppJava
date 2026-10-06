-- ============================================================
-- V3__add_indexes.sql
-- Indices de optimizacion para el Reto 4
-- ============================================================

-- ============================================================
-- medical_data.patients
-- ============================================================
CREATE INDEX idx_patients_document_number ON medical_data.patients (document_number);
CREATE INDEX idx_patients_email ON medical_data.patients (email);
CREATE INDEX idx_patients_last_name ON medical_data.patients (last_name);
CREATE INDEX idx_patients_active ON medical_data.patients (active);
CREATE INDEX idx_patients_blood_type ON medical_data.patients (blood_type);
CREATE INDEX idx_patients_first_name_lower ON medical_data.patients (LOWER(first_name));
CREATE INDEX idx_patients_last_name_lower ON medical_data.patients (LOWER(last_name));

-- ============================================================
-- medical_data.doctors
-- ============================================================
CREATE INDEX idx_doctors_specialty_id ON medical_data.doctors (specialty_id);
CREATE INDEX idx_doctors_active ON medical_data.doctors (active);
CREATE INDEX idx_doctors_license_number ON medical_data.doctors (license_number);
CREATE INDEX idx_doctors_user_id ON medical_data.doctors (user_id);
CREATE INDEX idx_doctors_last_name_lower ON medical_data.doctors (LOWER(last_name));

-- ============================================================
-- medical_data.appointments
-- ============================================================
CREATE INDEX idx_appointments_patient_id ON medical_data.appointments (patient_id);
CREATE INDEX idx_appointments_doctor_id ON medical_data.appointments (doctor_id);
CREATE INDEX idx_appointments_date ON medical_data.appointments (appointment_date);
CREATE INDEX idx_appointments_status ON medical_data.appointments (status);
CREATE INDEX idx_appointments_status_date ON medical_data.appointments (status, appointment_date);

-- ============================================================
-- medical_data.medical_records
-- ============================================================
CREATE INDEX idx_records_patient_id ON medical_data.medical_records (patient_id);
CREATE INDEX idx_records_doctor_id ON medical_data.medical_records (doctor_id);
CREATE INDEX idx_records_record_date ON medical_data.medical_records (record_date);
CREATE INDEX idx_records_appointment_id ON medical_data.medical_records (appointment_id);

-- ============================================================
-- security.users
-- ============================================================
CREATE INDEX idx_users_username ON security.users (username);
CREATE INDEX idx_users_email ON security.users (email);
CREATE INDEX idx_users_role_id ON security.users (role_id);

-- ============================================================
-- FIN V3
-- ============================================================
