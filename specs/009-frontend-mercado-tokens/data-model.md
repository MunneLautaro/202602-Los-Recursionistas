# Data Model: Frontend del Mercado de Tokens

## Existing entities

### Session

- `token: string`
- `nombreUsuario: string` cuando el frontend ya lo conoce
- `userId?: number` solo si proviene de una respuesta confiable del backend
- States: unauthenticated, authenticated, invalidated

### Paginated response

- `content: T[]`
- `totalPages: number`
- `number: number`
- `size: number`
- `totalElements: number`

## New read models

### PlayerStats

- `jugadorId`, `nombre`, `posicion`, `equipo`
- `estadisticasPorTorneo: SeasonStats[]`
- `totalesGlobal: GlobalStats`

### SeasonStats

- Tournament and season identifiers/dates
- Apps, minutes, goals, assists, shots, key passes, dribbles, tackles, interceptions, cards
- Average rating and passing accuracy when available

### Quote

- `id`, `jugadorId`, `valor`, `timestampCalculo`
- `estrategiaUsada`, `ventanaInicio`, `ventanaFin`

### RankingPlayer

- `jugadorId`, `nombre`, `posicion`, `equipo`, `liga`
- `valorActual`, `valorAnterior`, `variacion`

### Order

- `id`, `jugadorId`, `tipo`
- `cantidadOriginal`, `cantidadDisponible`
- `precioUnitario`, `precioTotalRemanente`
- `estado`, `fechaCreacion`

### Balance

- `usuarioId`, `saldo`, `saldoReservado`, `saldoDisponible`

### Transaction

- `id`, `jugadorId`, `compradorId`, `vendedorId`
- `cantidad`, `precioUnitario`, `total`, `ejecutadaEn`

## Validation and state rules

- Order and credit amounts must be positive before sending.
- Quote dates must form a valid inclusive range.
- Mutating actions are disabled while pending.
- A 401 invalidates the session; a 403 preserves it and reports forbidden access.
- Empty `content` is a valid state, not an error.
