# Contrato REST — Consultar última ejecución WhoScored

**Método:** `GET`  
**Ruta:** `/api/admin/whoscored/ingestas/ultima`  
**Autorización:** JWT con `ROLE_ADMIN`

## Response con ejecución

```json
{
  "disponible": true,
  "ejecucion": {
    "id": 42,
    "estado": "FINALIZADA",
    "alcance": "COMPLETA",
    "idAlcance": null,
    "inicio": "2026-10-19T02:00:00-03:00",
    "fin": "2026-10-19T02:18:12-03:00",
    "temporadaInicio": "2025-08-01",
    "temporadaFin": "2026-06-30",
    "contadores": {
      "jugadoresProcesados": 100,
      "jugadoresVinculados": 95,
      "jugadoresNoVinculados": 5,
      "partidosInsertados": 1200,
      "partidosActualizados": 30
    },
    "errores": 1,
    "anomalias": 5,
    "motivoRechazo": null,
    "detalle": []
  }
}
```

El controller mapea la entidad a este DTO; el servicio no conoce el protocolo
HTTP.

## Response sin ejecuciones

```json
{
  "disponible": false,
  "ejecucion": null
}
```

No se inventan estado, fechas ni contadores.

## Errores

- `401/403`: JWT ausente, inválido o sin `ROLE_ADMIN`.
- `500`: error de persistencia comunicado por el manejador central, sin
  presentar una respuesta exitosa ficticia.
