# Investigación: Mercado de Tokens

## Decisión 1: Concurrencia con transacciones y bloqueo pesimista

**Decisión**: Todas las operaciones que afecten saldo, tenencias o reservas se
ejecutarán en una transacción atómica con aislamiento `REPEATABLE_READ` o
superior, y adquirirán `PESSIMISTIC_WRITE` sobre los usuarios, tenencias y
órdenes relevantes antes de validar y modificar recursos.

**Rationale**: El flujo de compra y matching necesita leer y modificar el mismo
saldo o tenencia sin que otra operación pueda consumirlo entre ambas acciones.
El bloqueo pesimista hace explícita la exclusión mutua a nivel de base de datos,
es compatible con PostgreSQL y evita doble gasto, doble venta y saldos negativos.
El rollback transaccional elimina reservas parciales ante cualquier excepción.

**Alternativas consideradas**:

- **Bloqueo optimista**: reduce contención en lecturas, pero exige reintentos y
  manejo de conflictos en cada flujo financiero; se descarta para el primer
  motor porque aumenta la superficie de errores en operaciones monetarias.
- **Solo aislamiento transaccional sin locks explícitos**: no garantiza que dos
  transacciones no validen simultáneamente el mismo saldo; se descarta.

## Decisión 2: Precio capturado en la orden

**Decisión**: Al aceptar una orden se captura la cotización vigente como
`precioUnitario` inmutable de la orden. El matching exige igualdad de jugador y
precio, y el total se calcula como cantidad por precio unitario.

**Rationale**: Evita que una actualización posterior de cotización cambie el
valor de una reserva ya comprometida y hace reproducible cada transacción.

**Alternativas consideradas**:

- Recalcular el precio en cada match: podría dejar una reserva de créditos
  incorrecta y romper la trazabilidad; se descarta.
- Permitir precio indicado por el usuario: contradice el mercado exclusivo al
  precio vigente; se descarta.

## Decisión 3: Matching FIFO y ejecución parcial

**Decisión**: Las órdenes activas compatibles se consultarán por
`fechaCreacion ASC, id ASC`. Cada ciclo ejecuta el mínimo entre las cantidades
disponibles, actualiza ambos remanentes y conserva la fecha original de las
órdenes parcialmente ejecutadas.

**Rationale**: La clave estable de timestamp e id implementa FIFO incluso cuando
dos órdenes llegan en el mismo instante. El mínimo permite liquidar la compra
inmediata y mantener el remanente abierto sin crear órdenes derivadas.

**Alternativas consideradas**:

- Prioridad por cantidad: no cumple FIFO y favorece órdenes posteriores.
- Completar solo cuando las cantidades coinciden: elimina la liquidez parcial
  requerida por la feature; se descarta.

## Decisión 4: Idempotencia de liquidez inicial

**Decisión**: Un servicio de inicialización buscará por jugador una orden
inicial activa del superusuario antes de crearla. La emisión de la tenencia y la
orden se realizará bajo la misma transacción y una restricción única lógica por
jugador/superusuario/orden inicial.

**Rationale**: El arranque puede repetirse y el scheduler puede reintentar un
  jugador. La verificación más una restricción de datos evita duplicados bajo
  concurrencia. Una orden parcialmente consumida no se reemplaza ni se aumenta.

**Alternativas consideradas**:

- Crear siempre una orden de 100 tokens: duplica oferta en reinicios.
- Borrar y recrear la orden: altera órdenes en curso y contradice la
  inmutabilidad operativa; se descarta.

## Decisión 5: Auditoría append-only

**Decisión**: `TransaccionMercado` registrará cada match ejecutado y
`AuditoriaTransaccional` registrará el detalle de cada cambio financiero
relevante dentro de la misma transacción. Ambos modelos no expondrán operaciones
de actualización o eliminación y sus repositorios solo ofrecerán creación y
consulta.

**Rationale**: Separar el hecho de negocio de su detalle de auditoría permite
consultar el historial de usuario y conservar evidencia completa: entidad,
autor, fecha con zona `America/Argentina/Buenos_Aires`, detalle y estados antes
y después.

**Alternativas consideradas**:

- Un único registro de orden como historial: no representa correctamente cada
  match parcial ni todos los cambios de saldo.
- Auditoría en logs: no garantiza persistencia, consulta ni atomicidad con el
  match; se descarta.

## Decisión 6: Contratos y validación por capas

**Decisión**: Los DTOs validarán forma (`@NotNull`, `@Positive`); los servicios
resolverán jugador, cotización y usuario con fail-fast; los modelos protegerán
saldo, tenencias, reservas y transiciones. Los controladores solo traducirán
HTTP a puertos de servicio.

**Rationale**: Mantiene los Principios I, II, III y VI, permite invocar el
matching desde HTTP o scheduler y evita acoplar el motor a DTOs.

**Alternativas consideradas**:

- Validar todo en controller: deja desprotegidos scheduler y llamadas internas.
- Validar todo en service: concentra invariantes que pertenecen al dominio;
  se descarta.
