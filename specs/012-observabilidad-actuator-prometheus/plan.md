# Implementation Plan: Observabilidad del backend con Actuator y métricas Prometheus

**Branch**: `012-observabilidad-actuator-prometheus` | **Date**: 2026-11-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/012-observabilidad-actuator-prometheus/spec.md`

## Summary

Ampliar Spring Boot Actuator, ya presente desde la feature 003, para exponer
`health`, `info`, `metrics` y `prometheus` y agregar el registro Prometheus de
Micrometer. El enfoque es casi todo declarativo:

- **Dependencias**: una sola dependencia nueva y `buildInfo()` en Gradle para
  `info`.
- **`application.yml`**: el bloque `management` define la exposición, el
  acceso por defecto `none` con los cuatro endpoints en `read-only`, el detalle
  de health solo para `ROLE_ADMIN`, las probes desactivadas y el tag común
  `application`.
- **`SecurityConfig`**: una regla `/actuator/**` con `hasAuthority("ROLE_ADMIN")`
  después del `permitAll` de `/actuator/health`.
- **Clases Java**: no se crean clases de producción nuevas.
- **Test**: se agrega el único test admitido por la constitution v2.8.0,
  `ObservabilidadSeguridadTest`.

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.1 (plugin Gradle), Micrometer 1.17.1.

**Primary Dependencies**: `spring-boot-starter-actuator` (existente),
`io.micrometer:micrometer-registry-prometheus` (nueva, sin versión), Spring
Security con el JWT existente, springdoc-openapi 3.1.1 (existente).

**Storage**: N/A. No hay cambios de esquema. El pool de PostgreSQL existente
solo se observa mediante métricas `hikaricp.*` y el health `db`.

**Testing**: JUnit 5 y Mockito. `ArquitecturaCapasTest` (existente) y un único
test de integración `ObservabilidadSeguridadTest` con `@SpringBootTest`,
`@AutoConfigureMockMvc`, H2 en memoria y `@MockitoBean` para `JwtService` y
`UsuarioDetailsService` (ver [research.md](research.md#r7-test-observabilidadseguridadtest-principio-vii-excepción-v280)).

**Target Platform**: backend JVM 21 (servidor embebido) con verificación manual
desde PowerShell en Windows.

**Project Type**: aplicación web con backend REST; solo se modifica el backend.

**Performance Goals**: sin objetivos nuevos. La instrumentación por defecto de
Micrometer, sin histogramas, agrega un overhead despreciable por request.

**Constraints**:
- Solo `application.yml`, sin perfiles.
- Solo los cuatro endpoints aprobados.
- `health` es el único endpoint público y no muestra detalles.
- Sin histogramas ni percentiles.
- Sin clases Java nuevas en `main`.
- Español técnico e identificadores ASCII.

**Scale/Scope**: una instancia de backend. Archivos afectados: `build.gradle`,
`application.yml`, `.env.example`, `SecurityConfig.java` y un test nuevo.

Todas las incógnitas del contexto técnico quedaron resueltas en
[research.md](research.md) (R1–R9); no quedan `NEEDS CLARIFICATION`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Gate previo a Phase 0: APROBADO. Re-evaluación post-diseño: APROBADO.**

| Regla | Evaluación |
|-------|-----------|
| Stack tecnológico (v2.8.0) | `spring-boot-starter-actuator` y `micrometer-registry-prometheus` sin versión explícita: aprobados. No se agregan otras librerías. |
| RNF Observabilidad: solo Actuator bajo `/actuator`, en `application.yml`, sin perfiles | Cumple (R2). |
| RNF Observabilidad: solo `health`, `info`, `metrics` y `prometheus` | Cumple con exposición más `access.default: none`. Una variable de entorno no puede ampliar la lista (R2). |
| RNF Observabilidad: `health` público sin detalles; el resto con `ROLE_ADMIN` en `SecurityConfig` | Cumple (R3, R6). |
| RNF Observabilidad: Micrometer y formato Prometheus en `/actuator/prometheus` | Cumple (R1, R5). |
| RNF Observabilidad: sin credenciales, tokens, secretos, valores de configuración ni datos personales | Cumple: `env` y `configprops` no existen y `info` solo publica build (R9). |
| RNF Configuración: un único `application.yml`; configuración global en `configuration/` | Cumple; la seguridad se modifica solo en `configuration/SecurityConfig`. |
| Principio I (capas) y `ArquitecturaCapasTest` | No se crean clases de producción. El test vive en `src/test`, que ArchUnit excluye (`ImportOption.DoNotIncludeTests`). |
| Principio III (`SecurityConfig` en `configuration`) | Cumple. |
| Principio VII (testing) | Solo `observabilidad/ObservabilidadSeguridadTest` con los casos que fija la constitution. No se agregan tests de `configuration` ni de `security`. |
| Principio IX (ambigüedad) | Las alternativas evaluadas y la decisión elegida están documentadas en R2–R7. |
| Principio X (idioma) | Documentación en español; el nombre del test está en español y es ASCII. |
| Principio XI (YAGNI) | Sin métricas de negocio, histogramas, Correlation ID, logs estructurados ni probes. |
| RNF Documentación (OpenAPI) | Los cuatro endpoints aparecen en Swagger con `springdoc.show-actuator` (R8). |
| RNF Calidad (SonarCloud) | Cambios declarativos mínimos. El test usa `@BeforeEach`, nombres descriptivos en español y sin literales duplicados (constantes para rutas y tokens). |

No hay violaciones; la sección Complexity Tracking no aplica.

## Project Structure

### Documentation (this feature)

```text
specs/012-observabilidad-actuator-prometheus/
├── spec.md
├── plan.md              # Este archivo
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── actuator.md      # Phase 1
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 (/speckit-tasks, no creado aquí)
```

### Source Code (repository root)

```text
backend/
├── build.gradle                                   # + micrometer-registry-prometheus, + springBoot { buildInfo() }
├── .env.example                                   # MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE documentada
└── src/
    ├── main/
    │   ├── java/unq/losrecursionistas/backend/
    │   │   └── configuration/SecurityConfig.java  # + /actuator/** hasAuthority("ROLE_ADMIN")
    │   └── resources/application.yml              # bloque management ampliado
    └── test/java/unq/losrecursionistas/backend/
        └── observabilidad/
            └── ObservabilidadSeguridadTest.java   # único test nuevo (Principio VII, v2.8.0)
```

**Structure Decision**: solo se tocan archivos existentes del backend y se
agrega el test autorizado. No se modifica `frontend/`.

### Diseño de los cambios

**1. `backend/build.gradle`**

```groovy
implementation 'io.micrometer:micrometer-registry-prometheus'

springBoot {
	buildInfo()
}
```

**2. `backend/src/main/resources/application.yml`**: reemplaza el bloque
`management` actual.

```yaml
management:
  endpoints:
    access:
      default: none
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics,prometheus}
  endpoint:
    health:
      access: read-only
      show-details: when-authorized
      show-components: when-authorized
      roles: ROLE_ADMIN
      probes:
        enabled: false
    info:
      access: read-only
    metrics:
      access: read-only
    prometheus:
      access: read-only
  metrics:
    tags:
      application: ${spring.application.name}
```

**3. `backend/.env.example`**

```properties
# Endpoints de Actuator publicados por HTTP. Solo puede restringir la lista aprobada
# (health,info,metrics,prometheus); cualquier otro endpoint esta deshabilitado en application.yml.
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

**4. `configuration/SecurityConfig`**: agregar la regla después de los grupos
`permitAll` y antes de `anyRequest().authenticated()`.

```java
.requestMatchers("/actuator/**")
.hasAuthority("ROLE_ADMIN")
```

`/actuator/health` permanece en el primer `permitAll`, que tiene un match
exacto y se evalúa antes. `exceptionHandling` ya registra
`JwtAuthenticationEntryPoint` (401) y `JwtAccessDeniedHandler` (403).

**5. `ObservabilidadSeguridadTest`**: los casos y la preparación están en
[research.md R7](research.md). Es el único test nuevo.

### Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|-----------|
| El registro Prometheus no se autoconfigura en `@SpringBootTest` y `prometheus` responde 404 en el test | Agregar `spring-boot-starter-micrometer-metrics-test` (test) y `@AutoConfigureMetrics` (R7). |
| H2 no crea las tablas con `jsonb` | El test no usa esas tablas. Si el contexto no levanta, usar Testcontainers PostgreSQL (R7). |
| Un `.env` local con `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health` oculta los nuevos endpoints | Actualizar `.env.example` y avisarlo en el quickstart. |
| `build.time` hace que `bootBuildInfo` nunca esté al día | Costo menor en el build, aceptado porque la spec pide la fecha de build. |

## Complexity Tracking

No aplica: la Constitution Check no registra violaciones.
