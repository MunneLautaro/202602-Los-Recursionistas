# Quickstart de Validacion

## Prerrequisitos

- Java 21 y Docker Desktop activos.
- PowerShell desde la raiz del repositorio.
- Puertos locales disponibles para PostgreSQL y el backend.

## Backend local

```powershell
Set-Location backend
.\gradlew.bat bootRun
```

La aplicacion debe iniciar con PostgreSQL local configurado y publicar health checks y OpenAPI. La configuracion no debe contener secretos versionados.

## Tests

```powershell
Set-Location backend
.\gradlew.bat test
```

El resultado esperado incluye unitarias puras de Model, pruebas de services y
MockMvc web en paquetes separados. El test existente
`BackendApplicationTests.contextLoads` debe continuar pasando.

La ejecucion local usa PostgreSQL en la base `mercadofutbol`, configurada mediante
`backend/.env`. Los repositories de usuarios y jugadores delegan en DAOs Spring
Data JPA y no mantienen estado en memoria. La prueba rapida usa H2; la prueba de
integracion PostgreSQL se ejecuta con Testcontainers cuando Docker esta disponible.

## Flujo inicial de usuario y JWT

Crear un usuario y conservar la ApiKey solo en el cliente:

```powershell
$user = Invoke-RestMethod -Method Post -Uri http://localhost:8080/users `
	-ContentType 'application/json' -Body '{"username":"ana"}'
$token = Invoke-RestMethod -Method Post -Uri http://localhost:8080/auth/token `
	-ContentType 'application/json' -Body (@{ username = 'ana'; apiKey = $user.apiKey } | ConvertTo-Json)
```

El catálogo es público y debe devolver el header `X-Correlation-Id`:

```powershell
Invoke-WebRequest -Uri http://localhost:8080/players
Invoke-WebRequest -Uri http://localhost:8080/v3/api-docs
```

Los recursos protegidos requieren `Authorization: Bearer <token>`; una ApiKey
incorrecta responde `401` y no expone la clave almacenada.

## Validaciones funcionales

1. Inicializar un entorno vacio y ejecutar bootstrap dos veces. Confirmar cinco ligas, Superusuario Financiero, 100 tokens por jugador a 1.00 y cero duplicados.
2. Consultar `GET /players`, `GET /players/{id}`, `GET /players/{id}/quotes` y `GET /players/ranking`; comprobar filtros, historial append-only, score, precio, estrategia/version y `404/400`.
3. Ejecutar `POST /quotes/recalculate` con rol de operador; comprobar `202`, ejecucion trazable y nueva cotizacion sin modificar la anterior. Repetir la clave y provocar concurrencia para comprobar idempotencia y `409`.
4. Con un usuario autenticado, comprar y vender mediante `/orders/buy` y `/orders/sell`; comprobar saldo, posiciones, inventario global, transaccion y auditoria.
5. Repetir compra con saldo/token insuficiente y venta sin tenencia; comprobar `409` y ausencia de cambios parciales.
6. Ejecutar cuatro usuarios con operaciones intercaladas y verificar que la suma de tokens por jugador nunca supera 100 y que el historial reconstruye el resultado.
7. Simular timeout y respuesta parcial de proveedores; comprobar cache o snapshot local, campo `fuente`, log estructurado y Correlation ID.
8. Consultar portfolio e historial paginado; confirmar precio promedio, valor actual, ganancia/perdida y ausencia de endpoints de mutacion historica.

## Contratos y smoke test

El contrato canonico esta en [contracts/openapi.yaml](contracts/openapi.yaml). El endpoint OpenAPI generado por la aplicacion debe ser validable contra ese documento. La coleccion Postman de la Fase 5 debe ejecutar al menos un caso exitoso y uno de error por endpoint.

## Criterios de rendimiento

Usar el dataset reproducible acordado por el equipo y medir p95/tasa de error para catalogo, ranking, portfolio y ordenes. El criterio inicial es p95 < 1 s para lecturas y < 2 s para compra/venta bajo condiciones normales; documentar el resultado antes de cerrar la feature.
