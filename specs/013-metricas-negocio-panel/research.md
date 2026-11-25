# Research: Métricas de negocio y panel de observabilidad con gráficos

**Feature**: [spec.md](spec.md) | **Fecha**: 2026-11-24

Este documento registra las decisiones técnicas de la feature. Las tres decisiones de arquitectura abiertas
(D1–D3) las eligió el usuario el 2026-11-24 entre alternativas presentadas, como pide el Principio IX.
Las bases técnicas vienen de la feature 012: Spring Boot 4.1.1, Micrometer 1.17.1, Actuator con
`access.default: none` y `/actuator/**` restringido a `ROLE_ADMIN`.

---

## D1. Fuente de las métricas técnicas para el frontend

- **Decision**: crear un endpoint REST admin `GET /api/admin/observabilidad/tecnico`. Lee el
  `MeterRegistry` y la salud en el backend y devuelve un DTO tipado y resumido.
- **Rationale**:
  - El frontend hace una sola llamada cada 30 segundos, sin parsear el formato Prometheus.
  - El contrato es estable aunque cambien los nombres internos de Actuator.
  - La lógica de resumen (top de endpoints, tasas, exclusiones) se puede probar con tests de `service`
    usando `SimpleMeterRegistry`.
  - No es un endpoint de Actuator nuevo, así que cumple la política de Observabilidad v2.8.0.
- **Alternatives considered**:
  - Que el frontend parsee `/actuator/prometheus`: es frágil y descarga todas las series en cada refresco.
  - Que el frontend llame a `/actuator/metrics/{n}`: serían entre 15 y 30 requests cada 30 segundos.

## D2. Registro de métricas de negocio desde `service`

- **Decision**:
  - **Puerto**: `MetricasNegocio` en `service/interfaces`.
  - **Implementación**: `MetricasNegocioMicrometer` en `service/impl`, la única clase de negocio que
    conoce `MeterRegistry`.
  - **Momento del registro**: los eventos que dependen de una transacción (orden creada, emparejamiento)
    se registran después del commit mediante
    `TransactionSynchronizationManager.registerSynchronization(afterCommit)`. Si no hay una transacción
    activa, se registran en el momento.
  - **Eventos que se registran en el momento**: los rechazos y los recálculos. Un rechazo provoca
    rollback por definición y el recálculo no es transaccional.
- **Rationale**:
  - El Principio III exige que `service` dependa de puertos. `MercadoServiceImpl`,
    `MatchingEngineImpl` y `CotizacionServiceImpl` solo ven la interfaz.
  - Contar después del commit evita sobrecontar órdenes o emparejamientos que hacen rollback, como un
    fallo de matching o de persistencia (Principio XV).
  - Los tests de los servicios existentes mockean el puerto; la implementación se prueba con
    `SimpleMeterRegistry`.
  - ArchUnit (`consideringOnlyDependenciesInLayers`) no restringe dependencias de `service` hacia
    librerías externas.
- **Alternatives considered**:
  - Eventos de dominio con un listener transaccional: más clases, va contra el Principio XI.
  - `MeterRegistry` directo en los services: acopla `service` a Micrometer y cuenta aunque la
    transacción haga rollback.

## D3. Series históricas diarias

- **Decision**: usar SQL nativo de PostgreSQL agregado por día en los DAO.
  - **Proyecciones**: son interfaces en `persistence/sql`. Los repositorios las mapean a records de
    lectura en `model`, siguiendo el precedente de `RankingCotizacion` y `RankingProyeccion`.
  - **Completado de días**: el service genera los días sin actividad con valor cero.
- **Rationale**:
  - Cada serie tiene como máximo 90 filas por tipo, acotadas por diseño. Esto entra en la excepción del
    Principio IV ("enumeraciones acotadas por diseño"), así que no se pagina.
  - Agregar en la base evita traer todas las órdenes o transacciones del período.
- **Detalles por tabla**:
  - `ordenes.fecha_creacion` y `transacciones_mercado.ejecutada_en` son `OffsetDateTime`, es decir
    `timestamptz`. El día se calcula con `CAST((col AT TIME ZONE 'America/Argentina/Buenos_Aires') AS DATE)`.
  - `cotizaciones.timestamp_calculo` es `LocalDateTime`, que se persiste en hora local de Argentina
    (`hibernate.jdbc.time_zone`). El día se calcula con `CAST(timestamp_calculo AS DATE)`.
  - La variación diaria de cotización usa `LAG(valor) OVER (PARTITION BY jugador_id ORDER BY
    timestamp_calculo, id)` sobre **toda** la historia del jugador y después filtra el período. Así la
    primera cotización del período también tiene un valor previo.
- **Riesgo**: el SQL no se cubre con tests automáticos, porque el Principio VII limita los tests a
  `model` y `service`. Se valida con la verificación manual del [quickstart](quickstart.md) contra
  PostgreSQL.
- **Alternatives considered**: JPQL que trae las filas y agrega en Java. Carga todas las filas del
  período y roza el Principio IV.

---

## R1. Definiciones de las métricas

| Métrica | Cuándo se registra | Dónde |
|---------|--------------------|-------|
| Órdenes creadas por tipo | Después del commit de `crearOrden` | `MercadoServiceImpl.crearOrden`. Las de liquidez inicial no pasan por este método y no se cuentan (FR-001). |
| Emparejamientos y volumen | Después del commit, por cada `TransaccionMercado` creada | `MatchingEngineImpl.ejecutar`, con volumen = `transaccion.getTotal()` |
| Rechazos por saldo insuficiente | En el momento | `MercadoServiceImpl.crearOrden`, en la rama `COMPRA`, al capturar la `ExcepcionDominio` de `Usuario.reservar(...)`. Después se relanza la misma excepción sin cambiar el contrato HTTP. Es la única excepción posible en esa línea, porque cantidad y precio ya se validaron antes. |
| Recálculos exitosos o fallidos y duración | En el momento | `CotizacionServiceImpl.recalcular(List, LocalDate, LocalDate)`, que es el punto común de scheduler, worker asíncrono, carga inicial y controller. Cuenta como **fallido** si el método lanza una excepción; los errores por jugador que ya se loguean no hacen fallar el recálculo. |
| Jugadores cotizados | Al terminar un recálculo exitoso | Cantidad de cotizaciones guardadas en ese recálculo |
| Variación promedio | Al terminar un recálculo exitoso | Antes de guardar cada cotización se consulta `cotizaciones.vigente(jugadorId)` y se acumula `(nuevo − previo) / previo`. Se excluyen los jugadores sin valor previo o con previo igual a 0. Agrega una consulta por jugador en cada recálculo semanal, un costo aceptado. |
| Tokens en circulación | Gauge calculado en cada lectura | `SUM(cantidad_total)` de `tenencias_token`, excluyendo la cuenta custodio (`RepositorioUsuario.buscarCustodio()`) |
| Estrategia activa | Solo en el panel (no es una métrica; R8, K4) | `EstrategiaAdminService.consultar()` |

**Etiquetas**: solo `tipo` (`compra`/`venta`), `motivo` (`saldo_insuficiente`) y `resultado`
(`exitoso`/`fallido`). Ninguna etiqueta identifica
usuarios ni jugadores (FR-014). El tag `application` lo agrega la configuración de la feature 012.

**Sin histogramas**: el `Timer` de duración se registra sin `publishPercentiles` ni
`publishPercentileHistogram`. La feature 012 y la constitución lo dejan fuera de alcance.

## R2. Lectura de salud desde el service

- **Decision**: `ObservabilidadServiceImpl` inyecta
  `org.springframework.boot.health.actuate.endpoint.HealthEndpoint` y llama a `health()`. Mapea solo el
  estado global y el estado (`UP`/`DOWN`/…) de cada componente, **sin detalles**, para no exponer la ruta
  del disco ni el motor de la base.
- **Rationale**:
  - La llamada programática a `HealthEndpoint.health()` no pasa por la extensión web, así que no la
    afecta `show-components: when-authorized`. Devuelve todos los componentes.
  - El acceso ya está restringido a `ROLE_ADMIN` por el endpoint admin.
- **Riesgo a verificar al implementar**: si en 4.1.1 `health()` no devuelve componentes, el fallback es
  `HealthContributorRegistry` (`org.springframework.boot.health.registry`) e invocar cada
  `HealthIndicator`.

## R3. Resumen técnico (`MeterRegistry`)

| Sección | Fuente |
|---------|--------|
| Requests | Timers `http.server.requests` agrupados por `method` + `uri`. Se suman `count()` y `totalTime(MILLISECONDS)` por grupo; los errores se cuentan por `outcome` (`CLIENT_ERROR`, `SERVER_ERROR`). Se excluyen las URIs `/actuator/**` y `/api/admin/observabilidad/**` para que el refresco del panel no infle los datos. Se devuelve el top 10 por cantidad y el total global. |
| JVM | `jvm.memory.used` y `jvm.memory.max` con `area=heap` (suma), `jvm.threads.live`, `process.cpu.usage`, `system.cpu.usage`, `process.uptime` |
| Pool | `hikaricp.connections.active`, `.idle`, `.pending`, `.max` |

Si un meter no existe, por ejemplo en un test o con un pool distinto, se devuelve `null` y el frontend
muestra "sin datos".

## R4. Endpoints admin y seguridad

- **Decision**: crear `ObservabilidadAdminController` en `controller`, con
  `@RequestMapping("/api/admin/observabilidad")`, `GET /negocio?dias=` y `GET /tecnico`. En
  `SecurityConfig` se agrega `"/api/admin/observabilidad/**"` a la lista existente que exige
  `hasAuthority("ROLE_ADMIN")`.
- **Rationale**:
  - Sigue el patrón de `EstrategiaAdminController` (`/api/admin/cotizacion`).
  - Los 401 y 403 los resuelven los handlers existentes.
  - `dias` acepta solo 7, 30 o 90. Cualquier otro valor lanza `ExcepcionValidacion` y responde 400 por
    `ControladorErrores`.
  - Los endpoints se documentan en OpenAPI con anotaciones, como el resto de los controllers.

## R5. Frontend

- **Ubicación**: página nueva `pages/ObservabilidadPage.tsx` en la ruta `/admin/observabilidad`,
  envuelta en `AdminRoute`, con un acceso desde la cabecera de `AdminPage`.
  - **Rationale**: `AdminPage` ya concentra operaciones y estrategia; una página propia evita un
    componente monolítico (Principio XII) y sigue dentro del área admin.
- **Módulo**: `features/admin/` con `api/observabilidadApi.ts` (único que llama a `fetchClient`),
  `hooks/useObservabilidad.ts` (polling), `types.ts` ampliado y los componentes en
  `features/admin/components/` con su `.styles.ts`, igual que `EstrategiaCotizacionPanel`.
- **Polling**: `setInterval` de 30 segundos que se pausa con `document.visibilityState !== "visible"`.
  - **Errores por sección**: negocio y técnico se piden por separado y cada sección guarda su propio
    estado de error (FR-021).
  - **Sesión vencida**: un 401 detiene el polling y redirige al login, reutilizando el manejo vigente de
    sesión de `fetchClient` y `AuthContext`.
- **Evolución de la sesión**: buffer en memoria de las últimas 60 muestras técnicas, unos 30 minutos,
  para graficar memoria, CPU y pool. Se pierde al recargar la página, un supuesto aceptado en la spec.
- **Gráficos**: Recharts 3, que ya es dependencia del proyecto.
  - **Tipos de gráfico**: `BarChart` apilado para órdenes compra/venta, `ComposedChart` con barras de
    emparejamientos y línea de volumen, `LineChart` para cotización y recursos, y `BarChart` horizontal
    para el top de endpoints.
  - **Estilo**: se reutilizan los tokens visuales de `QuoteHistoryChart` (superficie, grilla, ejes,
    tooltip), las clases `ui-card` y `ui-tile`, y los colores `mint`, `coral` y `lavender` ya usados en
    el admin. Al implementar se consulta la skill `pantone-color-palette`.
- **Sin dependencias nuevas** y sin framework de tests de frontend (no existe en el proyecto). La
  verificación es `yarn lint` + `yarn build` + quickstart.

## R6. Tests (Principio VII)

| Test | Tipo | Cubre |
|------|------|-------|
| `MetricasNegocioMicrometerTest` (nuevo) | service, `SimpleMeterRegistry` | Contadores por tipo, motivo y resultado; volumen; timer; gauges; registro inmediato sin transacción; ausencia de tags de usuario o jugador |
| `ObservabilidadServiceImplTest` (nuevo) | service, Mockito + `SimpleMeterRegistry` | Validación de `dias` (7/30/90 y 400 para otros valores); completado de días vacíos y zona horaria; totales del período; mapeo de salud sin detalles; top 10 y exclusiones de URIs; meters ausentes devuelven `null` |
| `MercadoServiceCompraTest` / `MercadoServiceVentaTest` (ampliados) | service | Orden creada registra por tipo; un rechazo por saldo registra un rechazo y relanza; una venta sin tokens no registra rechazo |
| `MatchingEngineTest` (ampliado) | service | Cada transacción registra un emparejamiento con su total |
| `CotizacionServiceImplTest` (ampliado) | service | Recálculo exitoso registra duración, jugadores cotizados y variación promedio excluyendo jugadores sin previo; un recálculo que lanza excepción registra fallido |

No se agregan tests de `controller`, `configuration`, `security` ni `persistence`.
`ObservabilidadSeguridadTest` (012) no se modifica: el control de acceso del endpoint nuevo usa el mismo
mecanismo y se verifica en el quickstart.

## R8. Remediación del análisis `/speckit-analyze` (2026-11-24)

El usuario aprobó aplicar todas las recomendaciones:

| Hallazgo | Resolución |
|----------|-----------|
| K1: los endpoints de resumen están fuera de Actuator | Enmienda **v2.9.0** de la constitución: excepción acotada para `/api/admin/observabilidad/**` (solo `ROLE_ADMIN`, solo lectura, sin detalles de salud, solo datos que Actuator o Micrometer ya publican y agregados de negocio). |
| K2: `features/admin` sin `index.ts` | Se crea `frontend/src/features/admin/index.ts` con la API pública del panel, y `ObservabilidadPage` importa solo desde ahí. |
| I1: con la base caída, el panel desloguea | En cada ciclo el hook consulta primero `GET /actuator/health`, que es público y no falla por autenticación. Si devuelve `DOWN` (503), muestra "Servicio no disponible" y no llama a los endpoints protegidos, así que no hay 401 ni se invalida la sesión. |
| K3: validación de `dias` en el service | Se mueve al controller (Principio VI); el service asume un valor válido. |
| K4: estrategia como tag de métrica | Se elimina el gauge `cotizacion.estrategia.activa`; la estrategia se muestra solo en el panel. |
| C1: la lógica after-commit no tiene tests | `MetricasNegocioMicrometerTest` simula la sincronización transaccional: no cuenta antes de `afterCommit` ni ante un rollback. |
| K5: límite de 90 días solo en quien llama | Los repositorios lanzan `IllegalArgumentException` si el rango supera 90 días. |
| G1: SC-006 sin medición | Se agrega la medición en quickstart §6 y en T049. |
| F1: `periodo.jugadoresCotizados` ambiguo | Se renombra a `cotizacionesCalculadas`. |
| A1: alcance de la duración promedio | Promedio de recálculos exitosos. |
| A2: test de los read-models | Sin test de modelo: solo atributos y factory (data-model). |

## R9. Desvíos y hallazgos de la implementación (2026-11-24)

| Tema | Diseño | Implementado | Motivo |
|------|--------|--------------|--------|
| Custodio en consultas de solo lectura | `RepositorioUsuario.buscarCustodio()` | Nuevo `RepositorioUsuario.idCustodio()` (`SELECT MIN(u.id) … WHERE autoridad = 'ROLE_ADMIN'`, sin bloqueo). `buscarCustodio()` sigue intacto para la liquidez inicial. | `buscarCustodio()` usa `@Lock(PESSIMISTIC_WRITE)`, que en PostgreSQL es `FOR NO KEY UPDATE`. Dentro de una transacción `readOnly` falla y la aborta. Contra PostgreSQL, `/negocio` respondía 500 y el gauge de tokens daba 0. Los tests con mocks y H2 no lo detectaban. Se encontró en la verificación manual. |
| Inyección del servicio del panel | Repositorios sueltos en el constructor | Record `@Component` `RepositoriosObservabilidad` y `ObjectProvider<HealthEndpoint>` | Mantener el constructor en 7 parámetros o menos (Sonar S107) y tolerar que no exista el bean de salud. |
| `ActividadDiariaCotizacion` | `(dia, jugadoresCotizados, variacionPromedio)` | Agrega `conPrevio` | Ponderar la variación del período (T029/T031). No se expone en el contrato REST. |
| Utilidades de los repositorios | — | `persistence/repository/impl/AgregadosDiarios` | Límite de 90 días (K5) y conversión de tipos nativos (`java.sql.Date` → `LocalDate`, `Number`). |
| Salud | `HealthEndpoint.health()` | Funciona tal como estaba previsto. Devuelve `db`, `diskSpace`, `ping` y `ssl`; no hizo falta el fallback. | Verificado contra PostgreSQL. En tests, `HealthDescriptor` está sellado: se mockean `IndicatedHealthDescriptor` y `CompositeHealthDescriptor`. |
| Salud pública desde el frontend | `GET /actuator/health` | Con `Accept: application/json` | Sin ese header Actuator responde `application/vnd.spring-boot.actuator.v3+json`, y `fetchClient` lo trataría como texto. |
| Momento del registro del recálculo | "No transaccional, se registra en el momento" | Se registra al terminar `recalcular`, dentro de la transacción de clase de `CotizacionServiceImpl` | El servicio tiene `@Transactional` a nivel de clase. Si el commit fallara después de terminar el método, el recálculo quedaría contado como exitoso. El riesgo se acepta: el recálculo es semanal y no está atado a dinero. |

**Efecto lateral observado en la verificación**: al iniciar el backend, `InitialDataLoadScheduler` ejecuta
siempre `recalcularCotizaciones()` e `inicializarLiquidez()`, sin un flag para desactivarlos. Es un
comportamiento previo a esta feature. Los dos arranques contra PostgreSQL agregaron una cotización cada uno
para el jugador con estadísticas en la ventana.

## R7. Compatibilidad de constructores

`MercadoServiceImpl`, `MatchingEngineImpl` y `CotizacionServiceImpl` tienen constructores sobrecargados
que usan los tests existentes. Se agrega `MetricasNegocio` al constructor `@Autowired`. Los constructores
legados delegan con `MetricasNegocio.SIN_REGISTRO`, una implementación no-op constante en la interfaz,
para no pasar `null` ni romper los tests actuales.
