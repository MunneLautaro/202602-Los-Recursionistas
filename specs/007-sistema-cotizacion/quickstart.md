# Guía de validación rápida

## Prerrequisitos

- Java 21 y PostgreSQL accesible mediante `SPRING_DATASOURCE_*`.
- Datos de liga, equipo, jugador y estadísticas, incluyendo un jugador activo
  con `idWhoscored`.
- No se requiere JWT para probar los endpoints de cotización localmente.

## Tests unitarios

Desde `backend/`:

```powershell
.\gradlew.bat test --tests "*CotizacionServiceImplTest" `
  --tests "*PerformanceGeneralEstrategiaTest" `
  --tests "*PerformancePorPosicionEstrategiaTest"
```

Debe verificarse normalización, nulos, máximo cero, redondeo a dos decimales,
posiciones españolas/inglesas, fallback de posición desconocida, configuración
inválida y error aislado por jugador. Los tests serán solo de model/service
con Mockito.

## Prueba REST

Deshabilitar temporalmente el scheduler con
`COTIZACION_SCHEDULER_ENABLED=false` y ejecutar el backend:

```text
GET  /players/{id}/quotes?page=0&size=12
GET  /players/{id}/quotes?desde=2026-10-01&hasta=2026-10-21
GET  /players/ranking?ligaId=1&posicion=delantero&page=0&size=12
POST /quotes/recalculate
```

Comprobar historial ordenado y rangos inclusivos; ranking con actual, anterior
y variación; página default 12; exclusión de jugadores sin cotización; y
errores 400 para rango/paginación inválidos. El POST responde `202` antes de
terminar; una segunda solicitud durante el job responde `409`. Los endpoints
son públicos durante las pruebas locales.

## Scheduler y rendimiento

Habilitar el scheduler con cron de prueba posterior a WhoScored y comprobar
que se ejecuta sin sesión HTTP. Medir historial/ranking bajo 100 requests
concurrentes (p95 menor a 2 s) y aceptación del POST (menor a 1 s).
Finalmente comprobar en `cotizaciones` que cada fila tiene estrategia,
timestamp, valor y ventanas no nulos, y revisar logs de jugadores omitidos.
