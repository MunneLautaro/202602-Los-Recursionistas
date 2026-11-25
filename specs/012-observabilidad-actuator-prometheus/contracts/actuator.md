# Contrato: Endpoints de monitoreo (Spring Boot Actuator)

**Feature**: [../spec.md](../spec.md) | **Base path**: `/actuator` | **Autenticación**: JWT existente (`Authorization: Bearer <token>`)

Los errores 401 y 403 usan el formato de error vigente del proyecto, que
escriben `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`:

```json
{ "codigo": 401, "mensaje": "...", "detalle": "Unauthorized", "timestamp": "2026-11-24T21:15:00Z" }
```

```json
{ "codigo": 403, "mensaje": "...", "detalle": "Forbidden", "timestamp": "2026-11-24T21:15:00Z" }
```

> Es `exceptions/RespuestaError(codigo, mensaje, detalle, timestamp)`, sin
> cambios en esta feature.

## Resumen

| Método | Ruta | Rol requerido | Anónimo | `ROLE_USUARIO` | `ROLE_ADMIN` |
|--------|------|---------------|---------|----------------|--------------|
| GET | `/actuator/health` | Público | 200 / 503, solo `status` | 200 / 503, solo `status` | 200 / 503, con `components` |
| GET | `/actuator/health/{componente}` | `ROLE_ADMIN` | 401 | 403 | 200 / 503 |
| GET | `/actuator` | `ROLE_ADMIN` | 401 | 403 | 200 |
| GET | `/actuator/info` | `ROLE_ADMIN` | 401 | 403 | 200 |
| GET | `/actuator/metrics` | `ROLE_ADMIN` | 401 | 403 | 200 |
| GET | `/actuator/metrics/{nombre}` | `ROLE_ADMIN` | 401 | 403 | 200, o 404 si la métrica no existe |
| GET | `/actuator/prometheus` | `ROLE_ADMIN` | 401 | 403 | 200 (`text/plain`) |
| ANY | `/actuator/{otro}` (`env`, `heapdump`, `beans`, `configprops`, `loggers`, `mappings`, `threaddump`, `shutdown`, …) | No aprobado | 401 | 403 | 404 |

Un token inválido o vencido se trata como anónimo.

---

## GET `/actuator/health`

**Anónimo o `ROLE_USUARIO`, 200:**

```json
{ "status": "UP" }
```

**Anónimo con la base de datos caída, 503:**

```json
{ "status": "DOWN" }
```

**`ROLE_ADMIN`, 200:**

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL", "validationQuery": "isValid()" } },
    "diskSpace": { "status": "UP", "details": { "total": 511101005824, "free": 203450007552, "threshold": 10485760, "path": "C:\\...\\backend\\.", "exists": true } },
    "ping": { "status": "UP" },
    "ssl": { "status": "UP", "details": { "validChains": [], "invalidChains": [] } }
  }
}
```

> Verificado el 2026-11-24: un ADMIN recibe los componentes `db`, `diskSpace`,
> `ping` y `ssl`. El indicador `ssl` lo agrega Spring Boot 4 automáticamente y
> sin bundles configurados no informa certificados.

## GET `/actuator/info` (`ROLE_ADMIN`)

**200:**

```json
{
  "build": {
    "artifact": "backend",
    "name": "backend",
    "time": "2026-11-24T21:10:03.120Z",
    "version": "0.0.1-SNAPSHOT",
    "group": "unq.losrecursionistas"
  }
}
```

## GET `/actuator/metrics` (`ROLE_ADMIN`)

**200** (lista recortada):

```json
{
  "names": [
    "application.ready.time",
    "hikaricp.connections.active",
    "http.server.requests",
    "jvm.memory.used",
    "jvm.threads.live",
    "process.cpu.usage",
    "process.uptime",
    "system.cpu.usage"
  ]
}
```

## GET `/actuator/metrics/{nombre}` (`ROLE_ADMIN`)

Admite filtros por tag: `?tag=uri:/players/{id}&tag=method:GET`.

**200** para `/actuator/metrics/http.server.requests`:

```json
{
  "name": "http.server.requests",
  "baseUnit": "seconds",
  "measurements": [
    { "statistic": "COUNT", "value": 42.0 },
    { "statistic": "TOTAL_TIME", "value": 3.81 },
    { "statistic": "MAX", "value": 0.42 }
  ],
  "availableTags": [
    { "tag": "application", "values": ["backend"] },
    { "tag": "method", "values": ["GET", "POST"] },
    { "tag": "outcome", "values": ["SUCCESS", "CLIENT_ERROR"] },
    { "tag": "status", "values": ["200", "401", "404"] },
    { "tag": "uri", "values": ["/players/{id}", "/login", "/actuator/health"] },
    { "tag": "exception", "values": ["none"] },
    { "tag": "error", "values": ["none"] }
  ]
}
```

**404**: la métrica no existe; la respuesta no tiene cuerpo.

## GET `/actuator/prometheus` (`ROLE_ADMIN`)

**200**, `Content-Type: text/plain;version=0.0.4;charset=utf-8`. Si el cliente
pide OpenMetrics en `Accept`, devuelve
`application/openmetrics-text;version=1.0.0`.

```text
# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{application="backend",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/players/{id}"} 12
http_server_requests_seconds_sum{application="backend",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/players/{id}"} 0.734
http_server_requests_seconds_count{application="backend",error="none",exception="none",method="GET",outcome="CLIENT_ERROR",status="404",uri="/players/{id}"} 2
# TYPE http_server_requests_seconds_max gauge
http_server_requests_seconds_max{application="backend",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/players/{id}"} 0.12
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="backend",area="heap",id="G1 Eden Space"} 4.194304E7
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads{application="backend"} 38
# TYPE process_cpu_usage gauge
process_cpu_usage{application="backend"} 0.0042
# TYPE system_cpu_usage gauge
system_cpu_usage{application="backend"} 0.11
# TYPE process_uptime_seconds gauge
process_uptime_seconds{application="backend"} 512.3
# TYPE hikaricp_connections_active gauge
hikaricp_connections_active{application="backend",pool="HikariPool-1"} 0
# TYPE hikaricp_connections_idle gauge
hikaricp_connections_idle{application="backend",pool="HikariPool-1"} 10
# TYPE hikaricp_connections_pending gauge
hikaricp_connections_pending{application="backend",pool="HikariPool-1"} 0
```

> El conjunto exacto de tags HTTP (`error` además de `exception`) depende de
> la convención de observación de Spring Framework 7. El contrato garantiza
> como mínimo `method`, `uri`, `status`, `outcome` y `application`.

## Garantías de no exposición

Ninguna respuesta de este contrato incluye:

- la URL, el usuario o la contraseña de la base de datos;
- `JWT_SECRET` ni tokens;
- `FOOTBALL_DATA_API_KEY`;
- valores de propiedades de configuración;
- datos personales.
