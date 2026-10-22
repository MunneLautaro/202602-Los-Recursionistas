# Contrato: recálculo de cotizaciones

## `POST /quotes/recalculate`

Es público y no recibe cuerpo para facilitar pruebas locales. Reserva el job, fija la
ventana al aceptarlo y ejecuta asíncronamente en el executor dedicado, sin
propagar `SecurityContext`.

`202 Accepted` se devuelve inmediatamente con un mensaje de aceptación. No se
expone una entidad de ejecución persistente.

Errores:

- `409 Conflict`: otro recálculo activo; no se encola un segundo.
- `500 Internal Server Error`: configuración inválida u otro error operativo.

Las respuestas deben documentarse con `@Operation` y `@ApiResponse` y respetar
los handlers/formato de error existentes.
