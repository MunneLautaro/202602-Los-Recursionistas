---

description: "Task list for feature 013 - Métricas de negocio y panel de observabilidad con gráficos"
---

# Tasks: Métricas de negocio y panel de observabilidad con gráficos

**Input**: Design documents from `/specs/013-metricas-negocio-panel/`

**Prerequisites**:
- Required: [plan.md](plan.md), [spec.md](spec.md)
- Design: [research.md](research.md), [data-model.md](data-model.md)
- Contracts: [contracts/observabilidad-admin.md](contracts/observabilidad-admin.md), [contracts/metricas-negocio.md](contracts/metricas-negocio.md)
- Validation: [quickstart.md](quickstart.md)

**Tests**: el plan (research R6) y el Principio VII de la constitution piden tests unitarios de `service`.
Solo se incluyen esos: no hay tests de `controller`, `configuration`, `security` ni `persistence`, y el
frontend no tiene framework de tests.

**Organization**: las tareas se agrupan por historia de usuario (US1–US5 de la spec).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede ejecutar en paralelo (archivos distintos, sin dependencias pendientes).
- **[Story]**: historia de usuario de la spec.

## Path Conventions

Abreviaturas usadas en las descripciones de tareas:

- `BE/` = `backend/src/main/java/unq/losrecursionistas/backend/`
- `BT/` = `backend/src/test/java/unq/losrecursionistas/backend/`
- `FE/` = `frontend/src/`

---

## Phase 1: Setup (Shared Infrastructure)

- [X] T001 Registrar la línea base de backend y frontend.
  - Backend: ejecutar `cd backend; .\gradlew.bat clean test`. Debe quedar en verde.
  - Frontend: ejecutar `cd frontend; yarn lint; yarn build`.
  - Anotar los errores o warnings preexistentes para no confundirlos con regresiones. Si el backend falla, detenerse y reportar.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: dejar listos el puerto de métricas, su implementación, el servicio y el endpoint de negocio
protegido, y el esqueleto del panel. Todas las historias dependen de esta fase.

**⚠️ CRITICAL**: ninguna historia puede empezar antes de completar esta fase.

### Backend

- [X] T002 [P] Crear el puerto `BE/service/interfaces/MetricasNegocio.java` según [contracts/metricas-negocio.md](contracts/metricas-negocio.md).
  - Métodos:
    - `void registrarOrdenCreada(TipoOrden tipo)`
    - `void registrarRechazoSaldoInsuficiente()`
    - `void registrarEmparejamiento(BigDecimal total)`
    - `void registrarRecalculo(ResultadoRecalculo resultado)`
    - `MetricasEnVivo consultarEnVivo()`
  - Records anidados:
    - `ResultadoRecalculo(boolean exitoso, Duration duracion, int jugadoresCotizados, BigDecimal variacionPromedio)`, donde `variacionPromedio` puede ser `null`.
    - `MetricasEnVivo(OffsetDateTime inicio, long rechazosSaldoInsuficiente, long recalculosExitosos, long recalculosFallidos, Long duracionUltimoRecalculoMs, Long duracionPromedioRecalculoMs, Integer jugadoresCotizadosUltimoRecalculo, BigDecimal variacionPromedioUltimoRecalculo)`.
  - Constante `MetricasNegocio SIN_REGISTRO`: implementación no-op cuyo `consultarEnVivo()` devuelve ceros y `null` (research R7).
  - Comentarios en español.
- [X] T003 Crear `BE/service/impl/MetricasNegocioMicrometer.java` (`@Service`, implementa `MetricasNegocio`), con constructor que recibe `MeterRegistry`. El gauge de tokens en circulación lo agrega T014 (US1). La estrategia activa **no** es una métrica (research R8, K4). Depende de T002.
  - **Meters**: registrar exactamente los del [contrato](contracts/metricas-negocio.md):
    - `mercado.ordenes.creadas` (tag `tipo` = `compra`|`venta`)
    - `mercado.emparejamientos`
    - `mercado.volumen.creditos` (counter que suma `total.doubleValue()`)
    - `mercado.ordenes.rechazadas` (tag `motivo=saldo_insuficiente`)
    - `cotizacion.recalculos` (tag `resultado` = `exitoso`|`fallido`)
    - `cotizacion.recalculo.duracion`: `Timer` con tag `resultado`, **sin** `publishPercentiles` ni `publishPercentileHistogram`.
  - **Gauges**:
    - `cotizacion.jugadores.cotizados` y `cotizacion.variacion.promedio`, con valores en `AtomicReference`. Este último vale `NaN` sin datos.
  - **Registro después del commit**: `registrarOrdenCreada` y `registrarEmparejamiento` usan `TransactionSynchronizationManager.isSynchronizationActive()`. Si hay una transacción activa, registran una `TransactionSynchronization` con `afterCommit`; si no, incrementan en el momento.
  - **Registro inmediato**: `registrarRechazoSaldoInsuficiente` y `registrarRecalculo`.
  - **`consultarEnVivo`**: lee los counters y el timer `resultado=exitoso` (`count`, `totalTime`; la duración promedio considera solo los exitosos), la última duración exitosa guardada en `AtomicLong`, e `inicio` = instante de construcción en `America/Argentina/Buenos_Aires`.
  - **Restricción**: ningún tag con id ni nombre de usuario o jugador (FR-014). Solo se usan los tags `tipo`, `motivo` y `resultado`.
- [X] T004 [P] Crear `BT/service/MetricasNegocioMicrometerTest.java` con `SimpleMeterRegistry`, `@BeforeEach` y nombres en español ASCII. Debe cubrir:
  - orden creada por tipo sin transacción;
  - **after-commit** (C1): con `TransactionSynchronizationManager.initSynchronization()` activo, registrar una orden y un emparejamiento no cuenta hasta invocar `afterCommit()` de las sincronizaciones registradas. Si en cambio se invoca `afterCompletion(STATUS_ROLLED_BACK)`, no cuenta. Limpiar con `clearSynchronization()` en `@AfterEach`;
  - rechazo por saldo;
  - emparejamiento que suma el volumen;
  - recálculo exitoso y fallido (counter, timer sin histogramas y gauges);
  - `consultarEnVivo` antes y después de recálculos (`null` sin datos; el promedio solo cuenta los exitosos);
  - ningún meter con tags fuera de `tipo`, `motivo` y `resultado`.

  Depende de T003.
- [X] T005 [P] Crear `BE/service/interfaces/ObservabilidadService.java` con `ResumenNegocio resumenNegocio(int dias)` y `ResumenTecnico resumenTecnico()`. Agregar records anidados que reflejen **exactamente** los campos de [contracts/observabilidad-admin.md](contracts/observabilidad-admin.md):
  - `ResumenNegocio(dias, desde, hasta, MetricasEnVivo enVivo, Actual actual, TotalesPeriodo periodo, List<ActividadDiariaMercado> serieMercado, List<ActividadDiariaCotizacion> serieCotizacion)`
  - `Actual(long tokensEnCirculacion, EstrategiaActiva estrategiaActiva)`
  - `EstrategiaActiva(String codigo, String nombre)`
  - `TotalesPeriodo(long ordenesCompra, long ordenesVenta, long emparejamientos, BigDecimal volumenCreditos, long cotizacionesCalculadas, BigDecimal variacionPromedio)`
  - `ResumenTecnico(...)`, con sus sub-records `Salud`, `ComponenteSalud`, `ResumenRequests`, `EndpointRequests`, `Jvm` y `PoolConexiones`.
- [X] T006 [P] Crear los read-models en `BE/model/`, como records inmutables sin JPA y siguiendo el precedente `RankingCotizacion`:
  - `BE/model/ActividadDiariaMercado.java`: `record ActividadDiariaMercado(LocalDate dia, long ordenesCompra, long ordenesVenta, long emparejamientos, BigDecimal volumenCreditos)`, con método estático `vacia(LocalDate dia)` que usa `BigDecimal.ZERO.setScale(2)`.
  - `BE/model/ActividadDiariaCotizacion.java`: `record ActividadDiariaCotizacion(LocalDate dia, long jugadoresCotizados, BigDecimal variacionPromedio)`, con método estático `vacia(LocalDate dia)` que devuelve `0` y `null`.
- [X] T007 Crear `BE/service/impl/ObservabilidadServiceImpl.java` (`@Service`) implementando solo `resumenNegocio`. En esta fase las series de mercado y cotización se devuelven completas con `vacia(dia)` para cada día, y los totales en cero; US1 y US2 las llenan. `resumenTecnico` lanza `UnsupportedOperationException("Implementado en US3")` hasta T033.
  - **Validación de `dias`**: no se hace aquí. El controller ya entrega 7, 30 o 90 (Principio VI, K3).
  - **Rango**: `hasta = LocalDate.now(clock)` y `desde = hasta.minusDays(dias - 1)`, con `Clock` en zona `America/Argentina/Buenos_Aires`. El `Clock` se inyecta con un constructor alternativo para tests.
  - **`enVivo`**: `metricas.consultarEnVivo()`.
  - **`actual`**: estrategia desde `EstrategiaAdminService.consultar()` (`estrategiaActiva`, `nombreActivo`). `tokensEnCirculacion` queda en `0` hasta T014.

  Depende de T002, T005 y T006.
- [X] T008 [P] Crear los DTOs de respuesta en `BE/controller/dto/observabilidad/`: `ResumenNegocioResponse.java` y `ResumenTecnicoResponse.java`, como records con métodos estáticos `desde(...)` que mapean desde los records del service. Los nombres de campo JSON deben ser exactamente los del [contrato](contracts/observabilidad-admin.md). Las fechas se serializan como ISO y `inicio`/`instante` con offset.
- [X] T009 Crear `BE/controller/ObservabilidadAdminController.java` con `@RestController`, `@RequestMapping("/api/admin/observabilidad")` y `@Tag` de OpenAPI en español:
  - `GET /negocio` con `@RequestParam(defaultValue = "30") int dias`, que devuelve `ResumenNegocioResponse`. El controller valida que `dias` esté en `Set.of(7, 30, 90)` y, si no, lanza `ExcepcionValidacion("El periodo debe ser 7, 30 o 90 dias")` → 400 (Principio VI, K3).
  - Anotaciones `@Operation` y `@ApiResponse` para 200, 400, 401 y 403, siguiendo `EstrategiaAdminController`.

  Depende de T007 y T008.
- [X] T010 En `BE/configuration/SecurityConfig.java`, agregar `"/api/admin/observabilidad/**"` a la lista del `requestMatchers(...)` que ya exige `.hasAuthority("ROLE_ADMIN")` (la de `"/api/admin/whoscored/**", "/api/admin/cotizacion/**", ...`). No crear un matcher nuevo ni tocar las reglas de `/actuator/**`.
- [X] T011 Crear `BT/service/ObservabilidadServiceImplTest.java` para la parte foundational, con Mockito + `Clock.fixed(...)` en `America/Argentina/Buenos_Aires`. Debe cubrir:
  - las series tienen exactamente `dias` elementos ascendentes desde `desde` hasta `hasta`, para 7, 30 y 90;
  - `enVivo` delega en el puerto;
  - `actual` usa la estrategia activa.

  Depende de T007.

### Frontend

- [X] T012 [P] En `FE/features/admin/types.ts`, agregar los tipos TypeScript de [contracts/observabilidad-admin.md](contracts/observabilidad-admin.md): `PeriodoObservabilidad = 7 | 30 | 90`, `ResumenNegocio` (con `enVivo`, `actual`, `periodo`, `serieMercado` y `serieCotizacion`), `ResumenTecnico` y sus sub-tipos. Los campos que pueden ser `null` según el contrato se tipan como `number | null`.
- [X] T013 [P] Crear `FE/features/admin/api/observabilidadApi.ts`, único módulo que llama a `fetchClient` para esta feature:
  - `negocio: (dias: PeriodoObservabilidad) => fetchClient<ResumenNegocio>(\`/api/admin/observabilidad/negocio?dias=${dias}\`)`
  - `tecnico: () => fetchClient<ResumenTecnico>("/api/admin/observabilidad/tecnico")`
  - `saludPublica: () => Promise<"UP" | "DOWN" | string>`: llama a `fetchClient` con `GET /actuator/health`. Si responde 503, devuelve el `status` del cuerpo del `ApiError` (`DOWN`) en lugar de propagar el error (I1).

**Checkpoint**: `GET /api/admin/observabilidad/negocio` responde con series en cero, protegido con `ROLE_ADMIN`. `MetricasNegocioMicrometerTest` y `ObservabilidadServiceImplTest` están en verde.

---

## Phase 3: User Story 1 - Ver la actividad del mercado (Priority: P1) 🎯 MVP

**Goal**: contar órdenes creadas, emparejamientos, volumen y rechazos por saldo, y mostrar las series
diarias de mercado más los tokens en circulación (FR-001 a FR-005, FR-011, FR-012, FR-017, FR-020).

**Independent Test**: [quickstart.md §3, §4 y §6](quickstart.md). Al crear órdenes y emparejarlas, los
totales del período coinciden con el SQL directo, el gráfico muestra compra/venta y
emparejamientos/volumen por día con ceros en los días sin actividad, y el rechazo por saldo aparece
"desde el último reinicio".

### Backend de User Story 1

- [X] T014 [US1] Agregar los tokens en circulación (FR-004).
  1. **Persistencia**:
     - Agregar `long sumarTokensExcluyendoUsuario(Long usuarioId)` a `BE/persistence/repository/interfaces/RepositorioTenenciaToken.java`.
     - En `BE/persistence/sql/TenenciaTokenDAOSQL.java`: `@Query("SELECT COALESCE(SUM(t.cantidadTotal), 0) FROM TenenciaToken t WHERE t.usuario.id <> :usuarioId") long sumarExcluyendoUsuario(@Param("usuarioId") Long usuarioId)`.
     - En `BE/persistence/repository/impl/RepositorioTenenciaTokenImpl.java`: delegar en el DAO.
  2. **Gauge**: en `BE/service/impl/MetricasNegocioMicrometer.java`, agregar `RepositorioTenenciaToken` y `RepositorioUsuario` al constructor y el gauge `mercado.tokens.circulacion`. Es una función que devuelve `tenencias.sumarTokensExcluyendoUsuario(usuarios.buscarCustodio().getId())` y `0` si se lanza una excepción, por ejemplo cuando todavía no hay custodio.
  3. **Panel**: en `BE/service/impl/ObservabilidadServiceImpl.java`, `actual.tokensEnCirculacion` usa la misma consulta (repositorio + custodio).
  4. **Test**: ampliar `BT/service/MetricasNegocioMicrometerTest.java` con el gauge de tokens excluyendo al custodio y devolviendo `0` ante una excepción.
- [X] T015 [P] [US1] Agregar `List<ActividadDiariaMercado> contarOrdenesCreadasPorDia(LocalDate desde, LocalDate hasta)` a `BE/persistence/repository/interfaces/RepositorioOrden.java`.
  - **DAO**: en `BE/persistence/sql/OrdenDAOSQL.java`, agregar una consulta nativa con proyección `OrdenesPorDiaProyeccion` (interfaz anidada o en `persistence/sql`, con `getDia()`, `getTipo()` y `getCantidad()`):

    ```sql
    SELECT CAST(o.fecha_creacion AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE) AS dia,
           o.tipo AS tipo, COUNT(*) AS cantidad
    FROM ordenes o
    WHERE o.es_liquidez_inicial = false
      AND CAST(o.fecha_creacion AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE) BETWEEN :desde AND :hasta
    GROUP BY 1, 2
    ```

    Si el enum `tipo` está mapeado como ordinal y no como string, confirmarlo en `Orden.java` y ajustar el mapeo.
  - **Repositorio**: en `BE/persistence/repository/impl/RepositorioOrdenImpl.java`, combinar las filas en un `ActividadDiariaMercado` por día, con compras, ventas y emparejamientos/volumen en 0. Solo incluir los días con datos. Si el rango supera 90 días, lanzar `IllegalArgumentException` (K5).
- [X] T016 [P] [US1] Agregar `List<ActividadDiariaMercado> resumirTransaccionesPorDia(LocalDate desde, LocalDate hasta)` a `BE/persistence/repository/interfaces/RepositorioTransaccionMercado.java`.
  - **DAO**: en `BE/persistence/sql/TransaccionMercadoDAOSQL.java`, agregar una consulta nativa con proyección (`getDia()`, `getCantidad()` y `getVolumen()`):

    ```sql
    SELECT CAST(t.ejecutada_en AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE) AS dia,
           COUNT(*) AS cantidad, COALESCE(SUM(t.total), 0) AS volumen
    FROM transacciones_mercado t
    WHERE CAST(... AS DATE) BETWEEN :desde AND :hasta
    GROUP BY 1
    ```

  - **Repositorio**: implementar en `BE/persistence/repository/impl/RepositorioTransaccionMercadoImpl.java`, con el volumen redondeado a escala 2 `HALF_UP`. Si el rango supera 90 días, lanzar `IllegalArgumentException` (K5).
- [X] T017 [US1] En `BE/service/impl/MercadoServiceImpl.java`:
  1. Agregar `MetricasNegocio metricas` como último parámetro del constructor `@Autowired`. Los constructores legados delegan con `MetricasNegocio.SIN_REGISTRO`.
  2. En la rama `COMPRA`, envolver **solo** `usuarioBloqueado.reservar(...)` en `try { ... } catch (ExcepcionDominio e) { metricas.registrarRechazoSaldoInsuficiente(); throw e; }`, sin cambiar el tipo ni el mensaje de la excepción. Tener en cuenta que `ExcepcionRecursosInsuficientes` extiende `ExcepcionDominio`.
  3. Después de `ordenes.guardar(...)` y antes de `matching.ejecutar(orden)`, llamar a `metricas.registrarOrdenCreada(tipo)`.

  No tocar la rama `VENTA` (los rechazos por tokens quedan fuera de alcance).
- [X] T018 [US1] En `BE/service/impl/MatchingEngineImpl.java`: agregar `MetricasNegocio` al constructor de inyección, con un constructor legado que delega con `SIN_REGISTRO` si los tests construyen la clase manualmente, y después de `transacciones.crear(...)` llamar a `metricas.registrarEmparejamiento(transaccion.getTotal())`.
- [X] T019 [US1] En `BE/service/impl/ObservabilidadServiceImpl.java`, completar la serie de mercado:
  - Combinar `RepositorioOrden.contarOrdenesCreadasPorDia` y `RepositorioTransaccionMercado.resumirTransaccionesPorDia` por día sobre la lista completa del rango, usando `vacia(dia)` para los días sin datos.
  - Calcular `TotalesPeriodo.ordenesCompra`, `ordenesVenta`, `emparejamientos` y `volumenCreditos` (escala 2) sumando la serie.

  Depende de T015 y T016.
- [X] T020 [P] [US1] Ampliar los tests existentes de mercado. Si la clase construye el service manualmente, pasar `mock(MetricasNegocio.class)`.
  - `BT/service/MercadoServiceCompraTest.java`:
    - una compra exitosa llama a `registrarOrdenCreada(TipoOrden.COMPRA)`;
    - una compra con saldo insuficiente llama a `registrarRechazoSaldoInsuficiente()`, relanza la misma excepción y no llama a `registrarOrdenCreada`.
  - `BT/service/MercadoServiceVentaTest.java`:
    - una venta exitosa llama a `registrarOrdenCreada(TipoOrden.VENTA)`;
    - una venta sin tokens no llama a `registrarRechazoSaldoInsuficiente()`.
- [X] T021 [P] [US1] Ampliar `BT/service/MatchingEngineTest.java`: cada transacción creada llama a `registrarEmparejamiento` con su `total`, y una orden sin contraparte no registra nada.
- [X] T022 [US1] Ampliar `BT/service/ObservabilidadServiceImplTest.java`. Depende de T019. Debe cubrir:
  - combinación de órdenes y transacciones por día;
  - días sin datos en cero;
  - totales del período;
  - volumen con escala 2.

### Frontend de User Story 1

- [X] T023 [US1] Crear `FE/features/admin/hooks/useObservabilidad.ts`.
  - **Estado**: `periodo` (por defecto 30), `negocio`, `errorNegocio`, `cargandoNegocio` y `cambiarPeriodo(p)`.
  - **Polling**: carga inicial y un `setInterval` de 30 000 ms que solo consulta si `document.visibilityState === "visible"`. Al volver a estar visible (`visibilitychange`) consulta enseguida. Limpia el intervalo y el listener al desmontar.
  - **Salud primero (I1)**: cada ciclo llama antes a `observabilidadApi.saludPublica()`. Si devuelve algo distinto de `UP`, guarda `servicioNoDisponible = true` con ese estado, **no** llama a los endpoints protegidos (así no hay 401 ni se invalida la sesión) y sigue con el polling. Si devuelve `UP`, consulta normalmente y pone `servicioNoDisponible = false`.
  - **Errores**: si un error es `ApiError` con `status === 401`, detiene el polling para que el flujo de sesión vigente redirija al login (revisar cómo lo resuelven `fetchClient` y `AuthContext` y reutilizarlo).
  - Los errores de negocio no afectan otras secciones (FR-021).
- [X] T024 [P] [US1] Crear `FE/features/admin/components/TarjetaMetrica.tsx` + `TarjetaMetrica.styles.ts`.
  - Props: `etiqueta`, `valor: string`, `nota?` (por ejemplo "desde el último reinicio") y `tono?: "neutro" | "positivo" | "alerta"`.
  - Reutilizar las clases `ui-tile` y el estilo de tiles de `QuoteHistoryChart.styles.ts`.
  - Consultar la skill `.agents/skills/pantone-color-palette/SKILL.md`.
- [X] T025 [P] [US1] Crear `FE/features/admin/components/SelectorPeriodo.tsx` + `.styles.ts`: un grupo de 3 botones (7, 30 y 90 días) con `aria-pressed` y `onChange`, usando las clases `ui-btn-ghost` existentes.
- [X] T026 [P] [US1] Crear `FE/features/admin/components/GraficoMercado.tsx` + `.styles.ts` con Recharts:
  - `BarChart` apilado de `ordenesCompra`/`ordenesVenta` por `dia`;
  - `ComposedChart` con `Bar` de `emparejamientos` y `Line` de `volumenCreditos` en un eje Y derecho.
  - **Estilo**: copiar las constantes de superficie, grilla y ejes y el tooltip de `QuoteHistoryChart`. Fechas con `Intl.DateTimeFormat("es-AR", {day:"2-digit", month:"short"})` y montos con `Intl.NumberFormat("es-AR")`.
  - **Período vacío**: si todos los valores del período son 0, mostrar "Sin actividad en el período".
  - **Accesibilidad**: `role="img"` + `aria-label` descriptivo.
- [X] T027 [US1] Crear `FE/pages/ObservabilidadPage.tsx`:
  - Cabecera con el mismo estilo que `AdminPage` y `SelectorPeriodo`.
  - Sección **Mercado**: tarjetas de `periodo.ordenesCompra`, `ordenesVenta`, `emparejamientos`, `volumenCreditos`, `actual.tokensEnCirculacion` y `enVivo.rechazosSaldoInsuficiente` (con nota "desde el último reinicio"), más `GraficoMercado`.
  - Estados de carga y error de la sección con `role="status"` y `role="alert"`.
  - Un banner "Servicio no disponible (estado: DOWN)" cuando `servicioNoDisponible` (I1).
  - Usa `useObservabilidad`. Sin `fetch` directo. Importa todo lo de `features/admin` **solo** desde `features/admin/index.ts` (K2).
- [X] T028 [US1] Crear `FE/features/admin/index.ts` (Principio XII, K2), exportando la API pública del panel: `useObservabilidad`, `TarjetaMetrica`, `SelectorPeriodo`, `GraficoMercado` y, a medida que existan, `GraficoCotizacion`, `PanelSaludTecnica` y `GraficoRecursos`, más los tipos de observabilidad. En `FE/app/router.tsx`, agregar `<Route path="/admin/observabilidad" element={<AdminRoute><ObservabilidadPage /></AdminRoute>} />` antes del comodín `*`. En `FE/pages/AdminPage.tsx`, agregar en la cabecera un enlace (`Link` de react-router-dom, clase `ui-btn-ghost`) a **Observabilidad**.

**Checkpoint**: US1 funciona de punta a punta; es el MVP.

---

## Phase 4: User Story 2 - Ver el comportamiento de la cotización (Priority: P1)

**Goal**: medir recálculos (resultado y duración), jugadores cotizados, variación promedio y estrategia
activa, y mostrar la serie diaria de cotización (FR-006 a FR-011).

**Independent Test**: ejecutar "Recalcular cotizaciones" y verificar en el panel:
- un recálculo exitoso más;
- su duración;
- los jugadores cotizados;
- la variación promedio y la estrategia activa;
- un gráfico diario con el día del recálculo.

### Backend de User Story 2

- [X] T029 [P] [US2] Agregar `List<ActividadDiariaCotizacion> resumirPorDia(LocalDate desde, LocalDate hasta)` a `BE/persistence/repository/interfaces/RepositorioCotizacion.java`.
  - **DAO**: en `BE/persistence/sql/CotizacionDAOSQL.java`, agregar una consulta nativa con proyección (`getDia()`, `getJugadores()`, `getVariacionPromedio()`, `getConPrevio()`):

    ```sql
    WITH con_previo AS (
      SELECT c.jugador_id, c.timestamp_calculo, c.valor,
             LAG(c.valor) OVER (PARTITION BY c.jugador_id ORDER BY c.timestamp_calculo, c.id) AS previo
      FROM cotizaciones c)
    SELECT CAST(timestamp_calculo AS DATE) AS dia,
           COUNT(DISTINCT jugador_id) AS jugadores,
           AVG(CASE WHEN previo IS NOT NULL AND previo > 0 THEN (valor - previo) / previo END) AS variacionPromedio,
           COUNT(CASE WHEN previo IS NOT NULL AND previo > 0 THEN 1 END) AS conPrevio
    FROM con_previo
    WHERE CAST(timestamp_calculo AS DATE) BETWEEN :desde AND :hasta
    GROUP BY 1
    ```

    El `LAG` corre sobre toda la historia del jugador (research D3). `conPrevio` se usa para ponderar el total del período.
  - **Repositorio**: en `BE/persistence/repository/impl/RepositorioCotizacionImpl.java`, mapear a `ActividadDiariaCotizacion`, con la variación redondeada a escala 6 `HALF_UP` y `null` si no hay filas con previo. Si el rango supera 90 días, lanzar `IllegalArgumentException` (K5). Exponer el conteo `conPrevio` mediante un record auxiliar o un segundo método, según convenga para T031.
- [X] T030 [US2] En `BE/service/impl/CotizacionServiceImpl.java`, dentro de `recalcular(List<Jugador>, LocalDate, LocalDate)`:
  1. Agregar `MetricasNegocio` al constructor `@Autowired`; los constructores legados usan `SIN_REGISTRO`.
  2. Tomar `long inicio = System.nanoTime()` al principio y envolver todo el cuerpo en `try { ... } catch (RuntimeException e) { metricas.registrarRecalculo(new ResultadoRecalculo(false, duracion, 0, null)); throw e; }`.
  3. Antes de cada `cotizaciones.guardar(...)` exitoso, leer `Cotizacion previa = cotizaciones.vigente(jugadorId)` y, si `previa != null` y `previa.getValor().signum() > 0`, acumular `(valor - previo) / previo` con `MathContext.DECIMAL64`.
  4. Contar las cotizaciones guardadas. Los jugadores que caen en el `catch` por jugador no cuentan.
  5. Al final, registrar `ResultadoRecalculo(true, duracion, guardadas, promedio o null)`.

  No cambiar el cálculo, el logging ni la resolución de la estrategia (Principio XIV).
- [X] T031 [US2] En `BE/service/impl/ObservabilidadServiceImpl.java`, completar `serieCotizacion` con `RepositorioCotizacion.resumirPorDia`, usando `vacia(dia)` para los días sin datos. Calcular `TotalesPeriodo.cotizacionesCalculadas` como la suma de la serie y `variacionPromedio` como el promedio ponderado por `conPrevio` (`null` si el total con previo es 0). Depende de T029.
- [X] T032 [P] [US2] Ampliar `BT/service/CotizacionServiceImplTest.java`, pasando `mock(MetricasNegocio.class)` y usando `ArgumentCaptor<ResultadoRecalculo>`. Debe cubrir:
  - un recálculo exitoso registra `exitoso=true`, la cantidad guardada y la variación promedio correcta con dos jugadores con previo y uno sin previo, que queda excluido;
  - una estrategia inválida (`IllegalStateException`) registra `exitoso=false` y relanza;
  - un jugador cuyo cálculo falla no cuenta como cotizado.
- [X] T033 [US2] Ampliar `BT/service/ObservabilidadServiceImplTest.java` con la serie de cotización completada con días vacíos y el promedio ponderado del período (con y sin datos). Depende de T031.

### Frontend de User Story 2

- [X] T034 [P] [US2] Crear `FE/features/admin/components/GraficoCotizacion.tsx` + `.styles.ts` con un `ComposedChart`:
  - `Bar` de `jugadoresCotizados` en el eje izquierdo y `Line` de `variacionPromedio` en el eje derecho, formateada como porcentaje con `Intl.NumberFormat("es-AR", {style:"percent", maximumFractionDigits:1, signDisplay:"exceptZero"})`.
  - `connectNulls={false}` para los días sin variación.
  - Mismo estilo que `GraficoMercado` y mensaje vacío si no hubo cotizaciones.
- [X] T035 [US2] En `FE/pages/ObservabilidadPage.tsx`, agregar la sección **Cotización**:
  - Tarjetas: estrategia activa (`actual.estrategiaActiva.nombre`), recálculos exitosos y fallidos, duración del último y promedio en segundos, jugadores cotizados del último recálculo y variación del último. Todas con la nota "desde el último reinicio" excepto la estrategia, y "Sin datos" cuando el valor es `null`.
  - Totales del período (`cotizacionesCalculadas` y variación ponderada) y `GraficoCotizacion`.

**Checkpoint**: US1 y US2 funcionan de forma independiente.

---

## Phase 5: User Story 5 - Restringir el panel a administradores (Priority: P1)

**Goal**: solo `ROLE_ADMIN` accede al panel y a sus datos (FR-015, FR-016). La regla de backend ya está en T010.

**Independent Test**: [quickstart.md §2 y §6.4](quickstart.md).

- [X] T036 [US5] Verificar a mano con el backend levantado (quickstart §2) que `/api/admin/observabilidad/negocio` y `/tecnico` responden 401 a un anónimo, 403 a `ROLE_USUARIO` (con cuerpo `RespuestaError`) y 200 a un admin, y que `?dias=15` responde 400. Si `/tecnico` todavía no existe (US3 pendiente), repetir la verificación después de T038.
- [ ] T037 [US5] Verificar en el navegador que `/admin/observabilidad` con un usuario común o sin sesión no muestra el panel y aplica el comportamiento vigente de `AdminRoute` (redirección), y que el enlace de `AdminPage` solo es visible para el admin.

---

## Phase 6: User Story 3 - Ver la salud técnica de la aplicación (Priority: P2)

**Goal**: salud, top de endpoints con tasa de error y latencia, JVM y pool, con su evolución en la sesión (FR-018, FR-019, FR-021).

**Independent Test**: [quickstart.md §5, §6 y §7](quickstart.md).

### Backend de User Story 3

- [X] T038 [US3] Implementar `resumenTecnico()` en `BE/service/impl/ObservabilidadServiceImpl.java`, inyectando `MeterRegistry` y `org.springframework.boot.health.actuate.endpoint.HealthEndpoint`.
  - **Salud**: `healthEndpoint.health()` → estado global y lista `ComponenteSalud(nombre, estado)` **sin details**. Si el descriptor no trae componentes, usar el fallback `HealthContributorRegistry` (research R2) y documentarlo en research.md.
  - **Requests**:
    - Recorrer `registry.find("http.server.requests").timers()` y agrupar por `method` + `uri`, excluyendo las URIs que empiezan con `/actuator` o `/api/admin/observabilidad`.
    - Sumar `count()` y `totalTime(TimeUnit.MILLISECONDS)`.
    - `erroresCliente`/`erroresServidor` = conteos con `outcome` `CLIENT_ERROR`/`SERVER_ERROR`.
    - Top 10 por cantidad, en orden descendente.
    - Totales globales, con las tasas en `null` si `total == 0`.
  - **JVM**:
    - `jvm.memory.used` y `jvm.memory.max` con tag `area=heap`, sumando los gauges e ignorando valores negativos de `max`.
    - `jvm.threads.live`, `process.cpu.usage`, `system.cpu.usage` y `process.uptime` en segundos.
  - **Pool**: `hikaricp.connections.active`, `.idle`, `.pending` y `.max` (sumados si hay más de un pool).
  - **Meters ausentes**: devuelven `null`.
  - **`instante`**: `OffsetDateTime.now(clock)`.
- [X] T039 [US3] En `BE/controller/ObservabilidadAdminController.java`, agregar `GET /tecnico`, que devuelve `ResumenTecnicoResponse.desde(service.resumenTecnico())`, con su documentación OpenAPI. Depende de T038.
- [X] T040 [US3] Ampliar `BT/service/ObservabilidadServiceImplTest.java` con `SimpleMeterRegistry` para timers `http.server.requests` con tags sintéticos y `HealthEndpoint` mockeado. Debe cubrir:
  - top 10 ordenado y exclusiones de `/actuator/**` y `/api/admin/observabilidad/**`;
  - tasas y latencia media;
  - tasas `null` sin requests;
  - salud sin details;
  - JVM y pool con meters presentes y ausentes (`null`).

  Depende de T038.

### Frontend de User Story 3

- [X] T041 [US3] Ampliar `FE/features/admin/hooks/useObservabilidad.ts`:
  - Agregar `tecnico`, `errorTecnico`, `cargandoTecnico` y `muestrasTecnicas`, un buffer circular de 60 elementos `{instante, heapUsadoMb, cpuProceso, cpuSistema, conexionesActivas, conexionesPendientes}`.
  - Consultar `observabilidadApi.tecnico()` en el mismo ciclo de 30 s que negocio, después del chequeo de salud pública (I1), con un `Promise.allSettled` para que cada sección tenga su propio error.
  - Exportar los componentes nuevos desde `FE/features/admin/index.ts`.
- [X] T042 [P] [US3] Crear `FE/features/admin/components/PanelSaludTecnica.tsx` + `.styles.ts`:
  - **Salud**: badge de estado global (UP en `mint`, cualquier otro estado en `coral`) y una lista de componentes con su estado, resaltando los que no son `UP`.
  - **Requests**: tarjetas de total, latencia media, tasa de error de cliente y de servidor (porcentaje), más un `BarChart` horizontal de Recharts con el top de endpoints (`metodo uri` vs `cantidad`). El tooltip muestra errores y latencia.
- [X] T043 [P] [US3] Crear `FE/features/admin/components/GraficoRecursos.tsx` + `.styles.ts` con 2 `LineChart` sobre `muestrasTecnicas`:
  - heap usado (MB), CPU proceso y CPU sistema (%);
  - conexiones activas y pendientes del pool.

  Mostrar tarjetas de threads, uptime (formato `Xh Ym`), heap máximo e inactivas/máximas del pool. Con menos de 2 muestras, mostrar "Recolectando datos…". Agregar la nota "evolución durante esta sesión".
- [X] T044 [US3] En `FE/pages/ObservabilidadPage.tsx`, agregar la sección **Técnico** con `PanelSaludTecnica` y `GraficoRecursos`, y su propio estado de carga y error, independiente de negocio (FR-021).

**Checkpoint**: el panel muestra negocio y técnico; cada sección falla de forma independiente.

---

## Phase 7: User Story 4 - Exportar métricas de negocio a Prometheus (Priority: P2)

**Goal**: las métricas de negocio aparecen en `/actuator/prometheus` (FR-013, FR-014). Ya quedaron registradas en T003; esta fase solo verifica.

**Independent Test**: [quickstart.md §3](quickstart.md).

- [X] T045 [US4] Con el backend levantado, crear actividad (compra exitosa, compra rechazada por saldo, venta que empareja) y ejecutar un recálculo. Verificar en `/actuator/prometheus` como admin que:
  - aparecen todas las series de [contracts/metricas-negocio.md](contracts/metricas-negocio.md) con `application=`, y **no** aparece `cotizacion_estrategia_activa` (K4);
  - no hay `_bucket` ni `quantile` para `cotizacion_recalculo_duracion`;
  - ningún label contiene ids ni nombres de usuario o jugador.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [X] T046 Revisar el código nuevo y modificado contra `.agents/skills/sonarcloud-quality/SKILL.md`: literales duplicados (constantes para los nombres de meters y tags), complejidad de `resumenTecnico`, imports sin usar, `catch` que relanza sin perder la causa y nombres de tests en español ASCII.
- [ ] T047 [P] Revisar los componentes del panel contra `.agents/skills/pantone-color-palette/SKILL.md` y los tokens del tema (`mint`, `coral`, `lavender`, `ui-card` y `ui-tile`): contraste y accesibilidad de badges y gráficos, y layout responsive en mobile (grillas `sm:` y `md:`).
- [X] T048 Ejecutar `cd backend; .\gradlew.bat clean test`, que debe quedar en verde e incluir `ArquitecturaCapasTest`, `ObservabilidadSeguridadTest` y los tests nuevos y ampliados. Ejecutar `cd frontend; yarn lint; yarn build`, que no debe tener errores nuevos respecto de T001.
- [ ] T049 Recorrer [quickstart.md](quickstart.md) §1–§7 contra PostgreSQL. Incluye la comparación SQL de §4 (SC-002), la medición de carga de menos de 3 s con `dias=90` de §6 (SC-006, G1) y la caída de la base de §7, que debe mostrar "Servicio no disponible" sin cerrar la sesión (I1). Informar cualquier paso no ejecutado y su motivo.
- [X] T050 [P] Si hubo desvíos del diseño (fallback de salud, forma de exponer `conPrevio`, nombres de campos), actualizar [research.md](research.md), [plan.md](plan.md) y los contratos.

---

## Dependencies & Execution Order

### Phase Dependencies

| Fase | Depende de |
|------|------------|
| Setup (T001) | — |
| Foundational (T002–T013) | Setup. **Bloquea todas las historias.** |
| US1 (T014–T028) | Foundational |
| US2 (T029–T035) | Foundational. Comparte archivos con US1, ver más abajo. |
| US5 (T036–T037) | Endpoints de US1 (negocio) y US3 (técnico) para la verificación completa |
| US3 (T038–T044) | Foundational |
| US4 (T045) | T003 y las integraciones de T017, T018 y T030 |
| Polish (T046–T050) | Todas las historias |

### Dentro de Foundational

1. T002 primero.
2. Después, T003, T005, T006, T008, T012 y T013 en paralelo.
3. T007 → T009 → T010.
4. T004 depende de T003; T011 depende de T007.

### Conflictos de archivo (siempre en secuencia)

- `ObservabilidadServiceImpl.java`: T007 → T014 → T019 → T031 → T038.
- `MetricasNegocioMicrometer.java` y su test: T003/T004 → T014.
- `ObservabilidadServiceImplTest.java`: T011 → T022 → T033 → T040.
- `ObservabilidadPage.tsx`: T027 → T035 → T044.
- `useObservabilidad.ts`: T023 → T041.
- `ObservabilidadAdminController.java`: T009 → T039.

### Parallel Opportunities

- **Foundational**: backend (T003, T005, T006, T008) y frontend (T012, T013) en paralelo.
- **US1**: T014 primero (toca `ObservabilidadServiceImpl`) y luego los DAOs y repositorios T015 y T016 en paralelo; los tests T020 y T021 en paralelo; los componentes T024, T025 y T026 en paralelo.
- **Entre historias**: la persistencia de US2 (T029) y el frontend de US2 (T034) en paralelo con US1; los componentes de US3 (T042, T043) en paralelo entre sí.

---

## Parallel Example: User Story 1

```text
Task: "T015 [US1] contarOrdenesCreadasPorDia (SQL nativo) en OrdenDAOSQL + RepositorioOrdenImpl"
Task: "T016 [US1] resumirTransaccionesPorDia (SQL nativo) en TransaccionMercadoDAOSQL + RepositorioTransaccionMercadoImpl"
Task: "T024 [US1] TarjetaMetrica.tsx"
Task: "T025 [US1] SelectorPeriodo.tsx"
Task: "T026 [US1] GraficoMercado.tsx"
```

## Parallel Example: User Story 3

```text
Task: "T042 [US3] PanelSaludTecnica.tsx"
Task: "T043 [US3] GraficoRecursos.tsx"
```

---

## Implementation Strategy

### MVP First (US1)

1. Setup y Foundational: puerto, métricas y endpoint protegido.
2. US1: métricas de mercado, series y panel con la sección Mercado.
3. **STOP and VALIDATE** con quickstart §3, §4 y §6.

### Incremental Delivery

1. US1 Mercado (MVP).
2. US2 Cotización.
3. US5 verificación de acceso.
4. US3 Técnico.
5. US4 verificación de Prometheus.
6. Polish.

### Commits sugeridos

Sin push hasta que el equipo lo apruebe.

1. `feat(observabilidad): puerto MetricasNegocio e implementacion Micrometer` (T002–T004)
2. `feat(observabilidad): endpoint admin de resumen de negocio` (T005–T011)
3. `feat(observabilidad): metricas y series de mercado` (T014–T022)
4. `feat(frontend): panel de observabilidad - seccion mercado` (T012–T013, T023–T028)
5. `feat(observabilidad): metricas y series de cotizacion` (T029–T035)
6. `feat(observabilidad): resumen tecnico en el panel` (T038–T044)

---

## Notes

- **Rechazos**: solo se cuentan los rechazos por saldo en compras. Los rechazos por tokens quedan fuera de alcance.
- **Panel de 012**: no se modifica ninguna regla de `/actuator/**` ni `ObservabilidadSeguridadTest`.
- **SQL nativo**: depende de PostgreSQL (`AT TIME ZONE`, `LAG`). Se valida en T049 contra la base real, no con H2.
- **Desvíos**: ante un desvío no contemplado en research.md, detenerse y consultar (Principio IX).
