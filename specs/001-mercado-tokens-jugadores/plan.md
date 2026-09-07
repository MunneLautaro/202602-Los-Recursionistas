# Implementation Plan: Mercado de Tokens de Jugadores

**Branch**: `001-mercado-tokens-jugadores` | **Date**: 2026-09-06 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-mercado-tokens-jugadores/spec.md`

## Summary

Construir un mercado simulado de tokens con cotizaciones versionadas, portfolio,
ordenes atomicas y auditoria inmutable. El backend se implementara con Java 21,
Spring Boot 4.1.1, PostgreSQL y una arquitectura estricta
`Controller -> Service -> Model rico -> Persistence/Adapters`. El frontend sera
Vite + React + TypeScript estricto y consumira exclusivamente la API REST.
Las decisiones abiertas de la especificacion quedan resueltas en
[research.md](research.md); los contratos y entidades se detallan en
[contracts/openapi.yaml](contracts/openapi.yaml) y [data-model.md](data-model.md).

## Technical Context

**Language/Version**: Java 21; TypeScript estricto para frontend.

**Primary Dependencies**: Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Bean
Validation, Spring Security, Spring Cache, Actuator, Lombok acotado, springdoc
OpenAPI, Vite, React y cliente HTTP tipado generado o equivalente.

**Storage**: PostgreSQL como persistencia principal; cache in-memory mediante
Spring Cache; datos locales persistidos para fallback de proveedores.

**Testing**: JUnit 5; unitarias puras del Model; Testcontainers PostgreSQL para
Service/Repository; MockMvc en paquete E2E web; mocks de adapters externos.

**Target Platform**: Servicio backend ejecutable con Gradle y PostgreSQL local o
Docker; frontend web responsivo.

**Project Type**: Aplicacion web con backend REST y frontend SPA.

**Performance Goals**: p95 menor a 1 segundo para catalogo, ranking y portfolio;
p95 menor a 2 segundos para compra/venta en la carga de referencia documentada en
[quickstart.md](quickstart.md).

**Constraints**: operaciones financieras atomicas; historial y cotizaciones
inmutables; fallback externo identificable; no modificar tests existentes; no
usar `any` en frontend; inputs sanitizados; mensajes funcionales en espanol.

**Scale/Scope**: cinco ligas objetivo, hasta 100 tokens por jugador, cuatro
usuarios como escenario minimo de concurrencia y catalogo extensible.

## Constitution Check

_GATE: aprobado antes de Phase 0 y reevaluado despues de Phase 1._

- **Arquitectura**: PASS. El plan conserva Controller -> Service -> Model rico ->
  Persistence/Adapters y mantiene dependencias hacia puertos.
- **Calidad y nombres**: PASS. DTO, Service y Model validan solo lo que conocen;
  nombres de dominio en espanol sin acentos ni `n`; errores y documentacion en
  espanol.
- **API**: PASS. OpenAPI precede implementacion, controllers sin logica de
  negocio y `@ControllerAdvice` para `400/404/409`.
- **Persistencia**: PASS. PostgreSQL, cache in-memory, fallback y registros
  historicos inmutables estan contemplados.
- **Seguridad/observabilidad**: PASS. Spring Security, sanitizacion, auditoria,
  logs estructurados, Correlation ID, health checks y metricas son entregables.
- **Frontend**: PASS. Vite/React/TypeScript estricto y cliente HTTP unico.
- **Pruebas**: PASS. TDD por capa, Testcontainers, MockMvc separado y proteccion
  del test existente.
- **Documentacion/DoD**: PASS condicionado a validar OpenAPI, Postman, Docker,
  SLAs y todos los escenarios en CI antes de cerrar la feature.

No hay violaciones que requieran Complexity Tracking.

## Fases de Implementacion

Las tareas de cada fase deben convertirse a `tasks.md` manteniendo el orden y
escribiendo primero los tests de la capacidad o en paralelo con su implementacion.

### Fase 0: Setup e Infraestructura Base

1. Partir del proyecto Spring Boot con Gradle ya existente en `backend/`;
   ajustar su configuracion a Java 21, Spring Boot 4.1.1 y dependencias de JPA, MVC,
   Validation, Security, Cache, Actuator, Lombok, PostgreSQL, Testcontainers y
   documentacion OpenAPI. Mantener y hacer pasar `contextLoads` sin modificarlo.
2. Crear perfiles local/test, `docker-compose` para PostgreSQL, datasource,
   migraciones y datos de bootstrap idempotente del Superusuario Financiero.
3. Definir configuracion de logs estructurados, filtro/interceptor de Correlation
   ID, health checks y metricas de latencia/tasa de error.
4. Antes de los componentes productivos, agregar checks de infraestructura:
   contexto local, PostgreSQL real en Testcontainers, health endpoint y captura
   de Correlation ID.
5. Fijar en configuracion la vigencia de cotizacion, zona UTC, precision de
   credito y carga de referencia; no dejar secretos en el repositorio.

### Fase 1: Capa de Modelo y Reglas del Dominio (Model Layer - TDD)

1. Escribir unitarias puras para `Usuario`, `Jugador`, `Cotizacion`, `TokenOrder`,
   `Portfolio`, `Posicion`, `Transaccion`, `AuditoriaFinanciera` y estrategias.
2. Implementar entidades ricas con nombres en espanol sin acentos ni `n` donde
   corresponda; `TokenOrder` y `PlayerQuoteService` conservan los nombres
   tecnicos exigidos por el contrato.
3. Encapsular invariantes: cantidades positivas, saldo no negativo, maximo 100
   tokens globales por jugador, precio/importe consistentes, estados validos,
   historial inmutable y transiciones atomicas.
4. Implementar score reproducible con dos estrategias configurables, pesos,
   version, redondeo monetario y fuente. Las unitarias cubren casos borde y no
   cargan contexto de Spring ni base de datos.
5. Verificar DoD de la fase: invariantes, errores propios del dominio y cobertura
   de casos felices, limites e inmutabilidad.

### Fase 2: Persistencia, Caching e Integracion Externa (Persistence & Adapters)

1. Escribir primero pruebas Testcontainers para repositorios, constraints,
   indices, paginacion, inmutabilidad, rollback e inicializacion idempotente.
2. Implementar entidades JPA, mapeos y repositorios por puertos, con indices
   justificados sobre jugador/estado, cotizacion/jugador/fecha, posiciones por
   usuario-jugador, transacciones por usuario-fecha y auditoria por correlation.
3. Implementar adapters de WhoScored y Football-Data.org con timeout,
   normalizacion, respuesta parcial y errores tipados.
4. Escribir pruebas de cache/fallback para hit, miss, timeout, proveedor caido,
   cache vacio y recuperacion; implementar Spring Cache y datos locales con fuente
   explicita en toda cotizacion.
5. Verificar DoD: PostgreSQL real, atomicidad, indices, fallback identificable y
   ningun registro historico actualizable o eliminable por los adapters.

### Fase 3: Capa de Servicios, Scheduler y Auditoria (Service Layer)

1. Definir tests de servicio para existencia, autorizacion, cotizacion vigente,
   saldo, disponibilidad, tenencia, concurrencia e idempotency key antes del
   codigo de caso de uso.
2. Implementar `PlayerQuoteService`, `PortfolioService` y servicios de mercado
   con limites transaccionales. La compra inicial liquida contra el Superusuario
   Financiero; una venta devuelve tokens al inventario financiero para conservar
   el limite global de 100.
3. Validar IDs y reglas en el orden especificado; rechazar sin cambios parciales
   y persistir operacion, estados anterior/posterior y auditoria en una transaccion.
4. Implementar scheduler semanal configurable, recalculo manual autorizado,
   ejecuciones idempotentes y conflicto de recalculos concurrentes.
5. Implementar auditoria inmutable con autor, Correlation ID, timestamp, detalle,
   estrategia/version y estados previo/posterior; probar reconstruccion de los
   escenarios con cuatro usuarios.
6. Verificar DoD de services con Testcontainers: rollback, concurrencia,
   idempotencia, proveedor fallido, saldo/disponibilidad/tenencia insuficientes.

### Fase 4: Capa Web, API REST y Seguridad (Controller Layer)

1. Escribir pruebas MockMvc en un paquete independiente antes de los controllers:
   contrato, Bean Validation, trimming/sanitizacion, autenticacion, roles,
   `400/404/409`, Correlation ID y respuestas sin datos sensibles.
2. Implementar DTOs Request/Response y controllers exactamente según
   [contracts/openapi.yaml](contracts/openapi.yaml), con la identidad del usuario
   tomada del principal autenticado en operaciones protegidas.
3. Implementar `@ControllerAdvice` para validacion/dominio `400`, inexistencia
   `404`, conflictos `409` y fallback generico seguro; documentar cada respuesta.
4. Configurar Spring Security, roles de usuario y operador, protegiendo compra,
   venta, recalculo y actualizacion de datos.
5. Verificar todos los endpoints de catalogo, cotizaciones, ranking, ordenes,
   portfolio e historial mediante MockMvc, sin mezclar estas pruebas con service.

### Fase 5: Documentacion y Contratos

1. Publicar OpenAPI/Swagger desde el contrato versionado y comprobar que requests,
   responses, seguridad, errores, paginacion y Correlation ID coinciden.
2. Crear coleccion Postman con casos felices y errores por endpoint, incluyendo
   saldo insuficiente, falta de tokens, tenencia insuficiente, `404`, `400`,
   cotizacion vencida y `409` concurrente.
3. Ejecutar validacion automatizada de contrato y documentar en quickstart los
   comandos de backend, PostgreSQL, tests y smoke tests.

### Fase 6: Integracion Frontend (Vite + React + TypeScript)

1. Crear la app Vite y un cliente HTTP tipado derivado de OpenAPI; activar
   `strict`, prohibir `any` y centralizar autenticacion, errores y Correlation ID.
2. Escribir pruebas de cliente y vistas para estados loading, vacio, error,
   filtros invalidos y respuestas paginadas.
3. Implementar catalogo con filtros, detalle/evolucion del jugador, ranking,
   operativa de compra/venta y portfolio con ganancias/perdidas e historial.
4. Verificar responsividad, accesibilidad basica, ausencia de errores de consola,
   tipos sincronizados y que no existan llamadas HTTP fuera del cliente central.

## Project Structure

### Documentation

```text
specs/001-mercado-tokens-jugadores/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/openapi.yaml
└── tasks.md
```

### Source Code

```text
backend/src/main/java/unq/losrecursionistas/backend/
├── model/                  # todas las clases de dominio
├── controller/             # todos los controllers
│   └── dto/                # DTOs de request/response
├── services/               # todos los services
│   ├── interfaces/         # contratos de services
│   └── impl/               # implementaciones de services
├── persistence/
│   └── repository/
│       ├── interfaces/     # contratos de repositories
│       └── impl/           # implementaciones de repositories
├── exceptions/             # excepciones y @ControllerAdvice
├── configuration/          # configuracion Spring, cache, scheduler y OpenAPI
└── security/               # autenticacion, JWT y autorizacion

backend/src/test/java/unq/losrecursionistas/backend/
├── model/                  # unitarias puras del dominio
├── service/                # pruebas de services
└── web/                    # MockMvc exclusivamente

frontend/
├── src/api/                # cliente HTTP y tipos OpenAPI
├── src/features/           # catalogo, cotizacion, mercado, portfolio
└── src/shared/             # utilidades compartidas del frontend
```

**Structure Decision**: Se mantiene el backend existente como proyecto Gradle y
se agrega un frontend independiente en `frontend/`. Los paquetes del backend se
organizan transversalmente por responsabilidad, con interfaces e implementaciones
separadas para services y repositories. Cada implementacion concreta usa el sufijo
`Impl`, conforme a la Constitucion v1.4.1.

## Post-Design Constitution Check

- **PASS**: el modelo rico no depende de Spring/JPA; services coordinan y
  controllers solo transportan.
- **PASS**: los contratos OpenAPI y DTOs quedan definidos antes de endpoints.
- **PASS**: pruebas puras, Testcontainers y MockMvc tienen paquetes y objetivos
  separados; `BackendApplicationTests` queda protegido.
- **PASS**: PostgreSQL, cache, seguridad, auditoria, observabilidad y frontend
  estan incluidos en entregables verificables.
- **PASS**: las decisiones de vigencia UTC, precision, identidad autenticada,
  venta al inventario financiero, idempotencia y carga de referencia estan
  registradas en `research.md`.

## Complexity Tracking

Sin violaciones constitucionales justificadas.
