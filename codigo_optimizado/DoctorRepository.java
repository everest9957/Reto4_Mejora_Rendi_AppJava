package com.centromedico.domain.repository;

import com.centromedico.domain.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de doctores.
 *
 * OPTIMIZACIONES Reto 4:
 *  - findAllWithSpecialty(): JOIN FETCH para evitar LazyInitializationException
 *  - searchByNameWithSpecialty(): JOIN FETCH en busquedas
 *  - findBySpecialtyIdWithSpecialty(): JOIN FETCH
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    List<Doctor> findByActiveTrue();

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> searchByName(@Param("name") String name);

    @Query("SELECT d FROM Doctor d WHERE d.specialty.id = :specialtyId")
    List<Doctor> findBySpecialtyId(@Param("specialtyId") Long specialtyId);

    @Query("SELECT d FROM Doctor d ORDER BY d.lastName, d.firstName")
    List<Doctor> findAllOrderedByName();

    /**
     * OPTIMIZACION Reto 4: JOIN FETCH para traer specialty en la misma query.
     * Soluciona LazyInitializationException al serializar JSON.
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.specialty")
    List<Doctor> findAllWithSpecialty();

    /**
     * OPTIMIZACION Reto 4: JOIN FETCH con busqueda
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.specialty " +
           "WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> searchByNameWithSpecialty(@Param("name") String name);

    /**
     * OPTIMIZACION Reto 4: JOIN FETCH por specialty
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.specialty " +
           "WHERE d.specialty.id = :specialtyId")
    List<Doctor> findBySpecialtyIdWithSpecialty(@Param("specialtyId") Long specialtyId);

    /**
     * OPTIMIZACION Reto 4: JOIN FETCH para activos
     */
    @Query("SELECT d FROM Doctor d JOIN FETCH d.specialty WHERE d.active = true")
    List<Doctor> findActiveWithSpecialty();
}