# Contrato: ranking de jugadores

## `GET /players/ranking`

Devuelve `Page<RankingJugadorResponse>` por valor actual descendente.

Parámetros opcionales: `ligaId` (`Long`) y `posicion`; `page` default `0`;
`size` default `12`. La posición reconoce español/inglés normalizado.

```json
{
  "content": [{
    "jugadorId": 42,
    "nombre": "Jugador Ejemplo",
    "posicion": "Delantero",
    "equipo": "Equipo A",
    "liga": "Liga A",
    "valorActual": 735.25,
    "valorAnterior": 700.00,
    "variacion": 35.25
  }],
  "number": 0,
  "size": 12,
  "totalElements": 1,
  "totalPages": 1
}
```

Solo aparece la cotización más reciente de cada jugador. Sin cotización
anterior, `valorAnterior` y `variacion` son nulos. Parámetros inválidos
devuelven 400 y el formato de error es `RespuestaError`.
