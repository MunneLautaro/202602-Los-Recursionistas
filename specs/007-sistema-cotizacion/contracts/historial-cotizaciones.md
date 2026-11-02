# Contrato: historial de cotizaciones

## `GET /players/{id}/quotes`

Devuelve `Page<CotizacionResponse>` ordenado por
`timestampCalculo DESC, id DESC`.

Parámetros: `id` positivo obligatorio; `desde` y `hasta` opcionales,
`LocalDate` inclusivos; `page` default `0`, no negativo; `size` default `12`,
positivo y sujeto al límite del controller.

`desde > hasta` devuelve 400. Jugador inexistente devuelve 404. Rango sin
resultados devuelve página vacía válida.

```json
{
  "id": 10,
  "jugadorId": 42,
  "valor": 735.25,
  "timestampCalculo": "2026-10-21T04:00:00",
  "estrategiaUsada": "PERFORMANCE_GENERAL",
  "ventanaInicio": "2026-10-14",
  "ventanaFin": "2026-10-21"
}
```

La página conserva `content`, `number`, `size`, `totalElements` y
`totalPages`. Errores usan `RespuestaError`.
