---
description: "Tareas de implementacion para documentar la API REST con OpenAPI/Swagger v3"
---

# Tareas: Documentacion de la API REST con OpenAPI/Swagger v3

**Input**: Documentos de diseno de `/specs/003-documentar-api-openapi/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Alcance**: Documentar todos los endpoints REST existentes con OpenAPI/Swagger y documentar el health check de Actuator. No se agregan endpoints de negocio ni un `SecurityScheme` bearer/JWT; Swagger UI se utiliza para consultar y navegar contratos documentados.

## Formato

- Todas las tareas usan checkbox, identificador secuencial y ruta exacta.
- `[P]` indica que la tarea puede ejecutarse en paralelo con otras tareas del mismo bloque sin depender de archivos incompletos.
- `[US1]`, `[US2]` y `[US3]` corresponden a las historias del spec.

## Phase 1: Setup

**Objetivo**: Preparar dependencias y verificar la superficie actual antes de implementar.

- [x] T001 Agregar `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1` y `org.springframework.boot:spring-boot-starter-actuator` en `backend/build.gradle`, sin introducir un starter de documentacion alternativo.
- [x] T002 [P] Inventariar `@RestController`, `@Controller` con `@ResponseBody`, mappings y DTOs en `backend/src/main/java/unq/losrecursionistas/backend/` y registrar el resultado en `specs/003-documentar-api-openapi/research.md` sin crear endpoints nuevos.
- [x] T003 [P] Verificar que no existan propiedades de perfiles ni archivos `application-*.properties` adicionales bajo `backend/src/main/resources/`, preservando unicamente `backend/src/main/resources/application.properties`.

---

## Phase 2: Foundational

**Objetivo**: Configurar la base compartida de OpenAPI, Actuator y seguridad antes de completar las historias.

- [x] T004 Crear el bean de metadatos `OpenAPI` con titulo, version y descripcion en espanol tecnico dentro de `backend/src/main/java/unq/losrecursionistas/backend/configuration/ConfiguracionOpenApi.java`, sin agregar `SecurityScheme` bearer/JWT ni flujo `Authorize`.
- [x] T005 Agregar unicamente las propiedades necesarias para `springdoc.show-actuator=true` y `management.endpoints.web.exposure.include=health` en `backend/src/main/resources/application.properties`, manteniendo ocultos los detalles sensibles de health por defecto.
- [x] T006 Actualizar `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java` para declarar `permitAll` explicito en `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs`, `/v3/api-docs/**` y `/actuator/health`, sin relajar adicionalmente las demas rutas.
- [x] T007 [P] Crear la base de pruebas HTTP en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` con MockMvc y configuracion de contexto compatible con H2, sin modificar ni eliminar `BackendApplicationTests.java`.

**Checkpoint**: Dependencias, bean de OpenAPI, exposicion limitada de Actuator, seguridad de rutas publicas y base de pruebas estan listos para las historias.

---

## Phase 3: User Story 1 - Explorar y consultar la API existente (Priority: P1) 🎯 MVP

**Objetivo**: Permitir consultar y navegar la especificacion OpenAPI con endpoints, DTOs, codigos de estado y errores documentados, sin ejecutar solicitudes de negocio desde Swagger UI.

**Independent Test**: Con el backend iniciado, comprobar que `/swagger-ui.html` y `/v3/api-docs` responden 200 sin autenticacion y que el JSON contiene metadatos, paths, esquemas DTO y respuestas documentadas del inventario actual.

### Tests for User Story 1

- [x] T008 [US1] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` una prueba que confirme `GET /swagger-ui.html` responde 200 sin header `Authorization`.
- [x] T009 [US1] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` una prueba que confirme `GET /v3/api-docs` responde 200 sin header `Authorization` y contiene `openapi`, `info.title`, `info.version` y `paths`.
- [x] T010 [US1] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` una asercion sobre el JSON OpenAPI que verifique todos los endpoints del inventario y sus DTOs, codigos de estado y errores de dominio documentados, sin invocar endpoints de negocio ni probar autenticacion desde Swagger UI.

### Implementation for User Story 1

- [x] T011 [US1] Aplicar `@Tag`, `@Operation`, `@ApiResponse` y `@Schema` unicamente a los controllers y DTOs REST encontrados por T002, documentando proposito, request, response, codigos de estado y errores sin modificar logica de `controller`, `service`, `persistence` o `model`.
- [x] T012 [US1] Asociar `RespuestaError` como esquema de las respuestas 400, 422 y 500 en las operaciones reales que correspondan, respetando `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ControladorErrores.java` y sin crear handlers duplicados.
- [x] T013 [US1] Revisar el documento generado en `/v3/api-docs` contra `specs/003-documentar-api-openapi/contracts/api-publica.md` y confirmar que el 100% del inventario de endpoints queda navegable y sin secretos ni detalles de implementacion.

**Checkpoint**: La documentacion OpenAPI se consulta desde Swagger UI y refleja el contrato observable de todos los endpoints existentes, sin capacidad requerida de ejecucion autenticada.

---

## Phase 4: User Story 2 - Consultar el estado de salud de la API (Priority: P1)

**Objetivo**: Exponer y documentar `/actuator/health` como health check publico, con estados saludables y no saludables observables.

**Independent Test**: Consultar `/actuator/health` sin autenticacion, comprobar respuesta 200 con estado `UP` en entorno saludable y verificar el mapeo 503 para `DOWN` u `OUT_OF_SERVICE` cuando corresponda.

### Tests for User Story 2

- [x] T014 [US2] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` una prueba que confirme `GET /actuator/health` responde 200 sin header `Authorization` en el contexto saludable.
- [x] T015 [US2] Agregar una validacion en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` para que `/actuator/health` no exponga detalles de datasource, credenciales ni configuracion interna por defecto.
- [x] T016 [US2] Agregar una prueba de estado no saludable en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` que verifique el codigo 503 cuando Actuator informe `DOWN` u `OUT_OF_SERVICE`, usando una configuracion de prueba controlada.

### Implementation for User Story 2

- [x] T017 [US2] Verificar en `backend/src/main/resources/application.properties` que solo `health` este expuesto por HTTP y que la ruta efectiva sea `/actuator/health`, sin habilitar `metrics`, `env`, `beans`, `mappings` ni otros endpoints Actuator.
- [x] T018 [US2] Confirmar que `springdoc.show-actuator=true` incorpora `/actuator/health` a la especificacion consultable en Swagger UI y que el contrato coincide con `specs/003-documentar-api-openapi/contracts/api-publica.md`.

**Checkpoint**: El health check responde y queda documentado con acceso publico, estados verificables y sin informacion sensible.

---

## Phase 5: User Story 3 - Mantener documentacion segura y acotada (Priority: P2)

**Objetivo**: Garantizar que la documentacion publicada solo exponga contratos publicos y no secretos ni detalles internos.

**Independent Test**: Inspeccionar `/v3/api-docs`, Swagger UI y `/actuator/health` sin credenciales, verificando ausencia de tokens, contrasenas, datos de conexion y detalles internos.

### Tests for User Story 3

- [x] T019 [US3] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` una asercion que rechace secretos, tokens, contrasenas, credenciales, datos de conexion y URLs internas en el JSON de `/v3/api-docs`.
- [x] T020 [US3] Agregar en `backend/src/test/java/unq/losrecursionistas/backend/ConfiguracionDocumentacionTest.java` una prueba que confirme acceso sin autenticacion a las tres rutas publicas y ausencia de redireccion a login o respuestas 401/403.

### Implementation for User Story 3

- [x] T021 [US3] Revisar `backend/src/main/java/unq/losrecursionistas/backend/configuration/ConfiguracionOpenApi.java` para asegurar que los metadatos no incluyan secretos, URLs internas, credenciales ni definiciones de autenticacion no requeridas por el spec.
- [x] T022 [US3] Revisar `backend/src/main/resources/application.properties` y `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java` para confirmar que solo las tres rutas aprobadas son publicas para esta feature y que no se agregan perfiles ni reglas de acceso nuevas para endpoints de negocio.
- [x] T023 [US3] Ejecutar una comparacion final entre `/v3/api-docs`, `specs/003-documentar-api-openapi/data-model.md` y `specs/003-documentar-api-openapi/contracts/api-publica.md`, documentando cualquier diferencia observable antes de cerrar la historia.

**Checkpoint**: La documentacion es navegable, publica en las rutas aprobadas y no expone informacion sensible ni funcionalidad de autenticacion en Swagger UI.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Objetivo**: Validar el conjunto completo y dejar la feature lista para integracion.

- [x] T024 [P] Ejecutar `backend/gradlew.bat clean test` desde `backend/` y conservar todos los tests existentes sin modificarlos para ocultar fallos.
- [x] T025 [P] Ejecutar la validacion de infraestructura y arquitectura existente, incluyendo `ValidacionInfraestructuraTest`, `ArquitecturaBaseTest` y `ConfiguracionSeguridadTest`, desde `backend/`.
- [x] T026 [P] Ejecutar manualmente los escenarios de `specs/003-documentar-api-openapi/quickstart.md` contra una aplicacion levantada y registrar resultados de `/swagger-ui.html`, `/v3/api-docs` y `/actuator/health`.
- [x] T027 Revisar `backend/src/main/java/unq/losrecursionistas/backend/` y `backend/src/test/java/unq/losrecursionistas/backend/` para confirmar que todos los identificadores nuevos sean ASCII y que la documentacion de codigo nueva este en espanol tecnico, conforme al Principio X.
- [x] T028 Actualizar `specs/003-documentar-api-openapi/quickstart.md` y `specs/003-documentar-api-openapi/contracts/api-publica.md` si las rutas o respuestas verificadas difieren del diseno, sin ampliar el alcance funcional.

---

## Dependencias y orden de ejecucion

### Dependencias entre fases

- **Setup (Phase 1)**: no tiene dependencias y habilita la base del proyecto.
- **Foundational (Phase 2)**: depende de T001 y T002; bloquea las historias.
- **User Story 1 (Phase 3)**: depende de T003, T004, T005, T006 y T007; es el MVP y documenta el contrato OpenAPI.
- **User Story 2 (Phase 4)**: depende de T004, T005, T006 y T007; puede avanzar en paralelo con US1 cuando la base compartida este lista.
- **User Story 3 (Phase 5)**: depende de T004, T005, T006 y T007; puede avanzar en paralelo con US1 y US2, aunque su validacion final usa sus artefactos.
- **Polish (Phase 6)**: depende de las tres historias y de sus checkpoints.

### Dependencias dentro de las historias

- Las pruebas de cada historia deben escribirse antes de cerrar su implementacion y ejecutarse contra el contexto real.
- US1: T008-T010 preceden T011-T013.
- US2: T014-T016 preceden T017-T018.
- US3: T019-T020 preceden T021-T023.
- Ninguna tarea crea endpoints de negocio, modifica reglas de dominio o incorpora autenticacion para Swagger UI.

## Oportunidades de trabajo en paralelo

- T002 y T003 pueden ejecutarse en paralelo durante Setup.
- T004 y T005 pueden ejecutarse en paralelo si se coordinan los cambios de configuracion y luego se valida el arranque; T006 y T007 pueden comenzar una vez identificadas las rutas y dependencias.
- T008, T009 y T010 son pruebas independientes dentro de US1.
- T014 y T015 son pruebas independientes dentro de US2; T016 depende de la estrategia de simulacion del estado no saludable.
- T019 y T020 son pruebas independientes dentro de US3.
- US1 y US2 pueden implementarse en paralelo después de Phase 2; US3 puede revisar seguridad en paralelo sin tocar los mismos bloques hasta la integracion final.

## Estrategia de implementacion

### MVP

1. Completar Phase 1 y Phase 2.
2. Completar US1: dependencia, metadata, rutas `/swagger-ui.html` y `/v3/api-docs`, y documentacion del inventario actual.
3. Ejecutar el test independiente de US1: consultar la UI y JSON sin autenticacion, verificando endpoints, DTOs, estados y errores documentados.
4. Detenerse para validar antes de incorporar el refinamiento de health y seguridad.

### Entrega incremental

1. Phase 1 + Phase 2: base de documentacion y acceso publico lista.
2. US1: contrato OpenAPI navegable, entregable MVP.
3. US2: health check Actuator documentado y verificable.
4. US3: revision de exposicion segura y ausencia de informacion sensible.
5. Phase 6: suite completa, quickstart y contratos sincronizados.

## Validacion de formato

- Todas las tareas de T001 a T028 comienzan con `- [ ]`, tienen identificador secuencial y describen una ruta concreta.
- Las tareas de fases de historias incluyen exactamente una etiqueta `[US1]`, `[US2]` o `[US3]`.
- Las tareas de Setup, Foundational y Polish no incluyen etiqueta de historia.
- Las tareas marcadas `[P]` se limitan a trabajos independientes o a pruebas separables.
- No existen tareas de JWT, `SecurityScheme`, flujo `Authorize` ni ejecucion autenticada de endpoints de negocio desde Swagger UI.

## Notas

- La feature documenta contratos existentes; no inventa endpoints de negocio para completar la especificacion.
- La prueba de `/actuator/health` puede reflejar el estado del `DataSource`; los tests deben controlar su contexto para evitar depender de PostgreSQL local.
- No se modifican ni eliminan tests existentes sin autorizacion explicita.
