# Investigacion y decisiones

## Estado del repositorio

- El backend usa Java 21, Spring Boot 4.1.1, Gradle, Spring Data JPA,
  PostgreSQL y H2. No hay Playwright, Flyway, Liquibase, Testcontainers ni
  migraciones SQL versionadas.
- La persistencia separa contratos en `persistence/repository/interfaces`,
  implementaciones en `persistence/repository/impl` y DAOs Spring Data en
  `persistence/sql`.
- La configuracion actual esta en `application.properties` y usa
  `spring.jpa.hibernate.ddl-auto=update`.

## Extraccion de WhoScored

**Decision:** interceptar respuestas JSON de red; completar con JSON embebido;
usar DOM solo como respaldo. Centralizar patrones de URL, claves y selectores
en el adapter.

**Rationale:** las respuestas estructuradas conservan mejor IDs de jugador,
partido y metricas de una pagina dinamica y son menos fragiles que las clases
CSS. El JSON embebido permite cubrir datos que no se solicitan por red. El DOM
queda acotado a un fallback observable cuando ambas fuentes fallan.

**Alternativas consideradas:** DOM como fuente primaria (descartado por
fragilidad); depender de endpoints internos como API estable (descartado porque
WhoScored no ofrece un contrato estable y puede cambiar o bloquearlo); un
cliente HTTP fuera de Playwright (descartado porque no reproduce la pagina
dinamica ni cumple la constitucion).

**Decision operativa:** `Browser` administrado por Spring, contexto y pagina
cerrados por operacion controlada, concurrencia 1, rate limit, pausa entre
requests, reintentos acotados y backoff desde propiedades. Toda excepcion de
Playwright, red, timeout, parsing o anti-bot se envuelve en
`ScrapingException`. No se intentan evadir protecciones anti-bot.

## Descubrimiento, normalizacion y matching

**Decision:** descubrir equipos desde la pagina de liga o equipo, recorrer el
plantel visible/estructurado y obtener de la cabecera del jugador `playerId`,
nombre, fecha de nacimiento, nacionalidad, equipo, posiciones y slug/URL.
Conservar el `playerId` numerico aunque el slug este percent-encoded.

El matching se ejecuta fuera del adapter y en este orden:

1. `Jugador.idWhoscored` igual al identificador descubierto.
2. fecha de nacimiento exacta y nombre normalizado.
3. desempate por equipo mapeado y nacionalidad.
4. cero candidatos produce no encontrado; mas de un candidato despues del
   desempate produce ambiguedad y nunca persiste estadisticas.

La normalizacion vive en una clase testeable: decodifica URL, aplica Unicode
NFD, elimina diacriticos, convierte a minusculas, recorta y colapsa espacios.
El mapeo de equipo y liga se persiste con `idWhoscored` nullable y unico en
`Equipo` y `Liga`; nunca reutiliza `idExterno`.

**Alternativas consideradas:** comparar solo nombre (descartado por homonimos);
usar `idExterno` como ID WhoScored (descartado por mezclar fuentes); crear
jugadores no encontrados (descartado por el alcance y la integridad).

## Temporada, participaciones y torneos

**Decision:** la temporada activa se configura con `inicio` y `fin`. El adapter
devuelve fecha y torneo informado, y el servicio descarta fechas fuera del
intervalo. Se conservan todos los torneos de la temporada.

Solo se persisten participaciones informadas por WhoScored con `minutos > 0`.
Se descartan banco, cero minutos y ausencia de participacion antes de
persistir. Los valores faltantes de otras metricas siguen siendo `null`, nunca
ceros inventados. Esta decision explicita del pedido actual prevalece sobre la
aclaracion anterior del borrador que permitia persistir cero minutos.

## Persistencia e idempotencia

**Decision:** agregar `idWhoscored` separado a `Jugador`, `Equipo` y `Liga`, y
crear `EstadisticaPartido`, `CasoVinculacion` y `EjecucionIngesta`. La
estadistica usa una constraint unica `(jugador_id, partido_whoscored_id)`,
indices por jugador-fecha y torneo, y JSONB para metricas adicionales.

El repositorio debe leer por la clave compuesta y actualizar la entidad dentro
de la transaccion del jugador; la constraint de PostgreSQL es la garantia final
contra duplicados. `save()` aislado sin consulta por clave fue descartado porque
no expresa el upsert y puede duplicar ante concurrencia.

**Alternativas consideradas:** EAV para todas las metricas (descartado porque
debilita tipos y consultas); persistir todo como JSON (descartado porque rompe
los agregados futuros); agregar Flyway ahora (descartado porque el repositorio
no lo usa y la decision vigente mantiene `ddl-auto=update`, aunque queda como
deuda tecnica).

## Ejecuciones, errores y concurrencia

**Decision:** `EjecucionIngesta` registra alcance, estado, timestamps, contadores
FR-015, motivo de rechazo y errores/anomalias resumidos. Cada jugador se procesa
en una transaccion independiente; un fallo hace rollback de ese jugador y el
orquestador continua.

Una segunda ejecucion se rechaza y registra el intento. El servicio usara una
verificacion transaccional y una restriccion/estrategia PostgreSQL para evitar
dos estados activos; si `ddl-auto=update` no puede crear una constraint parcial,
la serializacion se implementara con bloqueo transaccional sobre una fila de
ejecucion activa y se documentara esa limitacion.

## Configuracion

**Decision:** migrar el contenido actual a la unica `application.yml` exigida
por la constitucion y agregar `whoscored.scraping` con headless, timeouts,
user-agent, pausas, rate limit, reintentos, backoff, temporada activa y
allowlist de ligas. No se crean perfiles.

## Testing

**Decision:** unitarios con fixtures locales para parser, adapter y matching;
Mockito para servicios; Testcontainers PostgreSQL para constraint, indices y
upsert real, agregado como dependencia de test y excluido si el entorno no lo
habilita; prueba contra WhoScored real con tag `real-web` y exclusion por
defecto.

**Alternativa descartada:** H2 solamente, porque no verifica JSONB, tipos,
constraints e indices PostgreSQL de la feature.

## Riesgos y mitigaciones

- Anti-bot, terminos de uso y cambios de estructura: pausas conservadoras,
  user-agent configurable, errores controlados, fixtures versionados y
  documentacion de que la ingesta puede quedar impedida.
- `ddl-auto=update` no tiene historial ni rollback: documentar la deuda y
  verificar el esquema con Testcontainers; migraciones versionadas quedan para
  una decision posterior.
- IDs/slugs codificados: extraer el ID numerico de datos estructurados/URL
  decodificada y testear `%C3%A9`.
- Metricas faltantes o nuevas: columnas nullable, `statsCrudas` JSONB y
  anomalias explicitas, nunca defaults silenciosos.
