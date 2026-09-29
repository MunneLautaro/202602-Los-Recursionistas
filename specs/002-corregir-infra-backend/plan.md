# Implementation Plan: Correccion de infraestructura del backend

**Branch**: `[002-corregir-infra-backend]` | **Date**: 2026-09-19 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-corregir-infra-backend/spec.md`

## Summary

La feature corrige la base del backend para que el modelo de dominio, la persistencia, la configuracion y las pruebas cumplan la Constitution v1.4.0. Se mantendra Java 21 con Spring Boot y PostgreSQL, se conservara la gestion de esquema de JPA/Hibernate sin una herramienta externa de migraciones, se definiran las entidades `Usuario`, `Liga`, `Equipo`, `Jugador`, `CotizacionJugador`, `PosicionPortfolio` y `TransaccionAuditoria` en `model`, y se separaran los repositorios en contratos, adaptadores y DAOs SQL.

Los servicios construiran `PageRequest.of(page, 12)` y los repositorios propagaran `Page<T>` sin paginacion en memoria. Las pruebas del backend usaran JUnit 5, AssertJ y Mockito, con suites separadas para `model` y `service`. Hibernate conservara `ddl-auto=update` para gestionar el esquema en esta etapa.

## Technical Context

**Language/Version**: Java 21 (toolchain ya configurado)

**Primary Dependencies**: Spring Boot 4.1.1, Spring Data JPA, Hibernate, Spring Security, Lombok, AssertJ, Mockito y JUnit Platform

**Storage**: PostgreSQL en `localhost:5432/football_market`; esquema gestionado por JPA/Hibernate

**Testing**: JUnit 5 con AssertJ y Mockito; ejecucion mediante Gradle y `useJUnitPlatform()`

**Target Platform**: Servidor JVM multiplataforma y CI de GitHub Actions

**Project Type**: Servicio web backend REST en monorepo

**Performance Goals**: Las consultas de listados deben paginar en la fuente de datos y no materializar resultados completos en memoria; el tamano por defecto es 12 registros por pagina.

**Constraints**: Una sola `application.properties`; sin perfiles separados; identificadores de codigo ASCII y espanol tecnico; arquitectura estricta; excepciones de dominio no chequeadas; no Jest, Vitest ni Mocha.

**Scale/Scope**: Siete entidades de dominio, siete contratos de repositorio, gestion inicial del esquema mediante JPA/Hibernate y pruebas unitarias del modelo y servicios. No incluye endpoints funcionales nuevos ni integraciones externas.

## Constitution Check

_GATE: Debe pasar antes de Phase 0 y se reevalua despues de Phase 1._

### Gate inicial

- **Principio I**: PASS. Las entidades se ubican en `model`; repositorios en `persistence/repository`; services y controllers conservan sus paquetes.
- **Principio II**: PASS. Las entidades tendran anotaciones JPA/Lombok y custodiaran invariantes de dominio.
- **Principio III**: PASS. Los servicios retornaran entidades o `Page<T>` y no DTOs ni JWT.
- **Principio IV**: PASS. Las consultas multiples usaran `Page<T>`; el service construira `PageRequest.of(page, 12)` al inicio de cada metodo.
- **Principio V**: PASS. Las excepciones personalizadas permaneceran en `exceptions` y extenderan `RuntimeException` directa o indirectamente.
- **Principio VI**: PASS. La validacion de forma queda en DTOs, la existencia en services y las invariantes en entidades.
- **Principio VII**: PASS. Se agregaran pruebas en `model` y `service` para casos felices y de borde sin borrar tests existentes.
- **Principio VIII**: PASS. No se implementan integraciones con WhoScored ni Football-Data.org en esta feature.
- **Principio IX**: PASS con decisiones documentadas en `research.md`. Las relaciones JPA, estrategia de gestion del esquema y contratos de repositorio tienen alternativas y tradeoffs registrados.
- **Configuracion**: PASS. PostgreSQL y la gestion de esquema JPA/Hibernate se configuraran unicamente en `backend/src/main/resources/application.properties`.

No hay violaciones que requieran excepcion.

## Project Structure

### Documentation (this feature)

```text
specs/002-corregir-infra-backend/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/README.md
└── tasks.md                 # Phase 2, generado por /speckit-tasks
```

### Source Code (repository root)

```text
backend/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/unq/losrecursionistas/backend/
│   │   │   ├── model/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Liga.java
│   │   │   │   ├── Equipo.java
│   │   │   │   ├── Jugador.java
│   │   │   │   ├── CotizacionJugador.java
│   │   │   │   ├── PosicionPortfolio.java
│   │   │   │   └── TransaccionAuditoria.java
│   │   │   ├── persistence/repository/
│   │   │   │   ├── interfaces/
│   │   │   │   └── impl/
│   │   │   ├── service/interfaces/
│   │   │   ├── service/impl/
│   │   │   ├── controller/dto/
│   │   │   ├── configuration/
│   │   │   ├── security/
│   │   │   └── exceptions/
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/unq/losrecursionistas/backend/
│       ├── model/
│       └── service/
└── gradlew.bat
```

**Structure Decision**: Se conserva el unico proyecto backend existente dentro de `backend`. La feature agrega el modelo y la persistencia en sus paquetes constitucionales, mantiene la separacion `interfaces`/`impl`, y conserva la gestion de esquema en `application.properties`. No se crea una estructura Node.js ni una segunda configuracion de Spring.

## Phase 0: Investigacion y decisiones

Los resultados completos estan en [research.md](research.md). Las decisiones que bloquean la implementacion son:

1. No se incorpora una herramienta externa de migraciones; se conserva `spring.jpa.hibernate.ddl-auto=update`.
2. Las relaciones de las siete entidades se modelan con referencias JPA explicitas y carga controlada.
3. El modelo conserva invariantes; cada service implementa su propia interfaz y resuelve existencia y paginacion dentro de su caso de uso, sin heredar de una clase base generica.
4. Los repositorios exponen `Page<T>` y `Pageable` para consultas multiples.
5. JUnit 5, AssertJ y Mockito son la unica estrategia de pruebas del backend.

## Phase 1: Diseno y contratos

### Plan 3.1 - Definicion del modelo de datos y persistencia

1. **Dependencias y configuracion**
   - Mantener `backend/build.gradle` sin dependencias adicionales de migraciones, conservando Java 21, JPA, Lombok y las dependencias existentes.
   - Actualizar unicamente `backend/src/main/resources/application.properties` para conservar `spring.jpa.hibernate.ddl-auto=update`; no crear `application-local.properties` ni perfiles.
   - Mantener la URL, usuario y contrasena de PostgreSQL en el archivo unico, documentando los valores esperados sin duplicarlos en otros archivos.

2. **Gestion del esquema**
   - Verificar que `spring.jpa.hibernate.ddl-auto=update` cree o actualice tablas, claves primarias, foraneas, restricciones de nulabilidad, unicidad, precision monetaria e indices de las siete entidades.
   - Validar el arranque contra PostgreSQL y documentar que los cambios de esquema no tienen historial versionado en esta etapa.
   - Dejar la evaluacion de una herramienta de versionado de esquema para una feature futura si aparecen necesidades de rollback, auditoria o despliegue controlado.

3. **Entidades en `model`**
   - Reemplazar o ampliar `EntidadDominio` solo si su contrato actual no permite entidades JPA concretas; no introducir herencia innecesaria.
   - Crear `Usuario`, `Liga`, `Equipo`, `Jugador`, `CotizacionJugador`, `PosicionPortfolio` y `TransaccionAuditoria` con `@Entity`, `@Table`, identificador generado, constructor sin argumentos para JPA, constructor completo/builder cuando corresponda y anotaciones Lombok controladas.
   - Definir relaciones JPA y nombres de columnas de forma explicita; evitar igualdad, hash y `toString` recursivos sobre asociaciones.
   - Implementar en cada entidad sus metodos de dominio e invariantes: saldo insuficiente, tokens insuficientes, valores positivos, jugador activo y auditoria inmutable. Las excepciones se ubican en `exceptions`.
   - Mantener todos los nombres de codigo en ASCII y espanol tecnico: `contrasena`, `fechaCreacion`, `cantidadTokens`, `estadoAnterior`.

4. **Repositorios**
   - Crear los contratos `RepositorioUsuario`, `RepositorioLiga`, `RepositorioEquipo`, `RepositorioJugador`, `RepositorioCotizacionJugador`, `RepositorioPosicionPortfolio` y `RepositorioTransaccionAuditoria` en `persistence/repository/interfaces`.
   - Definir operaciones de consulta multiple con `Page<T>` y `Pageable`; ninguna interfaz debe retornar listas para listados ordinarios.
   - Crear implementaciones o adaptadores en `persistence/repository/impl` que reciban sus DAOs por constructor; ubicar los DAOs Spring Data en `persistence/sql`, aprovechar consultas derivadas cuando sea posible y evitar logica de negocio alli.
   - Mantener el acceso a repositorios desde service, nunca desde controller.

### Plan 3.2 - Testing unitario de modelos

1. **Entorno de pruebas**
   - Confirmar en `backend/build.gradle` JUnit 5 mediante JUnit Platform, AssertJ y Mockito; declarar dependencias explicitamente si los starters actuales no las exponen de forma estable.
   - Conservar los tests existentes y agregar pruebas bajo `backend/src/test/java/unq/losrecursionistas/backend/model/` y `.../service/` sin usar Jest, Vitest ni Mocha.

2. **Pruebas de entidades**
   - Cubrir construccion valida, restricciones de campos obligatorios y valores positivos en `Usuario`, `Liga`, `Equipo`, `Jugador` y `CotizacionJugador`.
   - Cubrir debito con saldo insuficiente, compra/venta con tokens insuficientes, jugador inactivo y cambios de cotizacion en `PosicionPortfolio`.
   - Cubrir la inmutabilidad y presencia obligatoria de autor, detalle, timestamp y estados en `TransaccionAuditoria`.
   - Usar AssertJ para describir resultados y excepciones; no probar reglas de dominio mediante un contexto Spring completo.

3. **Pruebas de servicios y paginacion**
   - Agregar o ajustar pruebas en `.../service/` con Mockito para repositorios y servicios concretos.
   - Verificar que cada service construye `PageRequest.of(page, 12)` al inicio, propaga el `Page<T>` y resuelve entidades antes de tocar el modelo.
   - Verificar fail-fast cuando falta un ID y que no se invoca el repositorio posterior ni se muta una entidad invalida.

4. **Validacion de nombres y configuracion**
   - Agregar una comprobacion automatizada o tarea de CI que detecte archivos `application*.properties` duplicados, identificadores no ASCII en codigo nuevo y frameworks de pruebas no permitidos.
   - Ejecutar el flujo descrito en [quickstart.md](quickstart.md) y actualizar el workflow CI para ejecutar `./gradlew clean test` desde `backend`.

## Orden de implementacion

1. Dependencias Gradle y configuracion unica.
2. Gestion JPA/Hibernate y validacion de esquema.
3. Entidades y excepciones de dominio.
4. Contratos e implementaciones de repositorios.
5. Pruebas unitarias de modelo.
6. Pruebas de services y paginacion.
7. Validaciones de arquitectura, nombres y CI.

Las pruebas de modelo pueden desarrollarse en paralelo una vez definidos los contratos de las entidades. Las pruebas de service dependen de los contratos de repositorio y de los metodos publicos de las entidades.

## Constitution Check post-diseno

- **Principios I-II**: PASS. Las rutas y responsabilidades del modelo, persistencia y services coinciden con la constitucion.
- **Principios III-IV**: PASS. Los services no retornan DTOs; los listados retornan `Page<T>` y la paginacion se construye con tamano 12.
- **Principios V-VI**: PASS. Las excepciones viven en `exceptions` y cada capa valida solo lo que conoce.
- **Principio VII**: PASS. Se conservan tests existentes y se cubren metodos publicos, casos felices, saldo insuficiente y falta de tokens.
- **Principio VIII**: PASS. No se implementan proveedores externos.
- **Principio IX**: PASS. Las decisiones ambiguas estan justificadas en `research.md` con alternativas.
- **Principio X**: PASS. Codigo ASCII y documentacion en espanol tecnico.
- **Configuracion, observabilidad, auditoria y seguridad**: PASS para el alcance. La auditoria queda representada por `TransaccionAuditoria`; no se altera la seguridad existente ni se crean perfiles de configuracion.

No se detectan violaciones constitucionales pendientes.

## Complexity Tracking

No aplica: no se agregan proyectos ni desviaciones arquitectonicas.
