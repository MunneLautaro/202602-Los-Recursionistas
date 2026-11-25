# Contrato REST: `/api/admin/observabilidad`

**Feature**: [../spec.md](../spec.md)

**Autenticación**: JWT existente (`Authorization: Bearer <token>`).

**Acceso**: solo `ROLE_ADMIN`. Sin token responde **401**, con `ROLE_USUARIO` responde **403**. En ambos
casos el cuerpo es `RespuestaError(codigo, mensaje, detalle, timestamp)`.

Las cantidades monetarias van como número con 2 decimales. Las fracciones van entre 0 y 1
(0.018 = 1,8 %). Las fechas siguen ISO-8601 en `America/Argentina/Buenos_Aires`.

---

## GET `/api/admin/observabilidad/negocio?dias={7|30|90}`

`dias` es opcional; si no se envía, vale 30. Cualquier otro valor responde **400** con `ExcepcionValidacion`. La validación se hace en el controller (Principio VI).

**Respuesta 200**:

```json
{
  "dias": 30,
  "desde": "2026-10-26",
  "hasta": "2026-11-24",
  "enVivo": {
    "inicio": "2026-11-24T19:30:09-03:00",
    "rechazosSaldoInsuficiente": 3,
    "recalculosExitosos": 1,
    "recalculosFallidos": 0,
    "duracionUltimoRecalculoMs": 1834,
    "duracionPromedioRecalculoMs": 1834,
    "jugadoresCotizadosUltimoRecalculo": 2412,
    "variacionPromedioUltimoRecalculo": 0.0183
  },
  "actual": {
    "tokensEnCirculacion": 15230,
    "estrategiaActiva": { "codigo": "PERFORMANCE_GENERAL", "nombre": "Performance general" }
  },
  "periodo": {
    "ordenesCompra": 120,
    "ordenesVenta": 95,
    "emparejamientos": 80,
    "volumenCreditos": 125400.50,
    "cotizacionesCalculadas": 9650,
    "variacionPromedio": 0.0121
  },
  "serieMercado": [
    { "dia": "2026-10-26", "ordenesCompra": 0, "ordenesVenta": 0, "emparejamientos": 0, "volumenCreditos": 0.00 },
    { "dia": "2026-10-27", "ordenesCompra": 4, "ordenesVenta": 2, "emparejamientos": 3, "volumenCreditos": 4200.00 }
  ],
  "serieCotizacion": [
    { "dia": "2026-11-23", "jugadoresCotizados": 2412, "variacionPromedio": 0.0183 },
    { "dia": "2026-11-24", "jugadoresCotizados": 0, "variacionPromedio": null }
  ]
}
```

**Reglas**:
- Las series tienen exactamente `dias` elementos, ordenados de forma ascendente, incluidos los días en cero.
- `duracion*`, `jugadoresCotizadosUltimoRecalculo` y `variacionPromedioUltimoRecalculo` valen `null` si no
  hubo recálculos exitosos desde `inicio`.
- `periodo.cotizacionesCalculadas` es la suma de `serieCotizacion[].jugadoresCotizados`. Cada día cuenta
  jugadores **distintos**, así que un jugador cotizado en días distintos cuenta una vez por día, y dos
  recálculos el mismo día lo cuentan una sola vez. Por eso no se llama "jugadores".
- `periodo.variacionPromedio` es el promedio ponderado por cantidad de cotizaciones con valor previo. Vale
  `null` si no hay ninguna en el período.
- `duracionPromedioRecalculoMs` promedia solo los recálculos **exitosos**.
- Ningún campo identifica usuarios ni jugadores.

## GET `/api/admin/observabilidad/tecnico`

**Respuesta 200**:

```json
{
  "instante": "2026-11-24T19:45:00-03:00",
  "salud": {
    "estado": "UP",
    "componentes": [
      { "nombre": "db", "estado": "UP" },
      { "nombre": "diskSpace", "estado": "UP" },
      { "nombre": "ping", "estado": "UP" },
      { "nombre": "ssl", "estado": "UP" }
    ]
  },
  "requests": {
    "total": 412,
    "latenciaMediaMs": 12.4,
    "tasaErrorCliente": 0.08,
    "tasaErrorServidor": 0.0,
    "endpoints": [
      { "metodo": "GET", "uri": "/players/{id}", "cantidad": 120, "erroresCliente": 4, "erroresServidor": 0, "latenciaMediaMs": 8.1 }
    ]
  },
  "jvm": {
    "memoriaHeapUsadaBytes": 183500800,
    "memoriaHeapMaximaBytes": 4248829952,
    "threadsVivos": 38,
    "cpuProceso": 0.004,
    "cpuSistema": 0.11,
    "uptimeSegundos": 512
  },
  "poolConexiones": { "activas": 0, "inactivas": 10, "pendientes": 0, "maximas": 10 }
}
```

**Reglas**:
- `salud.componentes` solo lleva nombre y estado, sin `details`.
- `requests` excluye las URIs `/actuator/**` y `/api/admin/observabilidad/**`. `endpoints` es el top 10
  por `cantidad`, en orden descendente.
- Las tasas valen `null` si `total = 0`.
- Cualquier valor de `jvm` o `poolConexiones` vale `null` si su métrica no está registrada.

## Errores

| Caso | Código |
|------|--------|
| Sin token | 401 |
| `ROLE_USUARIO` | 403 |
| `dias` fuera de {7, 30, 90} | 400 |
| Error inesperado | 500, a cargo de `ControladorErrores` |
