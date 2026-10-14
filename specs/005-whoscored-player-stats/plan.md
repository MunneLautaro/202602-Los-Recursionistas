# Plan de implementacion: Ingesta de estadisticas WhoScored

**Branch**: `005-whoscored-player-stats` | **Date**: 2026-10-13 | **Spec**: [spec.md](spec.md)

## Resumen

Se agregara una ingesta retomable y acotable de estadisticas por partido de
jugadores existentes. El caso de uso descubrira planteles, vinculara
identidades con `idWhoscored`, combinara las cuatro pestanas de estadisticas y
hara upsert idempotente de `EstadisticaPartido`. WhoScored quedara aislado
detras de `ScrapingPort`; el adapter usara respuestas JSON como fuente
primaria, JSON embebido como segunda opcion y DOM centralizado como ultimo
respaldo.

Esta entrega no implementa scheduler ni endpoints REST. El servicio de
aplicacion queda invocable por liga, equipo o jugador para una futura ejecucion
periodica. No se agrega un disparador manual: introducir uno ahora duplicaria
la superficie REST fuera del alcance pedido.

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.7.1

**Primary Dependencies**: Spring Data JPA, Spring Security existente,
PostgreSQL, Playwright for Java, Jackson, JUnit 5, Mockito, AssertJ y
Testcontainers para la prueba PostgreSQL. Playwright y Testcontainers deben
agregarse al build.

**Storage**: PostgreSQL. El proyecto no tiene Flyway ni Liquibase y actualmente
usa `spring.jpa.hibernate.ddl-auto=update`; se mantiene esa estrategia para
esta feature y se documenta como deuda de versionado de esquema.

**Testing**: Unitarios de `service` y del adapter con fixtures locales
HTML/JSON; integracion de persistencia contra PostgreSQL real con Testcontainers;
prueba real contra WhoScored separada por tag y excluida del build normal.

**Target Platform**: Servicio backend Spring Boot sobre JVM 21, desarrollo
Windows y CI Linux, con PostgreSQL.

**Project Type**: Backend web REST por capas, con un caso de uso interno
invocable sin nuevo controller en esta entrega.

**Performance Goals**: Concurrencia de scraping 1 por defecto, pausas y rate
limit configurables, reintentos acotados y backoff; completar una carga sin
duplicados y sin detenerse por un jugador fallido. No se fija throughput porque
WhoScored es una fuente no contractual.

**Constraints**: No crear jugadores; no modificar `idExterno`; procesar solo
`Jugador.activo`; filtrar por fecha de temporada activa; persistir solo
participaciones informadas con minutos mayores que cero;
una transaccion por jugador; Playwright no sale del adapter; una sola
`application.yml` como configuracion base.

**Scale/Scope**: Cinco ligas (Premier League, Bundesliga, La Liga, Serie A y
Ligue 1), todos los torneos informados por WhoScored en el intervalo
configurado, y agregados semanales futuros consultables por jugador y rango de
fechas.

## Constitution Check

_GATE: pasa antes de Phase 0 y se reevalua despues de Phase 1._

- **Principios I y III**: el servicio depende solo de `ScrapingPort`; modelos,
  repositorios, servicios y adapter se ubican en sus capas existentes. No se
  agrega scheduler ni controller.
- **Principios V y VI**: `ScrapingException` encapsula Playwright, red, parsing,
  timeout y anti-bot; el servicio valida alcance y existencia; no se agregan
  validaciones de formato al modelo.
- **Principio VII**: fixtures locales cubren adapter/parser, matching y
  servicio; PostgreSQL real cubre constraints/upsert; la prueba real de web
  queda marcada y fuera del build normal.
- **Principios VIII y XIII**: se usa exactamente `adapter/whoscored/` con
  `dto/`, `ScrapingConfiguration`, `ScrapingException`, `ScrapingPort` y
  `ScrapingRestAdapter`; los DTOs no atraviesan la frontera.
- **Principio IX**: se resolvieron las ambiguedades de extraccion, upsert,
  migraciones, Testcontainers, cero minutos y disparador manual en
  [research.md](research.md).
- **Principios X y XI**: nombres nuevos en espanol ASCII, sin codigo
  anticipado para scheduler, endpoints, valuacion o agregados.
- **Configuracion unica**: el repositorio hoy tiene `application.properties`,
  pero la constitucion y el pedido exigen `application.yml`. La implementacion
  debe migrar el contenido existente a ese unico archivo, sin perfiles.

No hay violaciones constitucionales. La migracion de properties a YAML es
necesaria para cumplir la regla vigente y no introduce una segunda
configuracion.

## Estructura del proyecto

### Documentacion

```text
specs/005-whoscored-player-stats/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── README.md
│   └── scraping-port.md
└── tasks.md                 # Se generara en Phase 2
```

### Codigo fuente

```text
backend/
├── build.gradle
├── src/main/java/unq/losrecursionistas/backend/
│   ├── adapter/whoscored/
│   │   ├── dto/
│   │   ├── ScrapingConfiguration.java
│   │   ├── ScrapingException.java
│   │   ├── ScrapingPort.java
│   │   └── ScrapingRestAdapter.java
│   ├── model/
│   │   ├── EstadisticaPartido.java
│   │   ├── CasoVinculacion.java
│   │   └── EjecucionIngesta.java
│   ├── persistence/repository/{interfaces,impl}/
│   ├── persistence/sql/
│   └── service/{interfaces,impl}/
│       ├── MatchingWhoscoredService.java
│       └── IngestaWhoscoredService.java
├── src/main/resources/application.yml
└── src/test/
    ├── java/unq/losrecursionistas/backend/service/
    └── resources/whoscored/
```

**Structure Decision**: se mantiene el backend existente, con entidades en
`model`, contratos de repositorio separados de DAOs en `persistence`, casos de
uso en `service/interfaces` y `service/impl`, y scraping completamente aislado
en `adapter/whoscored`. Los tests siguen la jerarquia permitida por la
constitucion.

## Complejidad

No aplica: no hay violaciones constitucionales.

## Constitution Check posterior al diseno

El gate permanece aprobado: el modelo no importa Playwright, el servicio solo
consume `ScrapingPort`, los DTOs permanecen dentro del adapter, cada entidad
nueva tiene repositorio en `persistence`, y los tests previstos respetan la
jerarquia exigida. La unica decision de infraestructura adicional es agregar
Playwright y Testcontainers al build y consolidar la configuracion en
`application.yml`; ambas son necesarias para los requisitos explicitados y no
crean una capa paralela.
