# Feature Specification: Ingesta de estadísticas de jugadores WhoScored

**Feature Branch**: `005-whoscored-player-stats`

**Created**: 2026-10-13

**Status**: Draft

**Input**: User description: "Ingesta de estadísticas de jugadores desde WhoScored para la temporada actual."

## Alcance y restricciones

La funcionalidad permite descubrir, vincular y cargar estadísticas por partido
de jugadores de Premier League, Bundesliga, La Liga, Serie A y Ligue 1 durante
la temporada actual. Los jugadores de destino ya existen en la base y no se
crean nuevos registros.

WhoScored es la fuente de identificación, posición y estadísticas. La carga
debe utilizar el contenido dinámico de sus páginas, respetar pausas y límites
conservadores entre solicitudes, y poder retomarse sin reprocesar
innecesariamente la información ya cargada. El identificador existente de
Football-Data.org (`idExterno`) no se modifica ni se reutiliza: el identificador
de WhoScored se conserva en `idWhoscored`, único y nullable.

Se deben persistir todos los torneos informados por WhoScored durante la
temporada actual, conservando el valor informado en el campo `torneo`.

## Estado actual del modelo persistente

La entidad `Jugador` se persiste en `jugadores` con un identificador propio
`id` generado por la base. Sus campos actuales son `nombre`, `posicion`,
`idExterno` (Football-Data.org), `nacionalidad`, `fechaNacimiento`, `activo` y
una relación obligatoria con `Equipo` mediante `equipo_id`. Estos datos alcanzan
para el matching por fecha de nacimiento y nombre normalizado, usando equipo y
nacionalidad como desempate. El spec agrega el campo nuevo `idWhoscored`,
único y nullable, sin modificar `idExterno`.

`Equipo` se persiste en `equipos` con `id` propio, `idExterno`, nombre,
identificadores descriptivos (`nombreCorto`, `sigla`), datos informativos y
relación opcional con `Liga` mediante `liga_id`. `Liga` se persiste en `ligas`
con `id` propio, `idExterno`, `nombre`, `codigo` y `fechaCreacion`.
Actualmente ninguna de las dos entidades tiene un identificador WhoScored:
el mapeo Equipo/Liga con WhoScored deberá persistir un campo nuevo
`idWhoscored` único y nullable en ambas entidades, separado de `idExterno`.

`JugadorFiltro` es un objeto de filtro sin entidad ni tabla propia. `Usuario`
se persiste en `usuarios` y sus autoridades en `usuarios_autoridades;` no
participa del matching ni de la ingesta de estadísticas.

Los repositorios y servicios existentes trabajan con el `id` interno para
relaciones y con `idExterno` para sincronizar Football-Data.org. Las búsquedas
actuales de `Jugador`, `Equipo` y `Liga` por `idExterno` no incluyen todavía
`idWhoscored`. No hay migraciones SQL versionadas: el proyecto usa PostgreSQL
con `spring.jpa.hibernate.ddl-auto=update`, por lo que el esquema real se
deriva de las entidades al iniciar la aplicación.

## Clarifications

### Session 2026-10-13

- Q: ¿Debe `idWhoscored` mantenerse como un identificador separado de `idExterno` y usarse como la clave primaria de futuras cargas, dejando `idExterno` exclusivamente para Football-Data.org? → A: `idWhoscored` separado, único y nullable; `idExterno` no se modifica y sigue representando Football-Data.org.
- Q: ¿Debe persistirse un registro de estadísticas para los partidos en los que el jugador estuvo en el banco o disputó cero minutos? → A: Persistir apariciones con cero minutos cuando WhoScored lo informa como una aparición válida, diferenciando cero de dato ausente; excluir solo partidos sin participación registrada.
- Q: ¿Qué debe ocurrir si se inicia una segunda carga mientras todavía hay otra ejecución activa? → A: Rechazar la segunda ejecución y registrar el intento mientras la primera continúa.
- Q: ¿Cómo debe persistirse el mapeo de `Equipo` y `Liga` entre Football-Data.org y WhoScored? → A: Agregar `idWhoscored` único y nullable en `Equipo` y `Liga`, separado de `idExterno`.
- Q: ¿Qué alcance de torneos debe persistirse para la temporada actual? → A: Persistir todos los torneos informados por WhoScored durante la temporada actual, guardando el valor informado en `torneo`.
- Q: ¿La carga debe procesar únicamente los registros de `Jugador` con `activo = true`, excluyendo jugadores inactivos aunque tengan `idWhoscored` vinculado? → A: Procesar únicamente `Jugador.activo = true`.
- Q: ¿Quién debe poder iniciar y consultar las ejecuciones de ingesta de WhoScored? → A: Solo usuarios autenticados con autoridad administrativa específica pueden iniciar y consultar ejecuciones.
- Q: ¿Cómo debe determinarse la “temporada actual” para filtrar los partidos de WhoScored? → A: Usar una temporada activa configurada con fecha de inicio y fecha de fin.

## User Scenarios & Testing

### User Story 1 - Vincular jugadores descubiertos (Priority: P1)

Como operador, quiero descubrir los planteles de los equipos de las cinco
ligas y asociar cada jugador encontrado con el jugador correspondiente de mi
base, para habilitar la carga posterior sin crear identidades incorrectas.

**Why this priority**: Sin una vinculación confiable no es posible cargar
estadísticas con seguridad.

**Independent Test**: Con un plantel y un conjunto controlado de jugadores
persistidos, ejecutar la vinculación y verificar que los matches inequívocos
guardan el identificador y que los casos dudosos quedan pendientes de revisión.

**Acceptance Scenarios**:

1. **Given** un jugador persistido con fecha de nacimiento y nombre compatibles,
   **When** se descubre su jugador WhoScored, **Then** se guarda `idWhoscored`
   sin modificar `idExterno`.
2. **Given** dos candidatos con el mismo nombre normalizado, **When** ninguno
   puede desempatarse por fecha, equipo y nacionalidad, **Then** no se vincula
   el jugador y se registra la ambigüedad con sus candidatos.
3. **Given** un jugador descubierto sin candidato persistido, **When** termina
   la vinculación, **Then** no se crean jugadores ni estadísticas y el caso
   queda consultable como no encontrado.

---

### User Story 2 - Cargar partidos y estadísticas (Priority: P2)

Como operador, quiero cargar las estadísticas por partido de cada jugador
vinculado para disponer de datos completos y consultables de la temporada
actual.

**Why this priority**: Es el resultado de negocio que permite utilizar el
rendimiento de los jugadores en etapas posteriores.

**Independent Test**: Con un jugador vinculado y páginas de prueba de las
cuatro categorías de estadísticas, ejecutar la carga y verificar un único
registro por partido con sus datos principales y métricas adicionales.

**Acceptance Scenarios**:

1. **Given** un jugador vinculado con partidos de la temporada actual,
   **When** se ejecuta la carga, **Then** se combinan Summary, Defensive,
   Offensive y Passing en un registro por jugador y partido.
2. **Given** un partido con minutos, rating, goles, pases, tackles y tarjetas,
   **When** se consulta el registro, **Then** cada métrica requerida está
   disponible por separado y las restantes métricas se conservan.
3. **Given** un partido de una temporada anterior, **When** aparece en la
   fuente, **Then** se excluye de la persistencia.

---

### User Story 3 - Reintentar y actualizar sin duplicados (Priority: P2)

Como operador, quiero volver a ejecutar la carga total o acotada a una liga,
equipo o jugador, para incorporar partidos nuevos y actualizar datos corregidos
sin generar duplicados.

**Why this priority**: La fuente se actualiza durante la temporada y una carga
repetible es necesaria para mantener datos confiables.

**Independent Test**: Ejecutar dos veces la misma carga y comparar la cantidad
de partidos únicos y los valores actualizados antes y después.

**Acceptance Scenarios**:

1. **Given** un partido ya cargado, **When** se vuelve a ejecutar la carga,
   **Then** se actualiza el mismo registro y no aparece un duplicado.
2. **Given** una ejecución acotada a un equipo, **When** se solicita la carga,
   **Then** solo se procesan los jugadores de ese equipo.
3. **Given** un jugador que cambió de equipo, **When** se cargan sus partidos,
   **Then** cada partido conserva el equipo y la condición que informa la fuente.

---

### User Story 4 - Revisar resultados y fallos (Priority: P3)

Como operador, quiero consultar los jugadores no vinculados, las ambigüedades,
las anomalías y el resumen de cada ejecución, para corregir los casos dudosos
sin perder el avance del resto.

**Why this priority**: La trazabilidad permite resolver excepciones sin
arriesgar la integridad de las estadísticas ya confirmadas.

**Independent Test**: Provocar un timeout, una columna con formato inesperado y
un jugador ambiguo; verificar que el resto continúa, que no quedan datos
parciales del jugador fallido y que el resumen informa cada resultado.

**Acceptance Scenarios**:

1. **Given** un error de navegación o parsing para un jugador, **When** se
   procesa la ejecución, **Then** el resto de jugadores continúa y el error
   queda registrado con su jugador y causa operativa.
2. **Given** una métrica faltante o con formato inesperado, **When** se procesa
   el partido, **Then** se guarda el resto de los datos disponibles y se registra
   la anomalía.
3. **Given** una ejecución finalizada, **When** el operador consulta su resumen,
   **Then** ve procesados, vinculados, no vinculados, partidos insertados,
   partidos actualizados y errores.

### Edge Cases

- Los slugs con acentos o caracteres especiales deben resolverse sin alterar el
  identificador numérico del jugador.
- Un jugador puede aparecer en partidos con distintos equipos durante la
  temporada; cada partido conserva el equipo que figura en la fuente.
- Jugadores con el mismo nombre deben resolverse por fecha de nacimiento y,
  cuando sea necesario, equipo y nacionalidad; la duda impide persistir.
- Un jugador sin partidos se conserva vinculado, pero no genera estadísticas
  inexistentes. Una aparición válida informada por WhoScored se persiste aun
  con cero minutos; un partido sin registro de participación no se persiste.
- Los partidos de copas, competiciones europeas u otros torneos se guardan si
  pertenecen a la temporada actual y WhoScored informa el torneo.
- Un bloqueo anti-bot, timeout o cambio de estructura debe generar un error
  controlado, sin interrumpir a los demás jugadores.
- Los datos faltantes no deben convertirse silenciosamente en valores
  inventados; se guarda lo disponible y se informa la anomalía.

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE descubrir los equipos y jugadores de Premier
  League, Bundesliga, La Liga, Serie A y Ligue 1 desde WhoScored.
- **FR-002**: El sistema DEBE descubrir para cada jugador su nombre,
  identificador WhoScored, equipo y posición cuando estén informados.
- **FR-003**: El sistema DEBE asociar un jugador descubierto con un jugador
  persistido usando, en este orden, el `idWhoscored` ya guardado, la fecha de
  nacimiento exacta junto con el nombre normalizado sin acentos ni mayúsculas,
  y finalmente equipo y nacionalidad como desempate. `idExterno` se reserva
  exclusivamente para Football-Data.org y no se usa como identificador de
  WhoScored.
- **FR-004**: El sistema DEBE guardar `idWhoscored` como identificador único y
  nullable en `Jugador`, `Equipo` y `Liga`, separado de `idExterno`, y guardar
  la posición WhoScored asociada al jugador.
- **FR-005**: El sistema NO DEBE crear jugadores nuevos ni modificar
  `idExterno` durante esta funcionalidad.
- **FR-006**: El sistema DEBE registrar cada jugador no encontrado o ambiguo
  con su identificador y nombre WhoScored, motivo y candidatos disponibles.
- **FR-007**: El sistema NO DEBE persistir estadísticas de jugadores no
  vinculados, ambiguos o con una carga fallida parcialmente.
- **FR-008**: Para cada jugador vinculado, el sistema DEBE combinar Summary,
  Defensive, Offensive y Passing en un único registro por jugador y partido.
- **FR-009**: Cada registro DEBE conservar fecha, torneo, equipo, rival,
  condición local/visitante, identificador del partido WhoScored y temporada.
- **FR-010**: Cada registro DEBE exponer por separado minutos, rating, goles,
  asistencias, tiros, tiros al arco, pases, precisión de pases, pases clave,
  regates, tackles, intercepciones, faltas cometidas, amarillas y rojas.
- **FR-011**: El sistema DEBE conservar también las métricas adicionales que
  estén disponibles en las cuatro categorías, sin descartar información por no
  tener un campo consultable individual.
- **FR-012**: El identificador compuesto por jugador y partido WhoScored DEBE
  ser único; una nueva carga DEBE insertar partidos nuevos y actualizar los
  existentes.
- **FR-013**: El operador DEBE poder ejecutar la carga completa o limitarla a
  una liga, equipo o jugador, con un diseño reutilizable por una ejecución
  periódica futura. En la carga completa, solo se procesan registros
  `Jugador` con `activo = true`.
- **FR-014**: El sistema DEBE continuar con los demás jugadores ante un error
  individual y registrar el error sin conservar datos parciales de ese jugador.
- **FR-015**: El sistema DEBE registrar por ejecución jugadores procesados,
  vinculados, no vinculados, partidos insertados, partidos actualizados,
  errores y anomalías.
- **FR-016**: El sistema DEBE aceptar slugs codificados y nombres con
  caracteres especiales sin perder el identificador numérico de WhoScored.
- **FR-017**: El proceso DEBE aplicar pausas, límites de solicitudes y
  reintentos acotados configurables para evitar paralelismo agresivo y permitir
  reanudar la carga.
- **FR-018**: El sistema DEBE documentar que los términos de WhoScored y sus
  protecciones anti-bot pueden restringir o impedir la ingesta.
- **FR-019**: El sistema DEBE rechazar una nueva ejecución mientras exista otra
  ejecución activa, informar el motivo al operador y registrar el intento sin
  interrumpir la ejecución en curso.
- **FR-020**: El sistema DEBE permitir iniciar y consultar ejecuciones de
  ingesta únicamente a usuarios autenticados que posean una autoridad
  administrativa específica.
- **FR-021**: El sistema DEBE filtrar los partidos mediante una temporada
  activa configurada con fecha de inicio y fecha de fin, de modo que una
  ejecución repetida aplique siempre el mismo intervalo.

### Key Entities

- **Jugador**: Entidad existente de la tabla `jugadores`, con `id` interno
  generado, `idExterno`, `idWhoscored` nuevo y nullable, nombre, fecha de
  nacimiento, nacionalidad, posición, estado `activo` y relación obligatoria
  con `Equipo`.
- **Equipo**: Entidad existente de `equipos`, con `id` interno, `idExterno`,
  `idWhoscored` nuevo y nullable, datos de identificación y relación con
  `Liga`.
- **Liga**: Entidad existente de `ligas`, con `id` interno, `idExterno`,
  `idWhoscored` nuevo y nullable, `nombre`, `codigo` y fecha de creación.
- **JugadorFiltro**: Objeto de filtro existente, sin tabla persistente, usado
  para buscar jugadores por nombre, posición, liga y equipo.
- **Jugador descubierto**: Identidad obtenida desde WhoScored con nombre,
  identificador, equipo, nacionalidad, fecha de nacimiento y URL o slug.
- **Estadística de partido**: Rendimiento de un jugador en un partido de la
  temporada actual, con contexto del partido, métricas principales y el
  conjunto completo de métricas adicionales.
- **Caso de vinculación**: Resultado no resuelto de un match, con motivo,
  jugador descubierto, candidatos y estado para revisión.
- **Ejecución de ingesta**: Resumen auditable de una carga, su alcance,
  timestamps, contadores, errores y anomalías.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Al menos el 95% de los jugadores activos de las cinco ligas
  queda vinculado correctamente en una ejecución completa; el resto queda
  identificado y consultable como no vinculado o ambiguo.
- **SC-002**: El 100% de los registros persistidos corresponde a un jugador
  vinculado y a un partido de la temporada actual; ningún caso ambiguo genera
  estadísticas.
- **SC-003**: Dos ejecuciones consecutivas con la misma fuente producen la
  misma cantidad de partidos únicos por jugador y actualizan, en lugar de
  duplicar, los partidos existentes.
- **SC-004**: Para el 100% de los partidos cargados, las métricas mínimas
  definidas pueden consultarse por jugador y rango de fechas, incluyendo
  valores ausentes claramente diferenciados de cero.
- **SC-005**: Un error aislado no impide procesar el resto de los jugadores y
  el resumen de ejecución informa el 100% de los errores observados.
- **SC-006**: El operador puede identificar el motivo y los candidatos de cada
  caso no resuelto sin revisar manualmente el contenido original de la página.
- **SC-007**: Una ejecución acotada a una liga, equipo o jugador no procesa
  registros fuera del alcance seleccionado.

## Assumptions

- La temporada activa se configura con fecha de inicio y fecha de fin; no se
  infiere del año calendario ni cambia automáticamente por la fecha del
  sistema.
- Los jugadores persistidos tienen nombre, fecha de nacimiento, equipo y
  nacionalidad suficientes para intentar el matching.
- Los equipos equivalentes entre Football-Data.org y WhoScored pueden
  identificarse mediante un mapeo mantenido por la funcionalidad.
- La ejecución se realiza bajo demanda en esta entrega; una futura ejecución
  periódica reutilizará el mismo alcance y contrato.
- Los datos de WhoScored se usan respetando sus términos de uso y cualquier
  restricción legal u operativa aplicable.
- Las pruebas automatizadas utilizan datos locales controlados y no dependen
  de que WhoScored esté disponible.
