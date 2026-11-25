---

description: "Task list for feature 012 - Observabilidad del backend con Actuator y métricas Prometheus"
---

# Tasks: Observabilidad del backend con Actuator y métricas Prometheus

**Input**: Design documents from `/specs/012-observabilidad-actuator-prometheus/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/actuator.md](contracts/actuator.md), [quickstart.md](quickstart.md)

**Tests**: la spec (FR-017) y la constitution v2.8.0 (Principio VII) piden
explícitamente un único test, `observabilidad/ObservabilidadSeguridadTest`.
No se crean otros tests.

**Organization**: las tareas se agrupan por historia de usuario. Casi toda la
feature es configuración declarativa, así que varias historias se completan con
1 a 3 tareas más su verificación.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede ejecutar en paralelo (archivos distintos, sin dependencias pendientes).
- **[Story]**: historia de usuario de la spec (US1–US5).

## Path Conventions

- Backend: `backend/`, `backend/src/main/...`, `backend/src/test/...`.
- No se modifica `frontend/`.
- Comandos en PowerShell desde la raíz del repositorio, salvo que se indique otra cosa.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: dejar una línea base verificable antes de tocar la configuración
(Governance: verificar antes y después).

- [X] T001 Ejecutar `cd backend; .\gradlew.bat clean test` sobre `dev` sin cambios y registrar el resultado (debe estar en verde, incluido `ArquitecturaCapasTest`). Si falla, detenerse y reportar antes de continuar.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: asegurar todo `/actuator` y fijar qué endpoints pueden existir
**antes** de exponer alguno nuevo. Sin esta fase, exponer `info`, `metrics` o
`prometheus` los dejaría accesibles a cualquier usuario autenticado, porque
`SecurityConfig` termina en `anyRequest().authenticated()`.

**⚠️ CRITICAL**: ninguna historia puede empezar hasta completar esta fase. T002 y T003 deben quedar en el mismo commit.

- [X] T002 [P] En `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`, dentro de `authorizeHttpRequests`, agregar `.requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")`:
  - **Ubicación**: inmediatamente después de los dos bloques `permitAll()` existentes y antes de `.requestMatchers("/api/admin/whoscored/**", ...)`, o como mínimo antes de `.anyRequest().authenticated()`.
  - **Qué no cambia**: `"/actuator/health"` queda en el primer `permitAll()`; `exceptionHandling`, `JwtAuthFilter` y CORS siguen igual.
  - **Restricción**: no usar `EndpointRequest` (research.md R6).
- [X] T003 [P] En `backend/src/main/resources/application.yml`, reemplazar el bloque `management` actual por el acceso restringido y la nueva exposición por defecto:
  - `management.endpoints.access.default: none`, valor fijo y sin variable de entorno.
  - `management.endpoints.web.exposure.include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics,prometheus}`.
  - `management.endpoint.health.access: read-only`, `management.endpoint.info.access: read-only`, `management.endpoint.metrics.access: read-only` y `management.endpoint.prometheus.access: read-only`.
  - No tocar el bloque `springdoc` ni el resto del archivo. Ver research.md R2.
- [X] T004 [P] En `backend/.env.example`, cambiar `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health` por `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus` y agregar arriba este comentario en español (ASCII, sin acentos):
  ```properties
  # Endpoints de Actuator publicados por HTTP. Solo puede restringir la lista aprobada
  # (health,info,metrics,prometheus); cualquier otro endpoint esta deshabilitado en application.yml.
  ```
- [X] T005 Ejecutar `cd backend; .\gradlew.bat compileJava` para confirmar que `SecurityConfig` compila. Depende de T002.

**Checkpoint**: `/actuator/**` exige `ROLE_ADMIN` (salvo `/actuator/health`) y solo pueden existir los cuatro endpoints aprobados.

---

## Phase 3: User Story 1 - Consultar la salud según el nivel de acceso (Priority: P1) 🎯 MVP

**Goal**: `/actuator/health` sigue público con solo el estado global; un ADMIN
ve el detalle de componentes (FR-004, FR-005).

**Independent Test**: [quickstart.md §3](quickstart.md#3-health-público-sin-detalles-salvo-para-admin):
- anónimo y `ROLE_USUARIO` reciben solo `{ "status": ... }`, sin `components` ni `groups`;
- ADMIN recibe además `db`, `diskSpace` y `ping`;
- `/actuator/health/db` anónimo responde 401.

### Implementation for User Story 1

- [X] T006 [US1] En `backend/src/main/resources/application.yml`, bajo `management.endpoint.health` (junto al `access: read-only` de T003), agregar:
  - `show-details: when-authorized`
  - `show-components: when-authorized`
  - `roles: ROLE_ADMIN`, con el nombre completo de la authority y no `ADMIN` (research.md R3)
  - `probes.enabled: false`, para que la respuesta anónima no incluya `groups` (research.md R3)
- [X] T007 [US1] Levantar el backend (`cd backend; .\gradlew.bat bootRun`, con `backend/.env` actualizado según quickstart.md «Prerrequisitos») y ejecutar [quickstart.md](quickstart.md) §2 y §3. Comprobar contra [contracts/actuator.md](contracts/actuator.md#get-actuatorhealth). Depende de T006.

**Checkpoint**: US1 funciona de forma independiente.

---

## Phase 4: User Story 2 - Consultar métricas técnicas e información (Priority: P1)

**Goal**: un ADMIN consulta `/actuator/info` (nombre, versión y datos de
build), `/actuator/metrics` y `/actuator/metrics/{nombre}` (FR-008, FR-009).

**Independent Test**: [quickstart.md §6](quickstart.md#6-info-y-métricas):
- `info` muestra `build.name`, `build.version` y `build.time`;
- `metrics/http.server.requests` lista sus tags;
- `metrics/no.existe` responde 404.

### Implementation for User Story 2

- [X] T008 [US2] En `backend/build.gradle`, agregar al final del archivo, después del bloque `sonar { ... }`, el bloque `springBoot { buildInfo() }`:
  - Usar la indentación con tabs del archivo.
  - No configurar `excludes`: la spec pide la fecha de build (research.md R4).
  - No agregar propiedades `info.*` a `application.yml` ni habilitar `management.info.env.enabled`.
- [X] T009 [US2] Reiniciar `bootRun` y ejecutar [quickstart.md](quickstart.md) §6. Verificar:
  - que `info` contenga `build.artifact`, `build.name`, `build.group`, `build.version` y `build.time`, sin otros contribuidores (`env`, `java`, `os`, `process`);
  - que `/actuator/metrics/no.existe` responda 404.

  Depende de T008.

**Checkpoint**: US2 funciona de forma independiente.

---

## Phase 5: User Story 3 - Obtener métricas en formato Prometheus (Priority: P1)

**Goal**: `/actuator/prometheus` devuelve en texto Prometheus las series HTTP,
de JVM, de proceso/sistema y del pool Hikari, todas con el tag `application`
(FR-010 a FR-013).

**Independent Test**: [quickstart.md §7](quickstart.md#7-prometheus-series-http-jvm-proceso-y-pool):
- después de generar tráfico aparecen `http_server_requests_seconds_count` y `_sum` con `uri`, `method`, `status` y `outcome`;
- aparecen series de JVM, proceso y `hikaricp_connections_*`;
- ninguna serie carece de `application="..."`.

### Implementation for User Story 3

- [X] T010 [US3] En `backend/build.gradle`, agregar `implementation 'io.micrometer:micrometer-registry-prometheus'` **sin versión**, en el bloque `dependencies`, inmediatamente después de `implementation 'org.springframework.boot:spring-boot-starter-actuator'`. No agregar ninguna otra dependencia de producción (research.md R1). Si se hace junto con T008, editar el archivo de forma secuencial.
- [X] T011 [US3] En `backend/src/main/resources/application.yml`, agregar `management.metrics.tags.application: ${spring.application.name}`:
  - No agregar ninguna propiedad `management.metrics.distribution.*`: histogramas y percentiles están fuera de alcance (research.md R5).
  - No usar `management.observations.key-values`.
- [X] T012 [US3] Ejecutar `cd backend; .\gradlew.bat --refresh-dependencies compileJava`, reiniciar `bootRun` y ejecutar [quickstart.md](quickstart.md) §7. Confirmar:
  - el `Content-Type` `text/plain` de la respuesta;
  - que la latencia media de `/leagues` se puede calcular como `sum / count` ([data-model.md](data-model.md#exposición-prometheus)).

  Depende de T010 y T011.

**Checkpoint**: US3 funciona de forma independiente.

---

## Phase 6: User Story 4 - Restringir el acceso a la observabilidad (Priority: P1)

**Goal**: verificar de forma automática y manual la matriz 401/403/200, el 404
de los endpoints no aprobados y la ausencia de datos sensibles (FR-002,
FR-003, FR-006, FR-007, FR-014, FR-017).

**Independent Test**: `ObservabilidadSeguridadTest` en verde, más
[quickstart.md §4, §5 y §8](quickstart.md#4-matriz-de-acceso-a-los-endpoints-protegidos).

**Dependencias**: la implementación ya está en la Phase 2. Los casos 200 de
este test requieren `info` (US2) y `prometheus` (US3), así que esta fase va
después de T008 y T010.

### Tests for User Story 4 (requerido por FR-017 y el Principio VII) ⚠️

- [X] T013 [US4] Crear `backend/src/test/java/unq/losrecursionistas/backend/observabilidad/ObservabilidadSeguridadTest.java`, único test de integración admitido por la constitution v2.8.0 (research.md R7).

  **Anotaciones y propiedades de la clase**:
  - `@SpringBootTest(properties = { ... })` con:
    - `spring.datasource.url=jdbc:h2:mem:observabilidad;DB_CLOSE_DELAY=-1`
    - `spring.datasource.driver-class-name=org.h2.Driver`
    - `spring.datasource.username=sa`
    - `spring.datasource.password=`
    - `spring.jpa.hibernate.ddl-auto=create-drop`
    - `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect`
    - `scheduler.whoscored.enabled=false`
    - `scheduler.cotizacion.enabled=false`
    - `scheduler.football-data.startup-enabled=false`
    - `whoscored.scraping.startup-enabled=false`
    - `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
  - `@AutoConfigureMockMvc` de `org.springframework.boot.webmvc.test.autoconfigure`.

  **Mocks**:
  - `@MockitoBean JwtService jwtService` (`service.interfaces.JwtService`).
  - `@MockitoBean UsuarioDetailsService usuarioDetailsService` (`service.interfaces.UsuarioDetailsService`, que extiende `UserDetailsService`). Usar el subtipo para no romper los beans que inyectan `UsuarioDetailsService`.

  **`@BeforeEach`**: stubear con `TOKEN_ADMIN = "token-admin"` y `TOKEN_USUARIO = "token-usuario"`:
  - `jwtService.extraerUsername(TOKEN_ADMIN)` → `Optional.of("admin")` y `jwtService.extraerUsername(TOKEN_USUARIO)` → `Optional.of("usuario")`.
  - `usuarioDetailsService.loadUserByUsername(...)` → `User.withUsername(...).password("x").authorities("ROLE_ADMIN")` y `.authorities("ROLE_USUARIO")`, respectivamente.
  - `jwtService.tokenValido(anyString(), any())` → `true`.

  **Métodos** (nombres en español ASCII):
  - `testHealthAnonimoRespondeEstadoGlobalSinComponentes`: `GET /actuator/health` → 200, `jsonPath("$.status").exists()`, `jsonPath("$.components").doesNotExist()`.
  - `@ParameterizedTest @ValueSource(strings = {"/actuator/info", "/actuator/metrics", "/actuator/prometheus"})` con tres métodos:
    - `testEndpointProtegidoSinTokenRespondeNoAutorizado` → 401;
    - `testEndpointProtegidoConRolUsuarioRespondeProhibido` → 403, con header `Authorization: Bearer token-usuario`;
    - `testEndpointProtegidoConRolAdminRespondeOk` → 200, con `Bearer token-admin`.
  - `testEndpointNoAprobadoConRolAdminRespondeNoEncontrado`: `GET /actuator/env` con `Bearer token-admin` → 404.

  **Estilo**: constantes para rutas y tokens (sin literales duplicados, SonarCloud) y sin casos adicionales a los que fija la constitution.

  **Riesgo R7.1**: si `/actuator/prometheus` responde 404 con ADMIN porque el registro no se autoconfigura en `@SpringBootTest`, agregar `testImplementation 'org.springframework.boot:spring-boot-starter-micrometer-metrics-test'` en `backend/build.gradle` y anotar la clase con `@AutoConfigureMetrics` (`org.springframework.boot.micrometer.metrics.test.autoconfigure`), y documentarlo en research.md R7.

  **Riesgo R7.2**: si el contexto no levanta por el DDL `jsonb` en H2, detenerse y consultar antes de pasar a Testcontainers (Principio IX).
- [X] T014 [US4] Ejecutar `cd backend; .\gradlew.bat test --tests "unq.losrecursionistas.backend.observabilidad.ObservabilidadSeguridadTest"` y dejarlo en verde. Si falla por la configuración y no por el test, corregir T002, T003, T006, T008 o T010. Depende de T013.

### Verificación manual de User Story 4

- [X] T015 [US4] Con `bootRun` levantado, ejecutar [quickstart.md](quickstart.md) §4, §5 y §8. Verificar:
  - **§4**: `Anonimo=401`, `Usuario=403` y `Admin=200` en `info`, `metrics`, `metrics/jvm.memory.used` y `prometheus`, con cuerpo `RespuestaError` (`codigo`, `mensaje`, `detalle`, `timestamp`);
  - **§5**: `Admin=404` y `Anonimo=401` en `env`, `heapdump`, `beans`, `configprops`, `loggers`, `mappings`, `threaddump` y `shutdown`;
  - **§8**: la búsqueda de datos sensibles da `False`.
- [X] T016 [US4] Verificar FR-003: reiniciar con `$env:MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE="*"; .\gradlew.bat bootRun`. Confirmar:
  - que `GET /actuator/env` con token de ADMIN sigue respondiendo 404;
  - que `GET /actuator` con ADMIN lista solo `health`, `info`, `metrics` y `prometheus`.

  Al terminar, quitar la variable con `Remove-Item Env:MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`.

**Checkpoint**: las reglas de acceso quedan verificadas de forma automática y manual.

---

## Phase 7: User Story 5 - Descubrir los endpoints en Swagger (Priority: P3)

**Goal**: los cuatro endpoints aparecen en Swagger y ninguno no aprobado (FR-015).

**Independent Test**: [quickstart.md §9](quickstart.md#9-swagger-ui).

### Implementation for User Story 5

- [X] T017 [US5] Sin cambios de código: `springdoc.show-actuator` ya está activo (research.md R8). Abrir `http://localhost:8080/swagger-ui.html` con `bootRun` levantado y verificar:
  - que en la sección de Actuator aparezcan solo `health`, `info`, `metrics` (incluida `metrics/{requiredMetricName}`) y `prometheus`;
  - que `GET /v3/api-docs` no contenga rutas `/actuator/env`, `/actuator/beans` ni otras no aprobadas: `(Invoke-WebRequest http://localhost:8080/v3/api-docs -UseBasicParsing).Content -match '/actuator/(env|beans|configprops|heapdump)'` debe dar `False`.

**Checkpoint**: todas las historias funcionan.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: verificación final, calidad y coherencia de la documentación.

- [X] T018 Revisar los cambios contra `.agents/skills/sonarcloud-quality/SKILL.md`, sobre todo `SecurityConfig.java` y `ObservabilidadSeguridadTest.java`. Revisar: imports sin usar, literales duplicados, nombres de test descriptivos en español ASCII y ausencia de código comentado.
- [X] T019 Ejecutar la verificación final `cd backend; .\gradlew.bat clean test`. Debe quedar en verde, incluidos `ArquitecturaCapasTest` y `ObservabilidadSeguridadTest` (SC-006). Informar cualquier verificación no ejecutada y su motivo (RNF Calidad de la constitution). El escaneo SonarCloud corre en CI (`.github/workflows/build.yml`, `./gradlew build sonar`); localmente se ejecuta `.\gradlew.bat sonar` solo si `SONAR_TOKEN` está definido.
- [X] T020 [P] Si la implementación se desvió del diseño (por ejemplo, si se aplicó la mitigación R7.1), actualizar `specs/012-observabilidad-actuator-prometheus/research.md` y `specs/012-observabilidad-actuator-prometheus/plan.md` para reflejar lo implementado.
- [X] T021 Recorrer [quickstart.md](quickstart.md) completo, de §1 a §10, de punta a punta como validación final de SC-001 a SC-005.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias.
- **Foundational (Phase 2)**: depende de T001 y **bloquea todas las historias**. T002 y T003 van juntos en el mismo commit.
- **US1 (Phase 3)**: solo depende de la Phase 2.
- **US2 (Phase 4)**: solo depende de la Phase 2.
- **US3 (Phase 5)**: solo depende de la Phase 2.
- **US4 (Phase 6)**: depende de la Phase 2. Su test automático (T013, T014) necesita además T008 (US2) y T010 (US3) para los casos 200.
- **US5 (Phase 7)**: depende de la Phase 2. Para ver los cuatro endpoints conviene hacerla después de US2 y US3.
- **Polish (Phase 8)**: depende de todas las historias.

### Conflictos de archivo (tareas que no pueden ir en paralelo)

- `backend/src/main/resources/application.yml`: T003 → T006 → T011, en secuencia.
- `backend/build.gradle`: T008 y T010, en secuencia.
- `SecurityConfig.java` solo lo toca T002.

### Parallel Opportunities

- Phase 2: T002, T003 y T004 tocan archivos distintos.
- Después de la Phase 2:
  - T006 (US1, `application.yml`) en paralelo con T008 (US2, `build.gradle`);
  - luego T011 (US3, `application.yml`) en paralelo con T010 (US3, `build.gradle`) si los hacen personas distintas.
- T013 (test) se puede escribir en paralelo con las verificaciones manuales T007 y T009.

---

## Parallel Example: Phase 2

```text
Task: "T002 Agregar /actuator/** hasAuthority(ROLE_ADMIN) en backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java"
Task: "T003 Reemplazar bloque management (access.default none + exposure + read-only) en backend/src/main/resources/application.yml"
Task: "T004 Actualizar MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE en backend/.env.example"
```

## Parallel Example: después de Foundational

```text
Task: "T006 [US1] Configurar show-details/show-components/roles/probes en application.yml"
Task: "T008 [US2] Agregar springBoot { buildInfo() } en backend/build.gradle"
Task: "T013 [US4] Crear ObservabilidadSeguridadTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1: confirmar la línea base en verde.
2. Phase 2: asegurar `/actuator/**` y restringir el acceso (bloqueante).
3. Phase 3: health con detalle para ADMIN.
4. **STOP and VALIDATE** con quickstart §3.

### Incremental Delivery

1. Setup + Foundational: Actuator protegido, sin cambios visibles para usuarios.
2. US1, health con detalle: validar.
3. US2, info y metrics: validar.
4. US3, Prometheus: validar. Con esto se cumplen los dos ítems de la Entrega 3.
5. US4: test automático y verificación de seguridad.
6. US5 + Polish: Swagger, Sonar y `clean test` final.

### Commits sugeridos

Separados por historia, sin push hasta que el equipo lo apruebe:

1. `feat(observabilidad): proteger /actuator y restringir endpoints aprobados`: T002–T004.
2. `feat(observabilidad): detalle de health solo para ROLE_ADMIN`: T006.
3. `feat(observabilidad): exponer info con build-info`: T008.
4. `feat(observabilidad): metricas Prometheus con tag application`: T010–T011.
5. `test(observabilidad): agregar ObservabilidadSeguridadTest`: T013.
6. `docs(observabilidad): ajustes de research/plan`: T020, si corresponde.

---

## Notes

- No se crean clases de producción nuevas (plan.md, Principio XI).
- No se agregan otros tests ni se modifican tests existentes (Principio VII).
- Fuera de alcance: métricas de negocio, histogramas y percentiles, Correlation ID, logs estructurados, servidor Prometheus o Grafana.
- Ante cualquier desvío del diseño que no esté contemplado en research.md R7, detenerse y consultar (Principio IX).
