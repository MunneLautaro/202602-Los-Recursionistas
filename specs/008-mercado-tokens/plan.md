# Implementation Plan: Mercado de Tokens

**Branch**: `008-mercado-tokens` | **Date**: 2026-10-25 | **Spec**:
[spec.md](spec.md)

**Input**: Feature specification from
`/specs/008-mercado-tokens/spec.md`

## Summary

Implementar el mercado de tokens con órdenes de compra y venta al precio
vigente capturado, matching FIFO con ejecución parcial, reservas de créditos y
tokens, liquidez inicial idempotente y recarga ficticia. La implementación
seguirá los contratos por capas existentes: invariantes en `model`, orquestación
en `service`, persistencia mediante repositorios y DAOs, y DTOs/controladores
solo para transporte. Todas las modificaciones financieras serán atómicas,
con locks `PESSIMISTIC_WRITE`, rollback total y auditoría append-only.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.1.1, Spring Data JPA, Spring
Validation, Spring Security, PostgreSQL, H2 y Mockito

**Storage**: PostgreSQL en ejecución; H2 y Testcontainers PostgreSQL para
validación automatizada según el alcance de cada prueba

**Testing**: JUnit 5, Mockito, tests unitarios únicamente en `model` y
`service`, con escenarios de rollback, insuficiencia y concurrencia simulada

**Target Platform**: Servicio web backend ejecutado en JVM con API REST

**Project Type**: Web service con frontend existente

**Performance Goals**: Responder operaciones normales del mercado en una sola
transacción y mantener consultas paginadas con tamaño predeterminado 12; no se
fija una meta de throughput no solicitada.

**Constraints**: No doble gasto, doble venta ni saldos negativos; rollback
total; auditoría inmutable; zona `America/Argentina/Buenos_Aires`; sin precios
límite, stop-loss, vencimientos, comisiones ni pagos reales.

**Scale/Scope**: Mercado para jugadores activos del catálogo, 100 tokens
emitidos por jugador, órdenes paginadas y matching FIFO; alcance limitado a los
cinco endpoints definidos en la especificación.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Resultado | Evidencia |
|---|---|---|
| Capas y contratos (I, III) | PASS | `model`, `controller/dto`, `service/interfaces`, `service/impl`, `persistence/repository` y `persistence/sql` se mantienen separados. |
| Invariantes en modelo (II, VI) | PASS | Saldo, reservas, tenencias y estados se validan en entidades; formato en DTO y existencia en service. |
| Paginación (IV) | PASS | `Page<T>` y `PageRequest` tamaño 12 para órdenes y transacciones. |
| Errores (V) | PASS | Se reutilizan excepciones no chequeadas y manejo centralizado existente. |
| Testing (VII) | PASS | Pruebas aisladas en `model`/`service`, incluyendo rollback, insuficiencias y concurrencia. |
| Idioma/YAGNI (X, XI) | PASS | Nombres ASCII en español; se excluyen funcionalidades no solicitadas. |
| Integridad financiera (XV) | PASS | `@Transactional`, aislamiento sin lecturas sucias/no repetibles y `PESSIMISTIC_WRITE` sobre recursos financieros. |
| Auditoría (XVI) | PASS | Transacción y auditoría append-only en la misma transacción; sin update/delete históricos. |
| Matching aislado (XVII) | PASS | Puerto de matching en `service/interfaces`, implementación independiente de HTTP y scheduler. |

No se identifican violaciones que requieran justificación.

## Project Structure

### Documentation (this feature)

```text
specs/008-mercado-tokens/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
backend/
├── src/main/java/unq/losrecursionistas/backend/
│   ├── model/
│   │   ├── Orden.java
│   │   ├── TenenciaToken.java
│   │   ├── TransaccionMercado.java
│   │   └── AuditoriaTransaccional.java
│   ├── controller/
│   │   ├── dto/orden/
│   │   ├── dto/usuario/
│   │   ├── MercadoController.java
│   │   └── CreditosController.java
│   ├── service/
│   │   ├── interfaces/
│   │   │   ├── MercadoService.java
│   │   │   ├── MatchingEngine.java
│   │   │   └── LiquidezInicialService.java
│   │   └── impl/
│   ├── persistence/
│   │   ├── repository/interfaces/
│   │   ├── repository/impl/
│   │   └── sql/
│   └── scheduler/
└── src/test/java/unq/losrecursionistas/backend/
    ├── model/
    └── service/
frontend/src/features/mercado/
```

**Structure Decision**: Se selecciona la estructura web existente, pero el
alcance de esta feature se concentra en backend. Los contratos de entrada
residen en `controller/dto`, el caso de uso y matching en `service`, las
invariantes en entidades `model` y el acceso a datos detrás de
`persistence/repository` y `persistence/sql`. El frontend solo se extenderá si
la fase de tareas lo solicita explícitamente; no se agrega una pantalla por
anticipado.

## Phase 0: Research

La investigación se encuentra en [research.md](research.md) y resuelve:

1. Bloqueo pesimista, aislamiento y rollback para recursos financieros.
2. Precio unitario capturado e inmutable.
3. Matching FIFO con ejecución parcial.
4. Idempotencia de liquidez inicial y alta de jugadores nuevos.
5. Auditoría append-only separada de transacciones de mercado.
6. Validación distribuida entre DTO, service y model.

## Phase 1: Design & Contracts

El diseño se encuentra en [data-model.md](data-model.md), con entidades,
relaciones, invariantes, transiciones e índices. Los contratos HTTP están en
[contracts/openapi.yaml](contracts/openapi.yaml) para los cinco endpoints
requeridos. La guía ejecutable de validación está en
[quickstart.md](quickstart.md).

Decisiones de implementación para la fase de tareas:

- Añadir saldo reservado a `Usuario`, preservando el saldo total existente.
- Crear tenencias por usuario/jugador con cantidad total y reservada.
- Persistir órdenes con precio, remanente, estado y marca de liquidez inicial.
- Exponer puertos de mercado y matching sin dependencias de controladores.
- Consultar órdenes compatibles bloqueadas y procesarlas FIFO dentro de una
  única transacción.
- Crear transacción de mercado y auditoría antes del commit.
- Integrar el alta de liquidez nueva con el flujo de sincronización existente.
- Cubrir cada servicio financiero y cada entidad con lógica mediante Mockito.

## Constitution Check (post-design)

| Regla | Resultado | Verificación de diseño |
|---|---|---|
| Atomicidad y concurrencia | PASS | El diseño exige `@Transactional`, `REPEATABLE_READ` o superior y `PESSIMISTIC_WRITE` antes de validar/modificar. |
| Auditoría inmutable | PASS | Los modelos históricos solo tienen creación/consulta y se escriben en la transacción del match. |
| Matching aislado | PASS | `MatchingEngine` es puerto de service, invocable desde orden HTTP, scheduler o proceso asíncrono. |
| Paginación | PASS | El contrato define `page`/`size` y el servicio usará `PageRequest` con default 12. |
| Pruebas | PASS | Quickstart exige rollback, insuficiencia, idempotencia y carreras sin tests de controllers. |
| Alcance | PASS | No se incluyen cancelación, expiración, límites, comisiones ni pagos externos. |

No quedan decisiones marcadas como `NEEDS CLARIFICATION`.

## Complexity Tracking

No aplica: el diseño no introduce violaciones a la constitución ni proyectos
adicionales.
