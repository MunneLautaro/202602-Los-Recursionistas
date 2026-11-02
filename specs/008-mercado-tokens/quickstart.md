# Quickstart de validación: Mercado de Tokens

## Prerrequisitos

1. Java 21 y el wrapper de Gradle disponibles.
2. PostgreSQL de desarrollo configurado según el backend existente, o H2 para
   pruebas unitarias.
3. Un usuario con `ROLE_ADMIN`, al menos un jugador activo y una cotización
   vigente.
4. La aplicación configurada con zona
   `America/Argentina/Buenos_Aires`.

## Validación automatizada

Desde `backend/`:

```powershell
.\gradlew test
```

La suite debe permanecer acotada a `model` y `service`. Debe incluir pruebas
Mockito para modelo, servicio de órdenes, matching, liquidez, recarga y
transacciones, incluyendo rollback, fondos insuficientes, tokens insuficientes
y acceso concurrente simulado.

## Escenario 1: liquidez inicial idempotente

1. Iniciar la aplicación con jugadores activos y cotizaciones vigentes.
2. Ejecutar la inicialización de mercado.
3. Consultar `GET /orders?tipo=VENTA`.
4. Verificar una sola orden inicial activa de 100 tokens por jugador, su precio
   unitario y el total `100 * precio`.
5. Repetir la inicialización y verificar que no aparecen duplicados ni se
   incrementan cantidades de órdenes existentes.

## Escenario 2: compra parcial

1. Acreditar saldo al usuario:

```http
POST /users/{id}/credits
Content-Type: application/json

{"monto": 1000.00}
```

2. Comprar cinco tokens:

```http
POST /orders/buy
Content-Type: application/json

{"jugadorId": 1, "cantidad": 5}
```

3. Verificar que la respuesta expone precio unitario y total.
4. Consultar `GET /orders?jugadorId=1&tipo=VENTA` y verificar 95 tokens
   disponibles en la orden inicial.
5. Verificar el saldo disponible, la tenencia del comprador y un registro de
   `TransaccionMercado` con su auditoría.

## Escenario 3: venta P2P completa

1. Usar un usuario que tenga al menos 10 tokens disponibles.
2. Publicar:

```http
POST /orders/sell
Content-Type: application/json

{"jugadorId": 1, "cantidad": 10}
```

3. Publicar desde otro usuario una compra por 10 tokens al mismo jugador.
4. Verificar que ambas órdenes quedan `COMPLETADA`, el comprador recibe 10
   tokens y el vendedor recibe `10 * precioUnitario` créditos.

## Escenario 4: validaciones y rollback

1. Intentar comprar con saldo disponible menor al total y verificar rechazo sin
   orden ni cambio de saldo.
2. Intentar vender más tokens que los disponibles y verificar rechazo sin
   reserva ni cambio de tenencia.
3. Forzar una excepción durante el match y verificar rollback completo: saldos,
   tenencias, reservas, estados y auditorías permanecen como antes.
4. Intentar recargar cero y un monto negativo; ambos deben rechazarse sin
   modificar saldo.

## Escenario 5: concurrencia

1. Lanzar dos solicitudes de venta concurrentes que consuman la misma tenencia,
   o dos compras que consuman el mismo saldo.
2. Verificar que el bloqueo de base de datos permite solo operaciones válidas.
3. Confirmar que no hay saldo negativo, doble venta, doble gasto ni más de 100
   tokens emitidos para un jugador.
4. Repetir la inicialización de un jugador nuevo concurrentemente y verificar
   una sola orden inicial.

## Referencias

- Contratos: [contracts/openapi.yaml](contracts/openapi.yaml).
- Modelo: [data-model.md](data-model.md).
- Requisitos: [spec.md](spec.md).
