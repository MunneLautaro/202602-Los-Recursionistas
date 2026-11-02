# Frontend/API Contract

The frontend consumes only the existing REST endpoints below. Query parameters with no value are omitted.

## Public endpoints

| Method | Path | Request/query | Response |
|---|---|---|---|
| POST | `/registrar` | `{ nombreUsuario, contrasena, saldo, habilitado }` | `UsuarioResponseDTO` |
| POST | `/login` | `{ nombreUsuario, contrasena }` | `{ token }` |
| GET | `/players` | `nombre`, `posicion`, `ligaId`, `equipoId`, `page` | `Page<JugadorResponseDTO>` |
| GET | `/players/{id}` | path id | `JugadorResponseDTO` |
| GET | `/players/{id}/stats` | path id | stats object with tournament list and global totals |
| GET | `/players/{id}/quotes` | `desde`, `hasta`, `page`, `size` | `Page<CotizacionResponse>` |
| GET | `/players/ranking` | `ligaId`, `posicion`, `page`, `size` | `Page<RankingJugadorResponse>` |
| GET | `/leagues` | none | `LigaResponseDTO[]` |
| GET | `/teams/{ligaId}` | path league id | `EquipoResponseDTO[]` |
| GET | `/orders` | `jugadorId`, `tipo`, `page`, `size` | `Page<OrdenResponse>` |

## Authenticated endpoints

All authenticated requests send `Authorization: Bearer <token>`.

| Method | Path | Request/query | Response |
|---|---|---|---|
| POST | `/orders/buy` | `{ jugadorId, cantidad }` | `OrdenResponse` |
| POST | `/orders/sell` | `{ jugadorId, cantidad }` | `OrdenResponse` |
| POST | `/users/{id}/credits` | `{ monto }` | `SaldoResponse` |
| GET | `/users/{id}/transactions` | `page`, `size` | `Page<TransaccionResponse>` |

## Error behavior

- 401: invalidate local session and route the user to login.
- 403: preserve a valid session and show forbidden access.
- 400/409/422/500 or a domain error: show the backend message when available, otherwise a generic retryable message.
- Never display a mutation as successful unless the request returned a successful response.

## Identity limitation

Login returns only a token. The frontend must not invent `/me`, assume undocumented JWT claims, or choose an arbitrary user id. Account operations remain unavailable until a trusted `userId` is available.
