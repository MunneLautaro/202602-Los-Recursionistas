# Guia rapida de validacion

## Prerrequisitos

- Java 21 disponible.
- PostgreSQL disponible para una ejecucion normal, o usar el perfil de propiedades de prueba ya definido por `BackendApplicationTests` con H2.
- Ejecutar los comandos desde `backend/`.

## Compilar y probar

En Windows:

```powershell
.\gradlew.bat clean test
```

En Linux/CI:

```bash
./gradlew clean test
```

Resultado esperado: compilacion y suite completa exitosas, sin eliminar ni modificar tests existentes para ocultar regresiones.

## Levantar la aplicacion

```powershell
.\gradlew.bat bootRun
```

La aplicacion debe iniciar en el puerto configurado por defecto. Si PostgreSQL no esta disponible, usar un entorno de prueba que provea H2 y verificar el contexto mediante los tests existentes.

## Validar endpoints publicos

```powershell
Invoke-WebRequest http://localhost:8080/swagger-ui.html -UseBasicParsing
Invoke-WebRequest http://localhost:8080/v3/api-docs -UseBasicParsing
Invoke-WebRequest http://localhost:8080/actuator/health -UseBasicParsing
```

Resultados esperados:

1. `GET /swagger-ui.html` responde `3xx` hacia `/swagger-ui/index.html`, que responde `200` sin credenciales y renderiza la interfaz.
2. `/v3/api-docs` responde `200` con JSON OpenAPI v3, metadatos y paths.
3. `/actuator/health` responde `200` con `UP` cuando la aplicacion y sus dependencias estan saludables.
4. Si el indicador de base de datos esta caido, health responde `503` con `DOWN` u `OUT_OF_SERVICE`, sin detalles sensibles.

Validacion manual realizada el 2026-09-22 con H2: `/swagger-ui.html` termino en `200` siguiendo su redireccion a `/swagger-ui/index.html`; `/v3/api-docs` respondio `200` con OpenAPI `3.1.0`; `/actuator/health` respondio `200` con `UP` y sin detalles de indicadores.

## Validar seguridad y alcance

- Repetir las tres solicitudes sin headers `Authorization`.
- Confirmar que las rutas de Swagger y health no redirigen a login ni responden 401/403.
- Confirmar que solo `health` aparece como endpoint Actuator expuesto por HTTP.
- Revisar `/v3/api-docs` para que no incluya contrasenas, tokens, credenciales, URLs internas ni handlers de implementacion.
- Comparar los paths documentados con el inventario de controllers REST. Actualmente no debe aparecer ningun endpoint de negocio.

## Validacion de anotaciones

Si se incorporan controllers antes de implementar esta feature, verificar por cada operacion:

- `@Tag` y `@Operation` con proposito en espanol.
- `@ApiResponse` para respuestas exitosas y errores reales.
- `@Schema` para request/response DTOs cuando la inferencia no sea suficiente.
- `RespuestaError` para 400, 422 y 500 cuando correspondan al `ControladorErrores` existente.
