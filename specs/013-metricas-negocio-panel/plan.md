# Implementation Plan: Métricas de negocio y panel de observabilidad con gráficos

**Branch**: `013-metricas-negocio-panel` | **Date**: 2026-11-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/013-metricas-negocio-panel/spec.md`

## Summary

**Backend**:
- **Puerto `MetricasNegocio`**: se agrega en `service/interfaces` y su implementación Micrometer en
  `service/impl`. Registra:
  - órdenes creadas (después del commit);
  - emparejamientos y volumen (después del commit);
  - rechazos por saldo insuficiente;
  - recálculos (resultado, duración, jugadores cotizados, variación promedio);
  - dos gauges: tokens en circulación y estrategia activa.
- **Integración**: el puerto se usa desde `MercadoServiceImpl`, `MatchingEngineImpl` y
  `CotizacionServiceImpl`.
- **`ObservabilidadService`**: arma el resumen de negocio (gauges en vivo más series diarias por SQL nativo
  agregado, con los días vacíos en cero) y el resumen técnico (`MeterRegistry` más `HealthEndpoint`).
- **Endpoints**: los expone `ObservabilidadAdminController` en `/api/admin/observabilidad/{negocio,tecnico}`,
  solo para `ROLE_ADMIN`.

**Frontend**:
- **Página**: `/admin/observabilidad`, solo para admin, con tarjetas y gráficos Recharts de negocio y
  técnicos.
- **Refresco**: polling cada 30 segundos, con pausa cuando la pestaña no está visible y manejo de errores
  por sección.

Las decisiones D1–D3 de [research.md](research.md) las eligió el usuario.

## Technical Context

**Language/Version**: Java 21 + Spring Boot 4.1.1 (backend); TypeScript + React 19 + Vite (frontend).

**Primary Dependencies**:
- **Backend**: Micrometer 1.17.1 y Spring Boot Actuator (feature 012), Spring Data JPA, Spring Security
  con JWT.
- **Frontend**: Recharts 3 (ya presente), Tailwind CSS 4 y react-router-dom 7.
- **No se agregan dependencias.**

**Storage**: PostgreSQL. No hay tablas nuevas; se agregan consultas nativas de solo lectura sobre
`ordenes`, `transacciones_mercado`, `tenencias_token` y `cotizaciones`.

**Testing**:
- **Backend**: tests unitarios de `service` con JUnit 5, Mockito y `SimpleMeterRegistry` (ver R6), más
  `ArquitecturaCapasTest`.
- **Frontend**: `yarn lint` y `yarn build`; el proyecto no tiene framework de tests de frontend.

**Target Platform**: backend JVM 21 y SPA web.

**Project Type**: aplicación web (backend REST + frontend SPA).

**Performance Goals**: el panel carga en menos de 3 segundos con 90 días de historia (SC-006). Cada
refresco hace 2 requests.

**Constraints**:
- Sin endpoints de Actuator nuevos.
- Sin tags que identifiquen usuarios o jugadores.
- Sin histogramas.
- Sin tablas de snapshots.
- Zona horaria `America/Argentina/Buenos_Aires`.
- Español técnico con identificadores ASCII.

**Scale/Scope**:
- **Backend**: 4 clases nuevas, 2 archivos de DTOs de respuesta, 2 read-models en `model`, 3 servicios
  modificados, 4 repositorios/DAOs ampliados y `SecurityConfig`.
- **Frontend**: 1 página, 1 hook, 1 api y 6 componentes.

No quedan `NEEDS CLARIFICATION`: todo se resolvió en [research.md](research.md) (D1–D3, R1–R7).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Gate previo a Phase 0: APROBADO. Re-evaluación post-diseño: APROBADO.**

| Regla | Evaluación |
|-------|-----------|
| Stack tecnológico | No hay dependencias nuevas; Recharts y Micrometer ya están aprobados o presentes. |
| Principio I (capas y matriz) | Controller → `service/interfaces`. Service → `persistence/repository/interfaces`. Los DAOs solo se usan desde `persistence`. Los read-models van en `model` (precedente `RankingCotizacion`). No se depende de `service/impl` fuera de `service`. |
| Principio II (modelo) | Sin cambios en reglas de dominio. El rechazo sigue en `Usuario.reservar`; solo se observa en el service. |
| Principio III (puertos) | Los services dependen del puerto `MetricasNegocio`; solo `MetricasNegocioMicrometer` conoce Micrometer (D2). |
| Principio IV (paginación) | Las series están acotadas por diseño (≤ 90 días) y el top de endpoints está acotado a 10. Es la excepción de "enumeraciones acotadas por diseño" y se documenta en D3. |
| Principio V (errores) | Se reutilizan `ExcepcionValidacion` y `ControladorErrores`; no hay excepciones nuevas. |
| Principio VI (validación) | `dias` (7/30/90) se valida en el controller, que responde 400. El service asume un valor válido. Los repositorios rechazan rangos de más de 90 días (R8, K3/K5). |
| Principio VII (testing) | Solo tests de `service` (2 nuevos y 4 ampliados). No hay tests de controller, configuration, security ni persistence (R6). |
| Principio IX (ambigüedad) | Las decisiones D1–D3 se consultaron con el usuario y él las eligió. |
| Principio X (idioma) | Nombres en español ASCII (`MetricasNegocio`, `ObservabilidadService`, `mercado.ordenes.creadas`). |
| Principio XI (YAGNI) | Solo las métricas pedidas. Sin rechazos por tokens, sin métricas de ingesta ni de usuarios, sin alertas. |
| Principio XII (frontend) | Página en `pages/`, lógica en `features/admin/{api,hooks,components}`, estilos en `.styles.ts`, llamadas solo vía `fetchClient` desde `api/`, `Layout` compartido por el router. |
| Principio XIV (cotización) | La estrategia sigue resolviéndose en runtime. Las métricas solo observan; no cambian el cálculo. |
| Principio XV (transacciones) | Órdenes y emparejamientos se cuentan después del commit; el contrato transaccional no cambia. |
| RNF Observabilidad (v2.9.0) | Métricas con Micrometer, exportadas en `/actuator/prometheus` con el tag `application`. Sin endpoints de Actuator nuevos. Los endpoints `/api/admin/observabilidad/**` se amparan en la excepción v2.9.0: `ROLE_ADMIN`, solo lectura, sin detalles de salud. Sin datos sensibles: la estrategia activa no se exporta como tag (R8, K4). *Corregido después de `/speckit-analyze`: la primera versión de esta fila omitía el conflicto K1.* |
| Principio XII (`index.ts`) | `features/admin/index.ts` exporta la API pública del panel y la página importa solo desde ahí (R8, K2). |
| RNF Documentación | Los endpoints nuevos se documentan en OpenAPI. |
| RNF Calidad | Revisión con la skill `sonarcloud-quality`, `gradlew test`, `yarn lint` y `yarn build`. |

Sin violaciones; la sección Complexity Tracking no aplica.

## Project Structure

### Documentation (this feature)

```text
specs/013-metricas-negocio-panel/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── observabilidad-admin.md   # REST /api/admin/observabilidad/*
│   └── metricas-negocio.md       # puerto MetricasNegocio + series Prometheus
├── checklists/requirements.md
└── tasks.md                      # /speckit-tasks
```

### Source Code (repository root)

```text
backend/src/main/java/unq/losrecursionistas/backend/
├── model/
│   ├── ActividadDiariaMercado.java          # NUEVO record read-model (dia, compras, ventas, emparejamientos, volumen)
│   └── ActividadDiariaCotizacion.java       # NUEVO record read-model (dia, jugadoresCotizados, variacionPromedio)
├── persistence/
│   ├── repository/interfaces/
│   │   ├── RepositorioOrden.java            # + contarCreadasPorDia(desde, hasta)
│   │   ├── RepositorioTransaccionMercado.java # + resumirPorDia(desde, hasta)
│   │   ├── RepositorioTenenciaToken.java    # + sumarTokensExcluyendoUsuario(usuarioId)
│   │   └── RepositorioCotizacion.java       # + resumirPorDia(desde, hasta)
│   ├── repository/impl/…Impl.java           # mapeo proyección → read-model
│   └── sql/{Orden,TransaccionMercado,TenenciaToken,Cotizacion}DAOSQL.java  # consultas nativas + proyecciones
├── service/
│   ├── interfaces/
│   │   ├── MetricasNegocio.java             # NUEVO puerto (+ SIN_REGISTRO no-op)
│   │   └── ObservabilidadService.java       # NUEVO (+ records ResumenNegocio, ResumenTecnico)
│   └── impl/
│       ├── MetricasNegocioMicrometer.java   # NUEVO (único que conoce MeterRegistry para negocio)
│       ├── ObservabilidadServiceImpl.java   # NUEVO
│       ├── MercadoServiceImpl.java          # + registro de orden creada y rechazo
│       ├── MatchingEngineImpl.java          # + registro de emparejamiento
│       └── CotizacionServiceImpl.java       # + registro de recálculo y variación
├── controller/
│   ├── ObservabilidadAdminController.java   # NUEVO GET /api/admin/observabilidad/{negocio,tecnico}
│   └── dto/observabilidad/                  # NUEVO DTOs de respuesta
└── configuration/SecurityConfig.java        # + "/api/admin/observabilidad/**" en la regla ROLE_ADMIN

backend/src/test/java/unq/losrecursionistas/backend/service/
├── MetricasNegocioMicrometerTest.java       # NUEVO
├── ObservabilidadServiceImplTest.java       # NUEVO
├── MercadoServiceCompraTest.java / MercadoServiceVentaTest.java / MatchingEngineTest.java / CotizacionServiceImplTest.java  # ampliados

frontend/src/
├── app/router.tsx                           # + /admin/observabilidad (AdminRoute)
├── pages/
│   ├── AdminPage.tsx                        # + acceso a observabilidad
│   └── ObservabilidadPage.tsx               # NUEVA
└── features/admin/
    ├── api/observabilidadApi.ts             # NUEVO
    ├── hooks/useObservabilidad.ts           # NUEVO (polling 30 s, buffer de sesión)
    ├── types.ts                             # + tipos de los contratos
    └── components/
        ├── TarjetaMetrica.tsx/.styles.ts            # tile de valor
        ├── SelectorPeriodo.tsx/.styles.ts           # 7/30/90
        ├── GraficoMercado.tsx/.styles.ts            # órdenes, emparejamientos y volumen
        ├── GraficoCotizacion.tsx/.styles.ts         # jugadores cotizados y variación
        ├── PanelSaludTecnica.tsx/.styles.ts         # salud, top endpoints
        └── GraficoRecursos.tsx/.styles.ts           # memoria, CPU y pool (sesión)
```

**Structure Decision**: aplicación web existente. El backend respeta la matriz de capas y el frontend la
estructura del Principio XII. Los componentes del panel viven en `features/admin/components`, igual que
`EstrategiaCotizacionPanel`.

### Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|-----------|
| `HealthEndpoint.health()` no devuelve componentes | Fallback con `HealthContributorRegistry` (R2), verificado en la tarea de implementación. |
| El SQL nativo no tiene tests automáticos (Principio VII) | Validación manual contra PostgreSQL en el quickstart (SC-002) comparando con `COUNT`/`SUM` directos. |
| Cambiar constructores rompe tests existentes | Los constructores legados delegan con `MetricasNegocio.SIN_REGISTRO` (R7). |
| Una consulta extra por jugador en el recálculo para la variación | Es aceptable (recálculo semanal); queda documentado en R1. |
| El polling del panel infla las métricas de requests | Se excluyen `/actuator/**` y `/api/admin/observabilidad/**` del resumen (R3). |
| El custodio es "el primer `ROLE_ADMIN`" | Misma definición que la liquidez inicial, pero con la consulta **sin bloqueo** `RepositorioUsuario.idCustodio()`. La versión con bloqueo falla en transacciones de solo lectura de PostgreSQL (research R9). |

## Complexity Tracking

No aplica: la Constitution Check no registra violaciones.
