# Tasks: Correccion de infraestructura del backend

**Input**: Design documents from `/specs/002-corregir-infra-backend/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/README.md, quickstart.md

**Tests**: Se incluyen tareas de prueba porque la especificacion exige cubrir cada metodo publico nuevo del modelo y los servicios con JUnit 5, AssertJ y Mockito.

**Organization**: Las tareas estan agrupadas por historia de usuario para permitir implementacion y validacion independiente.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo cuando trabaja en archivos distintos y no depende de tareas incompletas.
- **[Story]**: Identifica la historia de usuario correspondiente.
- Cada tarea incluye rutas concretas del repositorio.

## Phase 1: Setup (Infraestructura compartida)

**Purpose**: Preparar el proyecto backend existente sin cambiar su stack.

- [x] T001 Verificar en `backend/build.gradle` Java 21, Spring Data JPA, PostgreSQL, Lombok y `useJUnitPlatform()` como base del proyecto.
- [x] T002 [P] Confirmar en `backend/build.gradle` dependencias de JUnit 5, AssertJ y Mockito, sin agregar dependencias de herramientas externas de migraciones ni frameworks de pruebas JavaScript.
- [x] T003 [P] Revisar `.github/workflows/ci.yml` para que el workflow ejecute `./gradlew clean test` desde `backend` y no incluya pasos de Node.js.
- [x] T004 [P] Documentar en `README.md` o `specs/002-corregir-infra-backend/quickstart.md` los comandos de compilacion, pruebas y arranque del backend.

---

## Phase 2: Foundational (Prerequisitos bloqueantes)

**Purpose**: Dejar lista la configuracion, las excepciones y los limites de paquetes antes de implementar las historias.

**CRITICAL**: Ninguna tarea de historia debe cerrarse antes de completar esta fase.

- [x] T005 Configurar `backend/src/main/resources/application.properties` con la conexion PostgreSQL existente y `spring.jpa.hibernate.ddl-auto=update`, manteniendo una sola configuracion y sin archivos `application-local.properties`.
- [x] T006 [P] Revisar `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ExcepcionDominio.java` y `ExcepcionValidacion.java` para asegurar que las excepciones personalizadas extienden `RuntimeException` directa o indirectamente.
- [x] T007 [P] Verificar `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ControladorErrores.java` para conservar el manejo centralizado de errores sin trasladar reglas de dominio al controller.
- [x] T008 Crear o ajustar la comprobacion de limites de paquetes en `backend/src/test/java/unq/losrecursionistas/backend/` para detectar entidades fuera de `model`, repositorios fuera de `persistence/repository` y acceso directo de controllers a repositorios.
- [x] T009 Ejecutar `backend/gradlew.bat clean test` y registrar el estado inicial sin modificar ni eliminar los tests existentes.

**Checkpoint**: La configuracion unica, la jerarquia de excepciones y la validacion arquitectonica estan listas para las historias.

---

## Phase 3: User Story 1 - Estructura backend conforme a la arquitectura (Priority: P1) MVP

**Goal**: Ubicar el dominio y el acceso a datos en los paquetes constitucionales, con repositorios paginados y sin acoplamiento controller-repository.

**Independent Test**: Inspeccionar los paquetes, compilar el backend y ejecutar la comprobacion de limites para confirmar `model`, `persistence/repository/interfaces` y `persistence/repository/impl`.

### Implementation for User Story 1

- [x] T010 [P] [US1] Crear los contratos `RepositorioUsuario` y `RepositorioLiga` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` con consultas multiples que retornen `Page<T>` y reciban `Pageable`.
- [x] T011 [P] [US1] Crear los contratos `RepositorioEquipo` y `RepositorioJugador` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` con listados paginados por liga, equipo, liga o estado segun `data-model.md`.
- [x] T012 [P] [US1] Crear los contratos `RepositorioCotizacionJugador`, `RepositorioPosicionPortfolio` y `RepositorioTransaccionAuditoria` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` con `Page<T>` y `Pageable` para historiales, posiciones y auditoria.
- [x] T013 [US1] Implementar las consultas personalizadas necesarias en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/` sin incluir reglas de negocio ni materializar listados en memoria.
- [x] T014 [P] [US1] Crear o ajustar las entidades base requeridas por los contratos en `backend/src/main/java/unq/losrecursionistas/backend/model/` para que todos los repositorios compilen con sus tipos de dominio.
- [x] T015 [US1] Revisar `backend/src/main/java/unq/losrecursionistas/backend/service/` y `backend/src/main/java/unq/losrecursionistas/backend/controller/` para asegurar que controller no invoca repository directamente y que service no retorna DTOs de respuesta.
- [x] T016 [US1] Ejecutar la comprobacion de arquitectura de `backend/src/test/java/unq/losrecursionistas/backend/` y `backend/gradlew.bat test` como prueba independiente de la historia.

**Checkpoint**: US1 queda demostrable con paquetes correctos, repositorios separados y contratos paginados.

---

## Phase 4: User Story 2 - Modelo persistible con reglas de dominio (Priority: P1)

**Goal**: Implementar las siete entidades JPA/Lombok y custodiar sus invariantes dentro del modelo.

**Independent Test**: Ejecutar exclusivamente los tests de `backend/src/test/java/unq/losrecursionistas/backend/model/` y confirmar construccion, persistencia declarativa y rechazo de operaciones invalidas.

### Tests for User Story 2

- [x] T017 [P] [US2] Crear `backend/src/test/java/unq/losrecursionistas/backend/model/UsuarioTest.java` para cubrir saldo no negativo, debito valido, saldo insuficiente y campos obligatorios `nombreUsuario` y `contrasena`.
- [x] T018 [P] [US2] Crear `backend/src/test/java/unq/losrecursionistas/backend/model/LigaEquipoJugadorTest.java` para cubrir nombres y codigos requeridos, relaciones obligatorias, jugador activo y valores vacios.
- [x] T019 [P] [US2] Crear `backend/src/test/java/unq/losrecursionistas/backend/model/CotizacionJugadorTest.java` para cubrir `valor` positivo, `tokensDisponibles` no negativo y actualizacion valida de cotizacion.
- [x] T020 [P] [US2] Crear `backend/src/test/java/unq/losrecursionistas/backend/model/PosicionPortfolioTest.java` para cubrir `cantidadTokens` y `precioCompra` positivos, saldo insuficiente, tokens insuficientes y jugador inactivo.
- [x] T021 [P] [US2] Crear `backend/src/test/java/unq/losrecursionistas/backend/model/TransaccionAuditoriaTest.java` para cubrir autor, detalle, timestamp, `estadoAnterior`, `estadoPosterior` obligatorios e inmutabilidad posterior a la creacion.

### Implementation for User Story 2

- [x] T022 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/model/Usuario.java` con `@Entity`, `@Table(name = "usuarios")`, Lombok controlado, identidad generada, `nombreUsuario`, `contrasena`, `saldo`, `fechaCreacion` e invariantes de saldo.
- [x] T023 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/model/Liga.java` y `backend/src/main/java/unq/losrecursionistas/backend/model/Equipo.java` con `@Entity`, `@Table`, nombres y codigos obligatorios/unicos y relacion `Equipo`-`Liga`.
- [x] T024 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/model/Jugador.java` con relacion obligatoria a `Equipo`, campos `nombre`, `posicion`, `activo` y regla que impide operaciones de portfolio cuando no esta activo.
- [x] T025 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/model/CotizacionJugador.java` con relacion a `Jugador`, `valor` positivo, `tokensDisponibles` no negativo y `fechaCotizacion`.
- [x] T026 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/model/PosicionPortfolio.java` con relaciones a `Usuario` y `Jugador`, `cantidadTokens` positiva, `precioCompra` positivo y operaciones que rechacen saldo o tokens insuficientes.
- [x] T027 [P] [US2] Crear `backend/src/main/java/unq/losrecursionistas/backend/model/TransaccionAuditoria.java` con referencia al autor, detalle, timestamp, estados anterior/posterior y proteccion de inmutabilidad.
- [x] T028 [US2] Completar las excepciones de dominio necesarias en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/` y conectar cada invariante del modelo con una excepcion no chequeada.
- [x] T029 [US2] Configurar relaciones, nombres de columnas, nulabilidad, unicidad, precision monetaria y carga controlada en las siete entidades, evitando ciclos en `equals`, `hashCode` y `toString`.
- [x] T030 [US2] Ejecutar `backend/gradlew.bat test --tests "*model*"` y corregir los tests de modelo hasta cubrir casos felices y de borde sin mover las reglas a service.

**Checkpoint**: US2 queda demostrable con entidades persistibles y reglas de negocio testeadas dentro de `model`.

---

## Phase 5: User Story 3 - Configuracion y validacion reproducibles (Priority: P2)

**Goal**: Confirmar PostgreSQL, la gestion de esquema JPA/Hibernate y la estrategia de pruebas del backend.

**Independent Test**: Ejecutar la suite completa y arrancar el backend contra PostgreSQL usando solamente `application.properties`.

### Tests for User Story 3

- [x] T031 [P] [US3] Crear pruebas de servicios concretos en `backend/src/test/java/unq/losrecursionistas/backend/service/` para verificar con Mockito la resolucion fail-fast de entidades y la ausencia de invocaciones posteriores cuando falta un ID.
- [x] T032 [P] [US3] Crear pruebas de servicios en `backend/src/test/java/unq/losrecursionistas/backend/service/` que verifiquen `PageRequest.of(page, 12)`, propagacion de `Page<T>` y uso de AssertJ para resultados y excepciones.
- [x] T033 [P] [US3] Crear una validacion en `backend/src/test/java/unq/losrecursionistas/backend/` que detecte mas de un archivo `application*.properties`, identificadores no ASCII y frameworks Jest, Vitest o Mocha en el backend.

### Implementation for User Story 3

- [x] T034 [US3] Implementar o ajustar interfaces y servicios en `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/` y `backend/src/main/java/unq/losrecursionistas/backend/service/impl/` para construir `PageRequest.of(page, 12)` al inicio de cada consulta multiple.
- [x] T035 [US3] Implementar la validacion de existencia en los servicios de `backend/src/main/java/unq/losrecursionistas/backend/service/` antes de invocar metodos de dominio o continuar con repositorios dependientes.
- [x] T036 [US3] Verificar `backend/src/main/resources/application.properties` y `backend/src/main/java/unq/losrecursionistas/backend/configuration/` para confirmar PostgreSQL, `ddl-auto=update`, una sola configuracion y ausencia de perfiles separados.
- [x] T037 [US3] Ajustar `.github/workflows/ci.yml` para ejecutar compilacion y `backend/gradlew.bat clean test`, haciendo fallar el workflow ante cualquier error de test o compilacion.
- [x] T038 [US3] Ejecutar `backend/gradlew.bat clean test` y arrancar el backend para comprobar la compatibilidad del esquema JPA/Hibernate con PostgreSQL usando `specs/002-corregir-infra-backend/quickstart.md`.

**Checkpoint**: US3 queda demostrable con configuracion unica, pruebas reproducibles, paginacion validada y CI bloqueante.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validar la feature completa sin modificar tests existentes para ocultar fallos.

- [x] T039 [P] Revisar nombres nuevos bajo `backend/src/main/java/` y `backend/src/test/java/` para asegurar espanol tecnico e identificadores ASCII, incluyendo `contrasena`.
- [x] T040 [P] Revisar que no existan archivos `application-local.properties`, perfiles separados ni configuraciones duplicadas bajo `backend/src/main/resources/`.
- [x] T041 [P] Revisar `specs/002-corregir-infra-backend/plan.md`, `data-model.md` y `contracts/README.md` contra la implementacion final y corregir solo documentacion desactualizada.
- [x] T042 Ejecutar la validacion completa de `specs/002-corregir-infra-backend/quickstart.md` y registrar el resultado final de compilacion, tests, arquitectura y arranque.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias; puede comenzar inmediatamente.
- **Foundational (Phase 2)**: Depende de Setup y bloquea todas las historias.
- **User Story 1 (Phase 3)**: Depende de Foundational y habilita los contratos de persistencia.
- **User Story 2 (Phase 4)**: Depende de Foundational; sus entidades deben estar disponibles para los repositorios de US1 y puede completarse en paralelo con parte de US1 si se coordinan archivos.
- **User Story 3 (Phase 5)**: Depende de los contratos y entidades de US1/US2 para probar servicios y arranque.
- **Polish (Phase 6)**: Depende de todas las historias deseadas.

### User Story Dependencies

- **US1 (P1)**: MVP estructural; depende de Phase 2 y es independiente de US3.
- **US2 (P1)**: MVP de dominio; depende de Phase 2 y comparte tipos con US1, por lo que sus tareas de entidades deben coordinarse con repositorios.
- **US3 (P2)**: Depende de los contratos de repositorio y entidades de US1/US2 para validar servicios, paginacion y arranque.

### Within Each User Story

- Escribir pruebas especificas antes de implementar cuando el archivo de prueba pueda compilar con el contrato previsto.
- Crear entidades antes de servicios que las orquesten.
- Crear contratos de repositorio antes de sus implementaciones.
- Validar cada historia en su checkpoint antes de avanzar.

### Parallel Opportunities

- T002, T003 y T004 pueden ejecutarse en paralelo.
- T006, T007 y T008 pueden ejecutarse en paralelo tras T001.
- T010, T011 y T012 pueden ejecutarse en paralelo si se acuerdan previamente los nombres de entidades.
- T017-T021 pueden ejecutarse en paralelo porque pertenecen a clases de prueba distintas.
- T022-T027 pueden ejecutarse en paralelo por entidad, coordinando relaciones compartidas.
- T031-T033 pueden ejecutarse en paralelo en archivos de prueba distintos.
- T039-T041 pueden ejecutarse en paralelo antes de la validacion final T042.

---

## Parallel Example: User Story 2

```text
Task: "Crear Usuario.java y UsuarioTest.java en model con invariantes de saldo"
Task: "Crear Liga.java, Equipo.java y LigaEquipoJugadorTest.java en model"
Task: "Crear Jugador.java y CotizacionJugador.java con sus pruebas de invariantes"
Task: "Crear PosicionPortfolio.java y PosicionPortfolioTest.java para saldo y tokens"
Task: "Crear TransaccionAuditoria.java y TransaccionAuditoriaTest.java para inmutabilidad"
```

---

## Implementation Strategy

### MVP First

1. Completar Phase 1: Setup.
2. Completar Phase 2: Foundational.
3. Completar US1 para obtener la estructura y contratos paginados.
4. Completar US2 para obtener el modelo persistible con invariantes.
5. Detenerse y validar US1 + US2 de forma independiente antes de ampliar servicios.

### Incremental Delivery

1. Setup + Foundational: configuracion y limites listos.
2. US1: estructura y repositorios paginados.
3. US2: entidades y reglas de dominio testeadas.
4. US3: servicios, configuracion PostgreSQL, validaciones y CI.
5. Polish: quickstart y validacion completa.

### Parallel Team Strategy

1. El equipo completa Setup + Foundational.
2. Un desarrollador trabaja en contratos de repositorio de US1.
3. Otro desarrollador trabaja en entidades y pruebas de US2.
4. Tras cerrar US1 y US2, otro desarrollador completa servicios, configuracion y CI de US3.
5. Todos ejecutan el checkpoint final sin modificar ni borrar tests existentes.

---

## Notes

- `[P]` solo se usa cuando las tareas trabajan en archivos distintos y no dependen de trabajo incompleto.
- `[US1]`, `[US2]` y `[US3]` mantienen trazabilidad con las historias de `spec.md`.
- No se agregan migraciones versionadas ni herramientas externas de migracion; el esquema se gestiona mediante JPA/Hibernate segun el plan vigente.
- Las pruebas existentes deben conservarse y cualquier fallo debe resolverse en el codigo, no ocultarse modificando tests.
