package com.centromedico.domain.repository;

import com.centromedico.domain.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repositorio de citas.
 *
 * OPTIMIZACIONES Reto 4:
 *  - findAllWithDetails(): JOIN FETCH para evitar N+1
 *  - findUpcomingWithDetails(): JOIN FETCH en proximas citas
 *  - findByStatusWithDetails(): ya tenia JOIN FETCH
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDateBetween(OffsetDateTime from, OffsetDateTime to);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByStatus(String status);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate >= :from ORDER BY a.appointmentDate")
    List<Appointment> findUpcoming(@Param("from") OffsetDateTime from);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p " +
           "JOIN FETCH a.doctor d " +
           "WHERE a.status = :status")
    List<Appointment> findByStatusWithDetails(@Param("status") String status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * OPTIMIZACION Reto 4: JOIN FETCH para evitar N+1
     * Carga las citas con patient, doctor y specialty en UNA sola query.
     * Antes: 1 + N + N + N queries (5101 en el baseline)
     * Despues: 1 query
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p " +
           "JOIN FETCH a.doctor d " +
           "JOIN FETCH d.specialty s")
    List<Appointment> findAllWithDetails();

    /**
     * OPTIMIZACION Reto 4: JOIN FETCH para proximas citas
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p " +
           "JOIN FETCH a.doctor d " +
           "WHERE a.appointmentDate >= :from " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingWithDetails(@Param("from") OffsetDateTime from);

    
/**
 * OPTIMIZADO: version paginada del findAllWithDetails.
 * Usa JOIN FETCH pero con paginacion para no cargar 30.000 citas.
 */
@Query(value = "SELECT a FROM Appointment a " +
               "JOIN FETCH a.patient p " +
               "JOIN FETCH a.doctor d " +
               "JOIN FETCH d.specialty s",
       countQuery = "SELECT COUNT(a) FROM Appointment a")
Page<Appointment> findAllWithDetails(Pageable pageable);
}