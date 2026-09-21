# Contrato publico de documentacion y health

## GET `/swagger-ui.html`

- **Acceso**: publico, sin autenticacion, en todos los entornos.
- **Respuesta esperada**: `200 OK` con la interfaz HTML de Swagger UI.
- **Contrato**: la interfaz debe cargar la especificacion desde `/v3/api-docs`.

## GET `/v3/api-docs`

- **Acceso**: publico, sin autenticacion, en todos los entornos.
- **Respuesta esperada**: `200 OK` con un documento JSON OpenAPI v3.
- **Campos minimos**: `openapi`, `info.title`, `info.version`, `info.description`, `paths` y `components.schemas` cuando existan DTOs documentados.
- **Restriccion**: no debe contener secretos, credenciales, tokens, contrasenas, datos de conexion ni detalles internos.

## GET `/actuator/health`

- **Acceso**: publico, sin autenticacion, en todos los entornos.
- **Respuesta saludable**: `200 OK`, cuerpo con estado `UP` y sin detalles sensibles.
- **Respuesta no saludable**: `503 Service Unavailable` para estado `DOWN` u `OUT_OF_SERVICE`; el cuerpo no debe revelar credenciales ni informacion interna.
- **Exposicion**: solo `health` debe estar habilitado por HTTP; `metrics`, `env`, `beans`, `mappings`, `loggers` y otros endpoints no forman parte del contrato de esta feature.
- **Documentacion**: debe aparecer en Swagger UI y en el documento OpenAPI gracias a la integracion de springdoc con Actuator.

## Respuestas de endpoints REST existentes

Cuando existan controllers REST, el documento debe reflejar sus respuestas reales:

- `400 Bad Request`: validacion de request, esquema `RespuestaError`.
- `422 Unprocessable Entity`: error de dominio, esquema `RespuestaError`.
- `500 Internal Server Error`: error no controlado, esquema `RespuestaError`.
- Los codigos adicionales solo se agregan cuando el controller existente los produzca realmente.
