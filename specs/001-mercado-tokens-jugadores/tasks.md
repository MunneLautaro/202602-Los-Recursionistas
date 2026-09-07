---
description: "Tareas del alcance inicial del Mercado de Tokens de Jugadores"
---

# Tasks: Alcance inicial - Mercado de Tokens de Jugadores

**Input**: Design documents from `/specs/001-mercado-tokens-jugadores/`

**Scope**: Esta lista cubre el alcance inicial del sistema. El backend Spring Boot
con Gradle ya existe en `backend/`; no se crea ni se reemplaza el proyecto.

**Prerequisites**: `plan.md`, `spec.md`, `data-model.md`, `contracts/openapi.yaml`,
`quickstart.md` y `.specify/memory/constitution.md`.

## Convenciones obligatorias de persistence.repository

Toda clase `RepositoryImpl` debe implementar su interfaz, usar `@Repository` o
`@Component` e inyectar por constructor un DAO Spring Data JPA/SQL en un atributo
`private final`. El acceso a datos debe delegarse exclusivamente en ese DAO. Queda
prohibido usar `Map`, `List`, `ConcurrentHashMap`, `AtomicLong`, mocks, datos
hardcoded o cualquier estado persistente en memoria. Los repositorios deben
retornar entidades de dominio o `Page<Entidad>` y desenvolver `Optional` con
`orElseThrow` para consultas sin resultado, salvo requerimiento explícito del flujo
de negocio. Sus métodos públicos deben usar nombres semánticos en español como
`recuperarPorId`, `guardar` y `recuperarUsuarios`, ocultando `findById`, `save` y
`findAll` del DAO.

## Convenciones obligatorias de modelos

Toda clase en `backend/src/main/java/unq/losrecursionistas/backend/model/` debe
cumplir la Constitucion v1.7.0: usar `@NoArgsConstructor`, `@AllArgsConstructor`,
`@Data` y `@Builder`; no declarar clases/campos `final` ni getters estilo record;
usar `@ToString.Exclude` en relaciones ciclicas y `@Builder.Default` en defaults
del builder; definir un constructor publico sin `id` ni campos autogenerados que
inicialice colecciones o relaciones; sobrescribir `equals` y `hashCode` solo por
`id`; y mantener validaciones de estado y mutaciones en metodos de dominio, no
en constructores.

## Convenciones obligatorias de services.impl

Toda clase en `backend/src/main/java/unq/losrecursionistas/backend/services/impl/`
debe cumplir la Constitucion v1.7.0:

- usar `@Service` y `@Transactional` a nivel de clase o metodo segun corresponda;
- inyectar dependencias por constructor explicito, con atributos `private final`,
  sin `@Autowired` en campos ni `@RequiredArgsConstructor`;
- retornar solo entidades de dominio o `Page<Entidad>`, nunca DTOs de response;
- mantener JWT, mapeo a DTO y logica de presentacion en seguridad/controllers;
- extraer construcciones complejas a metodos privados auxiliares y usar builders;
- delegar persistencia a repositorios, sin hashes criptograficos inline,
  `AtomicLong` ni utilitarios de seguridad dentro del servicio;
- construir `PageRequest.of(page, 12)` al inicio de los metodos paginados;
- lanzar excepciones de negocio semanticas y especificas, no `DomainException` con
  codigos estaticos ni excepciones genericas.

## Convenciones obligatorias de exceptions y paginacion

- Todas las excepciones personalizadas deben vivir en `exceptions`; las de negocio
  deben vivir en `exceptions.businessException` y heredar de `BusinessException`.
  Todas deben ser no chequeadas y extender directa o indirectamente de
  `RuntimeException`.
- El contexto de una excepcion debe ser `private final`, inicializarse mediante el
  constructor con `super(message)` y exponerse solo mediante getters explícitos.
- La respuesta HTTP unificada debe llamarse `ApiError` y ser un record o DTO.
  `GlobalExceptionHandler`, anotado con `@ControllerAdvice` o
  `@RestControllerAdvice`, es el unico componente que transforma excepciones en
  respuestas HTTP. Controllers y Services no pueden capturarlas para construir
  errores manualmente.
- Toda consulta de multiples registros debe retornar `Page<T>`; `List<T>` queda
  reservada para catalogos fijos o enumeraciones acotadas. El Service debe crear
  `PageRequest.of(page, tamano)`, el Controller recibir `page` por defecto 0 y
  mapear con `Page.map(...)`, y el Repository propagar el `Page<T>` del DAO.
  El tamaño por defecto del MVP es 12.

## Alcance funcional

Incluye GitHub Actions, build exitoso, SonarCloud con objetivo de menos de 10
issues, JWT, Swagger/OpenAPI v3, modelo mínimo, tests unitarios, creación de
usuario con ApiKey y endpoint inicial de catálogo de jugadores.

Quedan fuera del alcance actual frontend, compra/venta, portfolio completo,
scheduler, cotizaciones avanzadas, ranking y adapters o integraciones externas.

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
- [x] T008 [P] Definir el manejo de errores y respuesta `ApiError` con mensaje en español, contexto opcional y `correlationId` en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/`, centralizado exclusivamente en `GlobalExceptionHandler`.
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
- [x] T015 [US1] Implementar el modelo `Jugador` en `backend/src/main/java/unq/losrecursionistas/backend/model/Jugador.java` con invariantes básicas del dominio, nombres en español sin acentos ni `n` y las convenciones obligatorias de modelos.
- [x] T016 [US1] Implementar el modelo `Liga` en `backend/src/main/java/unq/losrecursionistas/backend/model/Liga.java` y su relación mínima con `Jugador`, aplicando las convenciones obligatorias de modelos y evitando ciclos en `toString`.
- [ ] T017 [P] [US1] Crear DTOs de respuesta y filtros del catálogo en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/` con validación de forma, límites, trimming y sanitización.
- [x] T018 [US1] Implementar `JugadorRepository` y `JugadorRepositoryImpl` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`, sin incorporar todavía proveedores externos y cumpliendo las convenciones de `persistence.repository`.
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
- [x] T025 Implementar los modelos `Usuario` y `ApiKey` en `backend/src/main/java/unq/losrecursionistas/backend/model/`, con estados, roles, clave almacenada de forma segura, reglas de dominio básicas y las convenciones obligatorias de modelos.
- [x] T026 Implementar DTOs de alta de usuario, respuesta de ApiKey y solicitud de JWT en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/`, aplicando Bean Validation y sanitización.
- [x] T027 Implementar `UsuarioService` y `UsuarioServiceImpl` en `backend/src/main/java/unq/losrecursionistas/backend/services/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/services/impl/` para crear usuarios habilitados y generar una ApiKey no repetida sin exponer secretos almacenados.
- [x] T028 Implementar `UsuarioRepository`, `UsuarioRepositoryImpl`, `JugadorRepository` y `JugadorRepositoryImpl` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/`, definiendo restricciones de unicidad necesarias para este alcance y cumpliendo estrictamente las convenciones de `persistence.repository`.
- [x] T029 Implementar el servicio JWT en `backend/src/main/java/unq/losrecursionistas/backend/security/JwtService.java`, incluyendo generación, validación, expiración y claims de usuario/rol mediante configuración externa.
- [x] T030 Implementar el filtro JWT en `backend/src/main/java/unq/losrecursionistas/backend/security/JwtAuthenticationFilter.java` y la configuración Spring Security en `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`.
- [x] T031 Implementar `UsuarioController` en `backend/src/main/java/unq/losrecursionistas/backend/controller/UsuarioController.java` con alta de usuario y endpoint de obtención de JWT a partir de una ApiKey válida, según el contrato OpenAPI.
- [x] T032 Implementar `GlobalExceptionHandler` con `@ControllerAdvice` en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/GlobalExceptionHandler.java` para transformar excepciones semánticas no chequeadas a `ApiError` y responder `400`, `401`, `403`, `404` y `409` sin filtrar secretos; Controllers y Services no deben capturar excepciones para responder manualmente.
- [ ] T033 Crear pruebas MockMvc de seguridad en `backend/src/test/java/unq/losrecursionistas/backend/web/SeguridadControllerTest.java`, cubriendo alta, ApiKey inválida, JWT inválido/expirado, endpoint protegido y respuestas sin datos sensibles.
- [x] T034 Verificar que la configuración de seguridad permite endpoints públicos únicamente donde corresponda y protege el resto, documentándolo en `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`.

## Phase 5: Calidad y documentación

- [x] T035 [P] Agregar documentación de ejecución y troubleshooting de CI, SonarCloud, Swagger, JWT y ApiKey en `README.md`.
- [x] T036 [P] Agregar o ajustar la configuración de cobertura y exclusiones justificadas en `backend/build.gradle` sin ocultar código de dominio, controllers ni seguridad.
- [x] T037 Ejecutar `backend/gradlew.bat clean test`, `backend/gradlew.bat build` y las validaciones de contrato disponibles; corregir fallos relacionados con este alcance.
- [ ] T038 Ejecutar el análisis SonarCloud desde `.github/workflows/ci.yml` y verificar que el resultado no supere el objetivo de 10 issues para el alcance actual.
- [ ] T039 Validar manualmente Swagger/OpenAPI, creación de usuario, ApiKey, obtención de JWT y `GET /players`, dejando los comandos y resultados esperados en `specs/001-mercado-tokens-jugadores/quickstart.md`.
- [x] T040 Refactorizar `UsuarioService` y `UsuarioServiceImpl` para cumplir las
      convenciones de `services.impl`: retornar modelos de dominio, trasladar DTO/JWT
      a la frontera correspondiente, declarar `@Transactional`, eliminar `AtomicLong`
      y hash inline, delegar persistencia al repositorio y usar excepciones semanticas.

## Dependencias y orden de implementación

- `T001` debe completarse antes de las tareas de implementación.
- `T002` y `T003` pueden desarrollarse en paralelo con `T004` y `T005`.
- `T007` a `T012` son la base compartida para seguridad y catálogo.
- `T025` a `T032` dependen de la configuración de dependencias y de los contratos de `T011`.
- `T045` a `T048` dependen de `T008`, `T032` y de la convergencia de persistencia de `T041` a `T044`.
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
- [ ] Todas las clases de `model` cumplen las convenciones de Lombok, constructores
      de negocio, igualdad por `id`, exclusión de relaciones en `toString` y
      encapsulamiento de reglas de dominio.
- [ ] Todas las clases de `services.impl` cumplen las convenciones de constructor,
      transaccionalidad, dependencias finales, retornos de dominio, repositorios,
      paginación, builders y excepciones semanticas de la Constitucion v1.7.0.
- [ ] Todas las clases de `persistence.repository.impl` implementan su interfaz,
      delegan exclusivamente en un DAO SQL/JPA por constructor, no mantienen estado
      en memoria, desenvuelven `Optional` con `orElseThrow` cuando corresponde y
      exponen nombres semánticos de dominio.
- [ ] GitHub Actions ejecuta build y tests con Java 21.
- [ ] SonarCloud está configurado y el resultado queda por debajo de 10 issues.
- [ ] Swagger/OpenAPI v3 está publicado y documenta los endpoints implementados.
- [ ] Se puede crear un usuario y obtener una ApiKey sin exponer su valor almacenado.
- [ ] Se puede obtener y validar un JWT usando una ApiKey válida.
- [ ] Los endpoints protegidos rechazan credenciales ausentes, inválidas o expiradas.
- [ ] Las pruebas unitarias del modelo pasan sin depender de Spring ni base de datos.
- [ ] `GET /players` devuelve el catálogo inicial y valida sus filtros.
- [ ] Las respuestas de error incluyen mensajes en español y `correlationId` cuando corresponda.
- [ ] Las excepciones personalizadas cumplen la jerarquía `exceptions`/
      `exceptions.businessException`, son no chequeadas, preservan `super(message)`
      y exponen contexto inmutable mediante getters.
- [ ] Todas las respuestas HTTP de error usan `ApiError` y pasan exclusivamente por
      `GlobalExceptionHandler`; no existen capturas manuales en Controllers o Services.
- [ ] Toda consulta de múltiples registros retorna `Page<T>`, con `PageRequest.of`
      en el Service y `Page.map(...)` en el Controller cuando corresponde.
- [ ] `BackendApplicationTests.contextLoads` permanece sin modificaciones y pasa.

## Phase 6: Convergencia de persistencia y servicios

Estas tareas se agregan a partir de la implementación actual y son necesarias
para que el código cumpla la Constitución v1.7.0. Las tareas T018 y T028
implementaron el comportamiento inicial, pero no satisfacen todavía las
convenciones obligatorias de `persistence.repository`.

- [x] T041 Reemplazar el almacenamiento en memoria de `UsuarioRepositoryImpl` y
      `JugadorRepositoryImpl` por DAOs Spring Data JPA/SQL inyectados mediante
      constructor explícito, eliminando `Map`, `ConcurrentHashMap`, `AtomicLong`,
      datos de demostración y constructores que simulan persistencia.
- [x] T042 Renombrar los contratos públicos de repositories a métodos semánticos
      en español (`recuperarPorId`, `guardar`, `estaRegistradoElUsername`,
      `recuperarJugadores` o equivalentes del dominio) y ocultar `findById`,
      `save` y `findAll` dentro de los DAOs. Las consultas únicas deben retornar
      la entidad o lanzar una excepción descriptiva con `orElseThrow`, sin
      exponer `Optional` salvo necesidad explícita del flujo.
- [x] T043 Agregar o adaptar pruebas de integración de `UsuarioRepository` y
      `JugadorRepository` con PostgreSQL/Testcontainers para verificar delegación
      real, persistencia entre operaciones, paginación, filtros, unicidad y
      errores de entidad inexistente.
- [x] T044 Revisar `UsuarioServiceImpl` para conservar una única inyección de
      dependencias explícita y eliminar la sobrecarga de compatibilidad con
      `JwtService` ignorado, manteniendo la generación de ApiKey en su componente
      de seguridad y la delegación de persistencia en el repository.

## Phase 7: Convergencia de excepciones y paginación

Estas tareas se agregan a partir de la revisión del código actual. La
implementación existente todavía usa `ErrorResponse` y `DomainException`, y el
controller del catálogo materializa el contenido paginado en una `List` antes de
construir su respuesta. Deben completarse antes de declarar satisfechas las
convenciones de excepciones y paginación.

- [ ] T045 Crear `BusinessException` en
      `backend/src/main/java/unq/losrecursionistas/backend/exceptions/businessException/`
      como excepción base no chequeada, y migrar las excepciones de reglas de
      negocio a ese subpaquete con atributos de contexto `private final`,
      constructores que invoquen `super(message)` y getters explícitos.
- [x] T046 Reemplazar `DomainException` y `ErrorResponse` por `ApiError` en
      `backend/src/main/java/unq/losrecursionistas/backend/exceptions/`, ajustar
      `GlobalExceptionHandler` para responder exclusivamente mediante `ApiError`
      y conservar los códigos HTTP y el `correlationId` sin exponer información
      sensible.
- [x] T047 Revisar Controllers y Services para eliminar capturas de excepciones
      destinadas a construir respuestas HTTP manuales; las excepciones semánticas
      deben propagarse hasta `GlobalExceptionHandler`.
- [x] T048 Ajustar todas las consultas de múltiples registros para retornar
      `Page<T>`. En particular, el Controller debe recibir `page` con default 0,
      invocar al Service y usar `Page.map(...)` para convertir entidades a DTOs;
      el Repository debe conservar la paginación proveniente del DAO.
