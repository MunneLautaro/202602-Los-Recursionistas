# Investigación y decisiones — Automatización de ingesta WhoScored

## Inspección del código real

### Identificadores y vínculos

- `model/Jugador.java` ya posee `idExterno` (Football Data) e `idWhoscored`,
  persistido como `id_whoscored` con restricción de unicidad.
- `model/Equipo.java` y `model/Liga.java` también poseen ambos identificadores,
  con sus columnas `id_externo` e `id_whoscored` y unicidad para WhoScored.
- Los DAOs de liga, equipo y jugador ya permiten buscar por ambos ids. El
  mapper de Football Data solo completa `idExterno`, por lo que los ids de
  WhoScored pueden actualizarse sin alterar la fuente Football Data.
- `ScrapingPort` ya expone descubrimiento de ligas, equipos y jugadores, además
  de apariciones. Los DTOs existentes están bajo `adapter/whoscored/dto/`.

### Autorización actual

- `configuration/SecurityConfig.java` habilita seguridad de métodos, pero la
  protección efectiva de ingesta no está aplicada: el `@PreAuthorize` del
  servicio está comentado.
- El filtro JWT solo carga `SecurityContext` para requests HTTP. Un scheduler
  no debe fabricar ese contexto.
- La nueva protección se ubicará en el controller y/o reglas explícitas de
  `SecurityConfig`, con `ROLE_ADMIN` para iniciar y consultar. El caso de uso
  quedará independiente de sesión y autorización.

### Ejecución y concurrencia actual

- `model/EjecucionIngesta` representa la tabla `ejecuciones_ingesta`, con
  estados `ACTIVA`, `FINALIZADA`, `FALLIDA` y `RECHAZADA`, alcances, fechas,
  temporada, contadores, anomalías, errores, motivo de rechazo y detalle JSONB.
- `IngestaWhoscoredServiceImpl.iniciar` usa `synchronized` y luego consulta
  `existeActiva()`. Si encuentra una activa persiste un rechazo.
- `EjecucionIngestaServiceImpl` contiene reglas paralelas de creación/rechazo.
  Esto no es atómico entre instancias y duplica la responsabilidad.

## Decisiones

### Matching de jugadores

**Decisión:** vinculación jerárquica por plantel. Primero se vinculan liga y
equipo; después se comparan jugadores dentro del plantel por `playerId` cuando
esté disponible. Si no existe, se permite únicamente nombre normalizado sin
acentos junto con fecha de nacimiento o equipo. Se registra el motivo y se
abstiene ante cero coincidencias o ambigüedad.

**Racional:** reduce homónimos y el espacio de búsqueda, respeta el
`playerId` como identificador primario y reutiliza el descubrimiento ya expuesto
por `ScrapingPort`.

**Alternativa descartada:** matching global por nombre y fecha/equipo. Es más
simple de centralizar, pero es más costoso y aumenta el riesgo de vincular
homónimos de equipos distintos.

### Alcance

**Decisión:** conservar `EjecucionIngesta.Alcance` como enum
`COMPLETA/LIGA/EQUIPO/JUGADOR` y usar un único `idAlcance` nullable (mapeado a
los campos existentes si se requiere compatibilidad). `COMPLETA` exige id nulo;
los demás alcances exigen un id positivo y existencia validada antes de aceptar.

**Alternativa descartada:** jerarquía de tipos persistidos. Expresa mejor la
forma de cada alcance, pero agrega entidades, discriminadores y mapeos sin
beneficio solicitado.

### Concurrencia y reserva

**Decisión:** centralizar reserva, rechazo y transición de estado en un solo
caso de uso. La reserva ACTIVA debe ser atómica en PostgreSQL, mediante una
operación transaccional con lock/advisory lock o constraint equivalente; no se
considera suficiente `synchronized` ni una consulta previa. El mismo camino lo
usan scheduler y REST, registrando intentos rechazados.

**Alternativa descartada:** conservar solo `synchronized`. Funciona para una
JVM, pero permite carreras con múltiples instancias.

### Asincronía

**Decisión:** `@Async` con `ThreadPoolTaskExecutor` dedicado en `configuration/`,
con un único worker. El servicio reserva y persiste ACTIVA antes de delegar el
trabajo; el endpoint devuelve el id inmediatamente. Los errores del worker
actualizan la ejecución a `FALLIDA` y conservan resultados parciales.

**Alternativas descartadas:** cola externa (infraestructura fuera de alcance)
y executor manual desde controller (gestión de ciclo de vida y errores más
dispersa).

### Ejecuciones huérfanas

**Decisión:** fuera de alcance. La caída con una ejecución ACTIVA se documenta
como limitación operativa; no se inventa una política de timeout ni se marca
automáticamente como FALLIDA. Se requerirá una decisión posterior para liberar
o recuperar ese registro.

### Tests y constitution

La constitution exige tests unitarios del adapter con fixtures HTML/JSON
locales, sin acceder a la web real durante CI. La feature los incorpora
directamente bajo `src/test/java/.../adapter/whoscored`; no requiere una
enmienda constitucional.

## Integraciones y restricciones

- Playwright permanece exclusivamente en `adapter/whoscored/`; sus errores de
  navegación, red, parsing, timeout y anti-bot se envuelven en
  `ScrapingException`.
- El scheduler se ejecutará con zona `America/Argentina/Buenos_Aires`, en una
  madrugada posterior al cron de Football Data, y será deshabilitable.
- La fuente puede aplicar anti-bot o restringir el scraping. La operación debe
  respetar sus términos, límites y pausas; no se agregan mecanismos de evasión.
