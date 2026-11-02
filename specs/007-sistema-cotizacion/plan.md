# Plan de implementación: Sistema de cotización de jugadores

**Branch**: `007-sistema-cotizacion` | **Fecha**: 2026-10-21 |
**Spec**: [spec.md](spec.md)

## Resumen

Se agregará un módulo de cotización histórica que agrega estadísticas
recientes de jugadores elegibles, calcula el valor mediante una estrategia
seleccionada por nombre de bean y persiste una cotización append-only. El
mismo caso de uso será invocado por un scheduler semanal y por un endpoint
administrativo asíncrono, con exclusión mediante `AtomicBoolean`. Historial y
ranking se expondrán como páginas REST documentadas con OpenAPI.

## Contexto técnico

**Lenguaje/versión:** Java 21, Spring Boot y Gradle Wrapper.

**Dependencias principales:** Spring Data JPA, PostgreSQL, Spring
Security/JWT existente, Spring Scheduling/Async, Springdoc/OpenAPI, JUnit 5 y
Mockito.

**Persistencia:** PostgreSQL existente. `Cotizacion` se crea con JPA y
`spring.jpa.hibernate.ddl-auto=update`; no se agregan migraciones. Las
consultas de historial y ranking mantienen la paginación en la base.

**Testing:** tests unitarios con Mockito únicamente para `model` y `service`;
no se agregan tests de controllers, configuration ni infraestructura.

**Plataforma:** backend Spring Boot JVM 21 bajo `backend/`.

**Metas de rendimiento:** historial y ranking por debajo de 2 s p95 con 100
requests concurrentes; aceptación del recálculo manual por debajo de 1 s.

**Restricciones:** una sola `application.yml`, `Page<T>` obligatorio con
tamaño 12, nombres españoles ASCII, OpenAPI en los tres endpoints, scheduler
sin `SecurityContext`, YAGNI estricto y entidades existentes sin cambios
funcionales.

**Alcance:** entidad `Cotizacion`, dos estrategias, persistencia, servicio,
controller, scheduler, configuración específica y documentación/validación.

## Constitution Check

**Gate previo a Phase 0: APROBADO.**

- **Principios I y III:** se conservan las capas `model`, `service`,
  `controller`, `persistence`, `scheduler` y `configuration`; el controller
  mapea DTOs y el servicio depende de repositorios/contratos.
- **Principio IV:** historial y ranking atraviesan todas las capas como
  `Page<T>`; el tamaño default es 12 y el `Pageable` se construye en service.
- **Principios V y VI:** errores de dominio/validación se traducen mediante
  `ControladorErrores`; el service valida existencia y rango de fechas.
- **Principio VII:** solo se planifican tests unitarios de model/service con
  Mockito, incluyendo cada estrategia.
- **Principio XIV:** se usa `Map<String, EstrategiaCotizacion>`, nombres de
  bean exactos, configuración externalizada y `CotizacionScheduler`.
- **Principios IX, X y XI:** las alternativas de estrategia, executor y
  ranking quedan justificadas en [research.md](research.md); no se agregan
  tablas de ejecución, porcentaje de variación ni frontend.

No hay violaciones constitucionales ni aclaraciones pendientes.

## Estructura del proyecto

```text
backend/src/main/java/unq/losrecursionistas/backend/
├── model/Cotizacion.java
├── service/
│   ├── interfaces/{CotizacionService,EstrategiaCotizacion}.java
│   └── impl/
│       ├── CotizacionServiceImpl.java
│       └── estrategia/
│           ├── PerformanceGeneralEstrategia.java
│           └── PerformancePorPosicionEstrategia.java
├── controller/
│   ├── CotizacionController.java
│   └── dto/cotizacion/{CotizacionResponse,RankingJugadorResponse}.java
├── persistence/
│   ├── repository/
│   │   ├── interfaces/RepositorioCotizacion.java
│   │   └── impl/RepositorioCotizacionImpl.java
│   └── sql/CotizacionDAOSQL.java
└── scheduler/CotizacionScheduler.java
```

Se selecciona el backend existente y se respeta su arquitectura por capas.
`CotizacionServiceImpl` orquesta y no calcula; `EstrategiaCotizacion` encapsula
cada fórmula; `CotizacionDAOSQL` mantiene consultas JPA/SQL;
`CotizacionScheduler` solo dispara el caso de uso. La separación evita que el
módulo conozca detalles de ingesta o scraping.

## Plan por fases

### Phase 0 — Investigación

1. Consolidar patrones actuales de JPA, `Page<T>`, seguridad, scheduling y
   async en [research.md](research.md).
2. Resolver la selección por `Map<String, EstrategiaCotizacion>` frente a
   enum/switch y factory, dejando la clave igual al nombre de bean.
3. Resolver ranking mediante proyección paginada y consulta nativa PostgreSQL
   con última/anterior cotización por jugador, sin listas en memoria.
4. Resolver asincronía con `AsyncCotizacionConfiguration` y bean separado con
   `@Async`; la exclusión funcional seguirá siendo `AtomicBoolean`.

### Phase 1 — Diseño y contratos

1. Definir `Cotizacion` append-only, relaciones, precisión monetaria, ventanas
   e índices en [data-model.md](data-model.md).
2. Documentar los tres endpoints en [contracts/](contracts/), incluyendo
   paginación, filtros, autorización, respuestas 202/409 y errores.
3. Documentar escenarios ejecutables en [quickstart.md](quickstart.md),
   incluidos cálculo, posiciones, historial, ranking, autorización y
   concurrencia.
4. Incorporar en `application.yml` la configuración indicada por la spec,
   incluidos pesos, variables de entorno, cron y zona.
5. Mantener como trabajo posterior la generación de `tasks.md` mediante
   `/speckit-tasks`; este plan no implementa código.

## Decisiones de diseño obligatorias

- `CotizacionServiceImpl` recibe `Map<String, EstrategiaCotizacion>` y obtiene
  la clave activa desde `cotizacion.estrategia.activa`. Se validará que exista
  y sea utilizable antes de persistir; no habrá `switch`.
- Las implementaciones se registran como `@Component("PERFORMANCE_GENERAL")`
  y `@Component("PERFORMANCE_POR_POSICION")`.
- Antes de delegar, el servicio agrega estadísticas por jugador y calcula
  máximos por métrica; las estrategias son puras y no acceden a DB.
- Se creará `AsyncCotizacionConfiguration` separado del executor de ingesta:
  un worker evita contención entre scraping y cotización, y la cola no
  sustituye el control de concurrencia.
- El método de aceptación reservará el `AtomicBoolean` antes de encolar y
  devolverá 409 si está ocupado; el worker liberará la variable en `finally`.
- La ventana se fija al inicio: `ventanaFin = LocalDate.now()` y
  `ventanaInicio = ventanaFin.minusDays(ventanaDias)`.

## Complexity Tracking

No aplica: no hay violaciones constitucionales que justificar.

## Constitution Check posterior al diseño

**Gate posterior a Phase 1: APROBADO condicionado a ejecución de tasks.**

- El modelo no contiene fórmulas; las estrategias concentran el cálculo.
- Los tres endpoints son `Page` cuando listan datos y tienen contratos
  OpenAPI explícitos.
- Scheduler y worker no consultan ni propagan `SecurityContext`.
- Los errores de configuración abortan antes de persistir; los errores por
  jugador se registran y se informan como omitidos.
- La persistencia usa la entidad e índices solicitados, sin entidad de
  ejecución ni tablas de versiones.
