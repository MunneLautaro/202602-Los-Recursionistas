# Backend

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
