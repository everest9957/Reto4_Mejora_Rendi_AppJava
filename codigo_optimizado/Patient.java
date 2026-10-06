package com.centromedico.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Paciente del centro medico.
 *
 * OPTIMIZACIONES Reto 4:
 *  - @BatchSize(20): carga las citas en lotes de 20 en lugar de 1 a 1
 *  - @JsonIgnore: evita referencia circular en JSON
 */
@Entity
@Table(name = "patients", schema = "medical_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long id;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "blood_type", length = 10)
    private String bloodType;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    /**
     * OPTIMIZADO: @BatchSize evita N+1
     * Antes: 1 query por cada paciente para cargar sus citas
     * Despues: 1 query por cada 20 pacientes
     * @JsonIgnore evita StackOverflowError al serializar
     */
    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    @JsonIgnore
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}