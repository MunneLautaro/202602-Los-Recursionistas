# Quickstart de validacion

## Prerrequisitos

1. Java 21 y Gradle Wrapper.
2. PostgreSQL disponible para la aplicacion.
3. Docker disponible para el test de persistencia con Testcontainers.
4. Una unica configuracion `backend/src/main/resources/application.yml` con
   `whoscored.scraping` y fechas de temporada activa. No ejecutar scraping real
   en CI.

## Validacion unitaria

Desde `backend/`:

```powershell
.\gradlew.bat test --tests "*MatchingWhoscoredServiceTest" --tests "*Scraping*Test"
```

Resultado esperado: matching por ID, fecha/nombre normalizado, equipo y
nacionalidad; acentos, slugs codificados, homonimos y ambiguedad; parser que
combina las cuatro pestanas, descarta cero minutos y conserva JSONB; y
errores de Playwright representados como `ScrapingException`.

## Validacion de persistencia

```powershell
.\gradlew.bat test --tests "*EstadisticaPartidoRepositoryTest" --tests "*IngestaWhoscoredServiceTest"
```

El contenedor PostgreSQL debe demostrar constraint unica, upsert sin duplicados,
actualizacion de un partido existente, indices por jugador/fecha/torneo y
rollback de un jugador fallido sin perder los demas.

## Validacion de alcance y reanudacion

Ejecutar el servicio con alcance completo, por liga, por equipo y por jugador
usando fixtures. Verificar que solo se procesan jugadores activos, que las
fechas fuera de la temporada se descartan, que se conservan todos los torneos
informados y que una segunda ejecucion activa se rechaza y registra.

## Prueba web real opcional

```powershell
.\gradlew.bat test -Dgroups=real-web
```

Debe quedar marcada con `@Tag("real-web")` y excluida de `test` normal. Requiere
aceptar los terminos aplicables y puede fallar por anti-bot o cambios de
estructura; nunca debe ser requisito de CI ni ejecutarse en paralelo agresivo.

Los campos y constraints esperados estan en [data-model.md](data-model.md) y
el contrato del adapter en [contracts/scraping-port.md](contracts/scraping-port.md).
