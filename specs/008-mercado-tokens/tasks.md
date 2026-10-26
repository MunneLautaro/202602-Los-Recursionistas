---

description: "Task list for Mercado de Tokens"
---

# Tasks: Mercado de Tokens

**Input**: Design documents from `specs/008-mercado-tokens/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/openapi.yaml`, `quickstart.md`

**Tests**: Required by FR-023, the constitution, and `quickstart.md`. Tests remain under `backend/src/test/java/.../model` and `backend/src/test/java/.../service`.

**Organization**: Tasks are grouped by user story so each increment can be implemented and validated independently after the foundational phase.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare the backend module for the market feature without introducing new applications or frontend scope.

- [X] T001 [P] Add market persistence and transaction dependencies/configuration required by the implementation plan in `backend/build.gradle`
- [X] T002 [P] Document market defaults, `America/Argentina/Buenos_Aires` timezone, and transactional isolation settings in `backend/src/main/resources/application.yml`
- [X] T003 [P] Verify the existing JWT authentication and `ROLE_ADMIN` conventions used by market endpoints in `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`
- [X] T004 [P] Record the five endpoint shapes and pagination defaults from `specs/008-mercado-tokens/contracts/openapi.yaml` as implementation constraints in the backend task scope

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared domain, persistence, transaction, and error-handling foundations before any user story.

**CRITICAL**: All user story work depends on this phase.

- [X] T005 Extend credit state and invariant operations in `backend/src/main/java/unq/losrecursionistas/backend/model/Usuario.java`: `saldo` and `saldoReservado` are `NUMERIC(19,2)`, non-negative, and `saldoReservado` never exceeds `saldo`; provide acreditar, reservar, liberar, and debitar operations
- [X] T006 [P] Define token/order/market enums and shared domain exceptions under `backend/src/main/java/unq/losrecursionistas/backend/model/` and `backend/src/main/java/unq/losrecursionistas/backend/exceptions/` for invalid quantities, insufficient resources, inactive players, and missing quotes
- [X] T007 [P] Create the `TenenciaToken` entity in `backend/src/main/java/unq/losrecursionistas/backend/model/TenenciaToken.java` with required `usuario`/`jugador` FKs, non-negative `cantidadTotal`/`cantidadReservada`, `cantidadDisponible`, and unique `(usuario_id, jugador_id)`
- [X] T008 [P] Create the `Orden` entity in `backend/src/main/java/unq/losrecursionistas/backend/model/Orden.java` with `COMPRA`/`VENTA`, positive immutable `cantidadOriginal`, bounded non-negative `cantidadDisponible`, positive immutable `precioUnitario`, `ACTIVA`/`COMPLETADA`, zoned creation timestamp, and `esLiquidezInicial`
- [X] T009 [P] Create immutable `TransaccionMercado` and append-only `AuditoriaTransaccional` entities in `backend/src/main/java/unq/losrecursionistas/backend/model/TransaccionMercado.java` and `backend/src/main/java/unq/losrecursionistas/backend/model/AuditoriaTransaccional.java`, including required snapshots, author, Argentina timezone, and no update/delete operations
- [X] T010 Create repository contracts and Spring Data DAOs under `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/`, `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`, and `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/` with `PESSIMISTIC_WRITE` locking for users, holdings, active orders, and liquidity initialization
- [X] T011 Add service ports `MatchingEngine`, `MercadoService`, and `LiquidezInicialService` under `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/`, keeping matching independent from controllers and schedulers
- [X] T012 Add shared request/response DTO types under `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/orden/` and `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/usuario/` with `@NotNull`, `@Positive`, integer token quantities, and the OpenAPI response fields
- [X] T013 [P] Add model tests for credit and holding invariants in `backend/src/test/java/unq/losrecursionistas/backend/model/UsuarioTest.java` and `backend/src/test/java/unq/losrecursionistas/backend/model/TenenciaTokenTest.java`
- [X] T014 [P] Add model tests for order transitions, immutable captured price, market transactions, and append-only audit behavior in `backend/src/test/java/unq/losrecursionistas/backend/model/OrdenTest.java` and `backend/src/test/java/unq/losrecursionistas/backend/model/TransaccionMercadoTest.java`

**Checkpoint**: Shared entities, locks, service ports, DTO validation, and domain invariants are ready; user stories can proceed.

---

## Phase 3: User Story 1 - Comprar tokens al precio vigente (Priority: P1) MVP

**Goal**: Accept an authenticated buy request at the player's current quote, reserve/debit credits atomically, match available sells, and transfer tokens without negative balances.

**Independent Test**: With a sell order and sufficient balance, `POST /orders/buy` buys a partial quantity, returns captured price/total, credits the buyer's holding, and leaves the exact sell remainder active.

### Tests for User Story 1

- [X] T015 [P] [US1] Add service tests for valid partial purchase, completed match, insufficient credits, inactive/missing player, and missing current quote in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoServiceCompraTest.java`
- [X] T016 [P] [US1] Add matching tests for same-player/same-price filtering, minimum-quantity execution, order completion, and preserved FIFO timestamp in `backend/src/test/java/unq/losrecursionistas/backend/service/MatchingEngineTest.java`
- [X] T017 [P] [US1] Add rollback/concurrency tests proving no partial reservation, negative balance, double spend, or audit remains after a failed match in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoTransactionTest.java`

### Implementation for User Story 1

- [X] T018 [US1] Implement locked credit/holding/order repository operations and price lookup in `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/UsuarioDAOSQL.java`, `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/TenenciaTokenDAOSQL.java`, `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/OrdenDAOSQL.java`, and `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/CotizacionDAOSQL.java`
- [X] T019 [US1] Implement FIFO partial matching in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MatchingEngineImpl.java`, matching only active orders of the same player and captured price and transferring `min(cantidadDisponible)` atomically
- [X] T020 [US1] Implement buy orchestration in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MercadoServiceImpl.java`, reserving `cantidad * precioUnitario`, using `@Transactional` with `REPEATABLE_READ` or stronger isolation, and rolling back all financial changes on failure
- [X] T021 [US1] Implement authenticated buy endpoint `POST /orders/buy` in `backend/src/main/java/unq/losrecursionistas/backend/controller/MercadoController.java` and map the result to `OrdenResponse` without exposing persistence details
- [X] T022 [US1] Persist one `TransaccionMercado` and the required append-only `AuditoriaTransaccional` records inside the same match transaction using `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioTransaccionMercadoImpl.java` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioAuditoriaTransaccionalImpl.java`

**Checkpoint**: The MVP purchase flow and its atomic match are independently testable.

---

## Phase 4: User Story 2 - Publicar y ejecutar ventas P2P (Priority: P1)

**Goal**: Allow token holders to reserve and sell tokens, then settle compatible peer buy/sell orders FIFO with partial execution.

**Independent Test**: A user with ten available tokens publishes a sell order; another user publishes a compatible buy order; balances, holdings, reservations, orders, transaction, and audit all reconcile.

### Tests for User Story 2

- [X] T023 [P] [US2] Add service tests for valid sell reservation, insufficient tokens, fractional/zero/negative quantities, and no order or holding change after rejection in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoServiceVentaTest.java`
- [X] T024 [P] [US2] Add FIFO P2P tests with multiple compatible orders, partial fills, and unchanged original creation timestamps in `backend/src/test/java/unq/losrecursionistas/backend/service/MatchingEngineP2PTest.java`
- [X] T025 [P] [US2] Add concurrent sell/buy tests ensuring locks permit at most the available tokens/credits and never create double sales in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoConcurrencyTest.java`

### Implementation for User Story 2

- [X] T026 [US2] Implement sell orchestration in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MercadoServiceImpl.java`, validating active player and current quote, reserving available holdings, and creating an active order at the captured price
- [X] T027 [US2] Extend matching settlement in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MatchingEngineImpl.java` to release reservations, transfer tokens/credits between distinct users, complete only zero-remnant orders, and preserve partial remnants
- [X] T028 [US2] Implement authenticated sell endpoint `POST /orders/sell` in `backend/src/main/java/unq/losrecursionistas/backend/controller/MercadoController.java` using the shared `OrdenRequest` and `OrdenResponse`
- [X] T029 [US2] Integrate market transaction/audit persistence for P2P seller proceeds and buyer holdings in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MatchingEngineImpl.java` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioAuditoriaTransaccionalImpl.java`

**Checkpoint**: P2P selling, partial FIFO settlement, and concurrency protections are independently testable.

---

## Phase 5: User Story 3 - Mantener liquidez inicial y de nuevos jugadores (Priority: P1)

**Goal**: Issue exactly 100 tokens and one custodial initial sell order per active quoted player, idempotently at startup and after catalog additions.

**Independent Test**: Run initialization twice with active quoted players and verify exactly one 100-token initial order per player; add a new quoted player and verify only that player is initialized.

### Tests for User Story 3

- [X] T030 [P] [US3] Add model/service tests for 100-token emission, initial order price/total, inactive or unquoted player rejection, and no mutation of existing orders in `backend/src/test/java/unq/losrecursionistas/backend/service/LiquidezInicialServiceTest.java`
- [X] T031 [P] [US3] Add idempotency and concurrent initialization tests proving one custodial order per player even across repeated or racing calls in `backend/src/test/java/unq/losrecursionistas/backend/service/LiquidezInicialConcurrencyTest.java`

### Implementation for User Story 3

- [X] T032 [US3] Implement idempotent initial liquidity in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/LiquidezInicialServiceImpl.java`, creating exactly 100 custodial tokens and one `esLiquidezInicial` sell order per active player with a current quote
- [X] T033 [US3] Add the unique logical/indexed liquidity lookup and locked creation path in `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/OrdenDAOSQL.java` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioOrdenImpl.java`
- [X] T034 [US3] Integrate new-player liquidity creation after quote/catalog synchronization in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/SincronizacionService.java` without altering existing players or orders
- [X] T035 [US3] Wire startup/new-player invocation through the existing scheduler or bootstrap entry point in `backend/src/main/java/unq/losrecursionistas/backend/scheduler/` and `backend/src/main/java/unq/losrecursionistas/backend/service/impl/WhoscoredBootstrapService.java`, without adding unrelated scheduling behavior

**Checkpoint**: Initial market liquidity is available, repeatable, and safe under concurrent initialization.

---

## Phase 6: User Story 4 - Consultar órdenes y operaciones (Priority: P2)

**Goal**: Expose paginated active orders and the authenticated user's own paginated transaction history with captured prices, quantities, totals, status, and zoned timestamps.

**Independent Test**: After creating and executing orders, query filtered active orders and user transactions with `page`/`size` and verify ownership and totals.

### Tests for User Story 4

- [X] T036 [P] [US4] Add service tests for active-order filters by player/type, default page size 12, remnant total calculation, and user-owned transaction pagination in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoConsultaServiceTest.java`
- [X] T037 [P] [US4] Add authorization/error tests for cross-user transaction access and invalid pagination in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoConsultaAutorizacionTest.java`

### Implementation for User Story 4

- [X] T038 [US4] Add paginated repository queries and indexes for active orders and user transactions in `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/OrdenDAOSQL.java` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/TransaccionMercadoDAOSQL.java`
- [X] T039 [US4] Implement paginated consultation methods in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MercadoServiceImpl.java` using `PageRequest` default size 12 and restricting transaction history to the requesting user
- [X] T040 [US4] Implement `GET /orders` and `GET /users/{id}/transactions` in `backend/src/main/java/unq/losrecursionistas/backend/controller/MercadoController.java`, including `jugadorId`, `tipo`, `page`, and `size` mapping to OpenAPI response DTOs

**Checkpoint**: The book and personal transaction history are paginated, filtered, and authorization-safe.

---

## Phase 7: User Story 5 - Recargar créditos (Priority: P2)

**Goal**: Provide fictitious positive credit top-ups with authorization and atomic auditability, without payment-provider integration.

**Independent Test**: A user credits a positive amount and sees the exact new balance; zero and negative amounts are rejected without balance or audit changes.

### Tests for User Story 5

- [X] T041 [P] [US5] Add service tests for positive top-up, zero/negative rejection, unauthorized account access, and unchanged balance after failure in `backend/src/test/java/unq/losrecursionistas/backend/service/RecargaCreditosServiceTest.java`
- [X] T042 [P] [US5] Add rollback/audit tests proving a failed top-up leaves no partial balance or audit record in `backend/src/test/java/unq/losrecursionistas/backend/service/RecargaCreditosRollbackTest.java`

### Implementation for User Story 5

- [X] T043 [US5] Implement positive fictitious credit recharge in `backend/src/main/java/unq/losrecursionistas/backend/service/impl/MercadoServiceImpl.java` or a dedicated service implementation under `backend/src/main/java/unq/losrecursionistas/backend/service/impl/`, using the `Usuario` invariant operation and transaction boundary
- [X] T044 [US5] Implement authenticated `POST /users/{id}/credits` in `backend/src/main/java/unq/losrecursionistas/backend/controller/CreditosController.java`, returning `SaldoResponse` with total, reserved, and available balances
- [X] T045 [US5] Add authorization checks and recharge audit persistence in `backend/src/main/java/unq/losrecursionistas/backend/controller/CreditosController.java` and `backend/src/main/java/unq/losrecursionistas/backend/service/impl/`, allowing only the account owner or the existing administrative rule

**Checkpoint**: Users can safely fund their accounts for the market without real-money dependencies.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Verify the complete feature against its contracts, invariants, and operational documentation.

- [X] T046 [P] Add repository schema/index verification for unique holdings, active-order FIFO lookup, liquidity idempotency, transaction history, and audit lookup in `backend/src/test/java/unq/losrecursionistas/backend/service/MercadoPersistenceTest.java`
- [X] T047 [P] Validate all five endpoint request/response mappings against `specs/008-mercado-tokens/contracts/openapi.yaml` and update `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/` where fields or validation differ
- [X] T048 [P] Document market limitations, fictitious credits, captured prices, no cancellation/expiration/fees, and append-only audit guarantees in `backend/README.md`
- [X] T049 Run the complete feature validation from `specs/008-mercado-tokens/quickstart.md` with `backend/gradlew.bat test`, including rollback, insufficient resources, idempotency, pagination, and simulated concurrency scenarios
- [X] T050 Review all market code for atomic `@Transactional` boundaries, `PESSIMISTIC_WRITE` acquisition before validation/mutation, non-negative balances/holdings, and absence of update/delete paths for historical records in `backend/src/main/java/unq/losrecursionistas/backend/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001-T004 can run in parallel.
- **Foundational (Phase 2)**: Depends on Setup; T005-T014 establish shared domain and persistence prerequisites.
- **User Stories (Phases 3-7)**: Depend on Phase 2. US1, US2, and US3 are P1 and can proceed in parallel when shared repository changes are coordinated; US2 consumes matching behavior from US1, and US3 supplies initial sell liquidity used by US1.
- **US4 and US5**: Depend on shared market service/repositories from US1/US2, while their DTO/controller work can be prepared after Phase 2.
- **Polish (Phase 8)**: Depends on all selected user stories.

### User Story Dependencies

- **US1 (P1)**: Depends on Foundational; establishes buy orchestration and matching settlement.
- **US2 (P1)**: Depends on Foundational and the matching settlement shape from US1; independently validates P2P sell behavior.
- **US3 (P1)**: Depends on Foundational and quote/player repositories; provides initial liquidity consumed by US1, but its idempotency tests are independently executable.
- **US4 (P2)**: Depends on order/transaction persistence from US1-US2; consultation remains independently testable with fixtures.
- **US5 (P2)**: Depends only on foundational `Usuario` credit operations and audit persistence; it can be implemented in parallel with US4.

### Parallel Opportunities

- Setup tasks T001-T004 are independent.
- Foundational entity/model tasks T006-T009 and their tests T013-T014 can run in parallel with repository/service-port preparation, subject to shared package coordination.
- Within each story, test tasks marked `[P]` can be written in parallel before implementation.
- US3 can be developed alongside US1 after the foundation; US4 and US5 can be developed in parallel once shared DTO and service contracts are stable.

### Parallel Example: User Story 1

```text
Task T015: buy service tests
Task T016: matching tests
Task T017: rollback/concurrency tests
```

### Parallel Example: User Story 2

```text
Task T023: sell validation tests
Task T024: FIFO P2P tests
Task T025: concurrent sell/buy tests
```

### Parallel Example: User Story 3

```text
Task T030: liquidity service tests
Task T031: idempotency/concurrency tests
```

### Parallel Example: User Stories 4 and 5

```text
Task T036-T040: order/transaction consultation
Task T041-T045: credit recharge flow
```

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2.
2. Complete US1, including matching, transaction/audit persistence, and rollback tests.
3. Complete US3 so the MVP has repeatable initial sell liquidity.
4. Validate the partial purchase scenario from `quickstart.md`.

### Incremental Delivery

1. Add US2 for user-to-user selling and FIFO partial execution.
2. Add US4 for paginated order and transaction visibility.
3. Add US5 for fictitious credit recharge.
4. Run Phase 8 and the full quickstart validation.

### Scope Guardrails

- Do not add cancellation, expiration, limit/stop-loss pricing, commissions, real payments, or a new frontend screen.
- Keep all financial mutations atomic and audited; historical transactions/audits are append-only.
- Keep matching behind `service/interfaces/MatchingEngine`, independent of HTTP and scheduling.

## Completion Criteria

- Every task uses the required `- [ ] Txxx` checklist format.
- Story tasks include exactly one `[USn]` label and explicit repository-relative file paths.
- `[P]` appears only on tasks that can work on different files or independent tests without incomplete prerequisites.
- Each story has a stated independent test and implementation checkpoint.
