---

description: "Task list for automating WhoScored ingestion"
---

# Tasks: Automatizar la ingesta de estadÃ­sticas WhoScored

**Input**: Design documents from `/specs/006-automate-whoscored-ingestion/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Organization**: Tasks are grouped by user story so each story can be implemented, validated, and delivered independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar la configuraciÃ³n y las dependencias comunes para scheduler, asincronÃ­a, seguridad y scraping.

- [x] T001 Confirmar en `backend/build.gradle` que Java 21, Spring Scheduling/Async, Spring Security, JPA, PostgreSQL, Jackson, Playwright, JUnit 5 y Mockito requeridos por `specs/006-automate-whoscored-ingestion/plan.md` estÃ©n disponibles; documentar que los cambios de esquema de `EjecucionIngesta` se aplican mediante `spring.jpa.hibernate.ddl-auto` existente, sin agregar una herramienta de migraciones
- [x] T002 [P] Consolidar en `backend/src/main/resources/application.yml` las propiedades `scheduler.whoscored.enabled`, `scheduler.whoscored.cron`, zona `America/Argentina/Buenos_Aires`, ventana posterior a Football Data, executor de un hilo, rango de temporada y lÃ­mites/reintentos de WhoScored
- [x] T003 [P] Revisar `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/ScrapingConfiguration.java` para enlazar las propiedades de scraping, rate limit, pausas, reintentos, user-agent y headless sin duplicar configuraciÃ³n

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Completar los contratos y la reserva persistente que bloquean las tres historias.

**âš ï¸ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 Extender `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/ScrapingPort.java` y los records de `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/dto/` para exponer descubrimiento de ligas/equipos/jugadores, vÃ­nculo y apariciones sin exponer Playwright al servicio
- [x] T005 [P] Adaptar `backend/src/main/java/unq/losrecursionistas/backend/model/EjecucionIngesta.java` a los estados `ACTIVA`, `FINALIZADA`, `FALLIDA` y `RECHAZADA`, alcances `COMPLETA`, `LIGA`, `EQUIPO` y `JUGADOR`, `idAlcance` nullable solo para `COMPLETA`, rango de temporada, contadores, errores, anomalÃ­as, detalle JSONB y `motivoRechazo` obligatorio para `RECHAZADA`
- [x] T006 [P] Verificar en `backend/src/main/java/unq/losrecursionistas/backend/model/Liga.java`, `backend/src/main/java/unq/losrecursionistas/backend/model/Equipo.java` y `backend/src/main/java/unq/losrecursionistas/backend/model/Jugador.java` que `idExterno` de Football Data no se modifique y `idWhoscored` conserve nulabilidad y unicidad
- [x] T007 Implementar en `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/EjecucionIngestaDAOSQL.java` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioEjecucionIngestaImpl.java` una reserva ACTIVA atÃ³mica en PostgreSQL, incluyendo registro de un intento `RECHAZADA` cuando otra ejecuciÃ³n estÃ© activa y verificando que el esquema actualizado se gestione con `spring.jpa.hibernate.ddl-auto` existente
- [x] T008 Actualizar `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/IngestaWhoscoredService.java` y `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredServiceImpl.java` para centralizar reserva, rechazo, transiciÃ³n de estado y ejecuciÃ³n, eliminando la duplicaciÃ³n con `EjecucionIngestaServiceImpl` y retirando dependencia de `SecurityContext`
- [x] T009 [P] Crear `backend/src/main/java/unq/losrecursionistas/backend/configuration/AsyncIngestaConfiguration.java` con un `ThreadPoolTaskExecutor` dedicado de un solo worker, lÃ­mites de cola explÃ­citos y habilitaciÃ³n de `@Async` para el flujo de ingesta
- [x] T010 [P] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/service/IngestaWhoscoredServiceTest.java` la base de Mockito para reserva, transiciones, puerto de scraping y ejecuciÃ³n sin sesiÃ³n HTTP; mantener nombres de prueba en espaÃ±ol ASCII

**Checkpoint**: la reserva atÃ³mica, el contrato de scraping y el executor estÃ¡n listos; las historias pueden implementarse de forma independiente.

---

## Phase 3: User Story 1 - Ejecutar automÃ¡ticamente la ingesta completa (Priority: P1) ðŸŽ¯ MVP

**Goal**: Ejecutar una ingesta completa en una ventana configurable, primero descubriendo/vinculando y luego cargando estadÃ­sticas sin contexto HTTP.

**Independent Test**: Habilitar el scheduler en un entorno de prueba, dispararlo sin `SecurityContext`, verificar una Ãºnica ejecuciÃ³n, las dos fases en orden, persistencia de resultados parciales y actualizaciÃ³n idempotente de apariciones repetidas.

### Tests for User Story 1

- [x] T011 [P] [US1] Agregar casos en `backend/src/test/java/unq/losrecursionistas/backend/service/IngestaWhoscoredServiceTest.java` para flujo completo, orden descubrimientoâ†’vinculaciÃ³nâ†’estadÃ­sticas, ausencia de sesiÃ³n, jugadores sin `idWhoscored`, errores parciales y estados `FINALIZADA`/`FALLIDA`
- [x] T012 [P] [US1] Agregar casos en `backend/src/test/java/unq/losrecursionistas/backend/service/MatchingWhoscoredServiceTest.java` para el contrato de matching jerÃ¡rquico por plantel, `playerId`, nombre normalizado sin acentos, fecha/equipo, cero coincidencias y ambigÃ¼edad

### Implementation for User Story 1

- [x] T013 [P] [US1] Implementar `backend/src/main/java/unq/losrecursionistas/backend/scheduler/WhoScoredScheduler.java` como `@Component` con `@Scheduled`, propiedad de habilitaciÃ³n, cron configurable, zona argentina y ventana posterior a `FootballDataScheduler`
- [x] T014 [US1] Implementar `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/MatchingWhoscoredService.java` y `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MatchingWhoscoredServiceImpl.java` para la vinculaciÃ³n jerÃ¡rquica de liga, equipo y jugador; registrar candidato, resultado, motivo y ambigÃ¼edad en `EjecucionIngesta.detalle`/`anomalias`, sin crear una entidad `CasoVinculacion`, sin crear jugadores ni modificar `idExterno`
- [x] T015 [US1] Implementar en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredServiceImpl.java` la orquestaciÃ³n de descubrimiento/vinculaciÃ³n antes de estadÃ­sticas, filtrando jugadores activos con `idWhoscored` y apariciones dentro del rango de temporada configurado
- [x] T016 [US1] Implementar en `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/ScrapingRestAdapter.java` el descubrimiento y la carga de apariciones mediante Playwright aislado en `adapter/whoscored`, envolviendo navegaciÃ³n, red, parsing, timeout y anti-bot en `ScrapingException`
- [x] T017 [US1] Completar el upsert idempotente por jugador y partido WhoScored en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioEstadisticaPartidoImpl.java` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/EstadisticaPartidoDAOSQL.java`, actualizando repeticiones sin duplicados y acumulando contadores/anomalÃ­as
- [x] T018 [US1] Persistir errores por liga/equipo/jugador y conservar datos vÃ¡lidos en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredServiceImpl.java` y `backend/src/main/java/unq/losrecursionistas/backend/model/EjecucionIngesta.java`, sin inventar Ã©xito ante respuestas externas vacÃ­as o incompletas

**Checkpoint**: la ejecuciÃ³n automÃ¡tica completa funciona sin HTTP, respeta el orden de fases, conserva parciales y es idempotente.

---

## Phase 4: User Story 2 - Iniciar una ingesta manual administrativa (Priority: P2)

**Goal**: Exponer un trigger REST administrativo asÃ­ncrono para alcances completos o limitados, con validaciÃ³n y autorizaciÃ³n `ROLE_ADMIN`.

**Independent Test**: Con JWT de administrador, enviar requests `COMPLETA`, `LIGA`, `EQUIPO` y `JUGADOR`, verificar `202 Accepted` en menos de dos segundos, referencia de ejecuciÃ³n, trabajo fuera del request y errores 400/404; comprobar que usuarios sin `ROLE_ADMIN` no inician cargas.

### Tests for User Story 2

- [x] T019 [P] [US2] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/service/IngestaWhoscoredServiceTest.java` casos para alcances `COMPLETA`, `LIGA`, `EQUIPO` y `JUGADOR`, ids positivos existentes, ids inexistentes, cuerpos invÃ¡lidos y rechazo concurrente registrado
- [x] T020 [P] [US2] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/service/IngestaWhoscoredServiceTest.java` Ãºnicamente casos de validaciÃ³n del DTO/caso de uso con Mockito, verificando que una entrada invÃ¡lida no crea ejecuciÃ³n ACTIVA; no probar autorizaciÃ³n en esta capa

### Implementation for User Story 2

- [x] T021 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/whoscored/IniciarIngestaRequest.java` como record con `alcance` obligatorio, `id` nullable solo para `COMPLETA` y positivo para los demÃ¡s alcances, incluyendo mensajes de validaciÃ³n explÃ­citos
- [x] T022 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/whoscored/IniciarIngestaResponse.java` y DTOs de error/estado mÃ­nimos en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/whoscored/` para devolver `ejecucionId`, estado y alcance sin filtrar detalles de scraping
- [x] T023 [US2] Implementar `backend/src/main/java/unq/losrecursionistas/backend/controller/WhoscoredIngestaController.java` con `POST /api/admin/whoscored/ingestas`, validaciÃ³n de alcance/existencia antes de reservar, `202 Accepted`, referencia de ejecuciÃ³n y delegaciÃ³n asÃ­ncrona al caso de uso
- [x] T024 [US2] Actualizar `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java` para proteger inicio y consulta con `ROLE_ADMIN`, manteniendo el scheduler fuera de la autorizaciÃ³n HTTP y usando los handlers 401/403 existentes
- [x] T025 [US2] Ajustar `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredServiceImpl.java` para que el servicio reserve ACTIVA antes de encolar, aplique el alcance solicitado y actualice a `FALLIDA` ante error no recuperable del worker

**Checkpoint**: el administrador puede iniciar cargas completas o acotadas de forma asÃ­ncrona, y las solicitudes invÃ¡lidas o no autorizadas no inician procesamiento.

---

## Phase 5: User Story 3 - Consultar el estado de la Ãºltima ejecuciÃ³n (Priority: P2)

**Goal**: Permitir al administrador consultar la Ãºltima ejecuciÃ³n real, incluida una respuesta explÃ­cita cuando no existe ninguna.

**Independent Test**: Consultar durante una ejecuciÃ³n `ACTIVA`, despuÃ©s de finalizar o fallar, despuÃ©s de un rechazo y con una base sin ejecuciones; verificar estados, alcance, fechas, contadores, errores, anomalÃ­as, detalle y motivo de rechazo sin valores inventados.

### Tests for User Story 3

- [x] T026 [P] [US3] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/service/EjecucionIngestaServiceTest.java` casos para Ãºltima ejecuciÃ³n activa/finalizada/fallida/rechazada y ausencia de ejecuciones, verificando que se conserva `null` cuando no hay datos

### Implementation for User Story 3

- [x] T027 [P] [US3] Crear `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/whoscored/UltimaEjecucionResponse.java` y `EjecucionWhoscoredResponse.java` con el esquema de `contracts/ultima-ejecucion.md`, incluyendo contadores, errores, anomalÃ­as, detalle y `motivoRechazo`
- [x] T028 [US3] Extender `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/EjecucionIngestaService.java` y `backend/src/main/java/unq/losrecursionistas/backend/service/impl/EjecucionIngestaServiceImpl.java` para consultar la ejecuciÃ³n mÃ¡s reciente sin convertir ausencia en estado o contadores ficticios
- [x] T029 [US3] Implementar en `backend/src/main/java/unq/losrecursionistas/backend/controller/WhoscoredIngestaController.java` `GET /api/admin/whoscored/ingestas/ultima`, mapeando entidad a DTO y comunicando errores de persistencia mediante el manejador central
- [x] T030 [US3] Completar en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/RepositorioEjecucionIngesta.java`, `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioEjecucionIngestaImpl.java` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/EjecucionIngestaDAOSQL.java` la consulta ordenada de la Ãºltima ejecuciÃ³n y la lectura consistente de detalle JSONB

**Checkpoint**: un administrador puede observar cualquier estado real de la Ãºltima ejecuciÃ³n o recibir `disponible: false`.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificar integraciÃ³n, documentaciÃ³n operativa y restricciones legales/tÃ©cnicas de la feature.

- [x] T031 [P] Actualizar `backend/README.md` y la documentaciÃ³n de `specs/006-automate-whoscored-ingestion/quickstart.md` con configuraciÃ³n, endpoints, respuesta 202, operaciÃ³n sin sesiÃ³n, limitaciÃ³n de ejecuciones ACTIVA huÃ©rfanas y advertencia de tÃ©rminos/anti-bot de WhoScored
- [x] T032 [P] Revisar `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ControladorErrores.java` para comunicar validaciÃ³n 400, ausencia 404, 401/403 y fallos 500 sin respuestas exitosas ficticias
- [x] T033 Ejecutar desde `backend/` `.\gradlew.bat test --tests "*IngestaWhoscoredServiceTest" --tests "*MatchingWhoscoredServiceTest" --tests "*EjecucionIngestaServiceTest"` y corregir Ãºnicamente regresiones de esta feature
- [x] T034 Ejecutar la validaciÃ³n manual de `specs/006-automate-whoscored-ingestion/quickstart.md`, incluyendo idempotencia, concurrencia, errores parciales, ausencia de `SecurityContext`, respuesta menor a 2 segundos y scheduler posterior a Football Data
- [x] T035 [P] [US1] Agregar fixtures locales y tests unitarios del adapter en `backend/src/test/java/unq/losrecursionistas/backend/adapter/whoscored/` para happy path, timeout, JSON invÃ¡lido, retry y excepciones envueltas; no acceder a WhoScored real en CI

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies; T002 y T003 pueden ejecutarse en paralelo despuÃ©s de confirmar T001.
- **Foundational (Phase 2)**: depende de Phase 1; T005, T006, T009 y T010 pueden ejecutarse en paralelo, mientras T007/T008 dependen de los contratos y modelo revisados.
- **User Story 1 (Phase 3)**: depende de Phase 2; es el MVP y habilita el flujo de negocio compartido.
- **User Story 2 (Phase 4)**: depende de Phase 2 y reutiliza el caso de uso de US1; su controller no debe duplicar la orquestaciÃ³n.
- **User Story 3 (Phase 5)**: depende de Phase 2 y de la forma final de `EjecucionIngesta`; puede desarrollarse en paralelo con US2 despuÃ©s de estabilizar los DTOs de estado.
- **Polish (Phase 6)**: depende de las historias que se quieran entregar y de la disponibilidad de PostgreSQL/configuraciÃ³n para la validaciÃ³n manual.

### User Story Dependencies

- **US1 (P1)**: depende Ãºnicamente de Phase 2; entrega el MVP funcional.
- **US2 (P2)**: depende de los contratos de reserva y ejecuciÃ³n de Phase 2; reutiliza US1, pero su validaciÃ³n de request/autorizaciÃ³n es independiente.
- **US3 (P2)**: depende del modelo y repositorio de ejecuciones de Phase 2; es independiente de la interfaz REST de US2 salvo por el DTO compartido de ejecuciÃ³n.

### Parallel Opportunities

- Phase 1: T002 y T003.
- Phase 2: T005, T006, T009 y T010.
- US1: T011/T012; luego T013 puede desarrollarse en paralelo con T014/T016 una vez disponibles los contratos.
- US2: T019/T020 y T021/T022.
- US3: T026/T027; T028/T030 pueden avanzar en paralelo tras definir el contrato del servicio.
- Polish: T031, T032 y T035.

## Implementation Strategy

1. Completar Phase 1 y Phase 2 para obtener reserva atÃ³mica, contratos y executor confiables.
2. Entregar **MVP = US1**: scheduler, matching, carga idempotente y persistencia de estados sin sesiÃ³n HTTP.
3. AÃ±adir US2 para operaciÃ³n administrativa manual y recuperaciÃ³n por alcance.
4. AÃ±adir US3 para observabilidad de ejecuciones.
5. Ejecutar Polish y la validaciÃ³n del quickstart; mantener los tests de adapter condicionados a la aprobaciÃ³n constitucional indicada en `plan.md`.
