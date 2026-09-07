<!--
 Sync Impact Report
- Version change: 1.6.0 -> 1.7.0
- Modified principles: PRINCIPLE_1_NAME -> I. Arquitectura; PRINCIPLE_2_NAME -> II. Calidad del Codigo;
  PRINCIPLE_3_NAME -> III. Diseno de la API; PRINCIPLE_4_NAME -> IV. Persistencia de Datos;
  PRINCIPLE_5_NAME -> V. Seguridad; added VI. Frontend, VII. Pruebas and VIII. Documentacion.
- Added sections: Project Purpose, Backend Stack, Valuation and Scheduling,
  Observability and Performance, Market Invariants, Code Validation by Layer,
  Definition of Done, and expanded Governance rules.
- Removed sections: none.
- Modified principles: I. Arquitectura (explicit package layout and domain model
  structure rules)
- Added sections: Backend package organization
- Removed sections: none.
- Follow-up TODOs: Confirm the historical ratification date.
- Modified conventions: repository implementation naming
- Added conventions: Lombok annotations, explicit business constructors,
  identity-based equality and encapsulated domain behavior for model classes
- Added conventions: ServiceImpl structure, transactional boundaries, domain-only
  returns, repository delegation, pagination and semantic business exceptions
- Added conventions: RepositoryImpl structure, DAO delegation, direct Optional
  unwrapping, semantic Spanish repository methods and prohibition of in-memory state
- Added conventions: exception hierarchy, immutable exception context, ApiError and
  centralized GlobalExceptionHandler; generalized Page<T> pagination contract
-->

# Mercado de Tokens de Jugadores Constitution

El proyecto construye un sistema de simulacion y mercado financiero basado en la
cotizacion periodica de jugadores de las cinco ligas principales de Europa: Premier
League, Bundesliga, La Liga, Serie A y Ligue 1. Permite comprar y vender tokens de
jugadores, gestionar portfolios en tiempo real, conservar la trazabilidad de las
operaciones e integrar metricas de rendimiento mediante scraping y APIs externas.

## Core Principles

### I. Arquitectura

La aplicacion MUST mantener una arquitectura en capas estricta: Controller, Service,
Model y Persistence (Repositories/Adapters). Las capas MUST estar aisladas y el
modelo de dominio MUST ser rico: la logica de negocio debe vivir en los objetos del
modelo, evitando servicios anemicos. Esta separacion permite probar reglas de
negocio sin depender de infraestructura.

La organizacion de paquetes del backend MUST seguir esta estructura bajo
`unq.losrecursionistas.backend`:

```text
model/
controller/
└── dto/
services/
├── interfaces/
└── impl/
persistence/
└── repository/
  ├── interfaces/
  └── impl/
exceptions/
configuration/
security/
```

Todas las clases de dominio MUST estar en `model`. Todos los controladores MUST
estar en `controller` y sus DTOs MUST estar dentro de `controller/dto`. Los services
MUST estar en `services`, separados entre contratos en `services/interfaces` e
implementaciones en `services/impl`. Los repositories MUST estar en
`persistence/repository`, separados entre interfaces en `interfaces` e
implementaciones en `impl`. Las excepciones, configuracion y seguridad MUST estar
respectivamente en `exceptions`, `configuration` y `security`. No se permiten
paquetes alternativos por dominio que dupliquen estas responsabilidades.

Las implementaciones concretas MUST usar el nombre de su interfaz seguido por
`Impl`: por ejemplo, `UsuarioRepositoryImpl`, `JugadorRepositoryImpl`,
`UsuarioServiceImpl` y `JugadorServiceImpl`. No se deben usar prefijos como
`InMemory` para ocultar que una clase es la implementacion de un contrato.

#### Reglas de Estructura y Diseno para los Modelos de Dominio

Al crear o modificar cualquier clase dentro de `model`, se MUST cumplir lo
siguiente:

- Cada clase de dominio MUST incluir `@NoArgsConstructor`, `@AllArgsConstructor`,
  `@Data` y `@Builder`. No se permiten clases `final`, campos `final` ni getters
  estilo record; se deben usar getters y setters tradicionales generados por
  `@Data`.
- Las relaciones que puedan generar ciclos en `toString` MUST marcarse con
  `@ToString.Exclude`. Los campos con valores por defecto usados por `@Builder`
  MUST marcarse con `@Builder.Default`.
- Además del constructor generado por `@AllArgsConstructor`, cada entidad MUST
  definir un constructor público explícito sin `id` ni campos autogenerados. Ese
  constructor MUST inicializar las colecciones y relaciones que tengan un valor
  inicial de dominio.
- `equals` y `hashCode` MUST sobrescribirse usando únicamente la identidad de la
  entidad: `Objects.equals(id, o.id)` y `Objects.hash(id)`, respectivamente.
- El modelo MUST ser rico y encapsulado: las reglas de negocio, validaciones de
  estado y mutaciones deben vivir en métodos del propio modelo, con excepciones
  de dominio específicas cuando corresponda. Los constructores no deben validar
  primitivos ni lanzar `IllegalArgumentException` por esas validaciones.

Estas reglas hacen uniforme el ciclo de vida de las entidades, evitan ciclos de
representacion y mantienen la identidad y las invariantes dentro del dominio.

#### Reglas de Estructura y Diseno para la Capa de Servicio (`services.impl`)

Al crear o modificar cualquier implementacion `ServiceImpl`, se MUST cumplir lo
siguiente:

- La clase MUST usar `@Service` y declarar `@Transactional` a nivel de clase o
  de metodo cuando corresponda. Las dependencias MUST inyectarse explicitamente
  por constructor y todos sus atributos MUST ser `private final`; no se permite
  `@Autowired` sobre campos ni `@RequiredArgsConstructor`.
- Los servicios de dominio MUST retornar solamente entidades de dominio o
  `Page<Entidad>`. No pueden retornar DTOs de response, generar JWTs ni contener
  logica de presentacion. El mapeo a DTO pertenece al Controller y la emision de
  JWT a la fachada o componente de seguridad correspondiente.
- La construccion compleja de entidades MUST extraerse a metodos privados
  auxiliares y usar builders expresivos (`.builder()...build()`).
- Los servicios MUST delegar la persistencia en repositorios mediante metodos
  descriptivos. No deben contener hashes criptograficos inline, `AtomicLong` ni
  logica ajena al caso de uso.
- Para resultados paginados, el servicio MUST construir al inicio del metodo un
  `Pageable` con `PageRequest.of(page, 12)` y pasarlo al repositorio.
- Las reglas de negocio MUST informar fallas mediante excepciones semanticas y
  especificas, como `EmailYaExisteException`; no se deben usar excepciones
  genericas ni codigos estaticos encapsulados en `DomainException`.

Estas reglas mantienen los limites transaccionales y de responsabilidad visibles,
permiten probar los casos de uso sin mezclar presentacion o seguridad.

#### Reglas de Estructura y Diseno para la Capa de Excepciones (`exceptions`)

Al crear o modificar excepciones y respuestas de error, se MUST cumplir lo
siguiente:

- Todas las excepciones personalizadas MUST residir en el paquete base
  `exceptions`. Las excepciones de reglas de negocio MUST agruparse en
  `exceptions.businessException` y heredar de `BusinessException`.
- Todas las excepciones personalizadas MUST ser no chequeadas y extender directa o
  indirectamente de `RuntimeException`.
- Las excepciones pueden transportar contexto en atributos `private final` (por
  ejemplo, `product`, `entityName` o `id`). Sus constructores MUST invocar
  `super(message)` y deben proveer getters explícitos, sin setters ni estado mutable.
- MUST existir un record o DTO `ApiError` para encapsular la respuesta HTTP de
  error unificada. Toda transformación de excepciones a respuestas HTTP MUST
  centralizarse exclusivamente en `GlobalExceptionHandler`, anotado con
  `@ControllerAdvice` o `@RestControllerAdvice`.
- Los Controllers y Services tienen PROHIBIDO capturar excepciones para construir
  manualmente respuestas de error. Deben propagar las excepciones semánticas al
  handler global.

#### Estándar de Paginación (`Page<T>`)

- Toda consulta que devuelva múltiples registros (listados, búsquedas o filtros)
  MUST retornar `Page<T>`. Se PROHIBE retornar `List<T>`, excepto para catálogos
  fijos o enumeraciones acotadas por diseño.
- El Service MUST crear el `Pageable` mediante `PageRequest.of(page, tamano)` y
  pasarlo al Repository. La implementación MUST definir y aplicar un tamaño
  acotado por configuración o contrato; el tamaño por defecto del MVP es 12.
- El Controller MUST recibir `page` con valor por defecto 0, invocar al Service y
  mapear el resultado con `Page.map(...)` cuando deba convertir entidades a DTOs.
- El Repository MUST propagar el `Page<T>` desde el DAO, sin materializarlo en
  listas ni paginar en memoria.

#### Reglas de Estructura y Diseno para la Capa de Persistencia (`persistence.repository`)

Al crear o modificar cualquier implementacion `RepositoryImpl`, se MUST cumplir lo
siguiente:

- La clase MUST implementar la interfaz `Repository` correspondiente, estar anotada
  con `@Component` o `@Repository` e inyectar mediante constructor explicito el DAO
  de Spring Data JPA/SQL correspondiente en un atributo `private final` (por ejemplo,
  `EntidadDAOSQL entidadDAOSQL`). El repository MUST delegar en ese DAO el acceso a
  datos.
- Queda PROHIBIDO mantener estado persistente en colecciones en memoria, incluyendo
  `ConcurrentHashMap`, `List` o `Map`, instanciar datos mock o hardcoded en el
  constructor, usar secuencias locales como `AtomicLong` o simular estados. La
  persistencia MUST resolverse mediante los DAOs o APIs correspondientes.
- Los métodos MUST retornar directamente entidades de dominio o `Page<Entidad>`.
  No deben exponer `Optional<T>`, salvo que el flujo de negocio lo requiera
  explicitamente. Las consultas por ID o campo unico sin resultado MUST usar
  `.orElseThrow(...)` con una excepcion descriptiva y especifica, como
  `EntityNotFoundException`.
- La interfaz y la implementacion MUST exponer nombres semanticos en espanol para
  el dominio, como `recuperarPorId`, `guardar`, `estaRegistradoElEmail` y
  `recuperarUsuarios`, ocultando la nomenclatura de Spring Data (`findById`, `save`,
  `findAll`) en los limites internos del DAO.

Esta separacion evita repositories falsamente persistentes, concentra el acceso a
la base en el DAO y mantiene los contratos de dominio independientes de Spring Data.

### II. Calidad del Codigo

Cada validacion MUST ejecutarse en el nivel que posee la regla. La documentacion y
los mensajes de error MUST estar en espanol. Los identificadores del dominio MUST
usar espanol sin acentos ni `n`, mientras que los terminos tecnicos y normativos MUST
permanecer en ingles, por ejemplo `PlayerQuoteService` y `TokenOrder`.

#### Validacion en su nivel

Las validaciones MUST ejecutarse en la capa que posee el conocimiento necesario:

- **DTO de request** MUST validar forma y tipos del request, incluyendo trimming y
  sanitizacion del input.
- **Service** MUST validar la existencia de lo solicitado y la viabilidad de la
  accion; los IDs MUST resolver entidades existentes antes de continuar.
- **Model** MUST proteger los invariantes del dominio y lanzar excepciones propias
  del dominio cuando una regla se incumpla.

Esta separacion MUST evitar mezclar concerns de transporte, orquestacion y reglas
de negocio: cada capa valida unicamente lo que puede conocer y controlar.

### III. Diseno de la API

La API MUST exponer endpoints RESTful limpios y documentados con OpenAPI/Swagger.
Los controladores MUST limitarse a deserializar requests, invocar servicios y mapear
resultados a DTOs de response. El manejo de excepciones MUST estar centralizado
mediante `@ControllerAdvice`, con respuestas consistentes y codigos semanticos:
`400` para fallas de validacion de dominio, `404` para entidades inexistentes y
`409` para conflictos de negocio.

### IV. Persistencia de Datos

PostgreSQL MUST ser el motor relacional principal para garantizar consistencia
transaccional. El sistema MUST usar una capa de cache in-memory mediante Spring
Cache para reducir la latencia de WhoScored y Football-Data.org. Ante una falla de
una fuente externa, el sistema MUST continuar operando con datos locales o
cacheados. Las cotizaciones historicas y las transacciones de compra y venta MUST
ser inmutables.

### V. Seguridad

La autenticacion y autorizacion MUST implementarse con Spring Security. Todo input
externo MUST validarse y sanitizarse para prevenir SQL injection, XSS y ataques
equivalentes. Cada operacion financiera MUST generar un registro inmutable con
autor, timestamp, detalle de cambios y estados anterior y posterior. Los logs
estructurados MUST incluir Correlation IDs para garantizar trazabilidad.

### VI. Frontend

El frontend MUST desarrollarse con Vite, React y TypeScript. MUST comunicarse con el
backend exclusivamente mediante un cliente HTTP que consuma la API REST. La
interfaz MUST ser responsiva y priorizar la claridad de portfolio, ganancias,
perdidas y evolucion de cotizaciones. TypeScript MUST usar tipado estricto, sin
`any`, y sus contratos MUST mantenerse sincronizados con OpenAPI.

### VII. Pruebas

Las pruebas unitarias del dominio MUST verificar logica pura sin contexto de Spring
ni base de datos. Las pruebas de services y repositories MUST usar un PostgreSQL
real mediante Testcontainers. Las pruebas E2E web MUST usar MockMvc, vivir
exclusivamente en su propio paquete y permanecer separadas de las pruebas de
servicios. Cada funcionalidad MUST cubrir casos felices y borde, incluidos saldo
insuficiente, falta de tokens y fallas de proveedores externos. Queda PROHIBIDO
modificar o borrar tests existentes sin permiso explicito y confirmacion.

### VIII. Documentacion

La especificacion OpenAPI/Swagger MUST mantenerse actualizada para cada endpoint
expuesto. El proyecto MUST mantener una coleccion de Postman con ejemplos de uso,
casos exitosos y casos de error.

## Constraints and Quality Standards

### Backend Stack

El backend MUST ejecutarse sobre Java 21 y Spring Boot 4.1.1, administrado con Gradle.
La implementacion MUST usar Spring MVC para la capa web, Spring Data JPA para el
acceso relacional, Bean Validation para validaciones declarativas y Lombok solo para
reducir codigo repetitivo sin ocultar logica de negocio. PostgreSQL MUST ser el
driver y motor relacional de ejecucion. Spring Security MUST gestionar autenticacion
y autorizacion, y Spring Cache MUST proveer la cache in-memory definida en este
documento.

### Valuation and Scheduling

El sistema MUST implementar al menos dos estrategias configurables de valuacion,
basadas en metricas de performance y contexto. Cada metrica MUST admitir un peso
configurable y las estrategias MUST permitir ponderaciones diferenciadas por
posicion cuando corresponda. El precio MUST derivarse de un score reproducible y
cada cotizacion MUST conservar la estrategia y version utilizadas.

El recalculo de cotizaciones y la actualizacion del catalogo MUST ejecutarse de
forma automatica mediante un scheduler, con una frecuencia definida por
configuracion y al menos una ejecucion semanal. El sistema MUST permitir tambien
el recalculo manual controlado para operaciones de soporte o demostracion.

### Observability and Performance

El backend MUST emitir logs estructurados, Correlation IDs, health checks y metricas
operativas que incluyan como minimo latencia y tasa de error. Las consultas
frecuentes y las integraciones externas MUST usar cache cuando sea apropiado. Las
tablas y consultas criticas MUST contar con indices justificados. Los objetivos de
rendimiento y disponibilidad MUST quedar definidos como SLAs antes de declarar
terminada una funcionalidad que dependa de ellos.

### Market Invariants

El sistema MUST permitir registrar usuarios antes de operar. Una compra MUST
validar disponibilidad, saldo y cotizacion vigente; una venta MUST validar la
tenencia del usuario y actualizar saldo, posicion e historial en una unica
transaccion. El portfolio MUST exponer cantidad de tokens, precio promedio de
compra, valor actual, ganancia o perdida e historial de operaciones.

La emision e inventario inicial MUST asignar 100 tokens por jugador a un credito base
y pertenecer al superusuario financiero. Las compras iniciales de usuarios MUST
liquidarse contra la posicion de ese superusuario. El motor de cotizacion MUST
registrar formalmente la version y estrategia usada para calcular cada score y
precio.

## Development Workflow

1. El equipo MUST definir el DTO y el contrato de API en OpenAPI antes de implementar.
2. El backend MUST implementarse con TDD y respetando el orden de capas.
3. Los endpoints MUST verificarse manualmente mediante Postman y su coleccion vigente.
4. El frontend MUST integrarse consumiendo los endpoints definidos y sus contratos.
5. Las operaciones transaccionales MUST verificarse mediante logs estructurados y
   registros de auditoria.

### Definition of Done

Una funcionalidad esta terminada unicamente cuando:

1. Sus tests unitarios y de integracion, incluidos casos felices y borde, pasan.
2. La aplicacion compila, inicia y funciona sin errores con la configuracion local
   de Docker/Postgres.
3. OpenAPI y Postman reflejan todos los endpoints nuevos o modificados.
4. El frontend consume la funcionalidad sin errores de consola ni descalces de tipos.

## Governance

Esta constitucion prevalece sobre practicas contradictorias del proyecto. Toda
modificacion MUST documentar el motivo, el impacto y, cuando corresponda, un plan
de migracion. La proteccion de tests existentes es obligatoria en todas las fases.

Las enmiendas requieren actualizar este documento, registrar el cambio en el Sync
Impact Report y obtener aprobacion explicita del responsable del proyecto. Cada
revision MUST comprobar arquitectura, seguridad, pruebas, contratos API,
observabilidad y Definition of Done.

El versionado usa Semantic Versioning: MAJOR para eliminar o redefinir reglas de
forma incompatible, MINOR para agregar o ampliar principios o secciones, y PATCH
para aclaraciones no semanticas, correcciones y mejoras de redaccion.

**Version**: 1.7.0 | **Ratified**: TODO(RATIFICATION_DATE): confirmar fecha de adopcion inicial | **Last Amended**: 2026-09-07
