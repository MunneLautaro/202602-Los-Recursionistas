# Backend

## Configuracion local

La configuracion se mantiene en `src/main/resources/application.yml` porque
representa mejor las secciones anidadas de Spring y de scraping que un archivo
`.properties`. El YAML importa opcionalmente `backend/.env` cuando la
aplicacion se ejecuta desde `backend/`; las variables del entorno del sistema
siguen teniendo prioridad.

Para configurar una instalacion nueva:

1. Copiar `.env.example` como `.env`.
2. Completar `FOOTBALL_DATA_API_KEY` con el token personal de
   [football-data.org](https://www.football-data.org/client/register).
3. Ajustar las credenciales de PostgreSQL y el resto de valores si hace falta.
4. Ejecutar Gradle desde `backend/` (`./gradlew bootRun` o `gradlew.bat bootRun`).

`.env` esta excluido de Git para no publicar credenciales. `WHOSCORED_ALLOWED_LEAGUES`
acepta IDs separados por comas; vacío significa que no se restringen ligas.
Al arrancar, la carga inicial sincroniza Football Data y luego descubre y
vincula las ligas, equipos y jugadores de WhoScored antes de cargar sus
estadisticas. `FOOTBALL_DATA_STARTUP_ENABLED=false` y
`WHOSCORED_STARTUP_ENABLED=false` desactivan cada etapa por separado.

## Cotizaciones

El historial se consulta en `GET /players/{id}/quotes` con filtros inclusivos
`desde`/`hasta` y paginacion (tamano predeterminado 12). El ranking paginado
esta disponible en `GET /players/ranking` y acepta `ligaId` y `posicion`.
`POST /quotes/recalculate` es público para facilitar las pruebas, responde
`202` y ejecuta el recálculo en segundo plano; una ejecución concurrente
responde `409`.

La estrategia activa se configura con `COTIZACION_ESTRATEGIA_ACTIVA` y puede
ser `PERFORMANCE_GENERAL` o `PERFORMANCE_POR_POSICION`. El scheduler semanal
se controla con `COTIZACION_SCHEDULER_ENABLED` y
`COTIZACION_SCHEDULER_CRON` (por defecto, domingos a las 04:00 de
`America/Argentina/Buenos_Aires`).

## Ingesta WhoScored

`IngestaWhoscoredService` procesa jugadores activos con `idWhoscored` y
permite alcances completos o acotados a liga, equipo o jugador. La temporada
se toma de `application.yml`; las apariciones fuera del rango, sin partido o
con cero minutos se registran como anomalías y no se persisten.

Las estadísticas se combinan dentro del adapter `whoscored` y se guardan con
upsert por `(jugador_id, partido_whoscored_id)`. Las ejecuciones activas
bloquean una segunda ejecución y registran el rechazo. Las operaciones de
ingesta deben exponerse únicamente a usuarios con `ROLE_ADMIN`.

Playwright se mantiene aislado en `adapter/whoscored`. WhoScored no ofrece un
contrato estable y puede responder con anti-bot o cambiar su estructura; la
prueba real es opcional, debe etiquetarse `real-web` y no forma parte del build
normal ni debe usarse para evadir protecciones de la fuente.

## Mercado de tokens

El mercado opera exclusivamente con créditos ficticios y usa la cotización
vigente al crear cada orden; el precio capturado no cambia durante su
ejecución. Las órdenes de compra y venta se reservan de forma transaccional y
se procesan con matching FIFO, incluyendo ejecuciones parciales.

La liquidez inicial emite 100 tokens por jugador activo con cotización vigente.
La inicialización es idempotente y no reemplaza órdenes existentes. No se
ofrecen cancelaciones, vencimientos, comisiones, precios límite ni pagos
reales. Las transacciones y auditorías se registran como historial
append-only, y las consultas de órdenes e historial son paginadas con tamaño
predeterminado 12.
