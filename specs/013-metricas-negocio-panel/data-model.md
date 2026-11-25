# Data Model: Métricas de negocio y panel de observabilidad

**Feature**: [spec.md](spec.md) | **Fecha**: 2026-11-24

**No hay tablas, columnas ni migraciones nuevas.** Todo lo de abajo son read-models o estructuras en
memoria.

## Read-models en `model` (records inmutables, sin JPA)

### ActividadDiariaMercado

| Campo | Tipo | Regla |
|-------|------|-------|
| `dia` | `LocalDate` | Día en `America/Argentina/Buenos_Aires` |
| `ordenesCompra` | `long` | ≥ 0; excluye `es_liquidez_inicial = true` |
| `ordenesVenta` | `long` | ≥ 0; excluye `es_liquidez_inicial = true` |
| `emparejamientos` | `long` | ≥ 0; cantidad de filas de `transacciones_mercado` del día |
| `volumenCreditos` | `BigDecimal` | ≥ 0; `SUM(total)` del día, escala 2 |

### ActividadDiariaCotizacion

| Campo | Tipo | Regla |
|-------|------|-------|
| `dia` | `LocalDate` | Día de `timestamp_calculo` (hora local AR) |
| `jugadoresCotizados` | `long` | ≥ 0; `COUNT(DISTINCT jugador_id)` del día |
| `variacionPromedio` | `BigDecimal` o `null` | Promedio de `(valor − previo) / previo` como fracción (0.018 = +1,8 %). Es `null` si ninguna cotización del día tiene previo > 0. |
| `conPrevio` | `long` | Cotizaciones del día con previo > 0. Pondera la variación del período y no se expone en REST (research R9). |

Las dos series se completan en el service: un elemento por cada día de `[hoy − (dias − 1), hoy]`, con 0
(o `null` en el caso de la variación) para los días sin datos.

## Métricas Micrometer (en memoria, se reinician con el backend)

El detalle está en [contracts/metricas-negocio.md](contracts/metricas-negocio.md).

| Meter | Tipo | Tags | Origen |
|-------|------|------|--------|
| `mercado.ordenes.creadas` | Counter | `tipo` = `compra`\|`venta` | Después del commit de `crearOrden` |
| `mercado.emparejamientos` | Counter | — | Después del commit, por transacción |
| `mercado.volumen.creditos` | Counter | — | Después del commit, suma `total` |
| `mercado.ordenes.rechazadas` | Counter | `motivo` = `saldo_insuficiente` | En el momento |
| `mercado.tokens.circulacion` | Gauge | — | Consulta a la base en cada lectura |
| `cotizacion.recalculos` | Counter | `resultado` = `exitoso`\|`fallido` | Al terminar `recalcular` |
| `cotizacion.recalculo.duracion` | Timer (sin histograma) | `resultado` | Al terminar `recalcular` |
| `cotizacion.jugadores.cotizados` | Gauge | — | Último recálculo exitoso |
| `cotizacion.variacion.promedio` | Gauge | — | Último recálculo exitoso; `NaN` si no hay datos |

La estrategia activa no es una métrica (hallazgo K4): el panel la obtiene de
`EstrategiaAdminService`.

Los records `ActividadDiaria*` no llevan test de modelo, porque solo tienen atributos y un
factory `vacia()` sin lógica de negocio (Principio VII).

**Transiciones de estado**:
- Los contadores solo crecen hasta que el backend se reinicia; ahí vuelven a 0.
- Los gauges `jugadores.cotizados` y `variacion.promedio` conservan el valor del último recálculo exitoso.
  Si después hay un recálculo fallido, no se modifican.

## Respuestas del panel

Son DTOs de `controller/dto/observabilidad` construidos desde los records de `ObservabilidadService`. Los
campos están en [contracts/observabilidad-admin.md](contracts/observabilidad-admin.md).

- **ResumenNegocio**:
  - período (`dias`, `desde`, `hasta`);
  - bloque `enVivo`, con métricas Micrometer y fecha de inicio;
  - bloque `actual`, con tokens en circulación y estrategia activa;
  - bloque `periodo`, con los totales de las series;
  - las series `serieMercado` y `serieCotizacion`.
- **ResumenTecnico**: `salud` (estado global y componentes, sin detalles), `requests` (totales, tasas y
  top 10 de endpoints), `jvm` y `poolConexiones`.

**Validaciones**:
- `dias` ∈ {7, 30, 90}, validado en el controller; cualquier otro valor responde 400.
- Los repositorios rechazan rangos de más de 90 días (`IllegalArgumentException`), para que el límite del
  Principio IV sea estructural.
- `periodo.cotizacionesCalculadas` es la suma de cotizaciones del período, no jugadores distintos.
- Las tasas de error van de 0 a 1. Son `null` si no hubo requests.

## Estado del frontend (en memoria)

- **`periodo`**: 7, 30 o 90; por defecto 30.
- **`negocio` y `tecnico`**: la última respuesta, con su propio `error` y `cargando`.
- **`muestrasTecnicas`**: buffer circular de 60 muestras `{instante, heapUsado, cpuProceso, cpuSistema,
  conexionesActivas, conexionesPendientes}`. Se vacía al recargar la página.
