# Contrato REST — Iniciar ingesta WhoScored

**Método:** `POST`  
**Ruta:** `/api/admin/whoscored/ingestas`  
**Autorización:** JWT con `ROLE_ADMIN`  
**Éxito:** `202 Accepted`

## Request

```json
{
  "alcance": "COMPLETA",
  "id": null
}
```

`alcance` es obligatorio y solo admite `COMPLETA`, `LIGA`, `EQUIPO` o
`JUGADOR`. `id` debe ser `null` para `COMPLETA` y positivo para los otros
alcances. El service valida existencia de liga, equipo o jugador antes de
reservar la ejecución.

## Response `202`

```json
{
  "ejecucionId": 42,
  "estado": "ACTIVA",
  "alcance": "COMPLETA"
}
```

La respuesta no espera la finalización y debe emitirse en menos de dos
segundos. La reserva ACTIVA ocurre antes de encolar el trabajo.

## Errores

- `400`: request ausente, enum inválido, id ausente para alcance acotado o id
  presente para `COMPLETA`.
- `404`: el id de alcance no existe.
- `401/403`: JWT ausente, inválido o sin `ROLE_ADMIN`.
- Un intento concurrente válido se registra como `RECHAZADA` y devuelve la
  referencia de esa ejecución rechazada, sin iniciar otra carga.
