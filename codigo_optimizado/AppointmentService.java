package com.centromedico.application.service;

import com.centromedico.domain.model.Appointment;
import com.centromedico.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de citas.
 *
 * OPTIMIZACIONES Reto 4:
 *  - findAll() usa findAllWithDetails() con JOIN FETCH
 *  - findUpcoming() usa findUpcomingWithDetails() con JOIN FETCH
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    /**
     * OPTIMIZADO: usa JOIN FETCH para evitar N+1
     * Antes: 30001 citas + N queries (5101 JDBC statements)
     * Despues: 1 query con JOINs
     */
    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findUpcoming(OffsetDateTime from) {
        return appointmentRepository.findUpcomingWithDetails(from);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteById(Long id) {
        appointmentRepository.deleteById(id);
        
    }

    /**
 * OPTIMIZADO: paginacion para no devolver 30.000 citas de golpe.
 */
@Transactional(readOnly = true)
public Page<Appointment> findAllPaged(Pageable pageable) {
    return appointmentRepository.findAllWithDetails(pageable);
     }
}