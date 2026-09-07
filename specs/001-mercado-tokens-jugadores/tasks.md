---
description: "Tareas del alcance inicial del Mercado de Tokens de Jugadores"
---

# Tasks: Alcance inicial - Mercado de Tokens de Jugadores

**Input**: Design documents from `/specs/001-mercado-tokens-jugadores/`

**Scope**: Esta lista cubre el alcance inicial del sistema. El backend Spring Boot
con Gradle ya existe en `backend/`; no se crea ni se reemplaza el proyecto.

**Prerequisites**: `plan.md`, `spec.md`, `data-model.md`, `contracts/openapi.yaml`,
`quickstart.md` y `.specify/memory/constitution.md`.

## Alcance funcional

Incluye GitHub Actions, build exitoso, SonarCloud con objetivo de menos de 10
issues, JWT, Swagger/OpenAPI v3, modelo mínimo, tests unitarios, creación de
usuario con ApiKey y endpoint inicial de catálogo de jugadores.

Quedan fuera del alcance actual frontend, compra/venta, portfolio completo,
scheduler, cotizaciones avanzadas, ranking, PostgreSQL/Testcontainers salvo que
una prueba puntual lo requiera, y adapters o integraciones externas.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Configurar el alcance inicial sobre el backend Spring Boot/Gradle existente.

- [x] T001 Verificar el proyecto existente en `backend/build.gradle`, conservar Java 21, Spring Boot 4.1.1 y `BackendApplicationTests.contextLoads` sin modificarlo.
- [x] T002 [P] Configurar el workflow de build y tests en `.github/workflows/ci.yml`, usando `backend/gradlew` o `backend/gradlew.bat`, Java 21 y ejecución desde el directorio `backend/`.
- [x] T003 [P] Configurar análisis SonarCloud y cobertura mínima en `backend/build.gradle` y `sonar-project.properties`, dejando documentadas las variables de proyecto y organización sin incluir secretos.
- [x] T004 [P] Agregar en `backend/build.gradle` las dependencias mínimas de Spring Security, JWT, Actuator y springdoc OpenAPI necesarias para este alcance.
- [x] T005 [P] Configurar perfiles local y test en `backend/src/main/resources/application.properties` y `backend/src/test/resources/application.properties`, sin secretos versionados.
- [x] T006 Ejecutar el build y los tests existentes desde `backend/` y corregir únicamente los problemas introducidos por la configuración de este alcance.

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Crear las capacidades compartidas que bloquean seguridad, modelo y
endpoints.

- [x] T007 Definir la organización transversal de paquetes bajo `backend/src/main/java/unq/losrecursionistas/backend/` con `model`, `controller/dto`, `services/interfaces`, `services/impl`, `persistence/repository/interfaces`, `persistence/repository/impl`, `exceptions`, `configuration` y `security`.
- [x] T008 [P] Definir el manejo de errores y respuesta `ErrorResponse` con mensaje en español y `correlationId` en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/`.
- [x] T009 [P] Implementar configuración de Correlation ID para requests en `backend/src/main/java/unq/losrecursionistas/backend/configuration/` y agregar el header en las respuestas HTTP.
- [x] T010 [P] Configurar Swagger/OpenAPI v3 mediante `backend/src/main/java/unq/losrecursionistas/backend/configuration/OpenApiConfig.java` y verificar que el endpoint de documentación se publique.
- [x] T011 Actualizar `specs/001-mercado-tokens-jugadores/contracts/openapi.yaml` con los endpoints de creación de usuario, generación/uso de ApiKey, obtención de JWT y catálogo inicial, manteniendo `bearerAuth` para endpoints protegidos.
- [x] T012 Documentar en `specs/001-mercado-tokens-jugadores/quickstart.md` el flujo inicial: crear usuario, obtener ApiKey, obtener JWT, invocar un endpoint protegido y consultar el catálogo.

## Phase 3: User Story 1 - Catálogo inicial de jugadores (Priority: P1)

**Goal**: Permitir consultar el catálogo de jugadores mediante una API REST
versionada y documentada.

**Independent Test**: Con datos de catálogo cargados, `GET /players` devuelve
jugadores activos, aplica filtros válidos y responde errores consistentes ante
parámetros inválidos.

- [x] T013 [P] [US1] Crear las pruebas unitarias del modelo `Jugador` en `backend/src/test/java/unq/losrecursionistas/backend/model/JugadorTest.java`, cubriendo nombre obligatorio, estado activo/inactivo y datos de liga/equipo/posición.
- [x] T014 [P] [US1] Crear las pruebas unitarias del modelo `Liga` en `backend/src/test/java/unq/losrecursionistas/backend/model/LigaTest.java`, cubriendo código único y estado activo.
- [x] T015 [US1] Implementar el modelo `Jugador` en `backend/src/main/java/unq/losrecursionistas/backend/model/Jugador.java` con invariantes básicas del dominio y nombres en español sin acentos ni `n`.
- [x] T016 [US1] Implementar el modelo `Liga` en `backend/src/main/java/unq/losrecursionistas/backend/model/Liga.java` y su relación mínima con `Jugador`.
- [ ] T017 [P] [US1] Crear DTOs de respuesta y filtros del catálogo en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/` con validación de forma, límites, trimming y sanitización.
- [x] T018 [US1] Implementar `JugadorRepository` y `JugadorRepositoryImpl` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`, sin incorporar todavía proveedores externos.
- [x] T019 [US1] Implementar `JugadorService` y `JugadorServiceImpl` en `backend/src/main/java/unq/losrecursionistas/backend/services/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/services/impl/` para resolver filtros, devolver solo jugadores activos por defecto y validar parámetros.
- [x] T020 [US1] Implementar `JugadorController` en `backend/src/main/java/unq/losrecursionistas/backend/controller/JugadorController.java` con `GET /players`, paginación básica y respuestas `200` y `400` según `contracts/openapi.yaml`.
- [x] T021 [US1] Agregar pruebas MockMvc del catálogo en `backend/src/test/java/unq/losrecursionistas/backend/web/JugadorControllerTest.java`, cubriendo respuesta exitosa, filtros inválidos y `correlationId`.
- [x] T022 [US1] Ejecutar la prueba independiente de US1 y verificar que Swagger/OpenAPI describe `GET /players` en `backend/src/main/java/unq/losrecursionistas/backend/configuration/OpenApiConfig.java` o mediante anotaciones del controller.

## Phase 4: Seguridad - Usuario, ApiKey y JWT

**Purpose**: Permitir registrar usuarios y proteger el acceso a los endpoints que
lo requieran mediante ApiKey y JWT.

**Independent Test**: Se puede crear un usuario, recibir una ApiKey, obtener un
JWT válido y comprobar que un endpoint protegido acepta el token mientras rechaza
credenciales ausentes, inválidas o expiradas.

- [x] T023 [P] Crear pruebas unitarias de `Usuario` y `ApiKey` en `backend/src/test/java/unq/losrecursionistas/backend/model/UsuarioTest.java` y `backend/src/test/java/unq/losrecursionistas/backend/model/ApiKeyTest.java`, cubriendo username único, estado habilitado, clave no vacía y representación segura.
- [x] T024 [P] Crear pruebas de servicio para creación de usuario y emisión/validación de credenciales en `backend/src/test/java/unq/losrecursionistas/backend/service/UsuarioServiceTest.java`.
- [x] T025 Implementar los modelos `Usuario` y `ApiKey` en `backend/src/main/java/unq/losrecursionistas/backend/model/`, con estados, roles, clave almacenada de forma segura y reglas de dominio básicas.
- [x] T026 Implementar DTOs de alta de usuario, respuesta de ApiKey y solicitud de JWT en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/`, aplicando Bean Validation y sanitización.
- [x] T027 Implementar `UsuarioService` y `UsuarioServiceImpl` en `backend/src/main/java/unq/losrecursionistas/backend/services/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/services/impl/` para crear usuarios habilitados y generar una ApiKey no repetida sin exponer secretos almacenados.
- [x] T028 Implementar `UsuarioRepository`, `UsuarioRepositoryImpl`, `JugadorRepository` y `JugadorRepositoryImpl` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`, definiendo restricciones de unicidad necesarias para este alcance.
- [x] T029 Implementar el servicio JWT en `backend/src/main/java/unq/losrecursionistas/backend/security/JwtService.java`, incluyendo generación, validación, expiración y claims de usuario/rol mediante configuración externa.
- [x] T030 Implementar el filtro JWT en `backend/src/main/java/unq/losrecursionistas/backend/security/JwtAuthenticationFilter.java` y la configuración Spring Security en `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`.
- [x] T031 Implementar `UsuarioController` en `backend/src/main/java/unq/losrecursionistas/backend/controller/UsuarioController.java` con alta de usuario y endpoint de obtención de JWT a partir de una ApiKey válida, según el contrato OpenAPI.
- [x] T032 Implementar `@ControllerAdvice` en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/GlobalExceptionHandler.java` para responder `400`, `401`, `403`, `404` y `409` sin filtrar secretos.
- [ ] T033 Crear pruebas MockMvc de seguridad en `backend/src/test/java/unq/losrecursionistas/backend/web/SeguridadControllerTest.java`, cubriendo alta, ApiKey inválida, JWT inválido/expirado, endpoint protegido y respuestas sin datos sensibles.
- [x] T034 Verificar que la configuración de seguridad permite endpoints públicos únicamente donde corresponda y protege el resto, documentándolo en `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`.

## Phase 5: Calidad y documentación

- [x] T035 [P] Agregar documentación de ejecución y troubleshooting de CI, SonarCloud, Swagger, JWT y ApiKey en `README.md`.
- [x] T036 [P] Agregar o ajustar la configuración de cobertura y exclusiones justificadas en `backend/build.gradle` sin ocultar código de dominio, controllers ni seguridad.
- [x] T037 Ejecutar `backend/gradlew.bat clean test`, `backend/gradlew.bat build` y las validaciones de contrato disponibles; corregir fallos relacionados con este alcance.
- [ ] T038 Ejecutar el análisis SonarCloud desde `.github/workflows/ci.yml` y verificar que el resultado no supere el objetivo de 10 issues para el alcance actual.
- [ ] T039 Validar manualmente Swagger/OpenAPI, creación de usuario, ApiKey, obtención de JWT y `GET /players`, dejando los comandos y resultados esperados en `specs/001-mercado-tokens-jugadores/quickstart.md`.

## Dependencias y orden de implementación

- `T001` debe completarse antes de las tareas de implementación.
- `T002` y `T003` pueden desarrollarse en paralelo con `T004` y `T005`.
- `T007` a `T012` son la base compartida para seguridad y catálogo.
- `T025` a `T032` dependen de la configuración de dependencias y de los contratos de `T011`.
- `US1` requiere los DTOs, manejo de errores y configuración base, pero su consulta pública no depende de JWT.
- Las pruebas `T021` y `T033` deben pasar antes de `T037`.
- `T037` y `T038` son bloqueantes para declarar terminado este alcance.

Orden recomendado: Setup -> Foundational -> Modelo de catálogo -> Seguridad ->
Validación final.

## Oportunidades de paralelismo

- `T002`, `T003`, `T004` y `T005` pueden comenzar en paralelo.
- `T008`, `T009` y `T010` pueden comenzar en paralelo.
- `T013`, `T014`, `T017` y `T023` pueden comenzar en paralelo una vez fijado el modelo.
- Las tareas de modelos de catálogo (`T015`, `T016`) pueden avanzar en paralelo con los DTOs (`T017`).
- La configuración de seguridad y los DTOs de usuario pueden prepararse en paralelo después de `T011`.
- La documentación (`T035`, `T036`) puede avanzar en paralelo con la implementación, pero la validación final depende de todo lo anterior.

## Estrategia de implementación incremental

El MVP del alcance inicial está compuesto por:

1. Build reproducible y CI en GitHub Actions.
2. Swagger/OpenAPI v3 publicado.
3. Modelo mínimo de usuario, ApiKey, liga y jugador con tests unitarios.
4. Alta de usuario, emisión/validación de ApiKey y JWT.
5. Endpoint `GET /players` con validación y prueba MockMvc.
6. `BackendApplicationTests.contextLoads` y el build completo pasando.
7. SonarCloud configurado y con menos de 10 issues para este alcance.

## Criterios de aceptación

- [ ] `backend/gradlew.bat clean test` finaliza exitosamente.
- [ ] `backend/gradlew.bat build` finaliza exitosamente.
- [ ] GitHub Actions ejecuta build y tests con Java 21.
- [ ] SonarCloud está configurado y el resultado queda por debajo de 10 issues.
- [ ] Swagger/OpenAPI v3 está publicado y documenta los endpoints implementados.
- [ ] Se puede crear un usuario y obtener una ApiKey sin exponer su valor almacenado.
- [ ] Se puede obtener y validar un JWT usando una ApiKey válida.
- [ ] Los endpoints protegidos rechazan credenciales ausentes, inválidas o expiradas.
- [ ] Las pruebas unitarias del modelo pasan sin depender de Spring ni base de datos.
- [ ] `GET /players` devuelve el catálogo inicial y valida sus filtros.
- [ ] Las respuestas de error incluyen mensajes en español y `correlationId` cuando corresponda.
- [ ] `BackendApplicationTests.contextLoads` permanece sin modificaciones y pasa.
