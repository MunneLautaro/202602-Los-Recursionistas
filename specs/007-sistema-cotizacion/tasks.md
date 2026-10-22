---

description: "Task list for the player quotation system"
---

# Tasks: Sistema de Cotización de Jugadores

**Input**: Design documents from `specs/007-sistema-cotizacion/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Required by FR-017 and the project constitution. Tests are limited to `model` and `service` packages and use JUnit 5, Mockito, and Gradle.

**Organization**: Tasks are grouped by user story so each increment can be implemented and validated independently after the shared foundation.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Register the feature's shared configuration and package structure without changing existing entity behavior.

- [X] T001 Create the quotation package structure under `backend/src/main/java/unq/losrecursionistas/backend/` for `model`, `service/interfaces`, `service/impl/estrategia`, `controller/dto/cotizacion`, `persistence/repository/interfaces`, `persistence/repository/impl`, `persistence/sql`, `scheduler`, and `configuration`
- [X] T002 [P] Add the quotation strategy, window, monetary, scheduler cron, and positional-weight properties with environment fallbacks to `backend/src/main/resources/application.yml`, including default window 7 days and Sunday 04:00 Argentina cron
- [X] T003 [P] Confirm the feature's JUnit 5 and Mockito test conventions and reusable fixtures in `backend/src/test/java/unq/losrecursionistas/backend/service` and `backend/src/test/java/unq/losrecursionistas/backend/model` without adding controller or infrastructure test suites

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement shared persistence, contracts, error mapping, and pagination primitives required by all stories.

**CRITICAL**: User-story work depends on this phase.

- [X] T004 [P] Create the append-only `Cotizacion` entity in `backend/src/main/java/unq/losrecursionistas/backend/model/Cotizacion.java` with required `jugador` (`LAZY`), `valor` (scale 2), `timestampCalculo`, immutable `estrategiaUsada`, inclusive `ventanaInicio`/`ventanaFin`, generated identity, and JPA indexes `idx_cotizacion_jugador_timestamp` on `(jugador_id, timestamp_calculo DESC)` and `idx_cotizacion_timestamp` on `(timestamp_calculo DESC)`
- [X] T005 [P] Create the immutable performance summary and strategy parameter types used between service and strategies in `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/ResumenRendimiento.java` and `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/ParametrosCotizacion.java`, preserving null-as-zero metrics and maximum-zero normalization inputs
- [X] T006 [P] Add `RepositorioCotizacion` in `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/RepositorioCotizacion.java` and its implementation in `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioCotizacionImpl.java` for append-only saves and paged history access
- [X] T007 [P] Add `CotizacionDAOSQL` in `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/CotizacionDAOSQL.java` with inclusive date predicates, stable `timestamp_calculo DESC, id DESC` history ordering, and a paged PostgreSQL projection that selects each player's latest and previous quotation for ranking without loading lists in memory
- [X] T008 [P] Define `CotizacionService` and `EstrategiaCotizacion` contracts in `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/CotizacionService.java` and `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/EstrategiaCotizacion.java`, including recalculation, paged history, paged ranking, and manual-job acceptance operations
- [X] T009 Add shared quotation response projections and DTOs in `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/cotizacion/CotizacionResponse.java` and `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/cotizacion/RankingJugadorResponse.java` with the exact contract fields and page shape
- [X] T010 Extend the existing validation/error mapping in `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ControladorErrores.java` and related exception types under `backend/src/main/java/unq/losrecursionistas/backend/exceptions/` for invalid ranges/pagination, missing players, invalid strategy configuration, and active-job conflict using `RespuestaError`

**Checkpoint**: Shared entity, DAO/repository contracts, DTOs, error conventions, and configuration are ready for story implementation.

---

## Phase 3: User Story 1 - Calcular cotizaciones periódicas (Priority: P1) MVP

**Goal**: Calculate and persist one historical quotation for every eligible active player from recent WhoScored statistics, using the configured strategy and weekly scheduler.

**Independent Test**: With active/inactive, missing-`idWhoscored`, and in/out-of-window players plus valid statistics, invoke the service and verify aggregation, null-as-zero handling, strategy metadata, inclusive window, append-only persistence, isolated player errors, and scheduler invocation without HTTP security context.

### Tests for User Story 1

- [X] T011 [P] [US1] Add model tests for quotation invariants, two-decimal monetary rounding, required window ordering, and append-only strategy metadata in `backend/src/test/java/unq/losrecursionistas/backend/model/CotizacionTest.java`
- [X] T012 [P] [US1] Add unit tests for `PERFORMANCE_GENERAL` normalization, exact weights `0.25/0.15/0.10/0.10/0.10/0.10/0.20`, `[0,1]` limits, maximum-zero behavior, and `valorBase + (score * factorEscala)` rounding in `backend/src/test/java/unq/losrecursionistas/backend/service/PerformanceGeneralEstrategiaTest.java`
- [X] T013 [P] [US1] Add unit tests for `PERFORMANCE_POR_POSICION` Spanish/English position normalization, configured goalkeeper/defender/midfielder/forward emphasis, unknown-position fallback, maximum-zero behavior, and final monetary value in `backend/src/test/java/unq/losrecursionistas/backend/service/PerformancePorPosicionEstrategiaTest.java`
- [X] T014 [P] [US1] Add Mockito unit tests for `CotizacionServiceImpl` aggregation, eligible-player filtering, null metrics, fixed `LocalDate` window, invalid/incomplete strategy configuration before persistence, isolated player failures, and omitted-player reporting in `backend/src/test/java/unq/losrecursionistas/backend/service/CotizacionServiceImplTest.java`

### Implementation for User Story 1

- [X] T015 [P] [US1] Implement `PERFORMANCE_GENERAL` as `@Component("PERFORMANCE_GENERAL")` with configurable normalization and weighted score in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/estrategia/PerformanceGeneralEstrategia.java`
- [X] T016 [P] [US1] Implement `PERFORMANCE_POR_POSICION` as `@Component("PERFORMANCE_POR_POSICION")` with accent/separator-insensitive Spanish and English position mapping and configured positional weights in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/estrategia/PerformancePorPosicionEstrategia.java`
- [X] T017 [US1] Implement `CotizacionServiceImpl` in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionServiceImpl.java` to select the configured strategy from `Map<String, EstrategiaCotizacion>`, aggregate eligible statistics, compute per-metric maxima, persist complete `Cotizacion` rows, continue after per-player errors, and fail explicitly before persistence for invalid configuration
- [X] T018 [US1] Implement the weekly `CotizacionScheduler` in `backend/src/main/java/unq/losrecursionistas/backend/scheduler/CotizacionScheduler.java` with `@Scheduled(cron = "${scheduler.cotizacion.cron}", zone = "America/Argentina/Buenos_Aires")`, enable flag, and execution after `WhoScoredScheduler` without accessing `SecurityContext`
- [X] T019 [US1] Wire quotation repository queries and existing `EstadisticaPartido`/`Jugador` access through `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/CotizacionDAOSQL.java` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioCotizacionImpl.java` so the service uses database filtering for active players and the configured inclusive date window

**Checkpoint**: The periodic calculation and both interchangeable strategies are independently executable and unit-tested.

---

## Phase 4: User Story 2 - Consultar historial de cotizaciones (Priority: P1)

**Goal**: Expose a stable, paginated quotation history for an existing player with optional inclusive date filters.

**Independent Test**: Seed multiple quotations for one player, query with and without dates and with an empty range, and verify `Page<CotizacionResponse>`, default size 12, stable ordering, all contract fields, 400 for invalid ranges/pagination, and 404 for an unknown player.

### Implementation for User Story 2

- [X] T020 [US2] Implement the history operation in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionServiceImpl.java` with player existence validation, inclusive `desde`/`hasta` filtering, default `PageRequest` size 12, positive-size validation, and `timestampCalculo DESC, id DESC` sorting
- [X] T021 [US2] Map history pages to `CotizacionResponse` in `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/cotizacion/CotizacionResponse.java` without dereferencing lazy relations outside the service transaction
- [X] T022 [US2] Implement `GET /players/{id}/quotes` in `backend/src/main/java/unq/losrecursionistas/backend/controller/CotizacionController.java`, documenting parameters, page response, 400/404 errors, and inclusive date semantics with `@Operation` and `@ApiResponse`

**Checkpoint**: History is independently consumable as a paginated REST resource and does not fabricate data for missing players.

---

## Phase 5: User Story 3 - Consultar ranking de jugadores (Priority: P1)

**Goal**: Expose a paginated ranking by latest quotation with optional league and normalized-position filters, including the previous value and absolute variation.

**Independent Test**: Seed players with current/previous quotations, players without quotations, and mixed leagues/positions; query each filter combination and verify page size 12, descending current value, latest-only selection, null previous fields, absolute variation, and exclusion of players without quotations.

### Implementation for User Story 3

- [X] T023 [US3] Complete the paged ranking projection query in `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/CotizacionDAOSQL.java`, joining `Jugador -> Equipo -> Liga`, applying optional `ligaId` and normalized Spanish/English `posicion`, selecting latest/previous quotations with deterministic timestamp/id ordering, and calculating `abs(valorActual - valorAnterior)`
- [X] T024 [US3] Implement ranking orchestration in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionServiceImpl.java` with default page size 12, parameter validation, and `Page<RankingJugadorResponse>` mapping from the DAO projection
- [X] T025 [US3] Implement `GET /players/ranking` in `backend/src/main/java/unq/losrecursionistas/backend/controller/CotizacionController.java`, documenting filters, pagination, response fields, and consistent 400 errors with OpenAPI annotations

**Checkpoint**: Ranking is independently paginated, filtered, stable, and backed by the database projection rather than an in-memory history.

---

## Phase 6: User Story 4 - Solicitar recálculo manual (Priority: P2)

**Goal**: Allow only administrators to accept an asynchronous quotation recalculation while rejecting concurrent jobs.

**Independent Test**: Invoke the endpoint with admin, non-admin, unauthenticated, and overlapping requests; verify 202 acceptance under one second, 401/403 authorization behavior, 409 conflict without a second enqueue, fixed acceptance-time window, and worker release in `finally`.

### Implementation for User Story 4

- [X] T026 [P] [US4] Add the dedicated `AsyncCotizacionConfiguration` and `cotizacionTaskExecutor` in `backend/src/main/java/unq/losrecursionistas/backend/configuration/AsyncCotizacionConfiguration.java` so quotation work does not contend with ingestion and does not propagate `SecurityContext`
- [X] T027 [US4] Add the separate asynchronous worker bean in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionAsyncWorker.java` using `@Async("cotizacionTaskExecutor")`, fixed acceptance-time window, per-player error reporting, and `finally` release of the shared `AtomicBoolean`
- [X] T028 [US4] Add atomic job reservation and conflict handling to `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionServiceImpl.java`, returning an explicit active-job result without enqueueing a second recalculation
- [X] T029 [US4] Implement `POST /quotes/recalculate` in `backend/src/main/java/unq/losrecursionistas/backend/controller/CotizacionController.java` with `ROLE_ADMIN`, immediate 202 response, 409 conflict response, and OpenAPI documentation for 202/401/403/409/500
- [X] T030 [US4] Verify the existing authorization wiring in `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java` protects `POST /quotes/recalculate` with `ROLE_ADMIN` while leaving scheduler execution independent of HTTP authentication

**Checkpoint**: Administrators can trigger one asynchronous recalculation at a time; unauthorized and overlapping requests are rejected explicitly.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validate the complete feature against its contracts, performance targets, documentation, and operational behavior.

- [X] T031 [P] Update backend API documentation and feature notes in `backend/README.md` with the three quotation endpoints, scheduler configuration, strategy names, and admin requirement
- [X] T032 [P] Review `backend/src/main/resources/application.yml` and all quotation classes for ASCII Spanish naming, constructor injection, no functional changes to existing entities, no execution/version tables, and no frontend scope leakage
- [X] T033 Run the focused unit suite from `backend/` with `.\gradlew.bat test --tests "*CotizacionServiceImplTest" --tests "*PerformanceGeneralEstrategiaTest" --tests "*PerformancePorPosicionEstrategiaTest" --tests "*CotizacionTest"` and resolve regressions in `backend/src/test/java/unq/losrecursionistas/backend/`
- [X] T034 Run the scenarios in `specs/007-sistema-cotizacion/quickstart.md`, including REST authorization, inclusive ranges, empty pages, scheduler-without-session, concurrent acceptance, non-null persisted fields, omitted-player logs, and the p95 targets for 100 concurrent history/ranking requests

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T002 and T003 can run in parallel after T001 establishes the package/configuration context.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all stories; T004-T010 are mostly parallel by file, while T009 and T010 consume the contracts/types defined by T004-T008.
- **User Stories (Phases 3-6)**: Depend on Phase 2. US1 must complete before the other stories can be demonstrated end-to-end because it supplies the calculation service and quotation persistence, although US2 and US3 implementation can proceed in parallel once shared DAO contracts exist.
- **Polish (Phase 7)**: Depends on the desired user stories and their focused validation.

### User Story Dependencies

- **US1 (P1)**: Depends on Phase 2; no other story dependency. This is the MVP.
- **US2 (P1)**: Depends on Phase 2 and reuses the quotation persistence/service from US1; its history endpoint remains independently testable with seeded quotations.
- **US3 (P1)**: Depends on Phase 2 and the ranking projection; it reuses the quotation service/DTO/controller from US2 where appropriate, but is independently testable with seeded quotation data.
- **US4 (P2)**: Depends on the recalculation service contract from Phase 2 and the US1 recalculation operation; authorization and async execution are independently testable.

### Within Each User Story

- Tests are written before implementation and must fail for missing behavior.
- Models and immutable contracts precede services; services precede controllers and schedulers.
- Database filtering/projections precede service mapping.
- A story is complete only when its independent test criteria pass without requiring a frontend.

### Parallel Opportunities

- Phase 1: T002 and T003.
- Phase 2: T004-T008 can be split by file; T009-T010 follow the shared contracts.
- US1: T011-T014 and T015-T016 can run in parallel; T017-T019 follow their outputs.
- US2 and US3 can be assigned in parallel after Phase 2, with care to avoid simultaneous edits to `CotizacionServiceImpl.java` and `CotizacionController.java`.
- US4: T026 can run in parallel with T027; T028-T030 require the async worker and service contract.
- Phase 7: T031-T032 can run in parallel; T033-T034 run after implementation.

## Parallel Example: User Story 1

```text
Task T011: Cotizacion model tests in backend/src/test/java/unq/losrecursionistas/backend/model/CotizacionTest.java
Task T012: General strategy tests in backend/src/test/java/unq/losrecursionistas/backend/service/PerformanceGeneralEstrategiaTest.java
Task T013: Positional strategy tests in backend/src/test/java/unq/losrecursionistas/backend/service/PerformancePorPosicionEstrategiaTest.java
Task T014: Service tests in backend/src/test/java/unq/losrecursionistas/backend/service/CotizacionServiceImplTest.java
Task T015: PerformanceGeneralEstrategia.java
Task T016: PerformancePorPosicionEstrategia.java
```

## Parallel Example: User Stories 2-4

```text
After Phase 2:
Developer A: T020-T022 (history)
Developer B: T023-T025 (ranking)
Developer C: T026-T030 (manual asynchronous recalculation)
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundation.
3. Complete US1 calculation, strategies, persistence, and scheduler.
4. Run T011-T014 and validate the US1 independent test criteria.
5. Stop for an MVP demo before adding query and manual-operation surfaces.

### Incremental Delivery

1. Add US2 history and validate it with seeded append-only quotations.
2. Add US3 ranking and validate latest/previous projection and filters.
3. Add US4 admin asynchronous recalculation and concurrency protection.
4. Complete Phase 7 quickstart, documentation, and performance validation.
