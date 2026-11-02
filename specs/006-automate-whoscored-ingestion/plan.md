# Plan de implementación: Automatización de ingesta WhoScored

**Branch**: `006-automate-whoscored-ingestion` | **Fecha**: 2026-10-19 |
**Spec**: [spec.md](spec.md)

## Resumen

Se agregará el disparador operativo que falta para WhoScored: un
`WhoScoredScheduler` configurable y posterior a Football Data, un trigger REST
administrativo asíncrono y una consulta de la última ejecución. Ambos
disparadores reutilizarán un único caso de uso que reserva una ejecución de
forma atómica, vincula ligas/equipos/jugadores y luego carga estadísticas
idempotentes. La autorización quedará en la frontera HTTP, nunca en el job.

## Contexto técnico

**Lenguaje/versión:** Java 21, Spring Boot, Spring Security, Gradle Wrapper.

**Dependencias principales:** Spring Data JPA, PostgreSQL, Jackson, Playwright
for Java existente en `adapter/whoscored`, OpenAPI, JUnit 5, Mockito y fixtures
locales.

**Persistencia:** PostgreSQL existente, tabla `ejecuciones_ingesta` y entidades
de liga, equipo, jugador y estadísticas. Los cambios de columnas, estados y
JSONB de `EjecucionIngesta` se aplican mediante la estrategia JPA/Hibernate ya
configurada (`spring.jpa.hibernate.ddl-auto`, actualmente `update`); no se
agrega una herramienta de migraciones.

**Testing:** tests de `model` y `service` con Mockito y tests unitarios del
adapter con fixtures locales. No se accede a la web real en CI.

**Plataforma:** backend Spring Boot sobre JVM 21.

**Rendimiento:** aceptación REST menor a 2 s; executor dedicado de un hilo;
concurrencia efectiva de ingesta igual a 1; rate limiting y reintentos
configurados para WhoScored.

**Restricciones:** una sola `application.yml`; Playwright únicamente en
`adapter/whoscored`; sin creación de jugadores; no modificar ids Football Data;
errores parciales conservan datos válidos; scheduler sin `SecurityContext`.

**Alcance:** cinco ligas existentes, rango de temporada configurado y
entidades ya sincronizadas desde Football Data.

## Constitution Check

**Gate previo a Phase 0: APROBADO con decisiones documentadas.**

- **Principio I:** scheduler, configuration, controller, service, persistence y
  adapter respetan capas; no se crea `client/` ni `gateway/`.
- **Principio III:** un contrato en `service/interfaces` y una implementación
  en `service/impl`; el controller mapea respuestas y el dominio depende de
  `ScrapingPort`.
- **Principio VI:** request DTO valida nullidad y positividad; el service hace
  fail-fast de existencia; no hay defaults silenciosos.
- **Principio VII:** se mantienen tests de model/service con Mockito y fixtures
  locales del adapter sin web real, como exige la constitución.
- **Principio VIII/XIII:** DTOs `record` en `adapter/whoscored/dto`, Playwright y
  `ScrapingException` aislados; propiedades de scraping externalizadas.
- **Principio IX:** matching, alcance, huérfanas y asincronía fueron consultados
  y sus alternativas están en [research.md](research.md).
- **Principio XI:** no se agregan recuperación de huérfanas, colas externas ni
  nuevas fuentes fuera de la spec.
- **Scheduler:** `WhoScoredScheduler` será un `@Component` con `@Scheduled`,
  cron `scheduler.whoscored.cron`, zona argentina, habilitación configurable y
  ventana posterior a Football Data.
- **Scraping:** todas las fallas se envuelven en `ScrapingException`, con
  límites anti-bot y cierre controlado de recursos.

No hay violaciones constitucionales ni enmiendas pendientes.

## Plan por fases

### Phase 0 — Investigación

1. Consolidar la inspección de entidades, servicios, repositorios, seguridad,
   scheduler y adapter en [research.md](research.md).
2. Eliminar duplicación entre `EjecucionIngestaServiceImpl` e
   `IngestaWhoscoredServiceImpl`, conservando un único caso de uso.
3. Definir la operación atómica de reserva ACTIVA en PostgreSQL y su registro
   de rechazo.
4. Confirmar en revisión de diseño que los fixtures del adapter son locales y
   que no se accede a la web real en CI.

### Phase 1 — Diseño y contratos

1. Extender mínimamente `ScrapingPort` y sus `record` DTOs para descubrimiento,
   vínculo y estadísticas, sin exponer detalles de Playwright.
2. Definir el modelo y transiciones en [data-model.md](data-model.md).
3. Implementar los contratos REST documentados en
   [contracts/iniciar-ingesta.md](contracts/iniciar-ingesta.md) y
   [contracts/ultima-ejecucion.md](contracts/ultima-ejecucion.md).
4. Aplicar `ROLE_ADMIN` en controller/SecurityConfig y retirar la dependencia
   de sesión del servicio.
5. Definir `ThreadPoolTaskExecutor` de un hilo en `configuration/` y el uso de
   `@Async`; reservar antes de encolar.
6. Configurar `scheduler.whoscored.cron` y habilitación en `application.yml`;
   ubicar el scheduler exclusivamente en `scheduler/`.
7. Ejecutar la validación descrita en [quickstart.md](quickstart.md), incluida
   idempotencia, errores parciales, estados y ausencia de ejecuciones.

## Estructura del proyecto

```text
backend/src/main/java/unq/losrecursionistas/backend/
├── adapter/whoscored/
│   ├── dto/
│   ├── ScrapingConfiguration.java
│   ├── ScrapingException.java
│   ├── ScrapingPort.java
│   └── ScrapingRestAdapter.java
├── configuration/
│   ├── AsyncIngestaConfiguration.java
│   └── SecurityConfig.java
├── controller/
│   ├── WhoscoredIngestaController.java
│   └── dto/whoscored/
├── model/
│   └── EjecucionIngesta.java
├── persistence/
│   ├── repository/{interfaces,impl}/
│   └── sql/
├── scheduler/
│   └── WhoScoredScheduler.java
└── service/
    ├── interfaces/
    │   ├── IngestaWhoscoredService.java
    │   └── MatchingWhoscoredService.java
    └── impl/
        ├── IngestaWhoscoredServiceImpl.java
        └── MatchingWhoscoredServiceImpl.java
```

Documentación: `specs/006-automate-whoscored-ingestion/` contiene este plan,
investigación, modelo, contratos y quickstart. `tasks.md` se generará en la
fase posterior de tareas.

**Decisión estructural:** conservar las capas existentes y componer servicios,
sin herencia. La configuración específica de WhoScored permanece en
`adapter/whoscored`; la configuración del executor es global en
`configuration/`.

## Constitution Check posterior al diseño

**Gate posterior a Phase 1: APROBADO condicionado a ejecución de tareas.**

- Los endpoints no filtran detalles de scraping y mapean entidades a DTOs.
- La reserva atómica elimina la carrera existente entre scheduler y REST.
- La asincronía no crea un hilo por request ni depende de `SecurityContext`.
- Los estados y errores parciales son persistidos y consultables.
- Se mantiene la advertencia legal/operativa de anti-bot y términos de
  WhoScored.
- Los tests del adapter son obligatorios como tests unitarios con fixtures
  locales y no acceden a WhoScored real en CI.
