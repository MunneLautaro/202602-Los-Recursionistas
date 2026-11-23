# Contratos modificados de cotizacion

## Historial

`GET /players/{id}/quotes`

`CotizacionResponse.estrategiaUsada` se construye en el controller con el nombre legible:

- `PERFORMANCE_GENERAL` -> `Rendimiento general`
- `PERFORMANCE_POR_POSICION` -> `Rendimiento por posicion`
- Codigo desconocido -> mantener un nombre seguro de fallback sin exponer el codigo interno.

El service sigue retornando `Page<Cotizacion>` y el codigo histórico no se altera.

## Serie

`GET /players/{id}/quotes/series`

`PuntoCotizacionResponse.estrategiaUsada` se construye en el controller con el mismo lookup centralizado usado por historial y detalle.

## Detalle

No hay endpoint especifico de detalle de cotizacion separado. El detalle mostrado por `JugadorDetailModal` consume las filas paginadas de `/players/{id}/quotes`, por lo que el nombre legible llega a tabla, grafico y detalle sin cambios historicos ni exposición del codigo.
