# Quickstart de validación

## Prerrequisitos

- Java 21 y Gradle Wrapper del backend.
- PostgreSQL disponible con el esquema de la aplicación.
- Variables de configuración de Football Data y WhoScored definidas en el
  único `backend/src/main/resources/application.yml`.
- Usuario con `ROLE_ADMIN` y JWT válido.
- Para tests del adapter: fixtures HTML/JSON locales; no se requiere acceso a
  WhoScored.

## Validación unitaria

Desde `backend/`:

```powershell
.\gradlew.bat test --tests "*IngestaWhoscoredServiceTest"
.\gradlew.bat test --tests "*MatchingWhoscoredServiceTest"
```

Verificar happy path, alcance, temporada, idempotencia, rechazo concurrente,
errores parciales, datos ambiguos y ausencia de `SecurityContext` en el caso
de uso. Los tests deben usar Mockito y nombres en español ASCII.

Los tests del adapter deben ejecutarse con fixtures locales y cubrir happy
path, timeout, JSON inválido, retry y excepciones envueltas.

## Validación manual REST

1. Autenticarse como administrador y enviar el request de [inicio de ingesta](contracts/iniciar-ingesta.md).
2. Confirmar `202 Accepted`, id de ejecución y respuesta en menos de 2 segundos.
3. Consultar [última ejecución](contracts/ultima-ejecucion.md) mientras está
   `ACTIVA` y después de finalizar.
4. Repetir el inicio mientras la primera está activa y verificar un registro
   `RECHAZADA` sin segundo procesamiento.
5. Repetir una carga finalizada y comprobar que los conteos de vínculos y
   estadísticas no crecen por duplicación.
6. Probar alcance por liga, equipo y jugador con ids existentes; luego ids
   inexistentes y cuerpos inválidos, verificando error explícito y ausencia de
   ejecución ACTIVA.
7. Ejecutar el scheduler con la propiedad habilitada y sin sesión HTTP;
   confirmar que respeta el cron posterior a Football Data y el mismo control
   de concurrencia.

## Operación legal y disponibilidad

WhoScored puede bloquear automatización o limitar el acceso según anti-bot y
términos de uso. Mantener headless, rate limit, pausas, reintentos y user-agent
configurados; no evadir controles. Ante bloqueo, el adapter debe registrar
`ScrapingException`, conservar datos válidos y dejar la ejecución operable.
