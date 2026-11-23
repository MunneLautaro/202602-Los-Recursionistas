---

description: "Tareas de implementacion para la gestion de estrategia de cotizacion"
---

# Tasks: Gestion de estrategia de cotizacion

**Input**: Design documents from `specs/011-gestion-estrategia-cotizacion/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Organization**: Las tareas estan agrupadas por historia de usuario para permitir implementacion y validacion incremental.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar las ubicaciones y contratos compartidos que usaran las historias.

- [X] T001 Confirmar en `backend/src/main/resources/application.yml` la propiedad `cotizacion.estrategia.activa` como fallback sin agregar perfiles de configuracion alternativos
- [X] T002 [P] Definir el catalogo compartido de codigos, nombres y descripciones de estrategia en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/estrategia/` reutilizando el mapa de estrategias existente
- [X] T003 [P] Revisar las rutas y convenciones de `fetchClient` para las nuevas llamadas en `frontend/src/lib/fetchClient.ts` y `frontend/src/features/admin/api/adminApi.ts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Crear la persistencia, contratos de repositorio y coordinacion de recalcualo que bloquean todas las historias.

**CRITICAL**: Ninguna historia puede completarse hasta terminar esta fase.

- [X] T004 [P] Crear la entidad singleton `ConfiguracionEstrategia` en `backend/src/main/java/unq/losrecursionistas/backend/model/ConfiguracionEstrategia.java` con `id=1`, `estrategiaActiva` no nula y `actualizadoEn`
- [X] T005 [P] Crear la entidad inmutable `CambioEstrategia` en `backend/src/main/java/unq/losrecursionistas/backend/model/CambioEstrategia.java` con `estrategiaAnterior` nullable, `estrategiaNueva` no nula, `usuarioAdmin` y `timestampCambio`
- [X] T006 [P] Crear el DAO JPA de configuracion en `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/ConfiguracionEstrategiaDAOSQL.java` y el contrato `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/RepositorioConfiguracionEstrategia.java`
- [X] T007 [P] Crear el DAO JPA de auditoria en `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/CambioEstrategiaDAOSQL.java` y el contrato `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/RepositorioCambioEstrategia.java`
- [X] T008 Implementar los adaptadores de repositorio en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioConfiguracionEstrategiaImpl.java` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioCambioEstrategiaImpl.java`, incluyendo la consulta del ultimo cambio por `timestampCambio DESC, id DESC`
- [X] T009 Actualizar `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/CotizacionService.java` y `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionServiceImpl.java` para exponer reserva/liberacion y consulta del estado de recalculo mediante el `AtomicBoolean` compartido
- [X] T010 Actualizar `backend/src/main/java/unq/losrecursionistas/backend/scheduler/CotizacionScheduler.java` y `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionAsyncWorker.java` para reservar al inicio y liberar en `finally`

**Checkpoint**: La persistencia y el mecanismo de coordinacion estan disponibles para implementar las historias sin carreras entre manual, scheduler y administracion.

---

## Phase 3: User Story 1 - Consultar la estrategia activa (Priority: P1) ðŸŽ¯ MVP

**Goal**: Un administrador autenticado puede consultar la estrategia efectiva, las dos alternativas y sus descripciones legibles.

**Independent Test**: Con `ROLE_ADMIN`, ejecutar `GET /api/admin/cotizacion/estrategia` y verificar estrategia activa, dos opciones, nombres, descripciones, origen y ultimo cambio.

### Tests for User Story 1

- [X] T011 [P] [US1] Crear casos de `EstrategiaAdminServiceTest` para consultar con configuracion persistida y sin fila persistida usando el fallback de `cotizacion.estrategia.activa` en `backend/src/test/java/unq/losrecursionistas/backend/service/EstrategiaAdminServiceTest.java`

### Implementation for User Story 1

- [X] T012 [US1] Definir el contrato de caso de uso y tipos de resultado de consulta en `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/EstrategiaAdminService.java`
- [X] T013 [US1] Implementar `consultar()` en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/EstrategiaAdminServiceImpl.java`, resolviendo primero la fila persistida y luego el fallback, con las dos estrategias y el ultimo cambio
- [X] T014 [US1] Crear `EstrategiaDisponibleResponse`, `EstrategiaActivaResponse` y los DTOs anidados necesarios en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/estrategia/`
- [X] T015 [US1] Crear `GET /api/admin/cotizacion/estrategia` en `backend/src/main/java/unq/losrecursionistas/backend/controller/EstrategiaAdminController.java` y mapear la respuesta del servicio sin exponer objetos HTTP desde el servicio
- [X] T016 [US1] Proteger la consulta con autenticacion y `ROLE_ADMIN` en `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`

**Checkpoint**: US1 queda funcional de forma independiente y permite conocer el estado persistido o fallback antes de cambiarlo.

---

## Phase 4: User Story 2 - Cambiar la estrategia activa (Priority: P1)

**Goal**: Un administrador puede confirmar un cambio valido, persistirlo atomico y usarlo en el siguiente recalcualo sin recalcular automaticamente.

**Independent Test**: Seleccionar la estrategia alternativa, ejecutar `PUT /api/admin/cotizacion/estrategia`, verificar respuesta exitosa, persistencia tras reinicio y uso en el siguiente recalcualo manual o programado.

### Tests for User Story 2

- [X] T017 [P] [US2] Cubrir cambio efectivo, auditoria, idempotencia, rechazo por recalcualo activo, estrategia invalida, rol insuficiente y rollback de persistencia en `backend/src/test/java/unq/losrecursionistas/backend/service/EstrategiaAdminServiceTest.java`
- [X] T018 [P] [US2] Actualizar pruebas de resolucion en tiempo de ejecucion, reserva/liberacion y estrategia fijada por job en `backend/src/test/java/unq/losrecursionistas/backend/service/CotizacionServiceImplTest.java`

### Implementation for User Story 2

- [X] T019 [P] [US2] Crear `CambiarEstrategiaRequest.java` en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/estrategia/` con `estrategia` obligatoria y no vacia
- [X] T020 [US2] Implementar `cambiar(String estrategia, Usuario usuarioAdmin)` en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/EstrategiaAdminServiceImpl.java` con validacion de codigo, rol, recalculo, idempotencia y zona `America/Argentina/Buenos_Aires`
- [X] T021 [US2] Asegurar la transaccion atomica de configuracion y auditoria en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/EstrategiaAdminServiceImpl.java`, con rollback ante fallo de persistencia y sin escrituras en operaciones idempotentes
- [X] T022 [US2] Crear `ExcepcionEstrategiaEnRecalculo.java` en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ExcepcionEstrategiaEnRecalculo.java` y traducirla a `409` en `backend/src/main/java/unq/losrecursionistas/backend/exceptions/ControladorErrores.java`
- [X] T023 [US2] Implementar `PUT /api/admin/cotizacion/estrategia` en `backend/src/main/java/unq/losrecursionistas/backend/controller/EstrategiaAdminController.java`, obteniendo el usuario autenticado y mapeando respuesta 200 sin iniciar recalculo
- [X] T024 [US2] Proteger el cambio con `ROLE_ADMIN` y conservar respuestas estandar 401/403 en `backend/src/main/java/unq/losrecursionistas/backend/configuration/SecurityConfig.java`
- [X] T025 [US2] Refactorizar `backend/src/main/java/unq/losrecursionistas/backend/service/impl/CotizacionServiceImpl.java` para resolver BD/fallback al inicio de cada recalculo, fijar el codigo durante todo el job y eliminar la estrategia fija inyectada
- [X] T026 [US2] Actualizar `backend/src/main/java/unq/losrecursionistas/backend/controller/CotizacionController.java` para conservar la estrategia historica y mapear nombres legibles en respuestas de historial y serie

**Checkpoint**: US2 queda funcional de forma independiente; los cambios efectivos sobreviven reinicios, no alteran cotizaciones previas y afectan solo al siguiente recalcualo.

---

## Phase 5: User Story 3 - Auditar el ultimo cambio (Priority: P1)

**Goal**: El panel muestra el origen inicial o el usuario y timestamp del ultimo cambio efectivo, incluso despues de reiniciar.

**Independent Test**: Consultar sin auditoria previa, realizar un cambio efectivo, recargar o reiniciar y verificar usuario, estrategias y timestamp en zona Buenos Aires.

### Tests for User Story 3

- [X] T027 [P] [US3] Agregar a `backend/src/test/java/unq/losrecursionistas/backend/service/EstrategiaAdminServiceTest.java` los casos de estado inicial sin auditoria, ultimo cambio ordenado y permanencia tras nueva consulta

### Implementation for User Story 3

- [X] T028 [US3] Completar el mapeo de `ultimoCambio` y `origen` en `backend/src/main/java/unq/losrecursionistas/backend/controller/EstrategiaAdminController.java` usando timestamp con offset de Buenos Aires
- [X] T029 [US3] Garantizar que `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioCambioEstrategiaImpl.java` exponga solo el ultimo registro y que `CambioEstrategia.java` no permita update/delete de dominio
- [X] T030 [US3] Verificar en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/EstrategiaAdminServiceImpl.java` que el primer cambio permita `estrategiaAnterior=null` y que la auditoria se cree una sola vez por cambio efectivo

**Checkpoint**: US3 queda funcional de forma independiente y ofrece trazabilidad operativa sin convertir la auditoria en una lista publica.

---

## Phase 6: User Story 4 - Identificar estrategias en el historial (Priority: P2)

**Goal**: Historial, grafico y detalle muestran nombres legibles sin exponer codigos internos ni modificar datos historicos.

**Independent Test**: Abrir historial de un jugador con ambas estrategias y verificar tabla, grafico, tooltip y detalle con nombres legibles.

### Tests for User Story 4

- [X] T031 [P] [US4] Actualizar las pruebas de `backend/src/test/java/unq/losrecursionistas/backend/service/CotizacionServiceImplTest.java` para demostrar que `estrategiaUsada` historica no se modifica al cambiar la configuracion activa

### Implementation for User Story 4

- [X] T032 [P] [US4] Mapear los codigos de estrategia a nombres legibles al construir las respuestas desde `backend/src/main/java/unq/losrecursionistas/backend/controller/CotizacionController.java`, usando un lookup centralizado para `CotizacionResponse.java` y `PuntoCotizacionResponse.java`
- [X] T033 [P] [US4] Actualizar los tipos de respuesta en `frontend/src/features/jugadores/types.ts` para recibir el nombre legible conservando compatibilidad con historial, serie y detalle
- [X] T034 [P] [US4] Ajustar etiquetas y tooltip de `frontend/src/features/jugadores/components/QuoteHistoryChart.tsx` y `frontend/src/features/jugadores/components/JugadorDetailModal.tsx` para renderizar exactamente el nombre recibido
- [X] T035 [US4] Verificar que `frontend/src/features/jugadores/components/QuoteHistoryChart.tsx`, `frontend/src/features/jugadores/components/JugadorDetailModal.tsx` y las herramientas de mercado no contienen lookup ni renderizado de codigos internos

**Checkpoint**: US4 queda funcional de forma independiente y todas las superficies historicas muestran nomenclatura legible sin mutar cotizaciones.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Integrar el panel administrativo, validar contratos y ejecutar la verificacion completa.

- [X] T036 [P] Implementar `frontend/src/features/admin/api/estrategiaApi.ts` o la extension equivalente de `frontend/src/features/admin/api/adminApi.ts` para GET/PUT y mensajes de error del backend
- [X] T037 [P] Crear tipos y hook de datos en `frontend/src/features/admin/types.ts` y `frontend/src/features/admin/hooks/useEstrategiaCotizacion.ts` con estados de carga, exito y error
- [X] T038 Crear `frontend/src/features/admin/components/EstrategiaCotizacionPanel.tsx` y `frontend/src/features/admin/components/EstrategiaCotizacionPanel.styles.ts` con seleccion, confirmacion y advertencia de que no hay recalculo automatico
- [X] T039 Integrar `EstrategiaCotizacionPanel` en `frontend/src/pages/AdminPage.tsx` junto a las acciones existentes de recalculo
- [X] T040 [P] Actualizar la documentacion de contrato y validacion en `specs/011-gestion-estrategia-cotizacion/quickstart.md` si la implementacion modifica nombres de respuesta o mensajes estandar
- [X] T041 Ejecutar `backend/gradlew.bat test` incluyendo `EstrategiaAdminServiceTest`, `CotizacionServiceImplTest` y ArchUnit, y corregir solo regresiones introducidas por esta feature
- [X] T042 Ejecutar `frontend/yarn lint` y `frontend/yarn typecheck`, y corregir errores de tipado o lint de esta feature
- [X] T043 Ejecutar los escenarios de `specs/011-gestion-estrategia-cotizacion/quickstart.md` para autenticacion, fallback, idempotencia, concurrencia, reinicio e historial

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias; T001-T003 pueden comenzar inmediatamente.
- **Foundational (Phase 2)**: Depende de Phase 1 y bloquea todas las historias.
- **US1 (Phase 3)**: Depende de Phase 2; habilita la consulta y es el MVP funcional.
- **US2 (Phase 4)**: Depende de Phase 2 y usa los contratos de US1; debe completar el servicio de consulta antes de integrar el PUT.
- **US3 (Phase 5)**: Depende de US2 para disponer de auditorias efectivas y su respuesta.
- **US4 (Phase 6)**: Depende de Phase 2 y de T026 para recibir nombres legibles en ambos endpoints; no modifica el historial.
- **Polish (Phase 7)**: T036-T039 dependen de los contratos de US1/US2; T041-T043 dependen de todas las implementaciones que validan.

### User Story Dependencies

- **US1 (P1)**: Depende de Phase 2. No depende de otra historia para consultar.
- **US2 (P1)**: Depende de Phase 2 y reutiliza el contrato creado en US1; la consulta sigue siendo independiente.
- **US3 (P1)**: Depende de US2 porque audita el cambio efectivo y expone el ultimo registro.
- **US4 (P2)**: Depende de Phase 2 y de la salida de historial existente; puede desarrollarse en paralelo con US1-US3 despues del refactor de cotizaciones.

### Parallel Opportunities

- Phase 1: T002 y T003 pueden ejecutarse en paralelo con T001.
- Phase 2: T004-T007 pueden ejecutarse en paralelo; T009-T010 requieren los contratos de reserva.
- US1: T011 puede prepararse en paralelo con T014; T012 y T013 son secuenciales.
- US2: T017-T019 pueden prepararse en paralelo; T025 y T026 requieren el servicio y sus contratos.
- US3: T027 puede escribirse en paralelo con T028-T029.
- US4: T031-T034 pueden ejecutarse en paralelo en archivos distintos.
- Polish: T036-T038 y T040 pueden ejecutarse en paralelo; T041-T043 son verificaciones posteriores.

### Parallel Example: User Story 1

```text
Task: "Definir catalogo de estrategias en backend/src/main/java/unq/losrecursionistas/backend/service/impl/estrategia/"
Task: "Crear DTOs de consulta en backend/src/main/java/unq/losrecursionistas/backend/controller/dto/estrategia/"
Task: "Crear casos de consulta en backend/src/test/java/unq/losrecursionistas/backend/service/EstrategiaAdminServiceTest.java"
```

### Parallel Example: User Story 2

```text
Task: "Crear CambiarEstrategiaRequest.java"
Task: "Agregar casos de idempotencia y rollback en backend/src/test/java/unq/losrecursionistas/backend/service/EstrategiaAdminServiceTest.java"
Task: "Actualizar casos de resolucion runtime en backend/src/test/java/unq/losrecursionistas/backend/service/CotizacionServiceImplTest.java"
```

### Parallel Example: User Story 4

```text
Task: "Actualizar tipos en frontend/src/features/jugadores/types.ts"
Task: "Ajustar QuoteHistoryChart.tsx y JugadorDetailModal.tsx"
Task: "Actualizar pruebas de historial en backend/src/test/java/unq/losrecursionistas/backend/service/CotizacionServiceImplTest.java"
```

## Implementation Strategy

### MVP First (US1 Only)

1. Completar Phase 1 y Phase 2.
2. Completar US1: servicio de consulta, DTOs, endpoint y seguridad.
3. Validar `GET /api/admin/cotizacion/estrategia` con administrador autenticado.
4. Detenerse para demostrar el estado activo, las dos alternativas y el fallback.

### Incremental Delivery

1. Agregar US2 para habilitar cambio persistido, idempotencia, concurrencia y recalcualo runtime.
2. Agregar US3 para exponer auditoria del ultimo cambio y permanencia tras reinicio.
3. Agregar US4 para nombres legibles en historial, grafico y detalle.
4. Completar Polish con panel, lint, typecheck y quickstart.

### Notes

- Las tareas con `[P]` solo son paralelizables cuando trabajan en archivos distintos y sus prerequisitos estan completos.
- Las pruebas se mantienen en `model` y `service` conforme a la constitucion; no se agregan pruebas de infraestructura redundantes.
- Cada tarea incluye al menos una ruta concreta y conserva la arquitectura Controller -> Service -> Persistence -> Model.
- No se agregan CRUD de estrategias, recalcualo automatico al cambiar ni soporte multi-instancia fuera del alcance definido.

