package com.centromedico.application.service;

import com.centromedico.domain.model.Doctor;
import com.centromedico.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de doctores.
 *
 * OPTIMIZACIONES Reto 4:
 *  - Todos los metodos usan JOIN FETCH para traer specialty
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * OPTIMIZADO: usa JOIN FETCH para evitar LazyInitializationException
     * y N+1 en specialty
     */
    @Transactional(readOnly = true)
    public List<Doctor> findAll() {
        return doctorRepository.findAllWithSpecialty();
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Doctor> findActive() {
        return doctorRepository.findActiveWithSpecialty();
    }

    @Transactional(readOnly = true)
    public List<Doctor> searchByName(String term) {
        return doctorRepository.searchByNameWithSpecialty(term);
    }

    @Transactional(readOnly = true)
    public List<Doctor> findBySpecialty(Long specialtyId) {
        return doctorRepository.findBySpecialtyIdWithSpecialty(specialtyId);
    }

    @Transactional
    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteById(Long id) {
        doctorRepository.deleteById(id);
    }
}