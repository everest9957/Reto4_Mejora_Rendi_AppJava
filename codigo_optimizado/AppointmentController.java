package com.centromedico.interfaces.rest;

import com.centromedico.application.service.AppointmentService;
import com.centromedico.domain.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controlador REST de citas.
 *
 * OPTIMIZACIONES Reto 4:
 *  - GET /api/appointments ahora usa PAGINACION (page, size, sort)
 *  - Reduce drásticamente el tiempo de respuesta
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * OPTIMIZADO: paginacion para no devolver 30.000 citas de golpe.
     * Uso: GET /api/appointments?page=0&size=20&sort=appointmentDate,desc
     */
    @GetMapping
    public ResponseEntity<Page<Appointment>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appointmentDate,desc") String[] sort) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(parseSort(sort)));
        return ResponseEntity.ok(appointmentService.findAllPaged(pageable));
    }

    private Sort.Order parseSort(String[] sort) {
        String field = sort[0].split(",")[0];
        String direction = sort[0].split(",").length > 1 ? sort[0].split(",")[1] : "asc";
        return new Sort.Order(
                direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                field);
    }

    /**
     * Endpoint antiguo SIN paginacion (mantenido por compatibilidad).
     * ⚠️ USAR SOLO PARA PRUEBAS DE ESTRES
     */
    @GetMapping("/all")
    public ResponseEntity<List<Appointment>> findAllWithoutPagination() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> findById(@PathVariable Long id) {
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Appointment>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(appointmentService.findByStatus(status));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Appointment>> findUpcoming() {
        return ResponseEntity.ok(appointmentService.findUpcoming(OffsetDateTime.now()));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> findByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.findByDoctorId(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.findByPatientId(patientId));
    }

    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.save(appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}