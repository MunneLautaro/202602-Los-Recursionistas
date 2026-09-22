<!-- Sync Impact Report
Version change: 1.3.0 -> 1.4.0
Modified principles: Principle I - layer boundaries; Principle III - service contracts
Added sections: none
Removed sections: none
Follow-up TODOs: none
-->

# Constitution — Valoración de Mercado de Jugadores de Fútbol

**Versión:** 1.4.0
**Fecha de ratificación:** 2026-09-15
**Última modificación:** 2026-09-22

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

- **Backend:** Java, Spring Boot, Spring Security, PostgreSQL.
- **Frontend:** React, Vite, TypeScript, Tailwind CSS.
- Comunicación Frontend–Backend vía HTTP/REST.

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
  contratos) e `impl` (las implementaciones).
- `persistence`: acceso a datos. El subpaquete `repository` también se divide
  en `interfaces` e `impl`. Las distintas tecnologías o mecanismos de
  persistencia (JPA, scraping, APIs externas) MUST separarse en
  subpaquetes propios dentro de `persistence`.
- `configuration`: configuración de la aplicación (beans, schedulers,
  OpenAPI, etc.).
- `security`: configuración de autenticación, autorización y manejo de tokens.
- `exceptions`: excepciones personalizadas y manejo centralizado de errores.

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

- Los servicios MUST retornar únicamente entidades de dominio o
  `Page<Entidad>`. **Prohibido** retornar DTOs de response, generar JWTs o
  contener lógica de presentación.
- El mapeo a DTO es responsabilidad exclusiva del `controller`. La emisión de
  JWT es responsabilidad de la fachada/componente de `security`.
- Los servicios se definen mediante interfaz (`service/interfaces`) y su
  implementación (`service/impl`).
- Cada implementación de servicio MUST implementar su propia interfaz de caso
  de uso. Las implementaciones MUST NOT extender una clase base genérica de
  servicios únicamente para reutilizar operaciones comunes; esa reutilización
  MUST resolverse por composición cuando sea necesaria.
- Los adaptadores de infraestructura que implementen contratos externos
  usados por la aplicación (por ejemplo, `UserDetailsService`) MUST ubicarse
  en `service/impl` cuando orquesten una identidad o caso de uso. La
  configuración de Spring Security, los filtros, handlers y tokens permanecen
  en `security`.

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
  trimming y sanitización del input (ej. `@NotNull`, `@Positive`, `@Size`).
- **Service:** MUST validar la existencia de lo solicitado y la viabilidad de
  la acción; los IDs MUST resolver entidades existentes antes de continuar
  (fail-fast antes de tocar el dominio).
- **Model:** MUST proteger los invariantes del dominio y lanzar excepciones
  propias cuando una regla de negocio se incumpla (ej. saldo insuficiente,
  tokens insuficientes).

---

## Principio VII — Testing obligatorio

- Los tests se separan en `model` y `service`.
- Cada método público del modelo o del servicio MUST estar testeado.
- Cada funcionalidad MUST cubrir casos felices y de borde, incluyendo (como
  mínimo): saldo insuficiente, falta de tokens disponibles y fallas de
  proveedores externos (con degradación a datos locales).
- **Prohibido** modificar o borrar tests existentes sin permiso explícito y
  confirmación previa del equipo.

---

## Principio VIII — Integración con APIs externas

- El sistema integra WhoScored (scraping) y Football-Data.org (API oficial),
  siempre a través de `persistence` bajo un subpaquete de `adapters`/fuente
  externa, nunca invocadas directamente desde `service` o `controller`.
- **Antes de implementar** cualquier integración concreta con una API o
  scraper externo (endpoints a usar, estrategia de reintentos, rate
  limiting, formato de credenciales), speckit MUST
  preguntar y proponer alternativas antes de codificar. Este paso es
  bloqueante: no se avanza sin confirmación.

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

## Requisitos no funcionales transversales

- **Configuración:** este proyecto MUST mantener una sola configuración base en
  `application.properties`, sin archivos `application-local.properties` ni
  perfiles separados a menos que el equipo apruebe explícitamente otra
  estrategia. La configuración local se documenta y mantiene en ese único
  archivo.
- **Observabilidad:** logs estructurados, Correlation ID por request, health
  checks y métricas de latencia/tasa de error.
- **Auditoría:** registro inmutable de toda transacción financiera (autor,
  detalle del cambio, timestamp, estado anterior y posterior).
- **Seguridad:** validación estricta de inputs, manejo seguro de tokens de
  autenticación/autorización.
- **Documentación:** todos los endpoints MUST documentarse con OpenAPI
  (Swagger).
- **Scheduler:** recálculo periódico de cotizaciones y actualización de datos
  vía job scheduler, configurado en `configuration`.

---

## Governance

- Esta constitution prevalece sobre cualquier spec, plan o tarea que la
  contradiga.
- Toda modificación a este documento MUST versionarse (semver) y justificarse
  con un resumen del cambio y su motivo.
- Las specs y planes generados por speckit se revisan contra esta
  constitution antes de aprobarse; cualquier desviación MUST quedar
  explícitamente documentada y aprobada.
