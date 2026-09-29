# Tasks: Infraestructura base del backend y CI

**Input**: Design documents from `/specs/001-infra-backend-ci/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md

**Tests**: Se incluyen tareas de prueba porque la especificacion requiere una estructura base para `model` y `service` y una validacion automatica del pipeline.

**Organization**: Las tareas estan agrupadas por historia de usuario para permitir implementacion y validacion independientemente.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias)
- **[Story]**: Se usa solo para tareas de historia de usuario (US1, US2, US3)
- Se incluyen rutas exactas de archivo en la descripcion

## Phase 1: Setup (Infraestructura compartida)

**Purpose**: Inicializacion del proyecto y estructura base

- [x] T001 Create the backend project structure per implementation plan in `backend/src/main/java/unq/losrecursionistas/backend/`, `backend/src/main/resources/`, and `backend/src/test/java/unq/losrecursionistas/backend/`
- [x] T002 [P] Configure Gradle project metadata and Java 21 toolchain in `backend/build.gradle`
- [x] T003 [P] Create the GitHub Actions workflow in `.github/workflows/ci.yml` to run `./gradlew clean test` from `backend/`

---

## Phase 2: Foundational (Prerequisitos bloqueantes)

**Purpose**: Infraestructura central sin la cual no puede arrancar ninguna historia de usuario

**⚠️ CRITICAL**: Ninguna tarea de historias puede comenzar antes de completar esta fase

- [x] T004 Create the package skeleton for `model`, `controller`, `dto`, `service/interfaces`, `service/impl`, `persistence/repository/interfaces`, `persistence/repository/impl`, `configuration`, `security`, and `exceptions` under `backend/src/main/java/unq/losrecursionistas/backend/`
- [x] T005 Configure the single base application properties file in `backend/src/main/resources/application.properties` with the required PostgreSQL settings for `postgres` / `root`
- [x] T006 Create the runtime exception hierarchy in `backend/src/main/java/unq/losrecursionistas/backend/exceptions/` with base classes for domain and validation errors
- [x] T007 [P] Implement the global error handler in `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ControladorErrores.java` using `@ControllerAdvice`
- [x] T008 [P] Create the base security package skeleton in `backend/src/main/java/unq/losrecursionistas/backend/security/` for future authentication and authorization configuration
- [x] T009 [P] Create the base configuration package in `backend/src/main/java/unq/losrecursionistas/backend/configuration/` for Spring Boot beans and OpenAPI setup

**Checkpoint**: La base arquitectonica y de configuracion queda lista para iniciar historias de usuario en paralelo

---

## Phase 3: User Story 1 - Inicializacion de la base del backend (Priority: P1) 🎯 MVP

**Goal**: Dejar la estructura base del backend con capas separadas y una configuracion centralizada

**Independent Test**: Ejecutar la aplicacion con Gradle y verificar que arranca con la unica configuracion en `application.properties` y que la arquitectura queda ordenada por paquetes.

### Implementation for User Story 1

- [x] T010 [P] [US1] Create the base domain package contracts in `backend/src/main/java/unq/losrecursionistas/backend/model/` with the required package boundary for future entities
- [x] T011 [P] [US1] Create the REST controller and DTO package skeleton in `backend/src/main/java/unq/losrecursionistas/backend/controller/` and `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/`
- [x] T012 [US1] Create the service contracts and implementations in `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/` and `backend/src/main/java/unq/losrecursionistas/backend/service/impl/`
- [x] T013 [US1] Create the persistence repository contracts and implementations in `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` and `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`
- [x] T014 [US1] Add the base validation and exception mapping rules in `backend/src/main/java/unq/losrecursionistas/backend/exceptions/` so domain and validation errors return consistent HTTP responses
- [x] T015 [US1] Ensure the app bootstrap in `backend/src/main/java/unq/losrecursionistas/backend/BackendApplication.java` is aligned with the single `application.properties` configuration and the strict package schema

**Checkpoint**: La historia 1 queda funcional e independiente y puede validarse sin depender de otras historias

---

## Phase 4: User Story 2 - Validacion automatizada del proyecto (Priority: P2)

**Goal**: Configurar la validacion automatica de compilacion y pruebas en GitHub Actions

**Independent Test**: Ejecutar `cd backend && ./gradlew clean test` y verificar que el workflow de GitHub Actions deja evidencia de compilacion y pruebas exitosas.

### Tests for User Story 2

- [x] T016 [P] [US2] Add the baseline model test suite in `backend/src/test/java/unq/losrecursionistas/backend/model/` to validate the foundational model and exception structure
- [x] T017 [P] [US2] Add the baseline service test suite in `backend/src/test/java/unq/losrecursionistas/backend/service/` to validate default service behavior and error handling

### Implementation for User Story 2

- [x] T018 [US2] Implement the CI pipeline in `.github/workflows/ci.yml` with checkout, Java 21 setup, dependency resolution, compilation, and `./gradlew clean test` execution
- [x] T019 [US2] Update the project run guide in `README.md` or `specs/001-infra-backend-ci/quickstart.md` to describe the CI and local validation commands

**Checkpoint**: La historia 2 queda validada de forma independiente y ofrece garantia automatica de integridad del proyecto

---

## Phase 5: User Story 3 - Preparacion para seguridad y extensibilidad (Priority: P3)

**Goal**: Preparar la base para autenticacion, autorizacion y futuras ampliaciones sin romper la arquitectura

**Independent Test**: Verificar que el paquete de seguridad permanece separado de `model`, `service`, y `persistence`, y que la aplicacion puede crecer sin mezclar responsabilidades.

### Implementation for User Story 3

- [x] T020 [P] [US3] Create the base security configuration classes in `backend/src/main/java/unq/losrecursionistas/backend/security/` for future authentication and authorization
- [x] T021 [US3] Define the extension points for future access control and token handling without coupling the business layer to transport or persistence details
- [x] T022 [US3] Add package-boundary validation and documentation in `README.md` or `specs/001-infra-backend-ci/plan.md` to keep future modules aligned with the constitution
- [x] T023 [P] [US3] Add final baseline tests in `backend/src/test/java/unq/losrecursionistas/backend/model/` and `backend/src/test/java/unq/losrecursionistas/backend/service/` to confirm the error and security scaffolding remain stable

**Checkpoint**: La infraestructura queda lista para incorporarse a nuevas funcionalidades con una base segura y extensible

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Ajustes finales para calidad, mantenibilidad y consistencia de la infraestructura

- [x] T024 [P] Review ASCII-only naming conventions across new files, classes, methods, and paths under `backend/src/main/java/`, `backend/src/test/java/`, and `.github/workflows/`
- [x] T025 [P] Run the local validation flow from `specs/001-infra-backend-ci/quickstart.md` and confirm the project meets the startup and test prerequisites
- [x] T026 Verify that all new code remains in the required package layout: `model`, `controller`, `dto`, `service/interfaces`, `service/impl`, `persistence/repository/interfaces`, `persistence/repository/impl`, `configuration`, `security`, and `exceptions`
- [x] T027 Confirm that no additional `application*.properties` files are introduced and that the single `application.properties` strategy remains in place

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias, puede comenzar inmediatamente
- **Foundational (Phase 2)**: depende del Setup y bloquea todas las historias de usuario
- **User Stories (Phase 3+)**: dependen del cierre del Phase 2
- **Polish (Phase 6)**: depende de que todas las historias deseadas hayan quedado completadas

### User Story Dependencies

- **User Story 1 (P1)**: puede ejecutarse luego del Phase 2 y no depende de otras historias
- **User Story 2 (P2)**: puede ejecutarse luego del Phase 2 y queda validada de forma independiente
- **User Story 3 (P3)**: puede ejecutarse luego del Phase 2 y sirve como base para seguridad y extensibilidad

### Within Each User Story

- Definir primero los contratos y la estructura base
- Crear paquetes y configuracion antes de la logica de servicio
- Validar la historia por separado antes de avanzar al siguiente nivel de prioridad

### Parallel Opportunities

- Los tasks T002, T003, T007, T008, T009, T010, T011, T016, T017, T020, T023, T024 y T025 pueden trabajarse en paralelo cuando el equipo tenga capacidad suficiente
- La implementacion de las historias puede ocurrir en paralelo despues del cierre del Phase 2
- Las pruebas de `model` y `service` para la base del backend pueden ejecutarse en paralelo dentro de la historia 2

---

## Parallel Example: User Story 1

```bash
# Ejecutar en paralelo las tareas del primer incremento base del backend:
Task: "Create the base domain package contracts in backend/src/main/java/unq/losrecursionistas/backend/model/"
Task: "Create the REST controller and DTO package skeleton in backend/src/main/java/unq/losrecursionistas/backend/controller/"
Task: "Create the service contracts and implementations in backend/src/main/java/unq/losrecursionistas/backend/service/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational
3. Completar Phase 3: User Story 1
4. Validar la historia 1 de forma independiente
5. Solo luego continuar con historias 2 y 3

### Incremental Delivery

1. Setup + Foundational -> base operativa
2. User Story 1 -> MVP funcional
3. User Story 2 -> validacion automatica
4. User Story 3 -> seguridad y extensibilidad
5. Phase 6 -> calidad final y validacion global

### Parallel Team Strategy

Con multiples desarrolladores:

1. Equipo completa Setup + Foundational junto
2. Una persona trabaja en User Story 1
3. Otra persona trabaja en User Story 2
4. Otra persona trabaja en User Story 3
5. Todos validan la integracion final antes de cerrar

---

## Notes

- [P] tasks = archivos distintos y sin dependencias directas
- [Story] label mantiene trazabilidad de la historia de usuario
- Cada historia debe poder completarse y testearse por separado
- La base de infraestructura debe conservarse con una sola configuracion y una arquitectura estricta
- No se debe introducir slack ni cambios de stack no justificados por la constitution
