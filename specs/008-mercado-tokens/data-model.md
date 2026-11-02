# Modelo de datos: Mercado de Tokens

## Entidades

### `Usuario`

Tabla existente `usuarios`, ampliada para separar saldo comprometido del saldo
disponible.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` / BIGINT | Identidad existente |
| `saldo` | `BigDecimal` / NUMERIC(19,2) | No negativo; saldo total de créditos |
| `saldoReservado` | `BigDecimal` / NUMERIC(19,2) | No negativo; no supera `saldo` |
| `autoridades` | colección existente | El custodio tiene `ROLE_ADMIN` |

El saldo disponible se define como `saldo - saldoReservado`. El modelo debe
ofrecer operaciones de acreditar, reservar, liberar y debitar que no permitan
resultados negativos.

### `Jugador`

Entidad existente. Solo jugadores activos con cotización vigente pueden recibir
emisión inicial y órdenes. La relación con `Cotizacion` se consulta para tomar
el valor actual, sin duplicar la lógica de cotización.

### `TenenciaToken`

Representa la cantidad de tokens de un jugador perteneciente a un usuario.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | Identidad |
| `usuario` | FK | Obligatorio |
| `jugador` | FK | Obligatorio |
| `cantidadTotal` | entero | No negativo |
| `cantidadReservada` | entero | No negativa y no supera `cantidadTotal` |

La cantidad disponible es `cantidadTotal - cantidadReservada`. La combinación
`usuario_id, jugador_id` debe ser única. El modelo debe reservar, liberar,
acreditar y debitar tokens protegiendo estas invariantes.

### `Orden`

Solicitud de compra o venta en el libro.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | Identidad, desempate FIFO |
| `usuario` | FK | Obligatorio |
| `jugador` | FK | Obligatorio |
| `tipo` | enum | `COMPRA` o `VENTA` |
| `cantidadOriginal` | entero | Positiva e inmutable |
| `cantidadDisponible` | entero | No negativa, no supera la original |
| `precioUnitario` | NUMERIC(19,2) | Positivo e inmutable |
| `estado` | enum | `ACTIVA`, `COMPLETADA` |
| `fechaCreacion` | timestamp con zona | Orden FIFO |
| `esLiquidezInicial` | boolean | Solo una por jugador y custodio |

Una orden activa de compra mantiene reservada
`cantidadDisponible * precioUnitario`; una orden activa de venta mantiene
reservada `cantidadDisponible` en la tenencia del vendedor.

### `TransaccionMercado`

Hecho inmutable de un match ejecutado.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | Identidad |
| `ordenCompra` / `ordenVenta` | FK | Órdenes involucradas |
| `jugador` | FK | Jugador negociado |
| `comprador` / `vendedor` | FK | Usuarios involucrados |
| `cantidad` | entero | Positiva |
| `precioUnitario` | NUMERIC(19,2) | Capturado en las órdenes |
| `total` | NUMERIC(19,2) | `cantidad * precioUnitario` |
| `ejecutadaEn` | `OffsetDateTime` | Zona Argentina |

No se permiten `UPDATE` ni `DELETE`; el repositorio solo expone creación y
consultas paginadas.

### `AuditoriaTransaccional`

Registro append-only del cambio financiero.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | Identidad |
| `entidadTipo` | texto | Tipo afectado |
| `entidadId` | `Long` | Identificador afectado |
| `autorId` | FK | Usuario o custodio que ejecutó |
| `ejecutadaEn` | `OffsetDateTime` | Zona `America/Argentina/Buenos_Aires` |
| `detalleCambio` | texto | Descripción reproducible |
| `estadoAnterior` | JSON/texto | Snapshot previo |
| `estadoPosterior` | JSON/texto | Snapshot resultante |

No se permiten operaciones de actualización o eliminación en entidad,
repositorio ni endpoint.

## Relaciones

- `Usuario 1:N TenenciaToken`.
- `Jugador 1:N TenenciaToken`.
- `Usuario 1:N Orden`.
- `Jugador 1:N Orden`.
- `Orden COMPRA` y `Orden VENTA` se relacionan con una
  `TransaccionMercado` por match; una orden puede intervenir en varias
  ejecuciones parciales.
- Cada `TransaccionMercado` genera uno o más registros de
  `AuditoriaTransaccional`.

## Invariantes y transiciones

1. La emisión inicial total de cada jugador es 100 tokens.
2. `cantidadTotal >= cantidadReservada >= 0`.
3. `cantidadDisponible >= 0`.
4. Los créditos y todas las cantidades de tokens son no negativos.
5. El precio unitario de una orden coincide con la cotización capturada al
   aceptarla y no cambia después.
6. Solo se emparejan órdenes activas del mismo jugador y precio.
7. El match transfiere exactamente la cantidad mínima de ambas órdenes.
8. Una orden pasa a `COMPLETADA` solo cuando su cantidad disponible llega a cero.
9. La orden parcialmente ejecutada conserva `fechaCreacion`.
10. Una orden inicial no se duplica para el mismo jugador y custodio.
11. Todas las modificaciones de saldos, tenencias, reservas, órdenes,
    transacciones y auditorías del match ocurren en una transacción atómica con
    locks `PESSIMISTIC_WRITE`.

## Consultas e índices

- Órdenes activas: `(jugador_id, tipo, precio_unitario, fecha_creacion, id)`.
- Liquidez inicial: índice único lógico por
  `(usuario_id, jugador_id, es_liquidez_inicial)` cuando es verdadero.
- Tenencias: única por `(usuario_id, jugador_id)`.
- Transacciones de usuario: `(comprador_id, ejecutada_en DESC)` y
  `(vendedor_id, ejecutada_en DESC)`.
- Auditoría: `(entidad_tipo, entidad_id, ejecutada_en DESC)` y
  `(autor_id, ejecutada_en DESC)`.

## DTOs previstos

- `OrdenRequest`: `jugadorId`, `cantidad`.
- `OrdenResponse`: identificador, jugador, tipo, cantidad original,
  cantidad disponible, precio unitario, precio total remanente, estado y fecha.
- `RecargaCreditosRequest`: `monto`.
- `SaldoResponse`: usuario, saldo total, saldo reservado y saldo disponible.
- `TransaccionResponse`: identificadores, comprador, vendedor, jugador,
  cantidad, precio unitario, total, estado y fecha con zona.
