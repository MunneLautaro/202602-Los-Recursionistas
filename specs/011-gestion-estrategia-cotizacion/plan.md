# Plan de implementación: Gestion de estrategia de cotizacion

**Branch**: `011-gestion-estrategia-cotizacion` | **Fecha**: 2026-11-22 |
**Spec**: [spec.md](spec.md)

## Summary

Agregar al panel administrativo la consulta y el cambio de la estrategia activa de cotizacion. Persistir la configuracion activa en una tabla singleton, registrar cambios efectivos append-only y resolver la estrategia en cada recálculo con prioridad BD/fallback de configuracion. Rechazar cambios mientras un recálculo compartido esté activo. Mapear los codigos históricos a nombres legibles en los DTOs existentes de historial y serie.

## Technical Context

**Language/Version:** Java 21, Spring Boot, Gradle.

**Primary Dependencies:** Spring Data JPA, Spring Security, PostgreSQL, JUnit 5, Mockito, ArchUnit, React 19, Vite, TypeScript, Tailwind CSS 4.

**Storage:** PostgreSQL; JPA con `spring.jpa.hibernate.ddl-auto=update`.

**Testing:** tests unitarios de service con JUnit 5/Mockito y ArchUnit.

**Target Platform:** backend JVM 21 y frontend web.

**Project Type:** aplicacion web con backend REST y frontend SPA.

**Performance Goals:** cambio visible por debajo de tres interacciones y respuesta HTTP inmediata salvo persistencia.

**Constraints:** sin CRUD de estrategias, sin recalculo al cambiar, español técnico ASCII y zona `America/Argentina/Buenos_Aires`.

**Scale/Scope:** una instancia de backend para el mecanismo de concurrencia elegido; dos estrategias y un ultimo registro de auditoría visible.

## Constitution Check

**Gate previo a Phase 0: APROBADO.**

- Principios I y III: respetar capas y separar interfaz/implementacion.
- Principio IV: el ultimo cambio es un unico DTO y no introduce listado.
- Principios V y VI: excepciones en `exceptions`; DTO valida entrada y service valida dominio.
- Principio VII: agregar `EstrategiaAdminServiceTest` y actualizar `CotizacionServiceImplTest`.
- Principio IX: persistencia y concurrencia se cerraron con alternativas y selección explicita.
- Principio XI: solo se administran las dos estrategias existentes.
- Principios XIV-XVI: resolución runtime, transaccion atomica y auditoría append-only.

## Project Structure

### Documentation (this feature)

```text
specs/011-gestion-estrategia-cotizacion/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── estrategia-admin.md
│   ├── servicio-estrategia-admin.md
│   └── cotizaciones-modificadas.md
└── spec.md
```

### Source Code (repository root)

```text
backend/src/main/java/unq/losrecursionistas/backend/
├── model/
│   ├── ConfiguracionEstrategia.java
│   └── CambioEstrategia.java
├── persistence/
│   ├── sql/
│   │   ├── ConfiguracionEstrategiaDAOSQL.java
│   │   └── CambioEstrategiaDAOSQL.java
│   └── repository/
│       ├── interfaces/RepositorioConfiguracionEstrategia.java
│       ├── interfaces/RepositorioCambioEstrategia.java
│       ├── impl/RepositorioConfiguracionEstrategiaImpl.java
│       └── impl/RepositorioCambioEstrategiaImpl.java
├── service/
│   ├── interfaces/EstrategiaAdminService.java
│   └── impl/EstrategiaAdminServiceImpl.java
├── controller/
│   ├── EstrategiaAdminController.java
│   └── dto/estrategia/
│       ├── EstrategiaDisponibleResponse.java
│       ├── EstrategiaActivaResponse.java
│       └── CambiarEstrategiaRequest.java
└── exceptions/ExcepcionEstrategiaEnRecalculo.java

frontend/src/
├── features/admin/
│   ├── api/estrategiaApi.ts
│   ├── types.ts
│   ├── hooks/useEstrategiaCotizacion.ts
│   └── components/EstrategiaCotizacionPanel.tsx
│   └── components/EstrategiaCotizacionPanel.styles.ts
└── pages/AdminPage.tsx
```

## Archivos existentes a modificar

### Backend
- `service/impl/CotizacionServiceImpl.java`: quitar estrategia fija y resolver en runtime.
- `service/interfaces/CotizacionService.java`: agregar consulta de recálculo activo si es necesario.
- `service/impl/CotizacionAsyncWorker.java`: mantener reserva/liberacion segura.
- `scheduler/CotizacionScheduler.java`: reservar recálculo al inicio y liberar al final.
- `controller/CotizacionController.java`: mantener reserva manual y mapear nombres legibles.
- `controller/dto/cotizacion/CotizacionResponse.java`: recibir nombre legible desde el controller.
- `controller/dto/cotizacion/PuntoCotizacionResponse.java`: recibir nombre legible desde el controller.
- `configuration/SecurityConfig.java`: proteger `GET/PUT /api/admin/cotizacion/estrategia`.
- `exceptions/ControladorErrores.java`: traducir conflicto si se crea la excepcion nueva.
- `src/test/java/.../service/CotizacionServiceImplTest.java`: actualizar firma y mocks.

### Frontend
- `pages/AdminPage.tsx`: integrar el nuevo panel.
- `features/admin/api/adminApi.ts`: integrar o reexportar API de estrategia.
- `features/jugadores/types.ts`: garantizar recepcion del campo como nombre legible.
- `features/jugadores/components/JugadorDetailModal.tsx`: ajustar solo si la pantalla exige adaptacion del DTO.
- `features/jugadores/components/QuoteHistoryChart.tsx`: ajustar solo si el tipo recibido cambia.

## Decisiones de diseño obligatorias

1. Tabla de configuracion singleton mas tabla de auditoría separada.
2. `AtomicBoolean` dentro de `CotizacionServiceImpl`; manual, worker y scheduler comparten la reserva.
3. Cada recálculo obtiene la estrategia persistida; si no existe usa `@Value("${cotizacion.estrategia.activa}")`.
4. El cambio efectivo actualiza configuracion y crea auditoria en una unica transaccion.
5. La operacion idempotente no escribe configuracion ni auditoria.
6. El mapeo legible ocurre al construir el DTO HTTP.
7. La zona Buenos Aires se usa para mostrar timestamps sin mutar su almacenamiento.

## Plan por fases

### Phase 0 - Investigacion

1. Relevar `CotizacionServiceImpl`, `CotizacionScheduler`, `CotizacionAsyncWorker` y `CotizacionService`.
2. Relevar `Cotizacion`, historial, serie y detalle.
3. Relevar panel admin, seguridad y patrones de API.
4. Consolidar hallazgos y alternativas en [research.md](research.md).

### Phase 1 - Diseño

1. Definir entidades y reglas en [data-model.md](data-model.md).
2. Definir contrato REST y servicio en [contracts/](contracts/).
3. Definir validacion end-to-end en [quickstart.md](quickstart.md).

### Phase 2 - Backend nuevo

1. Crear modelo/configuracion y auditoria.
2. Crear DAOs y repositorios.
3. Crear interfaz e implementacion de `EstrategiaAdminService`.
4. Crear controller, DTO request/response y excepcion de conflicto.
5. Agregar proteccion por `ROLE_ADMIN`.
6. Crear `EstrategiaAdminServiceTest`:
   happy path, idempotencia, recálculo en curso, rol insuficiente,
   estado inicial sin auditoría previa y rollback por fallo.

### Phase 3 - Refactor backend existente

1. Refactorizar `CotizacionServiceImpl` para quitar el campo fijo.
2. Resolver activo al inicio de cada recálculo y fijarlo para todo el job.
3. Integrar reserva/liberacion en scheduler y async.
4. Actualizar DTOs existentes con nombre legible desde el controller.
5. Actualizar `CotizacionServiceImplTest` y las pruebas de resolucion runtime.

### Phase 4 - Frontend

1. Crear tipos y API para consultar/cambiar.
2. Crear hook de datos.
3. Crear componente de estrategia con confirmacion y estados de carga/error.
4. Integrar el componente en `AdminPage`.
5. Verificar que historial, grafico y detalle muestran el nombre recibido sin codigo.

### Phase 5 - Integracion y verificacion

1. Ejecutar `./gradlew test`, incluyendo ArchUnit.
2. Ejecutar lint y typecheck frontend.
3. Validar contratos HTTP y OpenAPI.
4. Ejecutar escenarios de reinicio, idempotencia y concurrencia.

## Complexity Tracking

No aplica: no hay violaciones constitucionales justificadas.

## Constitution Check posterior al diseño

**Gate posterior: APROBADO condicionado a tareas de implementacion.**

- Configuracion persistida separada de auditoria append-only.
- Cambio transaccional y idempotente.
- Resolucion BD/fallback al iniciar cada recálculo.
- Rechazo por recálculo con `AtomicBoolean`.
- `ROLE_ADMIN` obligatorio para cambio.
- Mapeo de nombre legible en controller.
- Ultimo cambio expuesto como unico DTO sin paginacion nueva.
