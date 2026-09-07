# Feature Specification: Mercado de Tokens de Jugadores

**Feature Branch**: `001-mercado-tokens-jugadores`

**Created**: 2026-09-06

**Status**: Draft

**Input**: User description: "Generar la especificacion funcional y tecnica del sistema Mercado de Tokens de Jugadores, alineada con la Constitucion v1.4.1 y con los flujos del mercado, valuacion y API REST requeridos."

## 1. Descripcion General y Objetivos de Negocio

Mercado de Tokens de Jugadores es un sistema de simulacion financiera que representa la cotizacion periodica de jugadores de las cinco ligas principales de Europa: Premier League, Bundesliga, La Liga, Serie A y Ligue 1. Cada jugador puede tener hasta 100 tokens negociables. Los usuarios compran y venden tokens, administran un portfolio, consultan la evolucion de precios y revisan un historial inmutable de operaciones.

El backend debe ofrecer APIs REST que permitan administrar el catalogo, consultar cotizaciones y rankings, ejecutar ordenes, consultar portfolios e historiales, recalcular precios y actualizar datos deportivos. El frontend debe ofrecer una experiencia responsiva construida con Vite, React y TypeScript, consumiendo exclusivamente esas APIs mediante un cliente HTTP tipado.

Los objetivos de negocio son:

- Simular un mercado trazable donde cada operacion pueda reconstruirse a partir de su auditoria.
- Convertir metricas deportivas y contexto competitivo en cotizaciones reproducibles.
- Permitir que un usuario complete el ciclo de consulta, compra, seguimiento, venta y revision de resultados.
- Mantener el servicio operativo cuando los proveedores externos no respondan, usando datos locales o cacheados.
- Ofrecer una base extensible para nuevas estrategias de valuacion, ligas, metricas y funcionalidades de portfolio.

## 2. Invariantes del Dominio y Reglas del Mercado

### 2.1 Superusuario financiero e inventario inicial

El sistema debe crear o reconocer un Superusuario Financiero que posea inicialmente 100 tokens por cada jugador del catalogo, a un precio inicial de 1 credito por token. Esta posicion representa el inventario total disponible en el credito base y es la fuente de liquidacion para las compras iniciales de usuarios.

La asignacion inicial debe ser idempotente: repetir la inicializacion no puede duplicar tokens, saldo, jugadores ni operaciones de genesis. Toda asignacion inicial debe quedar identificada en el historial de auditoria.

### 2.2 Compra y venta

Una compra valida, en este orden logico:

1. Que el usuario exista, este habilitado y tenga saldo suficiente para la cotizacion vigente.
2. Que el jugador exista y la cantidad solicitada sea positiva.
3. Que exista disponibilidad de tokens en la posicion vendedora correspondiente.
4. Que la cotizacion vigente sea la aplicable al momento de liquidar la orden.
5. Que la operacion completa pueda actualizar saldo, posiciones e historial como una unica transaccion.

Una venta valida que el usuario sea titular de la cantidad solicitada, que la cantidad sea positiva y que exista una cotizacion vigente. La venta aumenta el saldo del usuario, reduce su posicion y registra el resultado sin modificar operaciones historicas.

Cada compra y venta debe:

- Crear una operacion inmutable con autor, timestamp, tipo, jugador, cantidad, precio unitario, importe total y estados anterior y posterior.
- Actualizar atomicamente las posiciones y saldos de las partes involucradas.
- Conservar el precio y la estrategia/version de cotizacion usada para liquidar.
- Rechazar la operacion completa si alguna validacion falla, sin cambios parciales.
- Emitir un resultado comprensible en espanol y un codigo HTTP coherente.

El portfolio debe mostrar cantidad de tokens, precio promedio de compra, valor actual, ganancia o perdida y el historial ordenado de operaciones. Las cotizaciones historicas y las operaciones financieras son inmutables; nuevas valuaciones crean nuevos registros.

### 2.3 Responsabilidades de validacion por capas

- **DTO de request**: valida forma, tipos, campos obligatorios, limites basicos, trimming y sanitizacion del input externo.
- **Service**: resuelve IDs a entidades existentes, valida disponibilidad, saldo, tenencia, cotizacion vigente y viabilidad de la accion; coordina la transaccion.
- **Model de dominio**: protege invariantes propias como cantidades no negativas, saldo no negativo, posiciones validas, transiciones de estado y consistencia de importes; lanza excepciones propias del dominio.
- **Persistence/Adapters**: garantiza la lectura y escritura consistente del estado, preserva registros inmutables y adapta fuentes externas sin trasladar reglas de negocio a infraestructura.

### 2.4 Cotizacion, ranking y valuacion

Cada cotizacion se deriva de un score reproducible y conserva la estrategia y version usadas. El sistema debe soportar al menos dos estrategias configurables:

- **Rendimiento ponderado**: combina metricas de performance del jugador con pesos configurables.
- **Rendimiento y contexto competitivo**: combina performance con contexto de equipo, liga, posicion y disponibilidad, permitiendo ponderaciones diferenciadas por posicion.

Las metricas y pesos deben ser configurables. Un recalculo no sobreescribe cotizaciones anteriores. El ranking debe ordenar jugadores por el criterio de valuacion vigente y permitir consultar el resultado con sus datos de cotizacion.

### 2.5 Auditoria y seguridad

Toda operacion financiera debe generar un registro de auditoria inmutable con autor, Correlation ID, timestamp, detalle de cambios y estados previo y posterior. La autenticacion y autorizacion deben restringir operaciones segun el usuario y el rol correspondiente. Todo input externo debe validarse y sanitizarse para reducir riesgos de inyeccion, XSS y ataques equivalentes.

## 3. Contrato de API REST (OpenAPI Spec Alignment)

### 3.1 Convenciones

- Los endpoints usan nombres RESTful y recursos en ingles por alineacion con el contrato HTTP acordado.
- Los identificadores del dominio en clases, campos y modelos usan espanol sin acentos ni `n`.
- Los terminos tecnicos y normativos permanecen en ingles, por ejemplo `PlayerQuoteService` y `TokenOrder`.
- La documentacion OpenAPI, los mensajes de error y los textos funcionales se escriben en espanol.
- Las respuestas de error incluyen codigo semantico, mensaje legible, Correlation ID y detalles de validacion cuando corresponda.
- Las operaciones de compra, venta y recalculo requieren autenticacion y autorizacion segun el rol.

### 3.2 Catalogo

#### `GET /players`

Lista jugadores disponibles para el mercado. Debe permitir filtrar por liga, equipo, posicion y estado activo, y devolver identificador, nombre, liga, equipo, posicion, disponibilidad y cotizacion vigente resumida.

Respuestas esperadas: `200` con la lista; `400` si los filtros no son validos.

#### `GET /players/{id}`

Devuelve el detalle de un jugador y su informacion vigente de mercado.

Respuestas esperadas: `200`; `404` si el jugador no existe.

### 3.3 Cotizacion y ranking

#### `GET /players/{id}/quotes`

Devuelve el historial inmutable de cotizaciones del jugador, incluyendo fecha, precio, score, estrategia, version, metricas consideradas y fuente de datos.

Respuestas esperadas: `200`; `404` si el jugador no existe; `400` si el rango de consulta es invalido.

#### `GET /players/ranking`

Devuelve el ranking vigente, con posicion, jugador, score, precio, estrategia/version y fecha de calculo. Debe aceptar parametros de liga, posicion, estrategia y limite dentro de rangos validos.

Respuestas esperadas: `200`; `400` si los parametros son invalidos.

#### `POST /quotes/recalculate`

Solicita un recalculo controlado de cotizaciones. Debe aceptar el alcance, estrategia y version de configuracion, validar permisos y registrar el resultado de la ejecucion.

Respuestas esperadas: `202` o `200` segun la politica operativa definida; `400` por parametros invalidos; `409` si existe un recalculo incompatible o concurrente.

### 3.4 Mercado

#### `POST /orders/buy`

Crea y liquida una orden de compra con usuario, jugador, cantidad y cotizacion aplicable. Devuelve el estado de la orden, importe total, posicion resultante, saldo resultante y referencia de auditoria.

Respuestas esperadas: `201` cuando se liquida; `400` ante request o regla de dominio invalida; `404` ante usuario o jugador inexistente; `409` ante saldo insuficiente, falta de tokens, cotizacion vencida o conflicto transaccional.

#### `POST /orders/sell`

Crea y liquida una orden de venta con usuario, jugador y cantidad. Devuelve el estado de la orden, importe total, posicion resultante, saldo resultante y referencia de auditoria.

Respuestas esperadas: `201` cuando se liquida; `400` ante request o regla de dominio invalida; `404` ante usuario o jugador inexistente; `409` ante tenencia insuficiente, cotizacion vencida o conflicto transaccional.

### 3.5 Usuario y portfolio

#### `GET /users/{id}/portfolio`

Devuelve el portfolio consolidado del usuario: saldo disponible, posiciones, cantidad de tokens, precio promedio, valor actual, ganancia o perdida absoluta y porcentual, y fecha de actualizacion.

Respuestas esperadas: `200`; `404` si el usuario no existe.

#### `GET /users/{id}/transactions`

Devuelve el historial inmutable y paginado de operaciones del usuario, con filtros por jugador, tipo, fecha y estado.

Respuestas esperadas: `200`; `400` ante filtros invalidos; `404` si el usuario no existe.

### 3.6 Manejo global de excepciones

Todas las excepciones personalizadas deben vivir en `exceptions` y las reglas de
negocio en `exceptions.businessException`, heredando de `BusinessException`.
Todas deben ser no chequeadas y extender directa o indirectamente de
`RuntimeException`. Pueden transportar contexto mediante atributos `private final`,
deben invocar `super(message)` y exponer getters sin setters.

Un record o DTO `ApiError` define la respuesta HTTP unificada. Unicamente
`GlobalExceptionHandler`, anotado con `@ControllerAdvice` o
`@RestControllerAdvice`, puede transformar excepciones en respuestas HTTP. Los
Controllers y Services no deben capturar excepciones para retornar errores
manualmente. Las fallas de validacion se responden con `400`, los recursos
inexistentes con `404`, los conflictos de negocio con `409` y los errores no
controlados con una respuesta segura que conserve el Correlation ID.

### 3.7 Estandar de paginacion

Cualquier consulta de multiples registros, incluyendo listados, busquedas y
filtros, debe retornar `Page<T>`. Solo se permiten `List<T>` para catalogos fijos
o enumeraciones acotadas por diseño. El Service debe construir el `Pageable` con
`PageRequest.of(page, tamano)` y pasarlo al Repository; el Controller debe recibir
`page` con valor por defecto `0` y mapear entidades a DTOs mediante `Page.map(...)`.
El Repository debe propagar el `Page<T>` del DAO sin paginar en memoria. El tamaño
por defecto del MVP es 12.

## 4. Diseno de Arquitectura Tecnica y Stack

### 4.1 Arquitectura

La solucion mantiene capas aisladas con el flujo `Controller -> Service -> Model (rico) -> Persistence (Repositories / Adapters)`.

- **Controller**: deserializa requests, valida DTOs declarativos, invoca servicios y mapea resultados a DTOs de response.
- **Service**: coordina casos de uso, existencia, viabilidad, autorizacion y limites transaccionales.
- **Model**: concentra reglas e invariantes del negocio, sin depender de Spring ni de base de datos para probar su logica pura.
- **Persistence**: implementa repositorios, mapeos y adaptadores hacia PostgreSQL, cache y proveedores externos.

Los controladores no contienen logica de negocio. Las dependencias deben apuntar hacia las abstracciones del dominio y cada capa debe poder validarse en el nivel que posee la regla.

La estructura de paquetes del backend debe ser unica y transversal, sin paquetes
separados por modulo de dominio:

```text
backend/src/main/java/unq/losrecursionistas/backend/
├── model/
├── controller/
│   └── dto/
├── services/
│   ├── interfaces/
│   └── impl/
├── persistence/
│   └── repository/
│       ├── interfaces/
│       └── impl/
├── exceptions/
├── configuration/
└── security/
```

Todas las clases model deben vivir en `model`; los controllers en `controller`; y
los DTO en `controller/dto`. Los services deben estar juntos en `services`, con sus
interfaces e implementaciones separadas. Los repositories deben estar en
`persistence/repository`, tambien separados en interfaces e implementaciones.

### 4.2 Stack backend y frontend

El backend usa Java 21, Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Bean Validation, Spring Security, Spring Cache, Lombok de forma acotada, PostgreSQL y Gradle. El frontend usa Vite, React y TypeScript estricto sin `any`, con contratos sincronizados con OpenAPI y un cliente HTTP como unico canal hacia el backend.

La API debe publicar OpenAPI/Swagger actualizado. La coleccion de Postman debe incluir endpoints, ejemplos exitosos y casos de error.

### 4.3 Integracion externa y resiliencia

El catalogo y las metricas se integran con scraping de WhoScored y consumos de Football-Data.org. El sistema debe cachear respuestas externas y conservar datos locales suficientes para operar ante timeout, indisponibilidad, respuesta incompleta o error del proveedor. La falla externa debe quedar registrada con fuente, causa, Correlation ID y resultado del fallback.

Las cotizaciones calculadas con datos de fallback deben identificar la fuente y no deben presentarse como datos frescos del proveedor. La recuperacion posterior debe permitir actualizar el catalogo y crear nuevas cotizaciones sin alterar el historial.

### 4.4 Scheduler, observabilidad y rendimiento

Un scheduler ejecuta como minimo una vez por semana la actualizacion del catalogo y el recalculo de cotizaciones, con frecuencia configurable. Debe existir tambien un recalculo manual autorizado para soporte o demostracion. Las ejecuciones son trazables, idempotentes y no deben crear duplicados al reintentarse.

El backend emite logs estructurados, Correlation IDs, health checks y metricas de latencia y tasa de error. Las tablas y consultas criticas deben tener indices justificados. Como objetivo inicial, el 95% de las consultas de lectura de catalogo, ranking y portfolio debe responder en menos de 1 segundo bajo la carga de referencia definida por el equipo; las operaciones de compra y venta deben responder en menos de 2 segundos en condiciones normales.

## 5. Criterios de Aceptacion y Definition of Done

### 5.1 Escenarios de aceptacion obligatorios

1. **Construccion inicial de base de datos**: dado un entorno vacio, cuando se ejecuta la inicializacion, entonces existen los jugadores configurados, el Superusuario Financiero y exactamente 100 tokens por jugador a 1 credito, sin duplicacion al repetirla.
2. **Catalogo y detalle**: dado un catalogo cargado, cuando un usuario consulta la lista y el detalle de un jugador, entonces recibe datos consistentes, filtros validos y `404` para un jugador inexistente.
3. **Evolucion de cotizacion**: dado un jugador con metricas y una cotizacion previa, cuando se ejecuta un recalculo con una estrategia configurada, entonces se crea una nueva cotizacion con score, precio, estrategia/version y fuente, conservando intacta la anterior.
4. **Ranking**: dado un conjunto de jugadores valuados, cuando se consulta el ranking, entonces se ordena por el criterio vigente y cada item identifica el precio y la version de calculo.
5. **Compra inicial**: dado un usuario registrado con saldo suficiente y el Superusuario con tokens disponibles, cuando compra tokens, entonces disminuye su saldo, aumenta su posicion, disminuye el inventario vendedor y se registra una operacion inmutable.
6. **Compra rechazada**: dado un usuario con saldo insuficiente o inventario vendedor insuficiente, cuando intenta comprar, entonces recibe `409` y no cambia ningun saldo, posicion ni historial.
7. **Venta**: dado un usuario que posee tokens, cuando vende una cantidad valida, entonces aumenta su saldo, disminuye su posicion y queda registrada la operacion con la cotizacion vigente.
8. **Venta rechazada**: dado un usuario sin tenencia suficiente, cuando intenta vender, entonces recibe `409` y el portfolio permanece sin cambios.
9. **Flujo con cuatro usuarios**: dado un Superusuario y cuatro usuarios con saldo, cuando los cuatro compran y luego algunos venden de forma intercalada, entonces cada portfolio conserva su saldo y posicion correctos, el inventario global no supera los 100 tokens por jugador y todas las operaciones pueden auditarse en orden.
10. **Falla de proveedor**: dado un proveedor externo no disponible, cuando se actualiza el catalogo o se recalcula una cotizacion, entonces el sistema usa datos locales/cacheados, informa la fuente fallback y mantiene operables las consultas permitidas.
11. **Portfolio e historial**: dado un usuario con operaciones, cuando consulta su portfolio e historial, entonces obtiene cantidad, precio promedio, valor actual, ganancia/perdida y operaciones paginadas sin poder modificar registros historicos.
12. **Seguridad y errores**: dado un request no autenticado, no autorizado, malformado o con input no sanitizable, cuando invoca un endpoint protegido, entonces la respuesta no expone datos sensibles y usa el codigo y mensaje definidos.

### 5.2 Definition of Done

Una funcionalidad se considera terminada solamente cuando:

- Las pruebas unitarias puras del Model cubren invariantes, casos felices y casos borde sin contexto de Spring ni base de datos.
- Las pruebas de Services y Repositories usan un PostgreSQL real mediante Testcontainers y cubren transacciones, persistencia e idempotencia.
- Las pruebas E2E web usan MockMvc, viven exclusivamente en su paquete propio y permanecen separadas de las pruebas de servicios.
- Se cubren saldo insuficiente, falta de tokens, tenencia insuficiente, cotizacion invalida, concurrencia y fallas de proveedores externos.
- La aplicacion compila, inicia y opera con la configuracion local de Docker/Postgres sin errores.
- OpenAPI/Swagger describe cada endpoint, request, response, error y seguridad.
- Postman contiene la coleccion vigente, ejemplos exitosos y casos de error.
- El frontend consume los contratos sin descalces de tipos, sin errores de consola y sin usar `any`.
- Los logs estructurados, Correlation IDs, health checks, metricas y auditoria financiera son verificables.
- No se modifican ni borran tests existentes sin permiso explicito.

## User Scenarios & Testing

### User Story 1 - Explorar jugadores y cotizaciones (Priority: P1)

Como usuario del mercado, quiero consultar jugadores, cotizaciones y ranking para decidir que tokens comprar.

**Why this priority**: Sin informacion de mercado no existe una decision de inversion reproducible.

**Independent Test**: Se puede probar con un catalogo inicial y verificar lista, detalle, historial de cotizaciones y ranking sin ejecutar ordenes.

**Acceptance Scenarios**:

1. **Given** un catalogo cargado, **When** consulto jugadores, **Then** recibo los jugadores activos y sus datos vigentes.
2. **Given** un jugador con cotizaciones historicas, **When** consulto sus cotizaciones, **Then** recibo todos los registros inmutables con estrategia y version.
3. **Given** jugadores valuados, **When** consulto el ranking, **Then** recibo el orden y score vigentes.

### User Story 2 - Comprar y vender tokens (Priority: P1)

Como usuario registrado, quiero comprar y vender tokens para construir y realizar mi posicion.

**Why this priority**: Es el flujo central que convierte la valuacion en una simulacion financiera operable.

**Independent Test**: Se puede probar con un usuario, el Superusuario Financiero y un jugador, verificando saldos, posiciones y auditoria antes y despues.

**Acceptance Scenarios**:

1. **Given** saldo y disponibilidad suficientes, **When** compro tokens, **Then** la operacion se liquida atomically y se actualiza el portfolio.
2. **Given** saldo insuficiente, **When** compro tokens, **Then** recibo un conflicto y no hay cambios parciales.
3. **Given** tenencia suficiente, **When** vendo tokens, **Then** recibo saldo y se reduce la posicion.
4. **Given** tenencia insuficiente, **When** vendo tokens, **Then** recibo un conflicto y no hay cambios.

### User Story 3 - Seguir el portfolio (Priority: P2)

Como usuario, quiero consultar valor, ganancias/perdidas e historial para evaluar mi rendimiento.

**Why this priority**: La transparencia del resultado es necesaria para que el mercado sea entendible y auditable.

**Independent Test**: Se puede probar con operaciones preexistentes, sin recalcular ni ejecutar nuevas ordenes.

**Acceptance Scenarios**:

1. **Given** operaciones de compra y venta, **When** consulto el portfolio, **Then** recibo posiciones, precio promedio, valor actual y ganancia/perdida.
2. **Given** operaciones historicas, **When** consulto transacciones, **Then** recibo un historial paginado e inmutable.

### User Story 4 - Administrar valuacion y datos (Priority: P2)

Como operador autorizado, quiero actualizar catalogo y recalcular cotizaciones para mantener el mercado vigente aun ante fallas externas.

**Why this priority**: La cotizacion periodica es el mecanismo que mantiene vivo el mercado.

**Independent Test**: Se puede probar con datos de proveedor disponibles y no disponibles, comprobando scheduler, recalculo manual, cache y trazabilidad.

**Acceptance Scenarios**:

1. **Given** una estrategia y metricas configuradas, **When** ejecuto un recalculo autorizado, **Then** se crean nuevas cotizaciones versionadas.
2. **Given** una falla de proveedor, **When** ejecuto la actualizacion, **Then** se usa el fallback local/cacheado y queda registrada la incidencia.

## Edge Cases

- Solicitud de cantidad cero, negativa, decimal cuando no corresponda o fuera del limite permitido.
- Usuario, jugador o cotizacion inexistente, inactivo o eliminado logicamente.
- Cotizacion vencida entre la lectura de la orden y su liquidacion.
- Dos compras simultaneas que compiten por los ultimos tokens disponibles.
- Reintento de inicializacion, orden o job despues de timeout.
- Proveedor externo con respuesta parcial, datos inconsistentes o latencia excesiva.
- Cache vacio durante una indisponibilidad externa.
- Filtros, paginacion o rangos de fechas invalidos.
- Intento de modificar o eliminar una cotizacion u operacion historica.
- Perdida de autenticacion, permisos insuficientes o input con caracteres no permitidos.

## Requirements

### Functional Requirements

- **FR-001**: El sistema MUST registrar usuarios antes de permitirles operar.
- **FR-002**: El sistema MUST mantener cinco ligas objetivo: Premier League, Bundesliga, La Liga, Serie A y Ligue 1.
- **FR-003**: El sistema MUST crear de forma idempotente el Superusuario Financiero y 100 tokens iniciales por jugador a 1 credito por token.
- **FR-004**: El sistema MUST exponer catalogo, detalle, cotizaciones, ranking, recalculo, compra, venta, portfolio e historial mediante los endpoints definidos.
- **FR-005**: El sistema MUST validar DTOs por forma, tipo, limites, trimming y sanitizacion antes de ejecutar casos de uso.
- **FR-006**: El sistema MUST resolver entidades por ID y validar existencia y viabilidad en Service antes de modificar el estado.
- **FR-007**: El Model MUST proteger invariantes de saldo, cantidad, posicion, precio e inmutabilidad y lanzar errores propios del dominio.
- **FR-008**: El sistema MUST liquidar compras y ventas atomically, actualizando saldos, posiciones e historial sin cambios parciales.
- **FR-009**: El sistema MUST rechazar compras por saldo o disponibilidad insuficiente y ventas por tenencia insuficiente.
- **FR-010**: El sistema MUST registrar cada operacion financiera y auditoria con autor, timestamp, Correlation ID, detalle y estados anterior/posterior.
- **FR-011**: El sistema MUST conservar cotizaciones y transacciones historicas inmutables.
- **FR-012**: El sistema MUST soportar al menos dos estrategias de valuacion con metricas, pesos, versiones y ponderaciones por posicion configurables.
- **FR-013**: El sistema MUST ejecutar actualizaciones y recalculos automaticamente como minimo una vez por semana y permitir un recalculo manual autorizado.
- **FR-014**: El sistema MUST integrar WhoScored y Football-Data.org y continuar operando con datos locales/cacheados ante fallas externas.
- **FR-015**: El sistema MUST centralizar errores mediante `@ControllerAdvice` y responder `400`, `404` y `409` segun la semantica definida.
- **FR-016**: El sistema MUST implementar autenticacion y autorizacion y proteger operaciones financieras y administrativas.
- **FR-017**: El sistema MUST emitir logs estructurados, Correlation IDs, health checks y metricas de latencia y tasa de error.
- **FR-018**: El frontend MUST ser responsivo, consumir exclusivamente el cliente HTTP definido y usar TypeScript estricto sin `any`.
- **FR-019**: El sistema MUST mantener OpenAPI/Swagger y Postman actualizados con ejemplos de exito y error.
- **FR-020**: El sistema MUST preservar los tests existentes y ampliar la cobertura con unitarias de Model, integracion con Testcontainers y E2E con MockMvc.

### Key Entities

- **Usuario**: Persona habilitada para operar, con identidad, estado, saldo y roles.
- **Jugador**: Activo deportivo negociable, asociado a liga, equipo, posicion y estado.
- **Token**: Unidad negociable que representa una fraccion de la posicion de un jugador.
- **Posicion**: Tenencia de tokens de un usuario sobre un jugador, con cantidad y precio promedio.
- **Cotizacion**: Valor historico de un jugador, con precio, score, fecha, estrategia, version, metricas y fuente.
- **Orden**: Solicitud de compra o venta con usuario, jugador, cantidad, cotizacion y estado de liquidacion.
- **Transaccion**: Registro inmutable de una orden liquidada y sus importes.
- **AuditoriaFinanciera**: Evidencia inmutable de autor, timestamp, Correlation ID, cambios y estados.
- **EstrategiaValuacion**: Configuracion versionada de metricas, pesos y reglas de calculo.
- **FuenteDatos**: Proveedor externo o fallback local/cacheado usado para catalogo y metricas.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Un usuario puede consultar catalogo, detalle, ranking y cotizaciones historicas en menos de 2 minutos desde una interfaz nueva.
- **SC-002**: El 95% de las consultas de catalogo, ranking y portfolio responde en menos de 1 segundo bajo la carga de referencia acordada.
- **SC-003**: El 95% de las compras y ventas validas se liquida en menos de 2 segundos bajo condiciones normales.
- **SC-004**: El 100% de las operaciones financieras liquidadas tiene una auditoria asociada, inmutable y reconstruible.
- **SC-005**: La inicializacion repetida produce exactamente una posicion inicial de 100 tokens por jugador, sin duplicados.
- **SC-006**: En pruebas con cuatro usuarios y operaciones intercaladas, el 100% de los saldos, posiciones e historiales coincide con el resultado esperado.
- **SC-007**: Ante una falla de proveedor externo, el 100% de las consultas cubiertas por fallback continua respondiendo con una fuente identificable o un error operativo explicito.
- **SC-008**: El 90% de los usuarios de prueba completa su primera compra valida sin asistencia y recibe un resultado de portfolio comprensible.
- **SC-009**: Las pruebas de aceptacion cubren el 100% de los escenarios obligatorios y cada endpoint documentado tiene al menos un caso exitoso y uno de error en Postman.

## Assumptions

- La especificacion del PDF de negocio no esta disponible en el workspace al momento de redactar este documento; los detalles no presentes se derivan de la Constitucion v1.4.1 y del pedido recibido.
- Las cinco ligas objetivo son las indicadas por la Constitucion y el catalogo inicial puede crecer sin cambiar el contrato base.
- El credito es una unidad simulada, no dinero real, y no se requiere integracion de pagos en esta version.
- Las cotizaciones tienen una vigencia configurable; si no se configura otra, se considera vigente la ultima cotizacion aprobada del ciclo activo.
- La paginacion por defecto protege el rendimiento y sus limites exactos se definiran en el contrato OpenAPI durante la planificacion.
- Los objetivos de carga se validaran contra una carga de referencia que el equipo debe fijar antes de cerrar la funcionalidad.
- La autenticacion y roles se implementaran con las capacidades estandar del proyecto; los detalles de proveedor de identidad quedan fuera de esta especificacion funcional.
- La interfaz movil y de escritorio consume el mismo contrato REST; no se requiere una aplicacion nativa en esta fase.
