# Quickstart: Validar la observabilidad con Actuator y Prometheus

**Feature**: [spec.md](spec.md) | **Contrato**: [contracts/actuator.md](contracts/actuator.md)

Guía de validación manual en PowerShell (Windows). Los códigos y respuestas
esperados están en el contrato; acá solo se indican los pasos.

## Prerrequisitos

1. PostgreSQL disponible en la URL configurada en `backend/.env`.
2. En `backend/.env`, la exposición debe incluir los cuatro endpoints. Si tu
   `.env` es anterior a esta feature, actualizá la línea:

   ```properties
   MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
   ```

3. Opcional, para arrancar más rápido: `FOOTBALL_DATA_STARTUP_ENABLED=false` y
   `WHOSCORED_STARTUP_ENABLED=false`.

## 1. Compilar, testear y levantar el backend

```powershell
cd backend
.\gradlew.bat clean test
.\gradlew.bat bootRun
```

`clean test` debe terminar en verde, incluidos `ArquitecturaCapasTest` y
`ObservabilidadSeguridadTest`. Dejá `bootRun` corriendo y usá otra terminal
para los pasos siguientes.

## 2. Preparar las variables de la sesión

```powershell
$base = "http://localhost:8080"

function Get-CodigoEstado {
    param([string]$Url, [hashtable]$Headers = @{})
    try {
        (Invoke-WebRequest -Uri $Url -Headers $Headers -UseBasicParsing).StatusCode
    } catch {
        [int]$_.Exception.Response.StatusCode
    }
}

# Token de administrador (usuario creado por AdminUserInitializer)
$login = @{ nombreUsuario = "admin"; contrasena = "admin123" } | ConvertTo-Json
$tokenAdmin = (Invoke-RestMethod -Method Post -Uri "$base/login" -ContentType "application/json" -Body $login).token
$hAdmin = @{ Authorization = "Bearer $tokenAdmin" }

# Usuario común (ROLE_USUARIO)
$registro = @{ nombreUsuario = "monitor_prueba"; contrasena = "prueba123"; saldo = 0; habilitado = $true } | ConvertTo-Json
try { Invoke-RestMethod -Method Post -Uri "$base/registrar" -ContentType "application/json" -Body $registro | Out-Null } catch { }
$loginUsuario = @{ nombreUsuario = "monitor_prueba"; contrasena = "prueba123" } | ConvertTo-Json
$tokenUsuario = (Invoke-RestMethod -Method Post -Uri "$base/login" -ContentType "application/json" -Body $loginUsuario).token
$hUsuario = @{ Authorization = "Bearer $tokenUsuario" }
```

> Si la contraseña del admin se cambió en tu base local, usá la vigente.

## 3. Health: público, sin detalles salvo para ADMIN

```powershell
Invoke-RestMethod "$base/actuator/health"                      # solo { status }
Invoke-RestMethod "$base/actuator/health" -Headers $hUsuario   # solo { status }
Invoke-RestMethod "$base/actuator/health" -Headers $hAdmin | ConvertTo-Json -Depth 5   # status + components
Get-CodigoEstado "$base/actuator/health/db"                    # 401
```

**Esperado**: las dos primeras respuestas no tienen `components` ni `groups`;
la de ADMIN muestra `db`, `diskSpace`, `ping` y `ssl`.

**Opcional (estado DOWN)**: detené PostgreSQL y consultá
`Get-CodigoEstado "$base/actuator/health"`. Debe responder `503` con cuerpo
`{"status":"DOWN"}` para un anónimo. Al volver a levantar la base, responde `200`.

## 4. Matriz de acceso a los endpoints protegidos

```powershell
$protegidos = "/actuator/info", "/actuator/metrics", "/actuator/metrics/jvm.memory.used", "/actuator/prometheus"
foreach ($ruta in $protegidos) {
    [pscustomobject]@{
        Ruta    = $ruta
        Anonimo = Get-CodigoEstado "$base$ruta"
        Usuario = Get-CodigoEstado "$base$ruta" $hUsuario
        Admin   = Get-CodigoEstado "$base$ruta" $hAdmin
    }
}
```

**Esperado**: en cada fila, `Anonimo=401`, `Usuario=403` y `Admin=200`.

Para ver el formato de error vigente:

```powershell
try { Invoke-WebRequest "$base/actuator/info" -UseBasicParsing } catch { $_.ErrorDetails.Message }
```

## 5. Endpoints no aprobados

```powershell
foreach ($ruta in "env", "heapdump", "beans", "configprops", "loggers", "mappings", "threaddump", "shutdown") {
    [pscustomobject]@{ Ruta = $ruta; Admin = Get-CodigoEstado "$base/actuator/$ruta" $hAdmin; Anonimo = Get-CodigoEstado "$base/actuator/$ruta" }
}
```

**Esperado**: `Admin=404` y `Anonimo=401` en todos.

## 6. Info y métricas

```powershell
Invoke-RestMethod "$base/actuator/info" -Headers $hAdmin | ConvertTo-Json -Depth 3
Invoke-RestMethod "$base/actuator/metrics/http.server.requests" -Headers $hAdmin | ConvertTo-Json -Depth 4
Get-CodigoEstado "$base/actuator/metrics/no.existe" $hAdmin    # 404
```

**Esperado**:
- `info` muestra `build.name`, `build.version` y `build.time`.
- `http.server.requests` lista los tags `method`, `uri`, `status`, `outcome` y
  `application`.

## 7. Prometheus: series HTTP, JVM, proceso y pool

```powershell
# Generar tráfico exitoso y con error
1..5 | ForEach-Object { Invoke-WebRequest "$base/leagues" -UseBasicParsing | Out-Null }
Get-CodigoEstado "$base/players/999999999" | Out-Null
Get-CodigoEstado "$base/no-existe-xyz" $hAdmin | Out-Null

$prom = (Invoke-WebRequest "$base/actuator/prometheus" -Headers $hAdmin -UseBasicParsing).Content
$prom -split "`n" | Select-String -Pattern '^http_server_requests_seconds_(count|sum|max)\{' | Select-Object -First 6
$prom -split "`n" | Select-String -Pattern '^(jvm_memory_used_bytes|jvm_threads_live_threads|jvm_gc_pause_seconds|process_cpu_usage|system_cpu_usage|process_uptime_seconds|disk_free_bytes|hikaricp_connections_(active|idle|pending))' | Select-Object -First 12

# Las rutas inexistentes no generan una serie por URL
($prom -split "`n" | Where-Object { $_ -match 'no-existe-xyz' }).Count   # 0

# Cada serie debe tener la etiqueta application
($prom -split "`n" | Where-Object { $_ -and -not $_.StartsWith('#') -and $_ -notmatch 'application="' }).Count   # 0
```

**Esperado**:
- Aparecen series HTTP con `uri="/leagues"` y otras con `outcome="CLIENT_ERROR"`.
- Aparecen series de JVM, proceso y `hikaricp`.
- El último conteo da `0`.

La latencia media de `/leagues` es `sum / count` de sus series
(ver [data-model.md](data-model.md#exposición-prometheus)).

## 8. Ausencia de datos sensibles

En Windows PowerShell 5.1, `Invoke-WebRequest` devuelve el cuerpo JSON de
Actuator como `byte[]` (content type `application/vnd.spring-boot.actuator.v3+json`),
así que hay que decodificarlo:

```powershell
function Get-Texto([string]$Url) {
    $c = (Invoke-WebRequest $Url -Headers $hAdmin -UseBasicParsing).Content
    if ($c -is [byte[]]) { [Text.Encoding]::UTF8.GetString($c) } else { $c }
}
$todo = (Get-Texto "$base/actuator/health") + (Get-Texto "$base/actuator/info") +
        (Get-Texto "$base/actuator/metrics") + $prom
$todo -match 'jdbc:|SPRING_DATASOURCE|JWT_SECRET|clave-local|Bearer |FOOTBALL_DATA_API_KEY|admin123|prueba123'   # False
```

> No uses `password` como patrón. Las métricas de observación de Spring
> Security incluyen el tag `spring_security_authentication_type="UsernamePasswordAuthenticationToken"`,
> que es un nombre de clase y no un dato sensible.

## 9. Swagger UI

Abrí `http://localhost:8080/swagger-ui.html`.

**Esperado**: en la sección de Actuator aparecen solo `health`, `info`,
`metrics` y `prometheus`, sin `env`, `beans` ni otros.

## 10. Verificación final

```powershell
cd backend
.\gradlew.bat clean test
```
