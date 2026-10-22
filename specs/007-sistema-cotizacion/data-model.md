# Modelo de datos: sistema de cotización

## `Cotizacion`

Tabla `cotizaciones`, histórica y append-only; no se actualiza ni se versiona.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` / BIGINT | Identidad generada, desempate descendente |
| `jugador` | `@ManyToOne` / FK | Obligatorio, `LAZY`, jugador existente |
| `valor` | `BigDecimal` / NUMERIC | Obligatorio, escala 2 |
| `timestampCalculo` | `LocalDateTime` | Obligatorio |
| `estrategiaUsada` | `String` | Obligatorio e inmutable |
| `ventanaInicio` | `LocalDate` | Obligatorio, inclusivo |
| `ventanaFin` | `LocalDate` | Obligatorio, inclusivo y no anterior |

Índices JPA requeridos:

- `idx_cotizacion_jugador_timestamp` sobre `(jugador_id,
  timestamp_calculo DESC)`.
- `idx_cotizacion_timestamp` sobre `(timestamp_calculo DESC)`.

No se agrega unicidad por timestamp: dos cotizaciones pueden compartirlo y se
ordenan por `id DESC`.

## Resumen de rendimiento

Objeto interno, no entidad ni DTO. Agrupa las estadísticas de la ventana por
jugador, suma métricas enteras y acumula `rating` según la regla definida por
el servicio. Todo valor nulo se trata como cero. El servicio calcula máximos
por métrica antes de delegar; máximo cero produce normalización cero.

## Relaciones y filtros

`Cotizacion.jugador` apunta a `Jugador`; el ranking navega
`Jugador -> Equipo -> Liga` sin modificar esas entidades. Solo se calculan
jugadores activos con `idWhoscored` y al menos una estadística en la ventana.
El ranking excluye jugadores sin cotización y filtra opcionalmente por `ligaId`
y posición normalizada (español/inglés, sin acentos ni separadores).

## DTOs

`CotizacionResponse`: `id`, `jugadorId`, `valor`, `timestampCalculo`,
`estrategiaUsada`, `ventanaInicio`, `ventanaFin`.

`RankingJugadorResponse`: `jugadorId`, `nombre`, `posicion`, `equipo`, `liga`,
`valorActual`, `valorAnterior`, `variacion`. Se construye desde una proyección
del DAO para no acceder a relaciones lazy fuera de transacción.

## Recálculo

No se persiste entidad de ejecución:

1. `AtomicBoolean.compareAndSet(false, true)` acepta una ejecución.
2. Se fija la ventana y valida la estrategia.
3. El worker procesa elegibles, omite y registra errores individuales.
4. `finally` libera el booleano.

Si ya está activo, el endpoint devuelve `409 Conflict`.
