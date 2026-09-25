# Feature Specification: Documentacion de la API REST con OpenAPI/Swagger v3

**Feature Branch**: `[003-documentar-api-openapi]`

**Created**: 2026-09-21

**Status**: Draft

**Input**: User description: "Documentacion de la API REST con OpenAPI/Swagger v3 para habilitar documentacion interactiva y navegable de la REST API del backend, con un endpoint de health check documentado y sin modificar la logica de negocio."

## Clarifications

### Session 2026-09-21

- Q: ¿Swagger UI y `/v3/api-docs` seran publicos o requeriran autenticacion, y en que entornos estaran habilitados? → A: Seran publicos, sin autenticacion, en todos los entornos.
- Q: ¿Se reutilizara un health check existente o se incorporara uno nuevo dentro del mecanismo de observabilidad aprobado? → A: Se incorporara un health check nuevo mediante Spring Boot Actuator en `/actuator/health`.

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Explorar y consultar la API existente (Priority: P1)

Como desarrollador del equipo, necesito consultar una documentacion visual y navegable de la API REST para comprender sus endpoints, sus DTOs, sus respuestas, sus codigos de estado y sus errores de dominio documentados.

**Why this priority**: La documentacion navegable reduce el tiempo necesario para integrar consumidores y permite validar rapidamente el contrato observable de la API.

**Independent Test**: Iniciar el backend, abrir la interfaz de documentacion y comprobar que se listan todos los endpoints existentes con sus DTOs de request y response, codigos de estado y errores documentados, sin exponer detalles internos.

**Acceptance Scenarios**:

1. **Given** el backend iniciado, **When** un desarrollador accede a la interfaz de documentacion, **Then** puede navegar la especificacion OpenAPI y visualizar los endpoints REST existentes.
2. **Given** un endpoint existente con DTO de request, **When** el desarrollador lo consulta desde la interfaz, **Then** puede identificar los campos de entrada, las respuestas esperadas y los errores de dominio documentados.
3. **Given** la especificacion OpenAPI generada, **When** el desarrollador revisa un endpoint existente, **Then** puede consultar su contrato documentado sin necesidad de ejecutar solicitudes contra el backend.

---

### User Story 2 - Consultar el estado de salud de la API (Priority: P1)

Como desarrollador u operador, necesito encontrar un health check documentado para verificar rapidamente si la API responde correctamente.

**Why this priority**: Un punto de comprobacion claro permite distinguir un backend disponible de uno que requiere diagnostico antes de probar otros endpoints.

**Independent Test**: Abrir la documentacion, localizar el health check, ejecutarlo y comprobar que su respuesta coincide con el estado operativo del backend.

**Acceptance Scenarios**:

1. **Given** el backend disponible, **When** se ejecuta el health check desde la interfaz, **Then** responde exitosamente e informa el estado de salud esperado.
2. **Given** una condicion de indisponibilidad contemplada por el mecanismo de observabilidad existente, **When** se consulta el health check, **Then** la respuesta comunica un estado no saludable sin revelar informacion sensible.

---

### User Story 3 - Mantener documentacion segura y acotada (Priority: P2)

Como responsable del backend, necesito que la documentacion muestre solo el contrato publico necesario y respete las reglas de acceso aprobadas para evitar filtrar implementacion interna o informacion sensible.

**Why this priority**: La documentacion debe ser util para integrar consumidores sin convertirse en una superficie de exposicion de detalles internos o de seguridad.

**Independent Test**: Revisar la especificacion generada y la interfaz en cada entorno habilitado, comprobar que no contienen secretos ni detalles de implementacion, y validar el comportamiento de acceso definido por el equipo.

**Acceptance Scenarios**:

1. **Given** la especificacion generada, **When** se inspeccionan sus modelos, operaciones y respuestas, **Then** solo aparecen contratos de API y no secretos, tokens, credenciales ni detalles internos.
2. **Given** cualquier entorno habilitado del backend, **When** un desarrollador accede a la especificacion o a Swagger UI, **Then** puede hacerlo sin autenticacion.

### Edge Cases

- La interfaz debe mostrar un resultado comprensible cuando un endpoint existente no tenga request body o no requiera parametros.
- Los errores de dominio traducidos por el manejo centralizado de excepciones deben aparecer con sus codigos y esquemas de respuesta sin duplicar reglas de negocio.
- Un DTO con campos opcionales, enumeraciones o paginacion debe conservar en la documentacion la semantica observable del endpoint.
- La documentacion no debe incluir credenciales, valores de tokens, datos de conexion, trazas internas ni informacion sensible de seguridad.
- El health check nuevo se expone mediante Spring Boot Actuator en `/actuator/health` y se documenta en Swagger como parte de esta feature.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: El backend MUST ofrecer una especificacion navegable y actualizada de su API REST mediante OpenAPI v3 y una interfaz visual Swagger UI, sin incorporar un stack de documentacion alternativo.
- **FR-002**: La especificacion MUST incluir todos los controllers y endpoints REST existentes dentro del alcance de esta feature, sin inventar endpoints de negocio nuevos.
- **FR-003**: Cada endpoint documentado MUST describir su proposito, parametros o request DTO, response DTO, codigos de estado posibles y errores de dominio mapeados por el manejo centralizado de excepciones.
- **FR-004**: La configuracion de la documentacion, incluyendo rutas, metadatos de la API, beans y reglas de acceso, MUST residir en el paquete `configuration` y utilizar unicamente la `application.properties` existente cuando se requiera configuracion declarativa.
- **FR-005**: La documentacion MUST incluir un health check basado en el mecanismo de observabilidad y health checks definido por el proyecto, con su respuesta exitosa y sus estados no saludables contemplados.
- **FR-006**: El acceso a la especificacion y a la interfaz visual MUST ser publico, sin autenticacion, en todos los entornos del backend.
- **FR-007**: La politica de acceso MUST evitar la exposicion de secretos, tokens, credenciales, detalles de implementacion interna y datos sensibles de seguridad.
- **FR-008**: El proyecto MUST incorporar un health check nuevo mediante Spring Boot Actuator en `/actuator/health`, documentarlo en Swagger y exponer sus estados saludable y no saludable sin informacion sensible.
- **FR-009**: La implementacion de esta feature MUST preservar la logica de negocio, los contratos funcionales y las validaciones existentes de controllers, services y models.
- **FR-010**: La feature MUST mantener la unica configuracion base del proyecto y no crear archivos de configuracion adicionales ni perfiles nuevos.
- **FR-011**: Los nombres de clases, beans y metodos nuevos relacionados con esta feature MUST estar en espanol tecnico y ASCII, excepto nombres oficiales como OpenAPI, Swagger, REST y springdoc.

### Key Entities _(include if feature involves data)_

- **Especificacion OpenAPI**: Representa el contrato navegable de la API REST, incluyendo operaciones, parametros, esquemas, respuestas y errores documentados.
- **Interfaz Swagger UI**: Representa la vista navegable desde la que un desarrollador consulta las operaciones, DTOs, respuestas y errores documentados.
- **Health check**: Representa el resultado observable del estado operativo de la API, incluyendo estados saludables y no saludables.
- **Contrato de error de dominio**: Representa las respuestas HTTP y esquemas con los que el manejo centralizado comunica errores de negocio.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: El 100% de los endpoints REST existentes incluidos en el alcance aparece en la especificacion OpenAPI y puede localizarse desde la interfaz visual.
- **SC-002**: Para cada endpoint documentado, el 100% de los DTOs de request y response, codigos de estado y errores de dominio contemplados por el contrato son visibles y verificables por un desarrollador.
- **SC-003**: Un desarrollador puede abrir la interfaz, localizar un endpoint y consultar su contrato documentado en menos de 2 minutos desde el inicio del backend.
- **SC-004**: El health check aparece en la interfaz, responde correctamente en un entorno saludable y comunica un estado no saludable cuando corresponde, sin datos sensibles.
- **SC-005**: Una inspeccion de la especificacion y de la interfaz no encuentra secretos, credenciales, tokens ni detalles de implementacion interna en ninguno de los entornos habilitados.
- **SC-006**: La suite de tests existente completa sin regresiones despues de habilitar la documentacion y el health check.
- **SC-007**: El proyecto conserva exactamente una configuracion base `application.properties` y no incorpora archivos de configuracion o perfiles adicionales para esta feature.

## Assumptions

- La API REST y sus controllers existentes son la unica fuente de operaciones a documentar en esta etapa.
- La dependencia aprobada para OpenAPI v3 es springdoc-openapi; no se agregara una segunda tecnologia de documentacion.
- La configuracion de metadatos, rutas, beans y seguridad se resolvera en `configuration`, respetando la arquitectura vigente.
- Swagger UI y `/v3/api-docs` seran publicos y no requeriran autenticacion en todos los entornos.
- Los DTOs y el manejo centralizado de excepciones existentes se consideran la fuente de verdad para documentar contratos y errores; esta feature no modifica sus reglas.
- El health check se incorporara mediante Spring Boot Actuator en `/actuator/health`, como mecanismo estandar compatible con la observabilidad del proyecto.
- La feature no incluye versionado adicional de la API, documentacion de endpoints inexistentes ni cambios de negocio.
