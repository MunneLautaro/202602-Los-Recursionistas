# Modelo de datos

## Cambios en entidades existentes

### `Jugador` -> `jugadores`

Agregar `idWhoscored` (`Long`, nullable, unico, columna `id_whoscored`).
`idExterno` permanece exclusivamente como identificador de Football-Data.org.
El servicio de matching puede actualizar `idWhoscored` y la posicion informada
por WhoScored, pero nunca crea jugadores ni modifica `idExterno`.

### `Equipo` -> `equipos`

Agregar `idWhoscored` (`Long`, nullable, unico, `id_whoscored`). El nombre del
equipo de la fuente se conserva en el DTO y se resuelve mediante este campo,
sin reemplazar `idExterno`.

### `Liga` -> `ligas`

Agregar `idWhoscored` (`Long`, nullable, unico, `id_whoscored`). El mapeo se
mantiene separado del codigo e `idExterno`.

## Entidades nuevas

### `EstadisticaPartido` -> `estadisticas_partido`

| Campo | Tipo | Regla |
|---|---|---|
| `id` | `Long` | PK autogenerada |
| `jugador` | `Jugador` | FK `jugador_id`, obligatoria |
| `partidoWhoscoredId` | `Long` | obligatorio |
| `fechaPartido` | `LocalDate` | obligatorio, dentro de temporada |
| `torneo` | `String` | obligatorio cuando WhoScored lo informa |
| `torneoWhoscoredId` | `Long` | nullable |
| `temporadaInicio` / `temporadaFin` | `LocalDate` | intervalo aplicado |
| `equipoWhoscoredId` / `equipoNombre` | `Long` / `String` | contexto de fuente |
| `rivalWhoscoredId` / `rivalNombre` | `Long` / `String` | contexto de fuente |
| `localVisitante` | `String` | `LOCAL`, `VISITANTE` o nullable |
| `minutos` | `Integer` | obligatorio y mayor que cero |
| `rating` | `BigDecimal` | nullable |
| `goles`, `asistencias`, `tiros`, `tirosAlArco`, `pases`, `pasesClave`, `regates`, `tackles`, `intercepciones`, `faltasCometidas`, `amarillas`, `rojas` | `Integer` | nullable |
| `precisionPases` | `BigDecimal` | nullable |
| `statsCrudas` | `JSONB` | resto de metricas de Summary, Defensive, Offensive y Passing |

Constraint unica `(jugador_id, partido_whoscored_id)`. Indices sobre
`jugador_id, fecha_partido`, `fecha_partido` y `torneo`. Las metricas
principales tipadas permiten agregados futuros; `statsCrudas` conserva
informacion que no tenga columna.

### `CasoVinculacion` -> `casos_vinculacion`

Registra `id`, `idWhoscored`, nombre, slug/URL, fecha de nacimiento,
nacionalidad, equipo de fuente, motivo (`NO_ENCONTRADO`, `AMBIGUO` u otro),
estado, candidatos internos serializados en JSONB, ejecucion asociada,
timestamps y detalle operativo. No tiene relacion que cree jugadores y no
habilita persistir estadisticas.

### `EjecucionIngesta` -> `ejecuciones_ingesta`

Registra `id`, estado (`ACTIVA`, `FINALIZADA`, `FALLIDA`, `RECHAZADA`),
alcance (`COMPLETA`, `LIGA`, `EQUIPO`, `JUGADOR`), identificadores de alcance,
temporada aplicada, inicio/fin y motivo de rechazo. Incluye contadores de
jugadores procesados/vinculados/no vinculados, partidos insertados/actualizados,
errores y anomalias, mas un detalle JSONB auditable sin credenciales.

## Relaciones y reglas transaccionales

- `EstadisticaPartido.jugador` es `ManyToOne` a `Jugador` por `jugador_id`;
  las consultas futuras por rango usan esa FK y `fecha_partido`.
- `CasoVinculacion` y `EjecucionIngesta` se relacionan por FK opcional/obligatoria
  segun el resultado de ejecucion, sin cascadas que creen identidades.
- Cada jugador se procesa en una transaccion propia. El fallo revierte sus
  cambios y actualiza el resumen de ejecucion en una transaccion separada.
- El upsert consulta `(jugador_id, partido_whoscored_id)`, aplica los nuevos
  valores y deja que la constraint unica de PostgreSQL proteja la idempotencia.
- La temporada activa se configura, no se persiste como entidad en esta entrega;
  sus fechas quedan copiadas en cada estadistica y ejecucion para auditoria.
