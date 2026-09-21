# Investigacion: Documentacion de la API REST con OpenAPI/Swagger v3

## Hallazgos del repositorio

- `backend/build.gradle` usa Spring Boot `4.1.1`, Java 21 y Spring MVC (`spring-boot-starter-webmvc`).
- No existen controllers REST de negocio ni mappings HTTP en `backend/src/main/java`; `controller/dto/RespuestaDto.java` es una interfaz vacia.
- `security/ConfiguracionSeguridad.java` deshabilita CSRF y aplica `permitAll` a cualquier request.
- `exceptions/ControladorErrores.java` mapea validacion a 400, dominio a 422 y errores no controlados a 500. El cuerpo comun es `RespuestaError`.
- Las pruebas de contexto usan H2 y ya existe una validacion que exige exactamente una `application.properties`.

## Decision: Dependencia OpenAPI

**Decision**: usar `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1`.

**Rationale**: la documentacion oficial de springdoc indica soporte para Spring Boot 4, recomienda la linea 3.x y confirma que el starter WebMVC UI publica `/swagger-ui.html` y `/v3/api-docs`. Incluye las anotaciones `@Operation`, `@ApiResponse`, `@Schema` y `@Tag`, por lo que no hace falta un stack alternativo.

**Alternatives considered**:

- `springdoc-openapi-starter-webmvc-api`: descartado porque no incluye Swagger UI, requerida por la feature.
- Springfox o una documentacion manual: descartados por no ser el stack pedido y por duplicar el contrato generado.
- Plugin de Gradle para generar un archivo estatico: fuera de alcance; la necesidad es documentacion runtime interactiva.

## Decision: Actuator y health check

**Decision**: agregar `org.springframework.boot:spring-boot-starter-actuator` sin version explicita y exponer solo `health` bajo `/actuator/health`.

**Rationale**: el BOM de Spring Boot `4.1.1` gestiona la version compatible. Spring Boot expone `health` por defecto, asigna 200 a `UP` y 503 a `DOWN`/`OUT_OF_SERVICE`. La respuesta oculta detalles por defecto, lo que reduce la exposicion publica acordada.

**Alternatives considered**:

- Controller `HealthController` propio: descartado porque duplicaria el mecanismo estandar y no seria necesario para documentar Actuator.
- Exponer `metrics`, `info` o todos los endpoints: descartado por el principio de minimo privilegio y porque metricas detalladas quedan fuera del alcance.
- Puerto de management separado: descartado porque la spec fija `/actuator/health` en la superficie publica existente y no solicita otro puerto.

## Decision: Documentacion de Actuator en OpenAPI

**Decision**: habilitar `springdoc.show-actuator=true` y verificar que el grupo de Actuator incluya `/actuator/health`.

**Rationale**: springdoc documenta que esta propiedad agrega endpoints Actuator a Swagger UI. No se creara un endpoint espejo ni una especificacion manual.

**Alternatives considered**:

- Agregar manualmente el path de health al bean OpenAPI: descartado porque puede divergir del contrato runtime de Actuator.
- Exponer Swagger sobre el puerto de management: descartado porque no es necesario y complicaria la prueba de acceso publico en un unico servidor.

## Decision: Seguridad

**Decision**: mantener una sola `SecurityFilterChain` y declarar `permitAll` explicito para Swagger UI, OpenAPI y health; las rutas restantes mantienen su regla actual.

**Rationale**: Spring Security esta presente y la configuracion propia hace que la auto-configuracion de Actuator no controle el acceso. Los matchers explicitos documentan la intencion y evitan depender solo de `anyRequest().permitAll()`.

**Alternatives considered**:

- Crear una segunda cadena exclusiva para Actuator: descartado porque el orden de cadenas y el back-off de auto-configuracion agregan riesgo sin beneficio para tres rutas publicas.
- Proteger Swagger o health con autenticacion: descartado por la aclaracion aprobada de la spec.

## Decision: Errores y DTOs

**Decision**: documentar las respuestas 400, 422 y 500 con `RespuestaError` y anotaciones sobre controllers reales; no modificar `ControladorErrores`.

**Rationale**: `@ControllerAdvice` es la fuente actual de verdad. Las anotaciones completan el contrato sin duplicar la logica de excepciones.

**Alternatives considered**:

- Crear una respuesta de error OpenAPI distinta: descartado por duplicar el contrato existente.
- Anotar entidades de dominio en vez de DTOs: descartado porque mezcla contrato de transporte con modelo de dominio.

## Fuentes consultadas

- Documentacion oficial de springdoc: https://springdoc.org/ (version 3.1.1, compatibilidad Boot 4, starter WebMVC UI, `show-actuator`, annotations y propiedades).
- Documentacion oficial de Spring Boot Actuator: https://docs.spring.io/spring-boot/reference/actuator/endpoints.html (exposicion, seguridad, health status HTTP y ocultamiento de detalles).
