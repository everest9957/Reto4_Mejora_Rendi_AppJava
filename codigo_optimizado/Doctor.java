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
 * Medico del centro.
 *
 * OPTIMIZACIONES Reto 4:
 *  - @ManyToOne LAZY a Specialty (antes EAGER) para no cargarla siempre
 *  - @BatchSize(20): carga las citas en lotes
 *  - @JsonIgnore: evita referencia circular
 */
@Entity
@Table(name = "doctors", schema = "medical_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    /**
     * OPTIMIZADO: LAZY en lugar de EAGER
     * Antes: cada Doctor cargaba siempre su Specialty
     * Despues: se carga bajo demanda (o con JOIN FETCH en las queries)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    /**
     * OPTIMIZADO: @BatchSize evita N+1
     */
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    @JsonIgnore
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}