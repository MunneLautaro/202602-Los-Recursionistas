<!-- Sync Impact Report
Version change: 1.9.0 -> 2.0.0
Modified principles: Stack tecnológico, Principio I, Principio VII, Principio VIII,
Principio XIII, Requisitos no funcionales, Governance
Added sections: none
Removed sections: none
Follow-up TODOs: none
-->

# Constitution — Valoración de Mercado de Jugadores de Fútbol

**Versión:** 2.0.0
**Fecha de ratificación:** 2026-09-15
**Última modificación:** 2026-10-13

**Resumen del cambio 2.0.0:** Se establece `adapter/` como única ubicación backend
para integraciones con sistemas externos. Se eliminan `client/` y `gateway/` como
alternativas estructurales, se exige un subpaquete por fuente externa y se refuerzan
los contratos por puertos, el aislamiento de DTOs y excepciones, la configuración
global, las verificaciones de refactors y las reglas de scraping con Playwright.

**Resumen del cambio 1.7.0:** Se incorpora el Principio XI — Desarrollo estrictamente acotado (No Over-Engineering / YAGNI) prohibiendo la creación anticipada de código, clases, métodos, DTOs, configuraciones o tests no solicitados explícitamente.

**Resumen del cambio 1.6.0:** Se eliminan las validaciones de formato de texto del modelo asignándolas a los DTOs/controllers, y se restringe la capa de tests de modelo únicamente a entidades con lógica/métodos de negocio reales (sin tests para entidades no iniciadas ni DTOs/anotaciones de Lombok).

**Resumen del cambio 1.5.0:** Se restringe la jerarquía de tests únicamente a los paquetes `model` y `service`, eliminando pruebas redundantes de infraestructura, seguridad y contexto básico.

**Resumen del cambio 1.4.0:** Se aclara que todo caso de uso tiene contrato en
`service/interfaces` e implementación en `service/impl`, incluyendo los
adaptadores de identidad que implementan contratos de Spring. Los repositorios
de dominio se desacoplan del mecanismo de persistencia mediante DAOs bajo
`persistence/sql` y composición por constructor.

**Resumen del cambio 1.3.0:** Se establece que cada servicio implementa su
interfaz específica y que la reutilización entre servicios debe resolverse por
composición, sin herencia de una clase base genérica.

**Resumen del cambio 1.2.0:** Se fija la regla de usar una sola
`application.properties` para este proyecto y evitar perfiles separados salvo
aprobación explícita del equipo.

**Resumen del cambio 1.1.0:** Se incorpora Spring Security como dependencia
obligatoria para autenticación y autorización dentro del paquete `security`.

## Propósito

Este documento define los principios innegociables que gobiernan el diseño e
implementación del sistema. Toda spec, plan o tarea generada por speckit MUST
cumplir estas reglas. Ante cualquier conflicto entre una spec/plan y esta
constitution, prevalece la constitution.

---

## Stack tecnológico

- **Backend:** Java 21, Spring Boot, Spring Security, PostgreSQL y Playwright for Java
  dentro del módulo de scraping gobernado por el Principio XIII.
- **Frontend:** React 19, Vite, TypeScript, Tailwind CSS 4, react-router-dom 7.
  - Manejador de paquetes: Yarn (`.yarnrc.yml` en `/frontend`).
  - Estilos: Tailwind CSS 4 integrado vía plugin de Vite (`@tailwindcss/vite`). Los estilos específicos de componente se extraen en archivos `.styles.ts` con clases Tailwind.
- Comunicación Frontend–Backend vía HTTP/REST. El cliente HTTP del frontend es `src/lib/fetchClient.ts`, que lee la URL base de `VITE_API_URL` (fallback `http://localhost:8080`).

Cualquier desviación de este stack (agregar una librería estructural, un motor
de persistencia alternativo, un framework de frontend distinto, etc.) MUST
consultarse con el equipo antes de aplicarse.

---

## Principio I — Arquitectura en capas estricta

El backend MUST organizarse en los siguientes paquetes, sin mezclar
responsabilidades entre ellos:

- `model`: entidades de dominio, con anotaciones (`@Entity`, `@Data`,
  `@Builder`, etc.) y la lógica de negocio e invariantes del dominio.
- `controller`: controladores REST. Contiene el subpaquete `dto` con los DTOs
  de request/response.
- `service`: lógica de orquestación de negocio, dividida en `interfaces` (los
  contratos) e `impl` (las implementaciones). El subpaquete `impl/exceptions`
  aloja excepciones específicas de servicios (ej. `ExcepcionNombreDeUsuarioExistente`).
- `persistence`: acceso a datos. El subpaquete `repository` se divide en
  `interfaces` e `impl`. El subpaquete `sql` contiene los DAOs Spring Data JPA.
- `adapter`: única ubicación de integraciones backend con sistemas externos,
  incluidas APIs HTTP y scraping. Cada fuente MUST tener su subpaquete
  `adapter/<fuente>/` con una estructura consistente de `dto/`, configuración,
  excepción, puerto e implementación. Es obligatorio reutilizar esta ubicación;
  queda prohibido crear `client/`, `gateway/` u otra carpeta paralela para la
  misma responsabilidad.
- `scheduler`: jobs programados (ej. `FootballDataScheduler`). Contiene únicamente
  beans `@Component` con métodos `@Scheduled`; la lógica de negocio de sincronización
  reside en `service`.
- `configuration`: configuración global de la aplicación (`SecurityConfig`,
  `RestClientConfig`, `ConfiguracionOpenApi`, `SchedulingConfig`, etc.).
- `security`: componentes de seguridad HTTP/JWT (`jwt/impl`, `handlers` como
  `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`, filtro `JwtAuthFilter`).
- `exceptions`: excepciones personalizadas de dominio y manejo centralizado de
  errores (`ControladorErrores`).

**Regla:** una capa nunca invoca directamente a una capa no adyacente ni
depende de detalles de implementación de otra capa (por ejemplo, el
`controller` no accede a `repository` directamente).

---

## Principio II — El modelo es dueño de la lógica de negocio

- Los modelos MUST llevar las anotaciones necesarias para que Spring
  Boot/JPA funcionen correctamente (`@Entity`, `@Table`, `@Data`, `@Builder`,
  `@NoArgsConstructor`/`@AllArgsConstructor`, relaciones, etc.).
- Las reglas y cálculos propios del dominio (por ejemplo, actualización de
  posición del portfolio, cálculo de ganancia/pérdida, invariantes de tokens)
  MUST vivir en los objetos de modelo, no en los servicios.
- El modelo MUST proteger sus invariantes y lanzar sus propias excepciones de
  dominio cuando una regla se incumpla (ver Principio V).

---

## Principio III — Contrato de los servicios de dominio

- Los servicios MUST retornar únicamente entidades de dominio, DTOs de autenticación/casos de uso o `Page<Entidad>`. **Prohibido** contener lógica de presentación o detalles del protocolo HTTP.
- El mapeo a DTO de respuesta HTTP es responsabilidad del `controller` (excepto flujos de autenticación donde `AuthService` expone el resultado de autenticación).
- Los servicios se definen mediante interfaz (`service/interfaces`) y su implementación (`service/impl`).
- Cada implementación de servicio MUST implementar su propia interfaz de caso de uso (ej: `AuthService` / `AuthServiceImpl`, `JwtService` / `JwtServiceImpl`). Las implementaciones MUST NOT extender una clase base genérica de servicios únicamente para reutilizar operaciones comunes; esa reutilización MUST resolverse por composición cuando sea necesaria.
- La configuración principal de Spring Security MUST llamarse `SecurityConfig` (o equivalente del contexto) y residir en `configuration`. Prohibido crear nombres redundantes u obsoletos (ej: `ManejadorToken`, `PoliticaAcceso`).
- Los adaptadores de infraestructura e identidad que implementen contratos de Spring Security (por ejemplo, `UserDetailsService`) MUST ubicarse en `service/impl` y su interfaz en `service/interfaces`. Los filtros HTTP (`JwtAuthFilter`) e interfaces de firma JWT viven en `security` y `service/interfaces` respectivamente, y los manejadores de excepciones 401/403 (`JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`) en `security/handlers`.
- El dominio y la aplicación MUST depender solo de puertos (interfaces) y
  nunca de adaptadores concretos, clientes HTTP ni Playwright.

---

## Principio IV — Paginación obligatoria

- Toda consulta que devuelva múltiples registros (listados, búsquedas,
  filtros) MUST retornar `Page<T>`. **Prohibido** retornar `List<T>`, salvo
  catálogos fijos o enumeraciones acotadas por diseño (ej. lista de ligas
  soportadas).
- El **Service** MUST construir el `Pageable` al inicio del método con
  `PageRequest.of(page, tamano)` y pasarlo al Repository. El tamaño de página
  por defecto del MVP es **12**.
- El **Controller** MUST recibir `page` con valor por defecto 0, invocar al
  Service y mapear el resultado con `Page.map(...)` cuando deba convertir
  entidades a DTOs.
- El **Repository** MUST propagar el `Page<T>` desde el DAO/JPA, sin
  materializar listas ni paginar en memoria.
- Cada repositorio de dominio MUST declarar su contrato en
  `persistence/repository/interfaces` y su adaptador en
  `persistence/repository/impl`. El adaptador MUST recibir el DAO concreto por
  constructor y delegar el acceso a datos; no debe contener consultas JPQL ni
  administrar directamente un `EntityManager` cuando exista un DAO Spring
  Data bajo `persistence/sql`.

---

## Principio V — Manejo de errores

- Todas las excepciones personalizadas MUST ser no chequeadas (extender
  directa o indirectamente de `RuntimeException`) y vivir en `exceptions`.
- El manejo centralizado de errores (`@ControllerAdvice` o equivalente) MUST
  vivir también en `exceptions`, traduciendo excepciones de dominio a
  respuestas HTTP adecuadas.

---

## Principio VI — Validación por capas

Cada capa valida únicamente lo que puede conocer y controlar. **Prohibido**
mezclar concerns de transporte, orquestación y reglas de negocio:

- **DTO de request:** MUST validar forma y tipos del request, incluyendo
  trimming, longitud mínima/máxima, regex de emails y cadenas no vacías (ej. `@NotBlank`, `@NotNull`, `@Positive`, `@Size`, `@Email`). Esa responsabilidad de saneamiento y formato corresponde exclusivamente a los DTOs de entrada (`dtoRequest`) y a la capa de controladores (`controller`).
- **Service:** MUST validar la existencia de lo solicitado y la viabilidad de
  la acción; los IDs MUST resolver entidades existentes antes de continuar
  (fail-fast antes de tocar el dominio).
- **Model:** MUST proteger los invariantes del dominio y lanzar excepciones
  propias cuando una regla de negocio se incumpla (ej. saldo insuficiente,
  tokens insuficientes). **Prohibido** agregar o probar validaciones de formato de texto en el modelo.

---

## Principio VII — Testing obligatorio

- La suite de pruebas MUST estar acotada estrictamente a la jerarquía de paquetes `model` y `service`.
- **Alcance en `model`:** Mantener únicamente tests unitarios simples para métodos de negocio específicos que pertenezcan intrínsecamente al dominio/entidad (ej. cálculo de totales, cambio de estados, lógica pura de negocio). Si una clase de modelo solo contiene atributos, getters, setters y constructores generados por Lombok (`@Data`, `@Getter`, `@Setter`, `@Builder`), **NO debe tener archivo de test individual**.
- **Remoción de tests redundantes:** **Prohibido** probar en la capa de modelo validaciones de formato de cadenas o campos de texto. **Prohibido** incluir tests redundantes, boilerplate, de contexto básico de Spring Boot, documentación de configuración o infraestructura de seguridad genérica (ej: `BackendApplicationTests`, `ArquitecturaBaseTest`, `ValidacionInfraestructuraTest`, o clases de prueba dentro de `configuration` o `security`).
- Los tests de la capa `service` MUST testear únicamente métodos y servicios reales que estén implementados en la aplicación. El alcance actual incluye: `AuthServiceTest`, `EquipoServiceTest`, `JugadorServiceTest`, `LigaServiceTest`, `SincronizacionServiceTest`, `UsuarioDetailsServiceTest` y `UsuarioServiceTest`. Los adaptadores de identidad (`UsuarioDetailsServiceImpl`) también son testeables dentro de `service`.
- El adapter de scraping MUST probarse con tests unitarios basados en fixtures
  HTML/JSON locales, sin acceder a WhoScored durante CI. Los tests de integración
  que levanten Playwright o accedan a la web real MUST quedar marcados aparte y
  ser opcionales, nunca un requisito de la suite por defecto.
- Cada método expuesto en los servicios y modelos MUST contar con:
  - **Camino feliz (Happy path):** verificación del funcionamiento correcto con datos válidos.
  - **Casos de borde y excepciones (Edge cases):** búsqueda de recursos inexistentes, credenciales inválidas, duplicaciones, saldos/tokens insuficientes y excepciones del dominio.
- **Estilo conciso y directo:** usar bloques `@BeforeEach` para instanciar fixture de pruebas, Mocks simples (`@ExtendWith(MockitoExtension.class)`) para tests unitarios y nombres de métodos de prueba descriptivos en español sin caracteres no ASCII (ej. `testLoginConCredencialesInvalidasLanzaExcepcion`).

---

## Principio VIII — Integración con APIs externas

- El sistema integra actualmente Football-Data.org (API oficial v4) como fuente
  externa de datos de jugadores, equipos y ligas. Su integración MUST residir
  bajo `adapter/footballdata/`. WhoScored queda reservado exclusivamente para
  estadísticas de jugadores mediante `adapter/whoscored/`.
- Cada fuente externa MUST tener su propio subpaquete `adapter/<fuente>/` con
  `dto/`, configuración propia, excepción propia, puerto e implementación.
  Los DTOs MUST permanecer dentro de `adapter/<fuente>/dto/` y no filtrarse al
  dominio; el mapeo MUST ocurrir en la frontera del adaptador.
- El dominio y la aplicación MUST invocar únicamente puertos. Está prohibido
  invocar directamente implementaciones concretas, clientes HTTP o Playwright
  desde `model`, `service`, `controller` o `scheduler`.
- Cada integración MUST encapsular los errores de su librería externa en una
  excepción propia, conservando la causa original; ninguna excepción de la
  librería externa puede propagarse fuera del adaptador.
- **Antes de agregar** una nueva fuente externa o modificar los endpoints/credenciales/estrategia de sincronización de las existentes, speckit MUST preguntar y proponer alternativas antes de codificar. Este paso es bloqueante: no se avanza sin confirmación.

---

## Principio XIII — Scraping hexagonal con Playwright

El scraping de estadísticas de jugadores desde WhoScored MUST ser un módulo
reemplazable y aislado. La aplicación MUST depender únicamente de `ScrapingPort`;
`ScrapingRestAdapter` MUST ser sustituible por otra implementación (por ejemplo,
una API oficial) sin modificar el dominio ni los casos de uso. Playwright MUST
ser conocido únicamente por `ScrapingRestAdapter` y `ScrapingConfiguration`.
Ninguna entidad, servicio de dominio, repositorio, controller o scheduler MUST
importar Playwright.

La estructura de paquetes y nombres es obligatoria y exacta dentro de
`adapter/whoscored/`: `dto/` para DTOs de estadísticas sin lógica;
`ScrapingConfiguration.java` para beans de Playwright/Browser, propiedades,
timeouts y ciclo de vida; `ScrapingException.java` para los errores propios;
`ScrapingPort.java` para el contrato; y `ScrapingRestAdapter.java` para la
implementación con Playwright. No se MUST agregar otra capa o paquete al módulo
sin una enmienda a esta constitution que justifique la necesidad. La
configuración global existente en `configuration/` permanece separada.

Todas las excepciones de Playwright, navegación, parsing, timeout, red o
anti-bot MUST envolverse en `ScrapingException`, conservando la causa original y
un mensaje operativo claro; ninguna excepción de Playwright MUST escapar del
adapter. Playwright, Browser y BrowserContext MUST crearse y cerrarse de forma
controlada mediante beans con destrucción explícita o `try-with-resources` por
página/contexto, sin fugas de procesos Chromium.

Los timeouts, modo headless, user-agent, demoras, límites de rate limiting y
reintentos MUST externalizarse en `application.yml`; no se permiten valores
hardcodeados para esos controles. El adapter MUST aplicar rate limiting entre
requests, reintentos acotados con backoff y concurrencia conservadora. La
documentación de la feature MUST advertir que la protección anti-bot y los
términos de WhoScored pueden restringir el scraping, y el adapter MUST fallar
de forma controlada ante ese riesgo.

La extracción MUST priorizar datos estructurados (JSON embebido o respuestas de
red interceptadas) sobre selectores CSS frágiles. Los selectores y claves MUST
centralizarse en un único punto del adapter. Los DTOs MUST ser inmutables
(`record` cuando Java 21 lo permita), contener solo datos validados y permanecer
separados de las entidades de persistencia. El mapeo DTO-dominio MUST vivir
fuera del adapter.

Las URLs MUST aceptar slugs codificados, incluidos caracteres especiales como
`%C3%A9`, y el `playerId` numérico de WhoScored MUST ser el identificador
primario para el matching con `Jugador.idExterno`. Solo cuando no exista ese
identificador se permite fallback por nombre normalizado sin acentos junto con
fecha de nacimiento o equipo. Si no hay match o existe ambigüedad, el sistema
MUST registrar el motivo y MUST abstenerse de persistir estadísticas dudosas.

Cada scraping MUST registrar de forma estructurada la URL, duración y resultado,
sin incluir credenciales ni datos sensibles. Los tests MUST verificar happy path,
timeouts, HTML/JSON inválido, retry y errores envueltos usando fixtures locales;
el CI MUST permanecer independiente de la web real.

---

## Principio IX — Consultar ante ambigüedad arquitectónica

Ante cualquier duda de diseño o implementación no resuelta explícitamente por
esta constitution (por ejemplo: estrategia de autenticación, versionado de
estrategias de cotización, modelado de la tabla de auditoría, elección entre
`@ManyToOne` vs. relación desnormalizada, etc.), speckit MUST detenerse y
presentar al equipo al menos dos alternativas viables con sus tradeoffs antes
de generar código o artefactos. No se asume una decisión por default.

---

## Principio X — Idioma del proyecto

- Toda la documentación generada por speckit MUST estar redactada en español
  técnico, coherente y consistente, incluyendo constitution, specs, planes,
  tareas, checklists, decisiones, criterios de aceptación y reportes.
- Todo el código nuevo MUST utilizar nombres en español cuando el dominio lo
  requiera, incluyendo clases, métodos, variables, DTOs, excepciones, tests y
  mensajes funcionales.
- Los identificadores de código MUST usar únicamente caracteres ASCII. En
  particular, está prohibido utilizar la letra `ñ` o `Ñ` en nombres de
  paquetes, clases, métodos, variables, propiedades, endpoints, archivos de
  código y nombres de tests. Cuando una palabra española contenga `ñ`, debe
  emplearse una forma ASCII equivalente, por ejemplo `contrasena`.
- Los nombres propios de tecnologías, proveedores, estándares y APIs pueden
  conservar su denominación oficial, por ejemplo WhoScored, Football-Data.org,
  Spring Boot, REST y OpenAPI.
- Los comentarios, documentación y mensajes de código también MUST estar en
  español, salvo nombres oficiales o contenido técnico que deba conservarse
  literalmente.

---

## Principio XI — Desarrollo estrictamente acotado (No Over-Engineering / YAGNI)

- **Desarrollo estrictamente acotado (No Over-Engineering / YAGNI):**
  Queda estrictamente prohibido crear clases, modelos, DTOs, métodos, servicios, configuraciones o tests para funcionalidades que no se hayan solicitado explícitamente en el pedido actual. No agregues código "por si acaso" ni implementes cosas de forma anticipada. Enfócate única y exclusivamente en resolver lo que se pide en el prompt puntual, manteniendo el código lo más simple y directo posible.

---

## Principio XII — Arquitectura del frontend

El frontend MUST organizarse con la siguiente estructura bajo `src/`:

- `app/`: punto de entrada de la aplicación. Contiene `main.tsx`, `App.tsx`, `router.tsx` (definición de rutas con react-router-dom) y `providers.tsx` (wrapping de proveedores globales como `BrowserRouter`).
- `features/`: módulos de dominio de UI. Cada feature (ej. `auth`, `jugadores`) es un directorio autocontenido con `api/`, `components/`, `hooks/` y `types.ts`. Los módulos MUST exportar su API pública a través de `index.ts`.
- `components/`: componentes compartidos entre features (ej. `Header`, `Footer`, `Layout`). Cada componente MUST tener su propio directorio con `ComponentName.tsx`, `ComponentName.styles.ts` e `index.ts`.
- `pages/`: componentes de página, montados directamente en el router. Las páginas orquestan componentes de `features/` y `components/` pero no contienen lógica de negocio propia.
- `lib/`: utilitarios transversales (ej. `fetchClient.ts`).

**Reglas:**
- Los estilos MUST definirse en archivos `.styles.ts` como objetos con clases Tailwind, no en línea ni en archivos CSS separados por componente.
- El token JWT y el nombre de usuario autenticado MUST almacenarse en `localStorage` con las claves `token` y `nombreUsuario` respectivamente.
- Las páginas MUST usar el `Layout` compartido como wrapper de rutas en el router, no incluirlo individualmente.
- Las llamadas HTTP MUST realizarse a través de `fetchClient` (nunca `fetch` directo en componentes); los módulos de `api/` dentro de cada feature son los únicos que invocan a `fetchClient`.

---

## Requisitos no funcionales transversales

- **Configuración:** este proyecto MUST mantener una sola configuración base en
  `application.yml`, sin archivos `application-local.yml` ni
  perfiles separados a menos que el equipo apruebe explícitamente otra
  estrategia. La configuración local se documenta y mantiene en ese único
  archivo. El paquete `configuration/` MUST contener únicamente configuración
  global de la aplicación; la configuración propia de cada integración MUST
  vivir en `adapter/<fuente>/`.
- **Observabilidad:** logs estructurados, Correlation ID por request, health
  checks y métricas de latencia/tasa de error.
- **Auditoría:** registro inmutable de toda transacción financiera (autor,
  detalle del cambio, timestamp, estado anterior y posterior).
- **Seguridad:** validación estricta de inputs, manejo seguro de tokens de
  autenticación/autorización.
- **Documentación:** todos los endpoints MUST documentarse con OpenAPI
  (Swagger).
- **Scheduler:** sincronización periódica de datos desde Football-Data.org vía
  `FootballDataScheduler` (cron configurado en `application.yml` bajo
  `scheduler.football-data.cron`; actualmente domingos a medianoche zona Argentina).
  La habilitación del scheduler MUST configurarse en `SchedulingConfig`.
- **Scraping:** toda operación de WhoScored MUST respetar el Principio XIII y
  sus propiedades externalizadas; no se permite activar scraping real en CI.

---

## Governance

- Esta constitution prevalece sobre cualquier spec, plan o tarea que la
  contradiga.
- Toda modificación a este documento MUST versionarse (semver) y justificarse
  con un resumen del cambio y su motivo.
- Las specs y planes generados por speckit se revisan contra esta
  constitution antes de aprobarse; cualquier desviación MUST quedar
  explícitamente documentada y aprobada.
- Todo refactor estructural MUST verificarse compilando y ejecutando los tests
  antes y después del refactor. El commit del refactor estructural MUST estar
  separado del commit de cambios funcionales; si no existe un commit previo
  verificable, el cambio no cumple esta regla.
- Cada revisión de una feature de scraping MUST comprobar la estructura exacta
  de `adapter/<fuente>/`, la ausencia de dependencias concretas en dominio y
  aplicación, el cierre de recursos, la configuración externalizada, el
  matching por `playerId`, la política de errores y la cobertura con fixtures.
- Un cambio que agregue una fuente externa, altere la estrategia de matching,
  aumente la concurrencia o modifique los límites de scraping MUST documentar
  el impacto legal, operativo y de disponibilidad, y requerir aprobación antes
  de implementarse.
