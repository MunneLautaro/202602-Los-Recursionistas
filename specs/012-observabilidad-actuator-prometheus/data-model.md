# Data Model: Observabilidad del backend con Actuator y métricas Prometheus

**Feature**: [spec.md](spec.md) | **Fecha**: 2026-11-24

Esta feature **no agrega entidades JPA, tablas ni cambios de esquema**. Las
entidades de la spec son representaciones de solo lectura que Spring Boot
Actuator genera en tiempo de ejecución. Se documentan como contrato observable;
los ejemplos completos están en [contracts/actuator.md](contracts/actuator.md).

## Estado de salud

| Campo | Tipo | Visible para | Notas |
|-------|------|--------------|-------|
| `status` | `UP` \| `DOWN` \| `OUT_OF_SERVICE` \| `UNKNOWN` | Todos | Estado global agregado. |
| `components` | mapa nombre → estado | Solo `ROLE_ADMIN` | Al menos `db`, `diskSpace` y `ping`. |
| `components.<n>.details` | mapa | Solo `ROLE_ADMIN` | Por ejemplo, `db.database` o `diskSpace.free`. No incluye URL ni credenciales. |

**Reglas**:
- Un usuario anónimo o sin `ROLE_ADMIN` recibe solo `status`.
- Con probes desactivadas, tampoco aparece `groups`.

**Transiciones**:
- Si algún componente está `DOWN`, el estado global es `DOWN` y la respuesta HTTP
  es 503.
- Si todos los componentes están `UP`, la respuesta es 200.

## Información de la aplicación

| Campo | Origen | Ejemplo |
|-------|--------|---------|
| `build.name` | `project.name` de Gradle | `backend` |
| `build.artifact` | nombre base de `bootJar` | `backend` |
| `build.group` | `group` de Gradle | `unq.losrecursionistas` |
| `build.version` | `version` de Gradle | `0.0.1-SNAPSHOT` |
| `build.time` | instante del build (ISO-8601, UTC) | `2026-11-24T21:10:03.120Z` |

**Regla**: no se publican propiedades `info.*` del entorno, porque
`management.info.env.enabled` queda en `false` por defecto.

## Métrica

| Campo | Descripción |
|-------|-------------|
| `name` | Nombre Micrometer, por ejemplo `http.server.requests`. |
| `measurements[]` | Estadística y valor (`COUNT`, `TOTAL_TIME`, `MAX`, `VALUE`). |
| `availableTags[]` | Tags filtrables y sus valores. |

**Tags relevantes**:
- `application`: común a todas las métricas.
- `method`, `uri`, `status`, `outcome` y `exception`: solo en las métricas HTTP.
- `pool`: en las métricas de Hikari.
- `area` e `id`: en las métricas de memoria de la JVM.

**Regla**: `uri` siempre es la plantilla de ruta (`/players/{id}`) o un valor
agregado, nunca la URL concreta. Los valores agregados verificados son:

- `UNKNOWN`: requests rechazados por seguridad (401/403) antes de llegar a un
  handler.
- `/**`: rutas sin handler que resuelven a 404.

## Exposición Prometheus

Es el texto con el formato de exposición de Prometheus. Cada meter de Micrometer
se convierte en una o más series, con `.` reemplazado por `_` y sufijos de
unidad:

| Meter Micrometer | Series Prometheus (ejemplos) |
|------------------|------------------------------|
| `http.server.requests` | `http_server_requests_seconds_count`, `_sum`, `_max` |
| `jvm.memory.used` | `jvm_memory_used_bytes` |
| `jvm.threads.live` | `jvm_threads_live_threads` |
| `process.cpu.usage` / `system.cpu.usage` | `process_cpu_usage`, `system_cpu_usage` |
| `process.uptime` | `process_uptime_seconds` |
| `hikaricp.connections.active` / `idle` / `pending` | `hikaricp_connections_active`, `hikaricp_connections_idle`, `hikaricp_connections_pending` |

**Derivaciones** (no requieren histogramas):
- Latencia media por endpoint =
  `http_server_requests_seconds_sum / http_server_requests_seconds_count`,
  filtrando por `uri` y `method`.
- Tasa de error =
  `count{outcome="SERVER_ERROR"}` (o también `CLIENT_ERROR`) dividido por el
  `count` total del mismo `uri`.
