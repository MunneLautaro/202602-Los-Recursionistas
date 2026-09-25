# Modelo de datos: Documentacion de la API REST con OpenAPI/Swagger v3

Esta feature no agrega entidades persistentes ni modifica el modelo de dominio. El modelo descrito aqui es el contrato observable generado por la aplicacion.

## Especificacion OpenAPI

- **Representacion**: documento JSON OpenAPI v3 generado en `/v3/api-docs`.
- **Atributos**: version OpenAPI, `info.title`, `info.version`, `info.description`, paths, operaciones, parametros, request bodies, respuestas y `components.schemas`.
- **Origen**: bean `OpenAPI` en `configuration` mas el inventario de controllers y DTOs anotados.
- **Reglas**: no contiene secretos, credenciales, tokens, contrasenas, URLs internas ni detalles de implementacion.
- **Ciclo**: se genera al consultar la API y se valida en pruebas HTTP; no se persiste como dato de negocio.

## Interfaz Swagger UI

- **Representacion**: HTML interactivo en `/swagger-ui.html`.
- **Atributos**: referencia a `/v3/api-docs`, lista de operaciones, modelos y acciones de prueba.
- **Reglas**: acceso publico sin autenticacion en todos los entornos; solo permite probar operaciones que el backend exponga realmente.

## Health check Actuator

- **Representacion**: endpoint GET `/actuator/health`.
- **Atributos observables**: estado general `UP`, `DOWN`, `OUT_OF_SERVICE` o `UNKNOWN`; detalles ocultos por defecto.
- **Relaciones**: puede incluir el indicador automatico `db` cuando existe un `DataSource`; no se deben exponer credenciales ni detalles de conexion.
- **Reglas**: solo el endpoint `health` se expone por HTTP; respuestas esperadas: 200 para `UP` y 503 para `DOWN` u `OUT_OF_SERVICE`.
- **Ciclo**: el estado se calcula en cada consulta a partir de los `HealthIndicator` disponibles.

## Respuesta de error de dominio

- **Representacion**: `RespuestaError` serializada en respuestas HTTP de los controllers existentes.
- **Atributos**: `codigo`, `mensaje`, `detalle` y `timestamp`.
- **Relaciones**: `ControladorErrores` produce la respuesta para excepciones de validacion, dominio y errores no controlados.
- **Reglas de documentacion**: 400 para validacion, 422 para `ExcepcionDominio` y 500 para errores no controlados, usando `RespuestaError` como esquema.

## DTOs de request y response

- No hay DTOs REST de negocio actualmente.
- Si aparecen controllers antes de implementar la feature, cada DTO existente sera inventariado y documentado con `@Schema` solo cuando la inferencia automatica no alcance para expresar tipo, obligatoriedad, enumeracion, paginacion o descripcion.
- No se crean DTOs nuevos para inventar endpoints de negocio.
