# Tasks: Ingesta de estadísticas de jugadores WhoScored

**Input**: Design documents from `/specs/005-whoscored-player-stats/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story so each story can be implemented, validated, and delivered independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicializar la base de la feature y dejar la estructura del backend lista para la ingesta.

- [x] T001 Create backend source structure for `adapter/whoscored`, `model`, `persistence`, `service`, and test fixtures in `backend/src/main/java/unq/losrecursionistas/backend/` and `backend/src/test/resources/whoscored/`
- [x] T002 Configure Java 21 / Spring Boot dependencies and Testcontainers / Playwright additions in `backend/build.gradle`
- [x] T003 [P] Create the single application configuration file `backend/src/main/resources/application.yml` with `whoscored.scraping`, season range, and operational defaults from `research.md`, and bind them through `ScrapingConfiguration` in `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/ScrapingConfiguration.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Definir los puertos, contratos de persistencia y entidades base que bloquean todas las historias de usuario.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 Define the `ScrapingPort` contract and `ScrapingException` in `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/`
- [x] T005 [P] Create immutable DTOs for discovery and match results under `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/dto/`
- [x] T006 [P] Add repository and service interfaces for `Jugador`, `Equipo`, `Liga`, `EstadisticaPartido`, `CasoVinculacion`, and `EjecucionIngesta` under `backend/src/main/java/unq/losrecursionistas/backend/persistence/` and `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/`
- [x] T007 Create the domain entities `EstadisticaPartido`, `CasoVinculacion`, and `EjecucionIngesta` in `backend/src/main/java/unq/losrecursionistas/backend/model/` with unique constraints and JSONB storage rules from `data-model.md`
- [x] T008 [P] Implement shared repository DAO and persistence infrastructure skeletons under `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`; leave `EstadisticaPartido` behavior to T017
- [x] T009 Update `Jugador`, `Equipo`, and `Liga` entities to add nullable unique `idWhoscored` fields in `backend/src/main/java/unq/losrecursionistas/backend/model/` without changing `idExterno`
- [x] T010 Add the ingestion execution guard and admin-oriented service contract for active execution rejection in `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/` and `backend/src/main/java/unq/losrecursionistas/backend/service/impl/`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Vincular jugadores descubiertos (Priority: P1) 🎯 MVP

**Goal**: Habilitar la conexión entre cada jugador descubierto de WhoScored y el jugador existente de la base, sin crear identidades ni tocar `idExterno`.

**Independent Test**: Con un plantel de prueba y jugadores persistidos con nombres y fechas compatibles, ejecutar el matching y verificar que los registros inequívocos quedan vinculados, los ambiguos quedan pendientes y los no encontrados se conservan como casos consultables.

### Implementation for User Story 1

- [x] T011 [P] [US1] Implement the `MatchingWhoscoredService` normalization and match sequence in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MatchingWhoscoredService.java`
- [x] T012 [P] [US1] Add the player/club discovery flow and `ScrapingRestAdapter` implementation in `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/ScrapingRestAdapter.java`
- [x] T013 [US1] Persist unresolved link cases with candidate metadata in `backend/src/main/java/unq/losrecursionistas/backend/model/CasoVinculacion.java` and the matching repository layer
- [x] T014 [US1] Wire discovered player/team mappings into `Jugador`, `Equipo`, and `Liga` updates without changing `idExterno` in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/`
- [x] T015 [US1] Implement the no-new-player guard and ambiguous/no-match handling for `Jugador` discovery in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredService.java`
- [x] T016 [P] [US1] Add unit fixtures and matching tests for normalized names, slugs, team disambiguation, and ambiguity thresholds in `backend/src/test/java/unq/losrecursionistas/backend/service/MatchingWhoscoredServiceTest.java` and `backend/src/test/resources/whoscored/`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Cargar partidos y estadísticas (Priority: P2)

**Goal**: Persistir la estadística por partido de cada jugador vinculado, combinando Summary, Defensive, Offensive y Passing según la temporada activa.

**Independent Test**: Con un jugador vinculado y fixtures de las cuatro categorías, ejecutar la carga y verificar un único registro por partido con métricas principales, JSONB complementario y exclusión de registros sin participación.

### Implementation for User Story 2

- [x] T017 [P] [US2] Implement the transactional `EstadisticaPartido` repository with unique key `(jugador_id, partido_whoscored_id)`, seasonal indexes, and idempotent upsert semantics that insert new matches and update existing ones in `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`
- [x] T018 [P] [US2] Add parsing and aggregation logic for `Summary`, `Defensive`, `Offensive`, and `Passing` in `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/`
- [x] T019 [US2] Implement season filtering and no-zero/minutes validation in the ingestion service and adapter boundary in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredService.java`
- [x] T020 [US2] Persist one record per player and match, preserving raw stats in `statsCrudas` and maintaining nullable fields for missing metrics in `backend/src/main/java/unq/losrecursionistas/backend/model/EstadisticaPartido.java`
- [x] T021 [US2] Add execution counters, anomaly handling, and rollback semantics per failing player in `backend/src/main/java/unq/losrecursionistas/backend/model/EjecucionIngesta.java` and service implementation
- [x] T022 [P] [US2] Add parser/service and PostgreSQL integration tests for combined metrics, zero-minute exclusions, season filtering, unique-key enforcement, and idempotent upsert updates in `backend/src/test/java/unq/losrecursionistas/backend/service/` and `backend/src/test/java/unq/losrecursionistas/backend/adapter/whoscored/`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently.

---

## Phase 5: User Story 3 - Reintentar y actualizar sin duplicados (Priority: P2)

**Goal**: Permitir ejecuciones reanudables y acotadas por liga, equipo o jugador, reutilizando el upsert idempotente de US2 y rechazando ejecuciones concurrentes.

**Independent Test**: Ejecutar cargas acotadas por liga, equipo y jugador, y simular una segunda ejecución activa para comprobar que el alcance se respeta y el intento concurrente se rechaza con un registro de intento.

### Implementation for User Story 3

- [x] T023 [P] [US3] Implement scope selection and filtering by league, team, or player in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/IngestaWhoscoredService.java`
- [x] T024 [US3] Add active-season configuration enforcement and a repeatable execution window using `ScrapingConfiguration` in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/` and `backend/src/main/resources/application.yml`
- [x] T025 [US3] Add the concurrency guard for active ingestion and rejection logging in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/` and `backend/src/main/java/unq/losrecursionistas/backend/model/EjecucionIngesta.java`
- [x] T026 [P] [US3] Add PostgreSQL integration tests for rollback of a failing player and execution rejection in `backend/src/test/java/unq/losrecursionistas/backend/service/IngestaWhoscoredServiceTest.java`
- [x] T027 [US3] Add real-web test tagging and operational documentation for `real-web` execution and anti-bot limitations in `backend/src/test/java/` and `specs/005-whoscored-player-stats/quickstart.md`

**Checkpoint**: All user stories should now be independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, documentation, and final validation across all user stories.

- [x] T028 [P] Review and update `specs/005-whoscored-player-stats/quickstart.md` and `backend/README.md` to document the ingestion flow, admin-only access, and anti-bot caveats
- [x] T029 [P] Refactor the adapter/service boundary to keep Playwright and parsing details isolated in `backend/src/main/java/unq/losrecursionistas/backend/adapter/whoscored/`
- [x] T030 Harden error sanitization and admin authorization checks around ingestion execution in `backend/src/main/java/unq/losrecursionistas/backend/security/` and `backend/src/main/java/unq/losrecursionistas/backend/service/impl/`
- [x] T031 Run the validation commands from `specs/005-whoscored-player-stats/quickstart.md` in `backend/` to confirm matching, parser, persistence, and scope/retry scenarios pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories.
- **User Stories (Phase 3-5)**: Each depends on Phase 2 and can be implemented in priority order: US1 → US2 → US3.
- **Polish (Phase 6)**: Depends on the desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after foundational tasks; no dependency on other stories.
- **User Story 2 (P2)**: Depends on US1 matching and player linking to be stable before stats ingestion.
- **User Story 3 (P3)**: Depends on the ingestion service and persistence rules from US1 and US2.

### Within Each User Story

- Matching and persistence contracts before service orchestration.
- Adapter parsing before ingestion logic.
- Service orchestration before execution guard and rerun logic.
- Story completion before starting the next priority.

---

## Parallel Opportunities

- `T003` can run alongside `T001` and `T002` in Setup.
- `T005`, `T006`, and `T008` can be developed in parallel once the foundational interfaces are defined; T008 does not include the `EstadisticaPartido` repository implementation.
- `T011`, `T012`, and `T016` are independent within US1 once the foundational contracts exist.
- `T017`, `T018`, and `T022` can proceed in parallel within US2.
- `T023`, `T026`, and `T027` can be worked in parallel within US3 after ingestion flow is in place.
- `T028`, `T029`, and `T031` are cross-cutting work items that can overlap near the end of the delivery.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Implement matching and unresolved case tracking (US1).
3. Validate the feature with the explicit matching fixtures.
4. Add ingest and persistence work for US2 only after US1 is stable.
5. Expand to scope, re-run, and concurrency handling under US3, reusing the idempotent persistence delivered in US2.

### Incremental Delivery

- Deliver US1 as the minimum viable ingesta foundation: discovery, matching, and unresolved case recording.
- Deliver US2 as the primary data product: player-match statistics and season filtering.
- Deliver US3 as operational hardening: scoped retries, re-entry protection, and scaling to repeated executions; idempotent persistence is delivered and verified in US2.

---

## Final Notes

The tasks are intentionally structured around the actual feature requirements from `spec.md`, the repository constraints in `plan.md`, and the technical decisions captured in `research.md`. This keeps the implementation aligned with the constitution and makes each story independently testable without forcing a full-stack delivery before the core matching and persistence logic is proven.
