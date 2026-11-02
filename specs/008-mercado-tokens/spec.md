# Feature Specification: Mercado de Tokens

**Feature Branch**: `008-mercado-tokens`

**Created**: 2026-10-25

**Status**: Draft

**Input**: User description: "Crear el sistema de Mercado de Tokens (Compra/Venta,
Libro de Órdenes, Matching Engine con Ejecución Parcial y Recarga de Créditos)
para la feature 008-mercado-tokens."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Comprar tokens al precio vigente (Priority: P1)

Como usuario con créditos disponibles, quiero comprar una cantidad de tokens de
un jugador al precio unitario de su cotización vigente, para incorporar esos
tokens a mi tenencia.

**Why this priority**: Es el flujo principal que habilita el acceso de usuarios
al mercado y consume la liquidez inicial.

**Independent Test**: Con una orden de venta disponible y saldo suficiente, un
usuario puede comprar una cantidad parcial y comprobar su tenencia, saldo y la
cantidad remanente de la orden.

**Acceptance Scenarios**:

1. **Given** una orden de venta de 100 tokens a una cotización X y un usuario
   con saldo suficiente, **When** el usuario compra 5 tokens, **Then** recibe
   5 tokens, se debitan `5 * X` créditos y la orden queda con 95 tokens
   disponibles.
2. **Given** una orden de compra y una orden de venta del mismo jugador al mismo
   precio, **When** ambas cantidades son iguales, **Then** se transfieren los
   tokens y créditos, y ambas órdenes pasan a estado completada.
3. **Given** una orden de venta con más tokens que la compra solicitada,
   **When** se ejecuta el match, **Then** la orden de venta permanece abierta
   únicamente por su cantidad remanente.

---

### User Story 2 - Publicar y ejecutar ventas P2P (Priority: P1)

Como usuario que posee tokens, quiero publicar una orden de venta al precio de
mercado y venderla a otro usuario, para convertir mis tokens en créditos.

**Why this priority**: Completa el ciclo económico del mercado y permite
intercambios entre usuarios más allá de la liquidez inicial.

**Independent Test**: Un usuario con tokens publica una venta y otro usuario
publica una compra equivalente; el sistema puede verificarse observando ambas
tenencias, saldos y órdenes.

**Acceptance Scenarios**:

1. **Given** un usuario con 10 tokens de un jugador, **When** publica una orden
   de venta por 10 tokens, **Then** esos tokens quedan comprometidos y no
   pueden venderse nuevamente mientras la orden esté activa.
2. **Given** una orden de venta P2P de 10 tokens, **When** otro usuario publica
   una compra por 10 tokens al mismo precio, **Then** el vendedor recibe
   `10 * X` créditos, el comprador recibe 10 tokens y ambas órdenes se
   completan.
3. **Given** varias órdenes compatibles, **When** el mercado las procesa,
   **Then** se atienden en orden de llegada y las cantidades parciales
   permanecen abiertas por sus remanentes.

---

### User Story 3 - Mantener liquidez inicial y de nuevos jugadores (Priority: P1)

Como operador del mercado, quiero que cada jugador activo tenga una única orden
inicial de venta por 100 tokens, para que exista liquidez desde el inicio y al
incorporar jugadores nuevos.

**Why this priority**: Sin la emisión y oferta iniciales no existe un mercado
operable para los jugadores del catálogo.

**Independent Test**: Al inicializar el mercado y después de incorporar un
jugador nuevo, se puede contar una orden inicial por jugador y verificar que las
órdenes existentes no se duplican ni modifican.

**Acceptance Scenarios**:

1. **Given** jugadores activos cargados al iniciar el sistema, **When** se
   inicializa el mercado, **Then** el superusuario custodio tiene exactamente
   una orden de venta de 100 tokens por jugador, con precio unitario X y total
   `100 * X`.
2. **Given** un jugador nuevo con cotización inicial, **When** finaliza su alta
   en el catálogo, **Then** se crea exactamente una orden de venta de 100 tokens
   para ese jugador y no se alteran jugadores u órdenes preexistentes.

---

### User Story 4 - Consultar órdenes y operaciones (Priority: P2)

Como usuario, quiero consultar el libro de órdenes y mi historial, para conocer
la liquidez disponible, mis operaciones y el valor de cada transacción.

**Why this priority**: La transparencia de precios, cantidades y estados es
necesaria para tomar decisiones y verificar el resultado de una operación.

**Independent Test**: Tras crear y ejecutar órdenes, se pueden consultar listados
paginados filtrados por jugador o tipo y verificar sus totales.

**Acceptance Scenarios**:

1. **Given** órdenes activas, **When** el usuario consulta el libro, **Then**
   cada resultado muestra cantidad original, cantidad disponible, tipo, jugador,
   precio unitario y precio total remanente.
2. **Given** operaciones ejecutadas por un usuario, **When** consulta su
   historial, **Then** obtiene únicamente sus operaciones, paginadas, con
   cantidad, precio unitario, total, autoría, estado y fecha con zona horaria.

---

### User Story 5 - Recargar créditos (Priority: P2)

Como usuario, quiero acreditar créditos ficticios a mi cuenta, para disponer de
saldo suficiente para comprar tokens sin utilizar dinero real ni una pasarela
externa.

**Why this priority**: Proporciona el saldo necesario para probar y utilizar el
mercado de forma autónoma.

**Independent Test**: Un usuario puede acreditar un monto positivo y verificar el
nuevo saldo; un monto cero o negativo es rechazado sin cambios.

**Acceptance Scenarios**:

1. **Given** una cuenta de usuario, **When** solicita acreditar un monto
   estrictamente positivo, **Then** el saldo aumenta exactamente por ese monto.
2. **Given** una cuenta de usuario, **When** solicita acreditar cero o un monto
   negativo, **Then** la operación es rechazada y el saldo permanece sin cambios.

---

### Edge Cases

- Una compra con saldo disponible menor que `cantidad * cotización` se rechaza
  sin crear una orden ni modificar el saldo.
- Una venta con menos tokens disponibles que la cantidad solicitada se rechaza
  sin comprometer tokens ni crear una orden.
- Una cantidad cero, negativa, fraccionaria o que exceda el máximo disponible
  se rechaza con un error de validación.
- Una orden no puede ejecutarse contra otra de un jugador distinto o con un
  precio unitario distinto de la cotización vigente.
- Si dos operaciones concurrentes intentan usar el mismo saldo o tenencia, como
  máximo una cantidad válida puede comprometer esos recursos; nunca se permiten
  doble gasto, doble venta o saldos negativos.
- Si falla una parte del match, la operación completa se revierte: no quedan
  créditos, tokens, reservas ni auditorías parciales.
- Reintentar la inicialización o el alta de un jugador no crea órdenes iniciales
  duplicadas.
- Si un jugador no existe, está inactivo o no tiene cotización vigente, la
  operación se rechaza sin modificar el libro.
- Una orden parcialmente ejecutada conserva su fecha de llegada original para
  mantener la política FIFO.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST representar la emisión de 100 tokens para cada
  jugador activo y asociar cada tenencia a un jugador.
- **FR-002**: El sistema MUST usar la cotización vigente global del jugador como
  precio unitario obligatorio para comprar y vender; el precio total MUST ser
  `cantidad * precio unitario`.
- **FR-003**: El sistema MUST permitir a usuarios autenticados publicar órdenes
  de compra y venta indicando jugador y cantidad entera positiva.
- **FR-004**: El sistema MUST reservar el total de créditos requerido por una
  orden de compra activa y los tokens de una orden de venta activa.
- **FR-005**: El sistema MUST rechazar compras cuando los créditos disponibles
  sean menores que `cantidad * cotización`, sin cambiar saldos ni crear órdenes.
- **FR-006**: El sistema MUST rechazar ventas cuando el usuario no posea la
  cantidad solicitada de tokens, sin cambiar tenencias ni crear órdenes.
- **FR-007**: El sistema MUST emparejar órdenes compatibles del mismo jugador y
  precio usando el orden FIFO de llegada.
- **FR-008**: El sistema MUST soportar ejecución parcial, transfiriendo solo la
  cantidad emparejada y manteniendo activo el remanente.
- **FR-009**: Un match exitoso MUST transferir atómicamente los tokens al
  comprador, acreditar al vendedor, consumir las reservas correspondientes y
  actualizar los estados de las órdenes.
- **FR-010**: All operations that modify credits, token holdings or order
  reservations MUST be atomic, prevent dirty and non-repeatable reads, and use
  database-level concurrency control to prevent double spending and negative
  balances.
- **FR-011**: Cada transacción de mercado ejecutada MUST generar un registro de
  auditoría inmutable y append-only con identificador de entidad afectada,
  autor, timestamp con zona horaria, detalle y estados anterior y posterior.
- **FR-012**: El sistema MUST NOT actualizar ni eliminar transacciones de mercado
  históricas ni registros de auditoría.
- **FR-013**: El proceso de matching MUST poder invocarse independientemente del
  protocolo de transporte y MUST permanecer desacoplado de sesiones web y
  objetos de request/response.
- **FR-014**: Al iniciar, el sistema MUST asegurar que un superusuario con
  `ROLE_ADMIN` custodie los 100 tokens de cada jugador activo y tenga
  exactamente una orden inicial de venta por jugador al precio vigente.
- **FR-015**: Cuando un scheduler incorpore un jugador nuevo y su cotización
  inicial esté disponible, el sistema MUST crear exactamente una orden inicial
  de venta para ese jugador sin cambiar órdenes existentes.
- **FR-016**: El sistema MUST exponer una operación equivalente a
  `POST /orders/buy` that accepts player identifier and quantity.
- **FR-017**: El sistema MUST exponer una operación equivalente a
  `POST /orders/sell` that accepts player identifier and quantity.
- **FR-018**: El sistema MUST exponer una operación equivalente a `GET /orders`
  with pagination and optional player and order-type filters.
- **FR-019**: El listado de órdenes activas MUST exponer cantidad original,
  cantidad disponible, tipo, jugador, precio unitario y precio total remanente.
- **FR-020**: El sistema MUST exponer una operación equivalente a
  `POST /users/{id}/credits` that accepts a strictly positive credit amount and
  rejects zero or negative amounts without changing the balance.
- **FR-021**: El sistema MUST exponer una operación equivalente a
  `GET /users/{id}/transactions` with pagination and only the requested user's
  executed buy and sell transactions.
- **FR-022**: El sistema MUST usar errores de validación claros para jugadores
  desconocidos o inactivos, cotizaciones no disponibles, cantidades inválidas,
  créditos insuficientes, tokens insuficientes y acceso no autorizado.
- **FR-023**: Financial and market services MUST include isolated unit tests for
  rollback after exceptions, insufficient funds or tokens, and simulated
  concurrent access.

### Key Entities

- **Jugador**: Active catalog player whose current global quote determines the
  mandatory unit price and whose token supply is initially 100.
- **Cotizacion**: Current market quote for a player, including the value used as
  the unit price for a new order.
- **Usuario**: Account holder with credit balance, token holdings and market
  permissions.
- **Tenencia de tokens**: Quantity of a player's tokens owned by a user, including
  the quantity available after active sell reservations.
- **Orden**: Buy or sell request with player, owner, original quantity, available
  quantity, unit price, total remainder, state and arrival timestamp.
- **Reserva**: Credits or tokens committed by an active order and unavailable for
  other operations.
- **Transacción de mercado**: Executed transfer of tokens and credits, including
  quantity, price, buyer, seller, state transition and timestamp.
- **Auditoría transaccional**: Immutable historical record of a market change,
  including previous and resulting states and its author.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For every active player at initialization, exactly one initial sell
  order of 100 tokens exists; repeated initialization never increases that
  count.
- **SC-002**: A valid partial purchase completes in a single user action and
  leaves the exact unmatched quantity active in the book.
- **SC-003**: In all accepted concurrent-operation tests, the resulting credit
  and token balances remain non-negative and the total number of transferred
  tokens never exceeds 100 per player.
- **SC-004**: 100% of successfully executed matches have one immutable audit
  record containing all required fields; no historical record can be modified or
  removed.
- **SC-005**: 100% of invalid purchases for insufficient credits and invalid
  sales for insufficient tokens are rejected without balance, holding,
  reservation or order changes.
- **SC-006**: Los usuarios pueden identificar precio unitario, cantidad original,
  cantidad disponible y precio total remanente de cada orden activa paginada.
- **SC-007**: Los usuarios pueden completar una recarga con una solicitud válida,
  mientras todo intento de recarga cero o negativo se rechaza sin cambiar saldo.
- **SC-008**: At least 95% of users in acceptance testing can complete a
  buy-or-sell flow and verify its result without external assistance.

## Assumptions

- Solo usuarios autenticados pueden publicar órdenes, comprar tokens, vender
  tokens o consultar transacciones de su cuenta; se reutilizan las reglas
  existentes de autenticación y autorización.
- El superusuario es una única cuenta existente o provisionada, identificada
  por `ROLE_ADMIN`; sus credenciales e identidad quedan fuera de esta feature.
- Token quantities are whole numbers and cannot be fractional.
- La cotización capturada al aceptar una orden permanece como su precio unitario
  de ejecución; un cambio posterior no altera retroactivamente esa orden.
- En esta feature el mercado solo admite matching inmediato al precio capturado;
  cancelación, expiración, precios límite, comisiones y retiros quedan fuera de
  alcance.
- La recarga de créditos es una simulación interna sin movimiento de dinero
  real, proveedor de pagos externo ni liquidación fuera de la aplicación.
- La sincronización de jugadores y la generación de cotizaciones existentes son
  dependencias; la liquidez inicial se crea después de disponer de cotización.
- Los listados paginados usan el tamaño de página predeterminado del proyecto y
  exponen solo órdenes activas o el historial propio del usuario solicitante.
