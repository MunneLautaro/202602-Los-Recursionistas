# Research: Observabilidad del backend con Actuator y métricas Prometheus

**Feature**: [spec.md](spec.md) | **Fecha**: 2026-11-24

Fuentes: documentación oficial de Spring Boot
(https://docs.spring.io/spring-boot/reference/actuator/), guía de migración a
Spring Boot 4.0 y verificación directa sobre los jars **4.1.1** resueltos por el
proyecto (metadata `META-INF/spring-configuration-metadata.json` y clases de
`spring-boot-actuator`, `spring-boot-health`, `spring-boot-micrometer-metrics` y
`spring-boot-security`). Versiones resueltas: Spring Boot 4.1.1, Micrometer
1.17.1.

---

## R1. Dependencia de Prometheus y módulos de Spring Boot 4

- **Decision**: agregar solo `implementation 'io.micrometer:micrometer-registry-prometheus'`
  sin versión (la gestiona el BOM de Spring Boot).
- **Rationale**: `spring-boot-starter-actuator` 4.1.1 ya trae de forma
  transitiva `spring-boot-starter-micrometer-metrics`, `spring-boot-health` y
  `micrometer-core`. La autoconfiguración
  `org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration`
  vive en `spring-boot-micrometer-metrics` y se activa al detectar el registro
  Prometheus en el classpath. La constitution v2.8.0 aprueba exactamente esta
  dependencia.
- **Alternatives considered**: declarar `spring-boot-starter-micrometer-metrics`
  explícitamente (redundante, ya es transitivo); usar otro registro de métricas
  (prohibido por la constitution).

## R2. Exposición y acceso a endpoints

- **Decision**:
  - `management.endpoints.web.exposure.include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics,prometheus}`.
  - `management.endpoints.access.default: none` (fijo, sin variable de entorno).
  - `management.endpoint.{health,info,metrics,prometheus}.access: read-only`.
- **Rationale**: el pedido conserva la variable de entorno de exposición, pero la
  spec (FR-003) exige que no permita ampliar la lista aprobada. En Spring Boot 4
  "los endpoints inaccesibles se eliminan por completo del contexto de la
  aplicación"; con acceso `none` por defecto y `read-only` solo para los cuatro
  aprobados, aunque alguien configure la variable con `*` o `env`, esos
  endpoints no existen y responden 404. La exposición controla *qué* se publica
  por HTTP y el acceso controla *qué existe*: es defensa en profundidad.
- **Alternatives considered**: eliminar la variable y fijar la lista (descartado:
  el pedido pide conservarla); `management.endpoints.web.exposure.exclude` con
  una lista negra (descartado: nunca es exhaustiva y cada versión agrega
  endpoints nuevos como `sbom` o `startup`).

## R3. Health: detalles solo para `ROLE_ADMIN`

- **Decision**:
  - `management.endpoint.health.show-details: when-authorized`
  - `management.endpoint.health.show-components: when-authorized`
  - `management.endpoint.health.roles: ROLE_ADMIN`
  - `management.endpoint.health.probes.enabled: false`
- **Rationale**:
  - Verificado en la clase `org.springframework.boot.actuate.endpoint.Show` de
    4.1.1: el chequeo de autorización llama a `SecurityContext.isUserInRole(role)`
    **y además**, cuando el principal es una `Authentication` de Spring Security,
    compara cada `GrantedAuthority` por nombre exacto (`checkAuthorities`). Por
    eso `ROLE_ADMIN` coincide con la authority que carga `JwtAuthFilter` desde
    `Usuario.autoridades` sin depender del prefijo automático de
    `isUserInRole`. Se usa el nombre completo para que coincida literalmente
    con `hasAuthority("ROLE_ADMIN")` de `SecurityConfig`.
  - `show-components` por defecto hereda `show-details`; se declara explícito
    para que la política sea legible.
  - En 4.1.1 `management.endpoint.health.probes.enabled` es `true` por defecto
    (en Boot 2.3–3.x solo se activaba en Kubernetes). Con probes activos la
    respuesta anónima agrega `"groups": ["liveness","readiness"]`, lo que
    contradice FR-004 ("únicamente el estado global"). El proyecto no corre en
    Kubernetes, así que se desactivan (Principio XI).
  - `JwtAuthFilter` no corta el request ante un token inválido: limpia el
    contexto y continúa. Un token inválido o vencido en `/actuator/health` se
    comporta como anónimo, como pide el edge case de la spec.
- **Alternatives considered**: `roles: ADMIN` (funciona vía `isUserInRole`,
  pero depende del prefijo implícito); `show-details: always` (expone
  componentes a anónimos, prohibido por la constitution).

## R4. Info: build-info de Gradle vs. propiedades `info.*`

- **Decision**: `springBoot { buildInfo() }` en `backend/build.gradle`.
- **Rationale**: genera `META-INF/build-info.properties` con `build.artifact`,
  `build.group`, `build.name`, `build.version` y `build.time` tomados del propio
  build, sin duplicar datos. El contribuidor `build` está habilitado por defecto
  (`management.info.build.enabled=true`). Las propiedades `info.*` requieren
  `management.info.env.enabled=true` (por defecto `false` desde Boot 2.6), que
  publica cualquier propiedad con prefijo `info.`; es una superficie de
  filtración más amplia y obliga a mantener la versión a mano. Los
  contribuidores `java`, `os`, `process` y `env` siguen deshabilitados por
  defecto; `git` está habilitado pero no aporta nada sin `git.properties`.
- **Alternatives considered**: `info.app.*` en `application.yml` (descartado por
  lo anterior). Excluir `time` (`excludes = ['time']`) para que la tarea sea
  incremental: descartado porque la spec pide la fecha de construcción.

## R5. Tag común y métricas incluidas

- **Decision**: `management.metrics.tags.application: ${spring.application.name}`.
  No se configura ninguna propiedad `management.metrics.distribution.*`.
- **Rationale**:
  - Verificado en la metadata 4.1.1: `management.metrics.tags` sigue siendo
    `Map<String,String>` y no está deprecada. Spring Boot no agrega un tag
    `application` automáticamente.
  - Las métricas pedidas se autoconfiguran sin código: `http.server.requests`
    (tags `method`, `uri`, `status`, `outcome`, `exception`), `jvm.*`,
    `process.*`, `system.*`, `disk.*`, `hikaricp.*` y `jdbc.connections.*`.
  - `uri` usa la plantilla de ruta, así que `/players/{id}` es una sola serie.
  - Sin propiedades de distribución no se publican buckets ni percentiles: el
    timer expone `_count`, `_sum` y `_max`, suficientes para la latencia media
    (`sum/count`) y la tasa de error (`outcome="SERVER_ERROR"` o
    `"CLIENT_ERROR"` sobre el total).
- **Alternatives considered**: `management.observations.key-values.application`
  (aplica a observaciones, no a todos los meters: JVM y Hikari no lo
  recibirían).

## R6. Seguridad: `EndpointRequest` vs. matchers por path

- **Decision**: matcher por path. En `SecurityConfig`, `/actuator/health` se
  mantiene en el grupo `permitAll` existente y se agrega
  `.requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")` inmediatamente
  después de los grupos `permitAll` y antes de `anyRequest().authenticated()`.
- **Rationale**:
  - En 4.x `EndpointRequest` se movió a
    `org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest`
    (módulo `spring-boot-security`) y solo matchea endpoints *existentes y
    expuestos*. Una ruta como `/actuator/env` caería en
    `anyRequest().authenticated()`, y un `ROLE_USUARIO` obtendría 404 en vez de
    403: revela qué existe y deja la decisión en una regla genérica.
  - `/actuator/**` con `hasAuthority("ROLE_ADMIN")` deniega por defecto todo el
    espacio de Actuator, sigue el estilo de matchers por string que ya usa
    `SecurityConfig` y no agrega imports de autoconfiguración en
    `configuration/`.
  - Como Spring Security evalúa las reglas en orden de declaración,
    `/actuator/health` (match exacto) resuelve primero.
  - Las subrutas `/actuator/health/**` caen en la regla de `ROLE_ADMIN`, así que
    un anónimo no puede consultar un componente individual (edge case de la
    spec).
  - Los handlers 401/403 existentes (`JwtAuthenticationEntryPoint`,
    `JwtAccessDeniedHandler`) se aplican sin cambios porque ya están
    registrados en `exceptionHandling` de la misma cadena.
- **Consecuencia sobre la spec**: un endpoint no aprobado responde **404 a un
  administrador** (no existe). Un anónimo recibe 401 y un `ROLE_USUARIO` 403
  antes de llegar al dispatcher, lo que no revela qué endpoints existen. Se
  ajustaron FR-002, US4-4 y SC-002 de la spec.
- **Alternatives considered**: `EndpointRequest.toAnyEndpoint().excluding("health")`
  (descartado por lo anterior); una `SecurityFilterChain` separada para
  Actuator (descartado: duplica handlers, filtro JWT y CORS).

## R7. Test `ObservabilidadSeguridadTest` (Principio VII, excepción v2.8.0)

- **Decision**: un único test en
  `backend/src/test/java/unq/losrecursionistas/backend/observabilidad/ObservabilidadSeguridadTest.java`
  con `@SpringBootTest` + `@AutoConfigureMockMvc`
  (`org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`).
  - **Base de datos**: H2 en memoria (`testRuntimeOnly 'com.h2database:h2'` ya
    existe), configurada con `properties` del propio test.
  - **Tareas de arranque**: se desactivan schedulers y cargas iniciales
    (`scheduler.whoscored.enabled=false`, `scheduler.cotizacion.enabled=false`,
    `scheduler.football-data.startup-enabled=false`,
    `whoscored.scraping.startup-enabled=false`).
  - **Usuarios**: `JwtService` y `UsuarioDetailsService` (el subtipo del
    proyecto, que extiende `UserDetailsService`, para no romper los beans que lo
    inyectan) se reemplazan con `@MockitoBean` para emitir un usuario `ROLE_ADMIN` y otro `ROLE_USUARIO`
    a partir de tokens de prueba. El `JwtAuthFilter` real, `SecurityConfig` y
    los handlers 401/403 se ejercitan sin cambios.
- **Rationale**:
  - Es el único test que admite la constitution v2.8.0.
  - En Boot 4 `@SpringBootTest` ya no provee MockMvc y `@MockBean` fue
    reemplazado por `@MockitoBean`.
  - No hay ningún `@SpringBootTest` previo en el proyecto, así que el test
    declara sus propias propiedades. Las propiedades inline del test tienen
    precedencia sobre `application.yml`, el `.env` importado y las variables de
    entorno.
  - Usar mocks de JWT evita depender del esquema de usuarios y de contraseñas.
- **Casos** (exactamente los de la constitution):
  - `health` anónimo → 200 y sin `components`.
  - `info`, `metrics` y `prometheus` → 401 sin token, 403 con `ROLE_USUARIO`
    y 200 con `ROLE_ADMIN`.
  - `env` con `ROLE_ADMIN` → 404.
- **Riesgos a verificar al implementar**:
  1. **Registro Prometheus en tests.** La doc indica que, con `@SpringBootTest`,
     no se autoconfiguran registros que no sean en memoria; ese comportamiento
     lo aporta el módulo de test de métricas (`@AutoConfigureMetrics`, paquete
     `org.springframework.boot.micrometer.metrics.test.autoconfigure`), que hoy
     **no está** en el classpath de test. Si `/actuator/prometheus` devuelve 404
     en el test, se agrega
     `testImplementation 'org.springframework.boot:spring-boot-starter-micrometer-metrics-test'`
     y `@AutoConfigureMetrics`, y se documenta.
  2. **Compatibilidad del esquema con H2.** Algunas entidades usan
     `columnDefinition = "jsonb"`, que H2 no soporta. Hibernate registra el
     error de DDL sin abortar el arranque y el test no usa esas tablas. Si el
     contexto no levantara, la alternativa es Testcontainers PostgreSQL (ya
     declarado en `build.gradle`, pero requiere Docker en CI).
- **Resultado de la implementación (2026-11-24)**: los 11 casos pasan, con
  rutas protegidas parametrizadas mediante un único `@MethodSource`. Ninguno de
  los dos riesgos se materializó:
  1. El registro Prometheus se autoconfigura en `@SpringBootTest` sin el módulo
     de test de métricas, así que no se agregó ninguna dependencia de test.
  2. Hibernate registra en el log los errores de DDL `jsonb` en H2, pero el
     contexto levanta.
- **Alternatives considered**: `@WebMvcTest` (no incluye los endpoints de
  Actuator); tokens reales con usuarios persistidos (más preparación y
  acoplamiento al esquema); `spring-security-test` con `@WithMockUser`
  (dependencia nueva y saltea `JwtAuthFilter`).

## R8. Swagger

- **Decision**: sin cambios; `springdoc.show-actuator: true` ya está activo.
- **Rationale**: springdoc documenta solo los endpoints de Actuator expuestos y
  disponibles, así que aparecen `health`, `info`, `metrics` y `prometheus`, y
  ningún otro.

## R9. Datos sensibles en las respuestas

- **Decision**: no se requieren sanitizadores adicionales.
- **Rationale**:
  - `env` y `configprops`, los endpoints que muestran valores de propiedades,
    no existen (R2).
  - `info` solo publica datos de build (R4).
  - Los tags de métricas son plantillas de URI, métodos, status, nombre del
    pool y nombre de la aplicación; ninguno incluye URLs JDBC, usuarios ni
    tokens.
  - El detalle de `health` para ADMIN muestra el motor de base de datos
    (`PostgreSQL`) y la ruta y el espacio libre del disco, pero no la URL ni
    las credenciales.

## Diferencias respecto de Baeldung (escrito para Spring Boot 2.x/3.x)

| Tema | Baeldung | Spring Boot 4.1.1 (verificado) | Impacto en esta feature |
|------|----------|--------------------------------|-------------------------|
| Habilitar endpoints | `management.endpoint.<id>.enabled`, `management.endpoints.enabled-by-default` | `management.endpoint.<id>.access` (`none`/`read-only`/`unrestricted`) y `management.endpoints.access.default`; los inaccesibles se quitan del contexto | Se usa `access` (R2) |
| Ejemplos de exposición | `include: "*"` | Default `health`; `*` sigue siendo válido | Lista explícita de cuatro (R2) |
| Módulos | Todo en `spring-boot-actuator(-autoconfigure)` | Separado en `spring-boot-health`, `spring-boot-micrometer-metrics`, `spring-boot-security`, etc. | Solo cambia paquetes; la dependencia Prometheus es la misma (R1) |
| Paquete de health | `org.springframework.boot.actuate.health.*` | `org.springframework.boot.health.contributor.*` | Sin impacto (no hay indicadores propios) |
| Probes liveness/readiness | Solo en Kubernetes | Habilitadas por defecto (`probes.enabled=true`) | Se desactivan para no exponer `groups` (R3) |
| `EndpointRequest` | `org.springframework.boot.actuate.autoconfigure.security.servlet` | `org.springframework.boot.security.autoconfigure.actuate.web.servlet` | No se usa (R6) |
| Info desde propiedades | `info.app.*` aparece directamente (Boot < 2.6) | Requiere `management.info.env.enabled=true` | Se usa build-info de Gradle (R4) |
| Cliente Prometheus | `simpleclient` 0.x | Prometheus Java client 1.x (Micrometer ≥ 1.13; el proyecto resuelve 1.17.1) | Los nombres de series siguen la convención 1.x (`_total` en counters) |
| Tests | `@SpringBootTest` provee MockMvc; `@MockBean` | Requiere `@AutoConfigureMockMvc`; `@MockBean` eliminado en favor de `@MockitoBean` | Aplicado en R7 |
| Jackson | Jackson 2 (`com.fasterxml`) | Jackson 3 (`tools.jackson`); Actuator usa un `JsonMapper` aislado | Sin impacto |
| Tags comunes | `management.metrics.tags.*` o bean `MeterRegistryCustomizer` | `management.metrics.tags.*` vigente | Se usa la propiedad, sin clases nuevas (R5) |
