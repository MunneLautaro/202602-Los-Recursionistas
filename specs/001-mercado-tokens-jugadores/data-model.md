# Modelo de Datos

Los nombres de dominio se mantienen en espanol sin acentos ni `n`. Las entidades historicas son append-only: no se permiten update/delete desde casos de uso.

## Entidades

### Usuario

- `id`, `username`, `nombre`, `estado`, `saldo`, `roles`, `createdAt`, `updatedAt`.
- `estado` admite `HABILITADO`, `BLOQUEADO`, `ELIMINADO_LOGICO`.
- saldo nunca es negativo; username es unico.
- El Superusuario Financiero se identifica por username estable y rol `FINANCIAL_ADMIN`.

### Liga

- `id`, `nombre`, `codigo`, `activo`.
- La seed inicial contiene Premier League, Bundesliga, La Liga, Serie A y Ligue 1.
- `codigo` es unico.

### Jugador

- `id`, `nombre`, `equipo`, `posicion`, `ligaId`, `estado`, `disponibilidad`, `fuente`, `externalId`.
- `estado` admite `ACTIVO` e `INACTIVO`; externalId + fuente es unico.
- Un jugador activo pertenece a una liga objetivo.

### Cotizacion

- `id`, `jugadorId`, `fechaCalculo`, `precio`, `score`, `estrategia`, `version`, `metricas`, `fuente`, `vigenteHasta`.
- precio es positivo; score esta entre 0 y 100; version y estrategia son obligatorios.
- append-only; no se sobreescribe una cotizacion previa.
- indice `(jugadorId, fechaCalculo desc)`.

### EstrategiaValuacion

- `codigo`, `version`, `metricas`, `pesos`, `pesosPorPosicion`, `configuracion`, `activo`.
- codigo + version es unico; configuracion inmutable una vez usada por una cotizacion.

### Posicion

- `id`, `usuarioId`, `jugadorId`, `cantidad`, `precioPromedio`, `updatedAt`.
- usuario + jugador es unico; cantidad no negativa; precio promedio no negativo.
- La posicion del Superusuario representa inventario financiero inicial y retornado.

### TokenOrder

- `id`, `usuarioId`, `jugadorId`, `tipo`, `cantidad`, `precioUnitario`, `importeTotal`, `cotizacionId`, `estado`, `idempotencyKey`, `createdAt`, `settledAt`.
- tipo `BUY` o `SELL`; estado `CREATED`, `SETTLED`, `REJECTED`.
- cantidad positiva; importe = cantidad \* precio unitario; idempotencyKey unico por usuario.

### Transaccion

- `id`, `orderId`, `autorId`, `jugadorId`, `tipo`, `cantidad`, `precioUnitario`, `importeTotal`, `saldoAnterior`, `saldoPosterior`, `posicionAnterior`, `posicionPosterior`, `cotizacionId`, `createdAt`.
- inmutable, una por orden liquidada.

### AuditoriaFinanciera

- `id`, `transaccionId`, `autorId`, `correlationId`, `timestamp`, `detalleCambios`, `estadoAnterior`, `estadoPosterior`, `estrategia`, `version`.
- inmutable; toda transaccion liquidada debe tener exactamente una auditoria.

### EjecucionRecalculo

- `id`, `scope`, `estrategia`, `version`, `idempotencyKey`, `estado`, `startedAt`, `finishedAt`, `resultado`, `error`.
- estado `REQUESTED`, `RUNNING`, `COMPLETED`, `FAILED`, `CONFLICTED`; una clave no se ejecuta dos veces.

## Relaciones y consistencia

- `Liga 1..* Jugador`; `Jugador 1..* Cotizacion`.
- `Usuario 1..* Posicion`, `TokenOrder`, `Transaccion` y `AuditoriaFinanciera`.
- `TokenOrder 1..1 Transaccion 1..1 AuditoriaFinanciera` al liquidarse.
- Para cada jugador, la suma de posiciones no supera 100 tokens.
- Compra: saldo usuario disminuye, saldo financiero aumenta, posicion usuario aumenta y financiera disminuye.
- Venta: saldo usuario aumenta, saldo financiero disminuye, posicion usuario disminuye y financiera aumenta.
- Fallo de cualquier validacion produce rollback completo.

## Estados y transiciones

`TokenOrder`: `CREATED -> SETTLED` o `CREATED -> REJECTED`; no hay transiciones desde estados finales.

`EjecucionRecalculo`: `REQUESTED -> RUNNING -> COMPLETED|FAILED`; una ejecucion concurrente incompatible pasa a `CONFLICTED`.

## Inmutabilidad

JPA debe impedir modificaciones de `Cotizacion`, `Transaccion` y `AuditoriaFinanciera` despues de insert. Los endpoints solo exponen lectura; no existen endpoints de update/delete historico.
