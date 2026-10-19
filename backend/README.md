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
