# Feature Specification: Observabilidad del backend con Actuator y métricas Prometheus

**Feature Branch**: `012-observabilidad-actuator-prometheus`

**Created**: 2026-11-24

**Status**: Draft

**Input**: User description: "Observabilidad del backend con Spring Boot Actuator y métricas Prometheus. Corresponde a los ítems de la Entrega 3 'Configurar spring-boot-actuators (Endpoints de Monitoreo)' y 'Configurar spring-boot-prometheus (Métricas)'. Objetivo: que un administrador pueda monitorear el estado y las métricas técnicas de la aplicación, y que esas métricas queden disponibles en formato Prometheus."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar la salud de la aplicación según el nivel de acceso (Priority: P1)

Como operador o administrador, quiero consultar el estado de salud de la
aplicación para saber si está disponible. Cualquier persona puede ver el estado
global, pero solo un administrador autenticado puede ver el detalle de cada
componente (base de datos, espacio en disco, etc.) para diagnosticar fallas.

**Why this priority**: El estado de salud es la señal mínima de monitoreo y ya
existe de forma pública; esta historia agrega el detalle para administradores
sin aumentar la exposición hacia usuarios anónimos.

**Independent Test**: Consultar el endpoint de salud sin credenciales y
verificar que solo informa el estado global; repetir con un token de
administrador y verificar que informa además el estado de cada componente.

**Acceptance Scenarios**:

1. **Given** la aplicación en funcionamiento, **When** un usuario anónimo
   consulta `/actuator/health`, **Then** recibe una respuesta exitosa que
   informa únicamente el estado global (`UP` o `DOWN`), sin detalle de
   componentes.
2. **Given** un administrador autenticado, **When** consulta
   `/actuator/health`, **Then** recibe el estado global y el estado
   individual de cada componente monitoreado, incluida la base de datos.
3. **Given** un usuario autenticado con `ROLE_USUARIO`, **When** consulta
   `/actuator/health`, **Then** recibe solo el estado global, igual que un
   anónimo.
4. **Given** la base de datos no disponible, **When** cualquier usuario
   consulta `/actuator/health`, **Then** el estado global informado es `DOWN`
   y la respuesta indica que el servicio no está disponible.

---

### User Story 2 - Consultar métricas técnicas e información de la aplicación (Priority: P1)

Como administrador, quiero consultar la información de la aplicación (nombre,
versión y datos de build) y las métricas técnicas disponibles, incluido el
valor de una métrica puntual, para entender qué versión está desplegada y cómo
se está comportando.

**Why this priority**: Es el núcleo del ítem "Endpoints de Monitoreo" de la
Entrega 3 y permite al administrador diagnosticar sin acceder al servidor.

**Independent Test**: Con un token de administrador, consultar
`/actuator/info`, `/actuator/metrics` y `/actuator/metrics/{nombre}` y
verificar que devuelven la información esperada.

**Acceptance Scenarios**:

1. **Given** un administrador autenticado, **When** consulta
   `/actuator/info`, **Then** recibe el nombre de la aplicación, su versión y
   los datos de build (por ejemplo, fecha de construcción), sin secretos.
2. **Given** un administrador autenticado, **When** consulta
   `/actuator/metrics`, **Then** recibe el listado de nombres de métricas
   disponibles.
3. **Given** un administrador autenticado y una métrica existente, **When**
   consulta `/actuator/metrics/{nombre}`, **Then** recibe el valor actual de
   esa métrica y las etiquetas disponibles para filtrarla.
4. **Given** un administrador autenticado, **When** consulta
   `/actuator/metrics/{nombre}` con un nombre inexistente, **Then** recibe
   una respuesta de recurso no encontrado.

---

### User Story 3 - Obtener métricas en formato Prometheus (Priority: P1)

Como administrador, quiero obtener todas las métricas técnicas de la
aplicación en el formato de texto de Prometheus para poder derivar la latencia
y la tasa de error por endpoint y dejar la aplicación lista para ser
monitoreada por un servidor Prometheus.

**Why this priority**: Es el ítem "Métricas" de la Entrega 3 y cumple el
requisito del documento de visión (5.1) de exponer latencia y tasa de error.

**Independent Test**: Ejecutar algunos requests contra la API (exitosos y con
error), consultar `/actuator/prometheus` con un token de administrador y
verificar que el resultado contiene series de requests HTTP, de la JVM, del
proceso y del pool de conexiones, todas con la etiqueta común de la aplicación.

**Acceptance Scenarios**:

1. **Given** un administrador autenticado y requests previos a la API,
   **When** consulta `/actuator/prometheus`, **Then** recibe texto en formato
   Prometheus con series de requests HTTP etiquetadas por método, URI, status
   y outcome.
2. **Given** la misma respuesta, **When** se inspeccionan las series, **Then**
   contiene métricas de memoria y threads de la JVM, de CPU y tiempo de
   actividad del proceso, y de conexiones activas, inactivas y pendientes del
   pool de base de datos.
3. **Given** la misma respuesta, **When** se inspecciona cualquier serie,
   **Then** todas incluyen una etiqueta común con el nombre de la aplicación.
4. **Given** requests exitosos y requests con error a un mismo endpoint,
   **When** se consultan sus series, **Then** es posible calcular la latencia
   media (tiempo total / cantidad) y la proporción de requests con error de
   ese endpoint.

---

### User Story 4 - Restringir el acceso a la observabilidad (Priority: P1)

Como responsable de seguridad del backend, quiero que solo los
administradores accedan a la información de monitoreo y que ningún otro
endpoint de Actuator quede expuesto, para que la observabilidad no se
convierta en una superficie de filtración de información.

**Why this priority**: La constitution (Requisitos no funcionales >
Observabilidad) lo exige y un endpoint de monitoreo mal protegido puede revelar
configuración, credenciales o la estructura interna de la aplicación.

**Independent Test**: Para cada endpoint protegido, consultar sin token, con
`ROLE_USUARIO` y con `ROLE_ADMIN` y verificar 401, 403 y 200 respectivamente;
consultar endpoints no aprobados y verificar 404. Esta historia se verifica
automáticamente con `observabilidad/ObservabilidadSeguridadTest`.

**Acceptance Scenarios**:

1. **Given** un usuario anónimo, **When** consulta `/actuator/info`,
   `/actuator/metrics`, `/actuator/metrics/{nombre}` o
   `/actuator/prometheus`, **Then** recibe 401 con el formato de error
   vigente del proyecto.
2. **Given** un usuario autenticado con `ROLE_USUARIO`, **When** consulta
   cualquiera de esos endpoints, **Then** recibe 403 con el formato de error
   vigente del proyecto.
3. **Given** un administrador autenticado, **When** consulta cualquiera de
   esos endpoints, **Then** recibe 200.
4. **Given** un administrador autenticado, **When** consulta un endpoint de
   Actuator no aprobado (por ejemplo `/actuator/env`, `/actuator/heapdump`,
   `/actuator/beans` o `/actuator/shutdown`), **Then** recibe 404 porque el
   endpoint no existe. Un anónimo o un `ROLE_USUARIO` recibe 401 o 403 antes
   de llegar a la ruta, sin que se revele si el endpoint existe.
5. **Given** cualquier respuesta de los endpoints de monitoreo, **When** se
   inspecciona su contenido, **Then** no contiene credenciales, el secreto
   JWT, la URL ni la contraseña de la base de datos, claves de APIs externas
   ni datos personales.

---

### User Story 5 - Descubrir los endpoints de monitoreo en Swagger (Priority: P3)

Como desarrollador del equipo, quiero ver los endpoints de monitoreo
expuestos en la documentación Swagger para saber cuáles existen y cómo
consultarlos.

**Why this priority**: Mejora la descubribilidad, pero no es necesaria para
que el monitoreo funcione.

**Independent Test**: Abrir Swagger UI y verificar que aparecen `health`,
`info`, `metrics` y `prometheus`, y que no aparece ningún endpoint de Actuator
no aprobado.

**Acceptance Scenarios**:

1. **Given** la documentación Swagger, **When** un desarrollador la
   consulta, **Then** encuentra los cuatro endpoints de monitoreo expuestos.
2. **Given** la documentación Swagger, **When** se buscan endpoints de
   Actuator no aprobados, **Then** no aparece ninguno.

### Edge Cases

- **Token inválido o vencido en `/actuator/health`**: el endpoint sigue
  siendo público; la respuesta es la misma que para un anónimo (solo estado
  global), sin detalle de componentes.
- **Consulta de un componente individual de salud** (por ejemplo
  `/actuator/health/db`) por un usuario no administrador: no debe revelar el
  estado ni el detalle del componente.
- **Índice de Actuator** (`/actuator`): lista enlaces a los endpoints
  expuestos; queda protegido con `ROLE_ADMIN`, igual que los demás endpoints
  no públicos.
- **Aplicación con un componente caído**: el estado global de salud es
  `DOWN` y se informa como servicio no disponible, sin revelar mensajes de
  error internos a usuarios no administradores.
- **Endpoint sin requests previos**: si un endpoint todavía no recibió
  requests, puede no tener series HTTP; no se considera un error.
- **URIs con variables de ruta** (por ejemplo `/players/{id}`): las series
  HTTP se agrupan por la plantilla de URI y no por cada valor concreto, para
  no generar una serie por identificador.
- **Requests a rutas inexistentes**: se registran con status 404 sin crear
  una serie por cada ruta inventada.
- **Configuración por entorno**: ninguna variable de entorno o configuración
  externa debe permitir exponer endpoints de Actuator fuera de los cuatro
  aprobados.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST exponer por HTTP exactamente cuatro endpoints
  de monitoreo bajo `/actuator`: `health`, `info`, `metrics` y `prometheus`.
- **FR-002**: Cualquier otro endpoint de Actuator (`env`, `beans`,
  `configprops`, `heapdump`, `threaddump`, `loggers`, `mappings`,
  `shutdown`, etc.) MUST NOT existir: un administrador recibe 404. Un
  anónimo recibe 401 y un `ROLE_USUARIO` 403, porque todo el espacio
  `/actuator` salvo `health` exige `ROLE_ADMIN`.
- **FR-003**: La lista de endpoints expuestos MUST quedar fija en la
  configuración base del proyecto. La variable de entorno documentada
  para la exposición (`MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`) MUST NOT
  poder ampliar esa lista. Riesgo residual aceptado: sobrescribir otras
  propiedades `management.*` (por ejemplo, el acceso por endpoint) es un
  cambio de configuración deliberado y fuera de este control. Aun en ese
  caso, todo `/actuator/**` salvo `health` sigue exigiendo `ROLE_ADMIN`.
- **FR-004**: `/actuator/health` MUST ser público y, para usuarios anónimos
  o sin `ROLE_ADMIN`, MUST informar únicamente el estado global.
- **FR-005**: `/actuator/health` MUST informar el detalle de cada componente
  monitoreado (al menos base de datos y espacio en disco) solo a usuarios
  autenticados con `ROLE_ADMIN`.
- **FR-006**: `/actuator`, `/actuator/info`, `/actuator/metrics`,
  `/actuator/metrics/{nombre}` y `/actuator/prometheus` MUST requerir
  `ROLE_ADMIN` mediante el JWT existente.
- **FR-007**: Un request sin token a un endpoint protegido MUST responder 401
  y uno con `ROLE_USUARIO` MUST responder 403, ambos con el formato de error
  vigente del proyecto.
- **FR-008**: `/actuator/info` MUST informar el nombre de la aplicación, su
  versión y datos de build, y MUST NOT incluir secretos ni valores de
  configuración.
- **FR-009**: `/actuator/metrics` MUST listar los nombres de las métricas
  disponibles y `/actuator/metrics/{nombre}` MUST devolver el valor actual y
  las etiquetas de la métrica solicitada, o 404 si no existe.
- **FR-010**: `/actuator/prometheus` MUST devolver las métricas en formato de
  texto Prometheus.
- **FR-011**: Las métricas expuestas MUST incluir como mínimo: memoria,
  threads y recolección de basura de la JVM; CPU y tiempo de actividad del
  proceso y del sistema; conexiones del pool de base de datos; y requests
  HTTP del servidor.
- **FR-012**: Las métricas de requests HTTP MUST etiquetarse con método, URI
  (plantilla de ruta), status y outcome, y MUST permitir derivar la cantidad,
  el tiempo total y el tiempo máximo de requests por endpoint.
- **FR-013**: Todas las métricas MUST incluir una etiqueta común con el
  nombre de la aplicación.
- **FR-014**: Ninguna respuesta de los endpoints de monitoreo MUST exponer
  credenciales, tokens, el secreto JWT, la URL o la contraseña de la base de
  datos, claves de APIs externas, valores de propiedades de configuración ni
  datos personales.
- **FR-015**: Los cuatro endpoints expuestos MUST aparecer en la
  documentación Swagger existente y ningún endpoint de Actuator no aprobado
  MUST aparecer en ella.
- **FR-016**: La feature MUST preservar la lógica de negocio, los contratos
  funcionales y las reglas de acceso de los endpoints existentes.
- **FR-017**: La verificación automática de las reglas de acceso MUST
  realizarse con el único test de integración admitido por el Principio VII,
  `observabilidad/ObservabilidadSeguridadTest`, sin agregar otros tests de
  `configuration` o `security`.

### Key Entities *(include if feature involves data)*

- **Estado de salud**: Resultado global de disponibilidad de la aplicación
  (`UP`/`DOWN`) y, para administradores, el estado de cada componente
  monitoreado.
- **Información de la aplicación**: Nombre, versión y datos de build de la
  instancia desplegada.
- **Métrica**: Medición técnica con nombre, valor y etiquetas (por ejemplo
  método, URI, status, outcome y aplicación).
- **Exposición Prometheus**: Representación en texto de todas las métricas,
  consumible por un servidor Prometheus o consultable manualmente.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un administrador obtiene respuesta exitosa en el 100% de los
  cuatro endpoints de monitoreo; un usuario anónimo solo en `health`; un
  usuario con `ROLE_USUARIO` recibe acceso denegado en el 100% de los
  endpoints protegidos.
- **SC-002**: El 100% de los endpoints de Actuator no aprobados que se
  consulten como administrador (al menos `env`, `heapdump`, `beans`,
  `configprops`, `loggers`, `mappings`, `threaddump` y `shutdown`) responden
  recurso no encontrado, y como anónimo responden acceso no autenticado.
- **SC-003**: Después de ejecutar requests contra la API, la exposición
  Prometheus contiene series de requests HTTP, de la JVM, del proceso y del
  pool de conexiones, y el 100% de las series incluye la etiqueta de la
  aplicación.
- **SC-004**: Un administrador puede calcular la latencia media y la tasa de
  error de cualquier endpoint ya ejecutado usando solo la exposición
  Prometheus, en menos de 5 minutos.
- **SC-005**: Una inspección de las respuestas de los cuatro endpoints, como
  anónimo y como administrador, no encuentra credenciales, el secreto JWT,
  la URL ni la contraseña de la base de datos, ni datos personales.
- **SC-006**: La suite de tests del backend, incluida la verificación de
  arquitectura y `ObservabilidadSeguridadTest`, se completa sin fallos ni
  regresiones.

## Assumptions

- La constitution v2.8.0 ya aprueba Spring Boot Actuator y el registro
  Prometheus de Micrometer, y fija la política de Observabilidad que esta
  feature implementa; no se requiere otra enmienda.
- El backend ya incluye Actuator (feature 003) y expone solo
  `/actuator/health` como público y sin detalles; esta feature amplía esa
  exposición sin cambiar su carácter público.
- La autenticación y los roles (`ROLE_ADMIN`, `ROLE_USUARIO`) se resuelven
  con el JWT y los manejadores de error 401/403 existentes; no se crean roles
  ni mecanismos de autenticación nuevos.
- La configuración se mantiene en el `application.yml` existente, sin
  perfiles nuevos, y las reglas de acceso en `SecurityConfig`.
- La documentación Swagger ya incluye los endpoints de Actuator
  (`springdoc.show-actuator` activo); esta feature solo verifica que
  aparezcan los aprobados.
- Como `/actuator/prometheus` exige un JWT de administrador, un servidor
  Prometheus externo necesitaría un token válido para hacer scraping. Montar
  Prometheus o Grafana queda fuera de alcance; la verificación se hace
  consultando el endpoint manualmente.
- Quedan fuera de alcance: métricas de negocio propias y endpoint de métricas
  avanzadas; percentiles e histogramas de latencia; auditoría de web
  services, logs estructurados y Correlation ID; optimización y caché del
  ranking. Se abordarán en features posteriores de la Entrega 3.
- Referencias técnicas de apoyo: https://www.baeldung.com/spring-boot-actuators
  y https://www.baeldung.com/spring-boot-prometheus.
