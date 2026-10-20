# Modelo de datos — Automatización de ingesta WhoScored

## Entidades existentes a extender

### EjecuciónIngesta

Tabla: `ejecuciones_ingesta`.

| Campo | Regla |
|---|---|
| `id` | Identificador generado y referencia devuelta por REST |
| `estado` | `ACTIVA`, `FINALIZADA`, `FALLIDA` o `RECHAZADA` |
| `alcance` | `COMPLETA`, `LIGA`, `EQUIPO` o `JUGADOR` |
| `idAlcance` | Nullable solo para `COMPLETA`; positivo y existente para los demás |
| `inicio`, `fin` | Fechas de reserva y finalización |
| `temporadaInicio`, `temporadaFin` | Rango vigente aplicado a estadísticas |
| contadores | Procesados, vinculados, no vinculados, insertados, actualizados |
| `errores`, `anomalias` | Cantidades acumuladas, incluyendo errores parciales |
| `detalle` | JSONB con errores por alcance, motivos de matching y anomalías |
| `motivoRechazo` | Obligatorio para `RECHAZADA` |

La implementación debe adaptar los campos `ligaId`, `equipoId` y `jugadorId`
existentes o introducir una representación equivalente, sin duplicar el
concepto de alcance. La reserva ACTIVA debe ser atómica.

### Liga, Equipo y Jugador

Las entidades existentes conservan sus ids de Football Data (`idExterno`) y
WhoScored (`idWhoscored`) separados. La vinculación solo actualiza
`idWhoscored`, respeta unicidad y nunca crea jugadores.

### Auditoría del matching

No se crea una entidad ni tabla `CasoVinculacion`. El resultado de matching se
registra en `EjecucionIngesta.detalle` y en los contadores/anomalías de la
ejecución, incluyendo entidad, candidato, resultado, motivo y ambigüedad. Un
vínculo ambiguo o sin coincidencia no actualiza la entidad.

### EstadisticaPartido

El upsert se identifica por jugador y partido WhoScored. Solo se persisten
jugadores activos con id WhoScored y apariciones dentro del rango vigente.
Las repeticiones actualizan el registro existente y no generan duplicados.

## Flujo de estados

```text
solicitud válida -> reserva atómica -> ACTIVA
solicitud inválida -> sin ejecución
reserva ocupada -> RECHAZADA
ACTIVA + fases completas sin error fatal -> FINALIZADA
ACTIVA + error no recuperable del flujo -> FALLIDA
ACTIVA + errores parciales -> continúa, registra detalle y finaliza o falla
```

No se define transición automática para una ACTIVA huérfana tras caída del
proceso; queda fuera de esta feature.
