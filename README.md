# 202602-Los-Recursionistas

## Validacion del backend

El backend utiliza Java 21 y Gradle. Para ejecutar la compilacion y las pruebas desde la raiz del repositorio:

```bash
cd backend
./gradlew clean test
```

En Windows puede ejecutarse el wrapper equivalente:

```powershell
cd backend
.\gradlew.bat clean test
```

El workflow de GitHub Actions en `.github/workflows/ci.yml` configura Java 21, resuelve dependencias, compila el proyecto y ejecuta la suite de pruebas en cada push o pull request hacia `main` o `dev`.

## Limites de arquitectura

La seguridad se concentra en `backend/src/main/java/unq/losrecursionistas/backend/security/` y utiliza Spring Security sin acoplarse a `model`, `service` o `persistence`. La configuracion inicial permite las solicitudes mientras no existan endpoints protegidos; `ManejadorToken` y `PoliticaAcceso` definen puntos de extension para incorporar autenticacion y autorizacion posteriormente.

Las responsabilidades permanecen separadas: `controller` traduce HTTP a DTOs, `service` orquesta entidades, `persistence` accede a datos, `configuration` registra componentes globales, `security` gestiona autenticacion y autorizacion, y `exceptions` traduce errores a respuestas HTTP. Las nuevas clases deben respetar estos limites y usar identificadores ASCII.

## Ingesta WhoScored

Al iniciar el backend se ejecuta una carga inicial en este orden:

1. Football Data sincroniza las cinco ligas (`PL`, `BL1`, `PD`, `SA` y `FL1`).
2. WhoScored descubre esas ligas y equipos, vincula sus identificadores con los
   registros recién guardados y carga las estadísticas de los jugadores
   vinculados.

La carga es idempotente para equipos, jugadores y estadísticas. Para habilitar
la carga inicial se debe copiar `.env.example` como `backend/.env` y
reemplazar `FOOTBALL_DATA_API_KEY=replace_with_your_football_data_api_key` por
un token real de [football-data.org](https://www.football-data.org/client/register).
La base PostgreSQL debe existir y estar disponible en la URL configurada.
`WHOSCORED_STARTUP_ENABLED=false` permite desactivar solamente la carga de
WhoScored. Los errores de conexión, credenciales o scraping quedan registrados
con detalle en el log de arranque.

La ingesta se ejecuta desde el servicio `IngestaWhoscoredService` con alcance
completo o acotado a liga, equipo o jugador. Solo procesa jugadores activos
vinculados mediante `idWhoscored`, filtra la temporada configurada y realiza un
upsert por `(jugador_id, partido_whoscored_id)`. Las ejecuciones concurrentes se
rechazan y quedan auditadas.

La integracion usa Playwright dentro de `adapter/whoscored`, con JSON de red
como fuente primaria y respuestas embebidas como respaldo. Los selectores,
URLs y metricas de WhoScored pueden cambiar y la fuente puede aplicar
anti-bot; por eso la prueba real se mantiene separada del build normal.
Los accesos destinados a exponer esta operacion deben requerir `ROLE_ADMIN`.
