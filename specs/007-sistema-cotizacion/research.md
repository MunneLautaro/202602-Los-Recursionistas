# Investigación: sistema de cotización

## Contexto existente

El backend ya utiliza Spring Data JPA, PostgreSQL, `Page<T>` propagado desde
repository a controller, `@EnableScheduling` y JWT stateless. No existe
entidad, DAO o servicio de cotización; el endpoint de quotes actual es un
placeholder y el ranking vigente devuelve una lista de estadísticas de
temporada. La implementación debe reemplazar esos puntos sin modificar la
estructura funcional de las entidades existentes.

## Selección de estrategia activa

**Decisión:** `CotizacionServiceImpl` recibirá
`Map<String, EstrategiaCotizacion>` por inyección de Spring y buscará la
estrategia con `cotizacion.estrategia.activa`.

**Rationale:** elimina condicionales por estrategia, permite agregar
implementaciones sin tocar el orquestador y hace explícito que el valor de
configuración es el nombre del bean. Las implementaciones tendrán exactamente
los nombres `PERFORMANCE_GENERAL` y `PERFORMANCE_POR_POSICION`.

**Alternativas consideradas:** un `enum` con `switch` es simple, pero obliga a
modificar el servicio; una factory agrega una capa innecesaria cuando Spring
ya resuelve el mapa; `@ConditionalOnProperty` oculta el conjunto de beans y
complica validar un nombre inválido.

## Agregación y normalización

**Decisión:** el servicio filtra jugadores activos con `idWhoscored`, obtiene
estadísticas en `[ventanaInicio, ventanaFin]`, agrega por jugador tratando
`null` como cero y calcula máximos por métrica antes de invocar la estrategia.
La estrategia recibe un resumen inmutable, posición, máximos y parámetros
validados; no conoce repositories ni realiza consultas.

Delegar la carga a cada estrategia duplicaría consultas. Normalizar en SQL
reduciría testabilidad y mezclaría fórmula con infraestructura.

## Persistencia e historial

**Decisión:** `Cotizacion` será append-only, con `LocalDateTime
timestampCalculo` y precisión monetaria de dos decimales. Se declararán los
índices solicitados `(jugador_id, timestamp_calculo DESC)` y
`(timestamp_calculo DESC)`, usando `id DESC` como desempate.

El historial usará `Specification` o predicados equivalentes y
`Page<Cotizacion>`, con `PageRequest.of(page, size, sort)` construido en
service. Los rangos serán inclusivos y el tamaño default será 12.

No se crearán métodos para cada combinación de fechas, listas paginadas en
memoria ni tabla de versiones: duplican código o incumplen la constitución.

## Ranking

**Decisión:** una consulta PostgreSQL paginada con proyección calculará la
última y anterior cotización por jugador mediante `ROW_NUMBER` o subconsultas,
unirá jugador/equipo/liga, aplicará `ligaId` y `posicion`, y ordenará por valor
actual descendente. La variación será `abs(valorActual - valorAnterior)`.

Esto evita N+1 y carga de historiales completos en Java. `GROUP BY MAX` sin
desempate puede seleccionar una fila ambigua cuando hay timestamps iguales;
ordenar una lista completa incumple `Page<T>`.

## Asincronía y concurrencia

**Decisión:** crear `AsyncCotizacionConfiguration` con un
`ThreadPoolTaskExecutor` dedicado y un worker `@Async("cotizacionTaskExecutor")`
en un bean separado. Scheduler y controller invocarán el mismo contrato de
servicio; solo controller verificará `ROLE_ADMIN`. Un `AtomicBoolean` reservado
antes de encolar devuelve conflicto si ya está activo y se libera en `finally`.

Compartir el executor de ingesta permitiría que scraping y cotización se
bloqueen mutuamente. Una cola externa, una entidad de ejecución o threads
creados desde controller exceden YAGNI y complican lifecycle.

## Scheduler y seguridad

**Decisión:** `CotizacionScheduler` será un `@Component` con
`@Scheduled(cron = "${scheduler.cotizacion.cron}", zone =
"America/Argentina/Buenos_Aires")`, condicionado por
`scheduler.cotizacion.enabled` y configurado después de WhoScored. No se
copiará ni fabricará `SecurityContext`; la ruta HTTP tendrá `ROLE_ADMIN`.

Autorización en service rompería el scheduler y copiar el contexto al worker
acoplaría el procesamiento a una request.

## Configuración y errores

Todos los pesos, estrategia, ventana, base, escala y cron se agregarán a la
única `application.yml` con los fallbacks de variables de entorno de la spec.
Una estrategia inexistente o incompleta falla antes de persistir. Un error por
jugador se registra y permite continuar; el job informa omitidos.
