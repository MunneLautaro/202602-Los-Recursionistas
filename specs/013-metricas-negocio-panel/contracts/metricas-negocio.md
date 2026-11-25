# Contrato: puerto `MetricasNegocio` y series Prometheus

**Feature**: [../spec.md](../spec.md)

## Puerto (`service/interfaces/MetricasNegocio`)

| Operación | Llamado desde | Momento del registro |
|-----------|---------------|----------------------|
| `registrarOrdenCreada(TipoOrden tipo)` | `MercadoServiceImpl.crearOrden` | Después del commit (o en el momento si no hay transacción activa) |
| `registrarRechazoSaldoInsuficiente()` | `MercadoServiceImpl.crearOrden`, rama `COMPRA` | En el momento |
| `registrarEmparejamiento(BigDecimal total)` | `MatchingEngineImpl.ejecutar`, por cada transacción | Después del commit |
| `registrarRecalculo(ResultadoRecalculo resultado)` | `CotizacionServiceImpl.recalcular(List, …)` | En el momento |

`ResultadoRecalculo` es un record anidado con estos campos: `boolean exitoso`, `Duration duracion`,
`int jugadoresCotizados` y `BigDecimal variacionPromedio`, que puede ser `null`.

`MetricasNegocio.SIN_REGISTRO` es una implementación no-op constante que usan los constructores legados
(research R7).

Las lecturas para el panel (`consultarEnVivo()`) devuelven un record con los valores acumulados desde el
inicio de la aplicación y la fecha de inicio.

## Series en `/actuator/prometheus`

Todas llevan `application="<spring.application.name>"`. Ninguna serie tiene buckets ni cuantiles.

```text
mercado_ordenes_creadas_total{application="backend",tipo="compra"} 12
mercado_ordenes_creadas_total{application="backend",tipo="venta"} 9
mercado_emparejamientos_total{application="backend"} 7
mercado_volumen_creditos_total{application="backend"} 10450.0
mercado_ordenes_rechazadas_total{application="backend",motivo="saldo_insuficiente"} 3
mercado_tokens_circulacion{application="backend"} 15230.0
cotizacion_recalculos_total{application="backend",resultado="exitoso"} 1
cotizacion_recalculos_total{application="backend",resultado="fallido"} 0
cotizacion_recalculo_duracion_seconds_count{application="backend",resultado="exitoso"} 1
cotizacion_recalculo_duracion_seconds_sum{application="backend",resultado="exitoso"} 1.834
cotizacion_recalculo_duracion_seconds_max{application="backend",resultado="exitoso"} 1.834
cotizacion_jugadores_cotizados{application="backend"} 2412.0
cotizacion_variacion_promedio{application="backend"} 0.0183
```

**Garantías**:
- Los tags solo toman valores de catálogos fijos (`tipo`, `motivo`, `resultado`).
- La estrategia activa **no** se exporta como métrica, porque puede venir de una
  propiedad de configuración y la constitución prohíbe exponerlas (hallazgo K4).
  Se muestra solo en el panel, mediante `/api/admin/observabilidad/negocio`.
- No hay series por usuario ni por jugador (FR-014).
