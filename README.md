# 🏥 Centro Médico Software — Reto 4
## Optimización de consultas en Hibernate

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![Hibernate](https://img.shields.io/badge/Hibernate-6.4-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![License](https://img.shields.io/badge/License-MIT-yellow)

> **Reto 4 del curso Código Samurái** — Análisis y optimización de consultas Hibernate en una API REST de gestión médica.

---

## 🎯 Objetivo del reto

Optimizar el rendimiento de una aplicación Java + Hibernate existente, aplicando técnicas de:
- Análisis de consultas (Hibernate Statistics, EXPLAIN ANALYZE)
- Optimización de queries (índices, JOIN FETCH, @BatchSize)
- Paginación y proyecciones
- Pruebas de rendimiento antes/después

---

## 📊 Resultados destacados

| Endpoint | Antes | Después | Mejora |
|---|---|---|---|
| `GET /api/appointments?size=20` | **6.856 ms** | **52 ms** | **🚀 131x** |
| `GET /api/patients` | 2.120 ms | 417 ms | **5,1x** |
| `GET /api/doctors` | 1.823 ms | 388 ms | **4,7x** |
| JDBC statements (`/appointments`) | **5.101** | **1** | **5.100x** |
| Plan de ejecución | `Seq Scan` | `Index Scan` | ✅ |

### 📈 Gráfico de mejora
Baseline: ████████████████████████████████████████ 6.856 ms
Con índices: ████████████████████████ 4.272 ms
Con FETCH: ██████████████████████████████████ 6.201 ms
Con paginación: ██ 52 ms ← ¡131x más rápido!

text

---

## 🛠️ Stack tecnológico

| Componente | Versión | Función |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 3.2.0 | Framework |
| Spring Data JPA | 3.2.0 | Persistencia |
| Hibernate | 6.4 | ORM |
| PostgreSQL | 14 | Base de datos |
| Flyway | 9.22 | Migraciones |
| Docker Compose | 2.39 | Orquestación |
| Lombok | Latest | Reducción boilerplate |

---

## 📁 Estructura del proyecto
centro-medico-software/
├── backend/ # API REST Spring Boot
│ ├── src/main/
│ │ ├── java/com/centromedico/
│ │ │ ├── domain/ # Entidades + Repositorios
│ │ │ ├── application/ # Servicios + DTOs
│ │ │ ├── interfaces/ # Controladores REST
│ │ │ └── infraestructure/ # Configuración
│ │ └── resources/
│ │ ├── application.yaml
│ │ ├── application-dev.yaml
│ │ └── db/migration/ # Flyway V1, V2, V3
│ ├── Dockerfile
│ └── pom.xml
├── database/
│ └── init/
│ └── 01_init_database.sql
├── docs/
│ ├── PERFORMANCE_REPORT.md # Informe de rendimiento
│ └── capturas/ # 16 capturas
│ ├── 01_fase_inicial_ANTES/
│ ├── 02_optimizaciones/
│ └── 03_fase_final_DESPUES/
├── docker-compose.yaml
└── README.md

text

---

## 🚀 Cómo ejecutar

### Requisitos previos

- Docker Desktop 4.x+
- Docker Compose 2.x+
- 4 GB RAM libre
- Puertos libres: 5433, 8080, 5050, 6379

### Arrancar la aplicación

```
# 1. Clonar el repo
git clone https://github.com/tu-usuario/centro-medico-software.git
cd centro-medico-software

# 2. Levantar los contenedores
docker compose up -d postgres backend

# 3. Esperar a que Flyway cargue los datos (~60 seg)
docker compose logs -f backend

# 4. Probar los endpoints
curl http://localhost:8080/api/doctors
curl "http://localhost:8080/api/appointments?page=0&size=20"
curl http://localhost:8080/api/patients
Verificar datos cargados
bash
docker exec medical-center-db psql -U medical_user -d medical_center -c "
SELECT 'pacientes' AS tabla, COUNT(*) FROM medical_data.patients
UNION ALL
SELECT 'citas', COUNT(*) FROM medical_data.appointments
UNION ALL
SELECT 'doctores', COUNT(*) FROM medical_data.doctors;
"
Acceder a pgAdmin
URL: http://localhost:5050

Email: admin@centromedico.com

Password: admin123

🔬 Optimizaciones aplicadas
1. Índices de BD (V3__add_indexes.sql)
24 índices nuevos:

idx_patients_document_number

idx_patients_email

idx_patients_last_name

idx_patients_first_name_lower (funcional)

idx_doctors_specialty_id

idx_appointments_patient_id

idx_appointments_doctor_id

idx_appointments_date

idx_appointments_status_date (compuesto)

Y 15 más...

2. JOIN FETCH (elimina N+1)
java
@Query("SELECT a FROM Appointment a " +
       "JOIN FETCH a.patient p " +
       "JOIN FETCH a.doctor d " +
       "JOIN FETCH d.specialty s")
List<Appointment> findAllWithDetails();
3. @BatchSize + LAZY
java
@OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
@BatchSize(size = 20)
@JsonIgnore
private List<Appointment> appointments = new ArrayList<>();
4. Paginación
java
@GetMapping
public ResponseEntity<Page<Appointment>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, Math.min(size, 100));
    return ResponseEntity.ok(appointmentService.findAllPaged(pageable));
}
📸 Documentación visual
Fase 1: Baseline (ANTES)
Captura	Descripción
https://docs/capturas/01_fase_inicial_ANTES/01_docker_compose_ps_ANTES.png	Estado inicial
https://docs/capturas/01_fase_inicial_ANTES/02_count_datos_ANTES.png	Datos cargados
https://docs/capturas/01_fase_inicial_ANTES/03_endpoint_doctors_ANTES.png	Endpoint lento
https://docs/capturas/01_fase_inicial_ANTES/04_endpoint_patients_ANTES.png	Endpoint lento
https://docs/capturas/01_fase_inicial_ANTES/05_endpoint_appointments_ANTES.png	Endpoint crítico
https://docs/capturas/01_fase_inicial_ANTES/07_hibernate_stats_ANTES.png	5.101 queries
Fase 2: Optimizaciones
Captura	Descripción
https://docs/capturas/02_optimizaciones/09_migracion_V3_indexes.png	Índices
https://docs/capturas/02_optimizaciones/10b_comparativa_ANTES_vs_DESPUES_indices.png	Mejora con índices
https://docs/capturas/02_optimizaciones/11_codigo_joinfetch.png	JOIN FETCH
https://docs/capturas/02_optimizaciones/12_codigo_batchsize.png	@BatchSize
Fase 3: Resultados
Captura	Descripción
https://docs/capturas/03_fase_final_DESPUES/13_hibernate_stats_DESPUES.png	1 query
https://docs/capturas/03_fase_final_DESPUES/14_endpoint_appointments_DESPUES.png	Endpoint optimizado
https://docs/capturas/03_fase_final_DESPUES/16_endpoint_appointments_paginado_DESPUES.png	52 ms
📄 Informe de rendimiento
El informe completo está en docs/PERFORMANCE_REPORT.md.

Incluye:

Análisis de baseline

EXPLAIN ANALYZE de queries lentas

Hibernate Statistics antes/después

Código de las optimizaciones

Conclusiones y recomendaciones

🎓 Lecciones aprendidas
El N+1 es el enemigo #1 de Hibernate. JOIN FETCH es la solución.

Los índices son gratis en escritura y oro en lectura. Indexar FKs siempre.

La paginación es obligatoria en endpoints con miles de filas.

@BatchSize complementa a JOIN FETCH para colecciones LAZY.

FetchType.LAZY por defecto en lugar de EAGER.

Hibernate Statistics es imprescindible para detectar problemas.

👤 Autor
Judit Giravent

GitHub: @everest9957


📜 Licencia
MIT © 2026 Judit Giravent

🙏 Agradecimientos
Curso Código Samurái por el reto

Spring Boot, Hibernate y PostgreSQL communities

