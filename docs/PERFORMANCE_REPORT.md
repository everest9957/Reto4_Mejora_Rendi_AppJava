# Informe de Rendimiento - Reto 4
## Optimización de consultas en Hibernate

**Proyecto**: Centro Médico Software
**Fecha**: 2026-10-06
**Autor**: Judit Giravent
**Stack**: Spring Boot 3.2 + Hibernate 6.4 + PostgreSQL 14

---

## 1. Resumen ejecutivo

Se optimizó el rendimiento de una API REST de gestión médica con Hibernate.
Se aplicaron 3 optimizaciones principales:
1. **Índices de BD** (24 índices nuevos)
2. **JOIN FETCH + @BatchSize** (elimina N+1)
3. **Paginación** (reduce respuesta de 30.000 a 20 registros)

### Resultados clave

| Métrica | Antes | Después | Mejora |
|---|---|---|---|
| Tiempo `/api/appointments` | 6.856 ms | **52 ms** | **131x** |
| Tiempo `/api/patients` | 2.120 ms | **417 ms** | **5,1x** |
| Tiempo `/api/doctors` | 1.823 ms | **388 ms** | **4,7x** |
| JDBC statements por petición | 5.101 | **1** | **5.100x** |
| Plan de ejecución | Seq Scan | **Index Scan** | ✅ |

---

## 2. Baseline (estado inicial)

### 2.1 Datos cargados

| Tabla | Registros |
|---|---|
| medical_data.patients | 5.000 |
| medical_data.appointments | 30.000 |
| medical_data.doctors | 100 |
| medical_data.medical_records | 10.000 |
| medical_data.specialties | 20 |

### 2.2 Tiempos de respuesta (ANTES)

| Endpoint | Filas | Tiempo |
|---|---|---|
| GET /api/doctors | 100 | 1.823 ms |
| GET /api/patients | 5.000 | 2.120 ms |
| GET /api/appointments | 30.000 | 6.856 ms |

### 2.3 Hibernate Statistics (baseline)
Session Metrics {
779.718 ns spent acquiring 1 JDBC connection
28.851.519 ns spent preparing 5.101 JDBC statements
849.779.706 ns spent executing 5.101 JDBC statements
}

text

### 2.4 EXPLAIN ANALYZE (baseline)

**Query 1: WHERE document_number = 'DOC00000001'**
Seq Scan on patients (cost=0.00..180.50 rows=1 width=173)
Filter: ((document_number)::text = 'DOC00000001'::text)
Rows Removed by Filter: 4999
Execution Time: 0.532 ms

text

**Query 2: WHERE appointment_date > '2024-01-01'**
Seq Scan on appointments
Rows Removed by Filter: 15035
Execution Time: 2.557 ms

text

**Query 3: WHERE doctor_id = 5**
Seq Scan on appointments
Rows Removed by Filter: 29700
Execution Time: 1.290 ms

text

### 2.5 Problemas identificados

1. **N+1 queries** por `@ManyToOne EAGER` en Appointment, Doctor
2. **Sin índices** en columnas filtradas
3. **Sin paginación** en endpoints masivos
4. **Sin caché L2** en catálogos
5. **Referencia circular JSON** (Patient ↔ Appointment)

---

## 3. Optimizaciones aplicadas

### 3.1 Índices de BD (V3__add_indexes.sql)

Se crearon **24 índices**:
- `idx_patients_document_number`
- `idx_patients_email`
- `idx_patients_last_name`
- `idx_patients_first_name_lower` (funcional)
- `idx_patients_last_name_lower` (funcional)
- `idx_doctors_specialty_id`
- `idx_appointments_patient_id`
- `idx_appointments_doctor_id`
- `idx_appointments_date`
- `idx_appointments_status_date` (compuesto)
- Y 14 más...

### 3.2 JOIN FETCH + @BatchSize

**AppointmentRepository**:
```java
@Query("SELECT a FROM Appointment a " +
       "JOIN FETCH a.patient p " +
       "JOIN FETCH a.doctor d " +
       "JOIN FETCH d.specialty s")
List<Appointment> findAllWithDetails();
Patient.java:

java
@OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
@BatchSize(size = 20)
@JsonIgnore
private List<Appointment> appointments = new ArrayList<>();
Doctor.java:

java
@ManyToOne(fetch = FetchType.LAZY)  // antes EAGER
private Specialty specialty;

@OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
@BatchSize(size = 20)
@JsonIgnore
private List<Appointment> appointments = new ArrayList<>();
3.3 Paginación
AppointmentController:

java
@GetMapping
public ResponseEntity<Page<Appointment>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "appointmentDate,desc") String[] sort) {
    Pageable pageable = PageRequest.of(page, Math.min(size, 100),
            Sort.by(parseSort(sort)));
    return ResponseEntity.ok(appointmentService.findAllPaged(pageable));
}
4. Resultados (DESPUÉS)
4.1 Tiempos de respuesta
Endpoint	Baseline	Después	Mejora
GET /api/doctors	1.823 ms	388 ms	4,7x
GET /api/patients	2.120 ms	417 ms	5,1x
GET /api/appointments?size=20	6.856 ms	52 ms	131x
GET /api/appointments?size=100	—	48 ms	—
GET /api/appointments/all	6.856 ms	3.075 ms	2,2x
4.2 Hibernate Statistics (después)
text
Session Metrics {
    651.113 ns    spent acquiring 1 JDBC connection
    167.188 ns    spent preparing 1 JDBC statement
    188.760.276 ns spent executing 1 JDBC statement
}
De 5.101 queries a 1 query.

4.3 EXPLAIN ANALYZE (después)
Query 1: document_number

text
Index Scan using idx_patients_document_number on patients
  Index Cond: ((document_number)::text = 'DOC00000001'::text)
  Execution Time: 0.076 ms   ← 7x más rápido
Query 2: appointment_date

text
Index Scan using idx_appointments_date on appointments
  Index Cond: (appointment_date > '2024-01-01 00:00:00+00')
  Execution Time: 0.195 ms   ← 13x más rápido
Query 3: doctor_id

text
Bitmap Index Scan on idx_appointments_doctor_id
  Index Cond: (doctor_id = 5)
  Execution Time: 0.443 ms   ← 3x más rápido
5. Conclusiones
5.1 Impacto de cada optimización
Optimización	Impacto principal
Índices	Query individual 7-13x más rápida
JOIN FETCH	Elimina N+1: 5.101 → 1 query
Paginación	Reduce respuesta 30.000 → 20 registros
5.2 Lecciones aprendidas
El N+1 es el enemigo #1 de Hibernate. JOIN FETCH es la solución.

Los índices son gratis en escritura y oro en lectura. Siempre indexar FKs.

La paginación es obligatoria en endpoints que pueden devolver miles de filas.

@BatchSize es un buen complemento a JOIN FETCH para colecciones LAZY.

FetchType.LAZY por defecto en lugar de EAGER. Cargar solo lo necesario.

Hibernate Statistics es una herramienta imprescindible para detectar problemas.

5.3 Recomendaciones para producción
✅ Desactivar show-sql en producción

✅ Usar logs asíncronos

✅ Ajustar pool de conexiones (HikariCP) según carga

✅ Considerar caché L2 con Redis para catálogos

✅ Monitorizar queries lentas con LOG_QUERIES_SLOWER_THAN_MS

6. Anexos
6.1 Capturas de pantalla
Todas las capturas están en docs/capturas/:

Fase 1 (Baseline):

01_docker_compose_ps_ANTES.png

02_count_datos_ANTES.png

03_endpoint_doctors_ANTES.png

04_endpoint_patients_ANTES.png

05_endpoint_appointments_ANTES.png

06_sql_logs_ANTES.png

07_hibernate_stats_ANTES.png

08_explain_analyze_ANTES.png

Fase 2 (Optimizaciones):

09_migracion_V3_indexes.png

10a_explain_analyze_DESPUES_indices.png

10b_comparativa_ANTES_vs_DESPUES_indices.png

11_codigo_joinfetch.png

12_codigo_batchsize.png

Fase 3 (Resultados):

13_hibernate_stats_DESPUES.png

14_endpoint_appointments_DESPUES.png

15_codigo_paginacion.png

16_endpoint_appointments_paginado_DESPUES.png

6.2 Archivos de código modificados
backend/src/main/resources/db/migration/V3__add_indexes.sql

backend/src/main/java/com/centromedico/domain/repository/AppointmentRepository.java

backend/src/main/java/com/centromedico/domain/repository/DoctorRepository.java

backend/src/main/java/com/centromedico/domain/model/Patient.java

backend/src/main/java/com/centromedico/domain/model/Doctor.java

backend/src/main/java/com/centromedico/application/service/AppointmentService.java

backend/src/main/java/com/centromedico/application/service/DoctorService.java

backend/src/main/java/com/centromedico/interfaces/rest/AppointmentController.java

FIN DEL INFORME