# Implementation Plan: Documentacion de la API REST con OpenAPI/Swagger v3

**Branch**: `[003-documentar-api-openapi]` | **Date**: 2026-09-21 | **Spec**: [spec.md](spec.md)

**Input**: Especificacion aprobada para documentar la API REST con OpenAPI v3, Swagger UI y Spring Boot Actuator.

## Resumen

Se incorporara `springdoc-openapi-starter-webmvc-ui:3.1.1` para generar OpenAPI v3 y servir Swagger UI sobre Spring MVC, y `spring-boot-starter-actuator` usando la version gestionada por Spring Boot `4.1.1`. La configuracion programatica nueva vivira en `configuration`, la configuracion declarativa se limitara a la unica `application.properties` existente y la seguridad dejara publicos unicamente los recursos de documentacion y health check requeridos.

El relevamiento actual no encontro `@RestController` ni endpoints de negocio implementados. Por eso la primera entrega documentara la superficie de Actuator y verificara el inventario de controllers antes de agregar anotaciones; no se crearan endpoints de negocio para completar artificialmente la documentacion.

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.7.1

**Primary Dependencies**: `springdoc-openapi-starter-webmvc-ui:3.1.1`; `spring-boot-starter-actuator` sin version explicita, gestionada por Spring Boot 4.1.1; Spring MVC y Spring Security existentes.

**Storage**: PostgreSQL en ejecucion normal; H2 en las pruebas de contexto existentes.

**Testing**: JUnit 5, AssertJ, Mockito y pruebas HTTP con MockMvc mediante `spring-boot-starter-webmvc-test`.

**Target Platform**: Servicio web Spring Boot ejecutado en JVM 21 y CI sobre Linux.

**Project Type**: Backend web REST MVC.

**Performance Goals**: Swagger UI, `/v3/api-docs` y `/actuator/health` deben responder en menos de 2 segundos en condiciones normales de desarrollo; no se fija un objetivo de throughput nuevo.

**Constraints**: una unica `application.properties`; sin perfiles nuevos; sin cambios de logica de negocio; acceso publico solo a las rutas aprobadas; no exponer detalles de salud mediante `show-details=always`.

**Scale/Scope**: todos los controllers REST existentes al momento de implementar, mas los endpoints `/swagger-ui.html`, `/v3/api-docs` y `/actuator/health`; actualmente no hay controllers REST de negocio.

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

Los gates pasan:

- Principio I: las clases de configuracion y beans de OpenAPI/Actuator se ubican en `backend/src/main/java/unq/losrecursionistas/backend/configuration`; las anotaciones OpenAPI, si aparecen controllers, permanecen en `controller` y sus DTOs.
- Principio V: las respuestas de error existentes continuan bajo `exceptions`; el plan solo las referencia en OpenAPI y no duplica manejo de excepciones.
- Principio VII: se agregan pruebas de contexto y HTTP sin modificar ni borrar tests existentes.
- Principio IX: las decisiones ambiguas fueron resueltas en la spec: acceso publico en todos los entornos y health check nuevo con Actuator.
- Principio X: clases, beans, metodos y propiedades nuevas usan nombres ASCII en espanol tecnico; la documentacion del feature esta en espanol.
- Configuracion unica: solo se modifica `backend/src/main/resources/application.properties`; no se crean perfiles ni archivos `application-*.properties`.
- Seguridad: se permiten explicitamente Swagger UI, OpenAPI y health; no se cambia la politica de endpoints de negocio mas alla de las rutas indicadas.

No hay violaciones constitucionales ni gates bloqueados.

## Estructura del proyecto

### Documentacion de esta feature

```text
specs/003-documentar-api-openapi/
├── plan.md              # Salida del comando /speckit-plan
├── research.md          # Investigacion de Phase 0
├── data-model.md        # Modelo de datos de Phase 1
├── quickstart.md        # Guia de validacion de Phase 1
├── contracts/           # Contratos de Phase 1
└── tasks.md             # Salida futura de /speckit-tasks
```

### Codigo fuente

```text
backend/
├── src/
│   ├── main/java/unq/losrecursionistas/backend/
│   │   ├── configuration/
│   │   │   └── ConfiguracionOpenApi.java
│   │   ├── controller/dto/
│   │   ├── exceptions/
│   │   └── security/ConfiguracionSeguridad.java
│   ├── main/resources/application.properties
│   └── test/java/unq/losrecursionistas/backend/
│       ├── ConfiguracionDocumentacionTest.java
│       └── BackendApplicationTests.java
└── build.gradle

specs/003-documentar-api-openapi/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
  ├── README.md
  └── api-publica.md
```

**Structure Decision**: Se mantiene el backend Gradle existente. La configuracion nueva se concentra en `configuration`; `ConfiguracionSeguridad` permanece en `security` porque controla la cadena de filtros. Los controllers y DTOs existentes solo reciben anotaciones OpenAPI cuando el inventario real los encuentre.

## Decisiones de diseno

### Dependencias

- Agregar `implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1'`. Esta version soporta Spring Boot 4.x y contiene Swagger UI, anotaciones Swagger y generacion de `/v3/api-docs`.
- Agregar `implementation 'org.springframework.boot:spring-boot-starter-actuator'` sin version, para que el plugin de Spring Boot 4.1.1 gestione el conjunto compatible.
- No agregar el starter `springdoc-openapi-starter-webmvc-api` por separado: el starter UI ya incluye la superficie API requerida.
- No agregar `micrometer-registry-prometheus`: las metricas quedan fuera de alcance de esta feature; Actuator se incorpora para health y su soporte estandar futuro.

### Configuracion y seguridad

- Crear `configuration/ConfiguracionOpenApi.java` con un bean `OpenAPI` que defina titulo, version y descripcion en espanol tecnico, sin secretos, credenciales ni URLs internas.
- No definir `SecurityScheme` de tipo bearer/JWT ni habilitar un flujo `Authorize` en Swagger UI: la feature solo requiere consultar y navegar el contrato documentado.
- En `application.properties`, mantener los defaults `/swagger-ui.html` y `/v3/api-docs` salvo que una prueba de colision exija fijarlos explicitamente; agregar `springdoc.show-actuator=true` para incluir Actuator en la especificacion.
- Exponer solo `health` por HTTP con `management.endpoints.web.exposure.include=health`. Mantener los detalles de salud ocultos por defecto, salvo que una prueba demuestre una necesidad aprobada.
- Ajustar `security/ConfiguracionSeguridad.java` con `permitAll` explicito para `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs`, `/v3/api-docs/**` y `/actuator/health`. Las rutas no listadas conservan la regla vigente y no reciben una relajacion general nueva.
- Evitar una segunda `SecurityFilterChain`: la configuracion actual ya controla la cadena completa y una segunda cadena podria cambiar el orden y dejar rutas sin proteccion involuntariamente.

### Documentacion de controllers, DTOs y errores

- Antes de editar, inventariar `@RestController`, `@Controller` con `@ResponseBody`, mappings y DTOs. El estado actual no contiene controllers REST de negocio.
- Para cada controller que exista al implementar, agregar `@Tag` a nivel de clase, `@Operation` por operacion y `@ApiResponse` para cada respuesta observable, incluyendo los codigos de `ControladorErrores`: 400 para validacion, 422 para dominio y 500 para error no controlado.
- Asociar `RespuestaError` mediante `@Content(schema = @Schema(implementation = RespuestaError.class))` en las respuestas de error, sin modificar `ControladorErrores` ni crear handlers alternativos.
- Usar `@Schema` solo en DTOs de request/response existentes cuando la inferencia no exponga correctamente campos, obligatoriedad, enumeraciones o paginacion. No anotar entidades de dominio para resolver problemas de contrato de transporte.
- Configurar `springdoc.override-with-generic-response` solo si el comportamiento automatico de `@ControllerAdvice` no refleja las respuestas existentes; preferir las anotaciones explicitas para evitar duplicados.
- Con `springdoc.show-actuator=true`, verificar que `/actuator/health` aparezca en una agrupacion de Actuator. No crear un controller espejo para health.

### Testing

- Agregar una prueba HTTP con MockMvc que confirme 200 sin autenticacion para `/swagger-ui.html`, `/v3/api-docs` y `/actuator/health`.
- Validar que el JSON OpenAPI contenga `openapi` 3.x, metadatos definidos, la operacion de health y los endpoints, DTOs, codigos de estado y errores documentados de cualquier controller inventariado.
- Verificar que Swagger UI permita consultar y navegar esos contratos sin exigir configuracion de autenticacion; no probar desde la interfaz solicitudes autenticadas ni ejecuciones de endpoints de negocio.
- Validar que `/actuator/health` no exponga detalles de datasource por defecto y que una indisponibilidad se traduzca en el estado HTTP esperado por Actuator, normalmente 503 para `DOWN`.
- Mantener `BackendApplicationTests` con H2 y ajustar solo propiedades de prueba si el starter Actuator cambia el arranque; no sustituir ni eliminar pruebas existentes.
- Ejecutar `./gradlew clean test` y la validacion de infraestructura para comprobar dependencias, arquitectura, configuracion unica e identificadores ASCII.

## Plan de trabajo

### 4.1 Configuracion de Swagger/Actuator

1. Agregar las dos dependencias en `backend/build.gradle` con las versiones y gestion indicadas.
2. Crear `configuration/ConfiguracionOpenApi.java` con el bean de metadatos.
3. Agregar en `application.properties` solo las propiedades necesarias para `springdoc.show-actuator` y exposicion de `health`, sin credenciales nuevas ni perfiles.
4. Actualizar `security/ConfiguracionSeguridad.java` con matchers publicos explicitos para Swagger, OpenAPI y health, preservando la proteccion restante.
5. Crear pruebas HTTP y de configuracion para rutas publicas, metadata, inclusion de health y ausencia de detalles sensibles.

### 4.2 Anotacion de endpoints existentes

1. Repetir el inventario de controllers y DTOs antes de modificar fuentes; si continua vacio, registrar la tarea como verificada sin crear endpoints de negocio.
2. Anotar cada controller real con `@Tag`, `@Operation`, `@ApiResponse` y esquemas DTO solo donde sean necesarios.
3. Documentar las respuestas 400, 422 y 500 del `@ControllerAdvice` con `RespuestaError`, manteniendo el manejo centralizado existente.
4. Regenerar y revisar `/v3/api-docs` para confirmar cobertura del 100% del inventario y ausencia de secretos o detalles de implementacion.
5. Ejecutar la suite completa y actualizar la guia de validacion si el inventario de endpoints cambia, sin agregar pruebas de ejecucion autenticada desde Swagger UI.

## Artefactos de diseno

- [research.md](research.md): decisiones de dependencias, Actuator, seguridad y estado real del backend.
- [data-model.md](data-model.md): entidades contractuales y esquemas observables; no agrega entidades de negocio.
- [contracts/api-publica.md](contracts/api-publica.md): rutas publicas y contratos HTTP verificables.
- [quickstart.md](quickstart.md): pasos reproducibles para levantar, consultar y verificar la documentacion.

## Reevaluacion constitucional posterior al diseno

- Principio I sigue cumplido: configuracion en `configuration`, seguridad en `security`, excepciones en `exceptions` y anotaciones solo sobre controllers/DTOs.
- Principio V sigue cumplido: `ControladorErrores` y `RespuestaError` permanecen como fuente unica para errores de dominio.
- Principio VII sigue cumplido: las pruebas nuevas amplian cobertura HTTP sin modificar ni eliminar pruebas existentes.
- Principio X sigue cumplido: los nombres nuevos propuestos son ASCII y la documentacion del feature esta en espanol tecnico.
- La configuracion unica sigue cumplida: no se agregan perfiles ni archivos fuera de `application.properties`.
- La exposicion publica queda limitada a las rutas aprobadas; no se habilitan endpoints Actuator sensibles ni se exponen detalles de salud.
- El ajuste de alcance no introduce desviaciones: no se agrega `SecurityScheme` bearer/JWT ni capacidad de autenticacion desde Swagger UI; solo se documentan y verifican contratos navegables.

Resultado: gates aprobados despues del diseno, sin desviaciones que documentar.

## Complexity Tracking

No aplica: el Constitution Check no identifica violaciones que requieran justificacion.
