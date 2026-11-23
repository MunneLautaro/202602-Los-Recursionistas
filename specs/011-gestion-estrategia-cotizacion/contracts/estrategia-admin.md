# Contrato REST de estrategia administrativa

## Consulta

`GET /api/admin/cotizacion/estrategia`

Autorizacion: usuario autenticado con `ROLE_ADMIN`.

Respuesta `200`:

```json
{
  "estrategiaActiva": "PERFORMANCE_GENERAL",
  "nombreActivo": "Rendimiento general",
  "descripcionActiva": "Calcula segun pesos generales para todas las métricas.",
  "estrategias": [
    {
      "codigo": "PERFORMANCE_GENERAL",
      "nombre": "Rendimiento general",
      "descripcion": "Calcula segun pesos generales para todas las métricas."
    },
    {
      "codigo": "PERFORMANCE_POR_POSICION",
      "nombre": "Rendimiento por posicion",
      "descripcion": "Calcula segun pesos específicos de la posicion; usa los pesos generales si la posicion es desconocida."
    }
  ],
  "origen": "CONFIGURACION",
  "ultimoCambio": null
}
```

Cuando existe ultimo cambio, `ultimoCambio` contiene:

```json
{
  "estrategiaAnterior": "PERFORMANCE_GENERAL",
  "estrategiaNueva": "PERFORMANCE_POR_POSICION",
  "usuarioAdmin": "admin",
  "timestampCambio": "2026-11-22T19:45:42-03:00"
}
```

## Cambio

`PUT /api/admin/cotizacion/estrategia`

Request:

```json
{
  "estrategia": "PERFORMANCE_POR_POSICION"
}
```

Validaciones:
- `estrategia` no puede ser null ni vacia.
- Solo se admiten los dos codigos conocidos.
- La operacion es idempotente si el codigo coincide con el activo efectivo.

Respuesta `200` para cambio efectivo o idempotente:

```json
{
  "mensaje": "Estrategia activa cambiada correctamente.",
  "estrategiaActiva": "PERFORMANCE_POR_POSICION",
  "nombreActivo": "Rendimiento por posicion",
  "cambioAplicado": true,
  "ultimoCambio": {
    "estrategiaAnterior": "PERFORMANCE_GENERAL",
    "estrategiaNueva": "PERFORMANCE_POR_POSICION",
    "usuarioAdmin": "admin",
    "timestampCambio": "2026-11-22T19:45:42-03:00"
  }
}
```

Para idempotencia, `cambioAplicado=false`, `ultimoCambio` conserva el ultimo cambio anterior o null.

Errores:
- `400`: estrategia vacia o desconocida.
- `401`: no autenticado.
- `403`: autenticado sin `ROLE_ADMIN`.
- `409`: recálculo en curso.
- `500` o respuesta estándar existente ante fallo de persistencia.

