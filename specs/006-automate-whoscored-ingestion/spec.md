# Feature Specification: Automatizar la ingesta de estadísticas WhoScored

**Feature Branch**: `006-automate-whoscored-ingestion`

**Created**: 2026-10-19

**Status**: Draft

**Input**: User description: "Crea la spec para inicializar y automatizar la ingesta de estadísticas de WhoScored. Actualmente las tablas de WhoScored y estadísticas de partidos están vacías porque no existe un disparador en runtime, los jugadores no tienen idWhoscored y la autorización administrativa no puede ejecutarse desde un job en segundo plano. Se necesita un scheduler, una fase previa de descubrimiento y vinculación, un trigger REST administrativo asíncrono y consulta de la última ejecución."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ejecutar automáticamente la ingesta completa (Priority: P1)

Como operador del sistema, quiero que la ingesta de WhoScored se ejecute
automáticamente en una ventana programada para que las estadísticas de los
jugadores se mantengan actualizadas sin intervención manual.

**Why this priority**: Es la solución principal al problema de tablas vacías:
sin un disparador operativo, el servicio existente nunca procesa datos reales.

**Independent Test**: Configurar una ejecución programada en un entorno de
prueba y verificar que se inicia una única ejecución, se completan las fases
de vinculación e ingesta y quedan resultados consultables.

**Acceptance Scenarios**:

1. **Given** que llegó la ventana programada y no existe una ejecución activa,
   **When** el sistema dispara la tarea de WhoScored, **Then** inicia el flujo
   completo sin requerir una sesión de usuario.
2. **Given** que los jugadores locales todavía no tienen identificador
   WhoScored, **When** comienza una ejecución automática, **Then** primero
   descubre los planteles y vincula los jugadores coincidentes, y luego carga
   estadísticas solamente para jugadores con identificador vinculado.
3. **Given** que una fuente externa falla para una liga, equipo o jugador,
   **When** continúa la ejecución, **Then** registra el error, conserva los
   resultados válidos y permite finalizar o informar la ejecución sin perder
   los datos ya procesados.
4. **Given** que el proceso se ejecuta nuevamente sobre datos ya cargados,
   **When** procesa las mismas apariciones, **Then** actualiza los registros
   existentes sin crear duplicados.

### User Story 2 - Iniciar una ingesta manual administrativa (Priority: P2)

Como administrador, quiero iniciar manualmente una ingesta completa o limitada
por alcance para recuperar datos después de una falla o actualizar una parte
del catálogo sin esperar a la ventana programada.

**Why this priority**: Permite operar y recuperar el sistema cuando la
actualización automática no alcanza o debe ejecutarse de inmediato.

**Independent Test**: Autenticar un usuario administrador, solicitar una
ingesta completa y otra acotada, verificar una respuesta de aceptación
inmediata y comprobar que cada ejecución respeta el alcance solicitado.

**Acceptance Scenarios**:

1. **Given** un administrador autenticado y ninguna ejecución activa, **When**
   solicita una ingesta completa, **Then** el sistema acepta la solicitud,
   devuelve `202 Accepted` y procesa el trabajo fuera de la solicitud HTTP.
2. **Given** un administrador autenticado, **When** solicita una ingesta por
   liga, equipo o jugador con un identificador válido, **Then** el sistema
   acepta la solicitud y procesa únicamente el alcance indicado, incluyendo la
   vinculación previa necesaria.
3. **Given** un usuario autenticado sin autoridad administrativa, **When**
   intenta iniciar o consultar una ejecución, **Then** el sistema rechaza la
   operación y no inicia ninguna carga.
4. **Given** una solicitud con alcance o identificador inválido, **When** se
   recibe el pedido, **Then** el sistema devuelve un error de validación claro
   y no crea una ejecución activa.

### User Story 3 - Consultar el estado de la última ejecución (Priority: P2)

Como administrador, quiero consultar el estado de la última ejecución para
conocer si la carga está activa, finalizó, fue rechazada o tuvo errores.

**Why this priority**: La ejecución automática y la respuesta asíncrona no
permiten conocer el resultado desde la solicitud inicial; la observabilidad es
necesaria para operar el sistema.

**Independent Test**: Ejecutar una carga, consultar el estado durante y después
del procesamiento y verificar que los estados, contadores y errores reflejan
el resultado real.

**Acceptance Scenarios**:

1. **Given** que existe una última ejecución registrada, **When** un
   administrador consulta su estado, **Then** recibe su estado, alcance,
   fechas, contadores y motivo de rechazo o error cuando corresponda.
2. **Given** que no existe ninguna ejecución registrada, **When** un
   administrador consulta la última ejecución, **Then** recibe una respuesta
   que indica claramente que todavía no hay datos disponibles.
3. **Given** que una ejecución está activa, **When** se consulta su estado,
   **Then** se informa que continúa activa y no se presenta como finalizada.

### Edge Cases

- Si llega el disparador programado mientras existe una ejecución activa, el
  nuevo intento se rechaza y queda registrado sin interrumpir la ejecución
  existente.
- Si la fase de vinculación no encuentra coincidencias, la fase de
  estadísticas no crea jugadores ni procesa jugadores sin identificador
  WhoScored; la ejecución queda informada con cero jugadores vinculados.
- Si una respuesta externa está vacía, incompleta o no puede interpretarse, el
  error se registra para el alcance afectado y no se convierte en una
  ejecución exitosa sin explicación.
- Si el proceso se detiene después de vincular jugadores pero antes de cargar
  estadísticas, una nueva ejecución puede retomarlo sin duplicar vínculos ni
  estadísticas.
- Si una consulta manual se repite mientras la primera ejecución sigue activa,
  ambas solicitudes se registran como intentos rechazados y solo una carga
  continúa.
- Si el sistema no tiene sesión de usuario durante una ejecución automática,
  el flujo de negocio igualmente puede ejecutarse, mientras que los endpoints
  manuales siguen protegidos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST ejecutar una ingesta de WhoScored de forma
  programada en la ventana configurada para la aplicación, con una frecuencia
  predeterminada durante la madrugada de la zona horaria de Argentina.
- **FR-002**: La ejecución programada MUST orquestar dos fases en orden:
  descubrimiento y vinculación de ligas, equipos y jugadores; luego carga de
  estadísticas de partidos.
- **FR-003**: La fase de vinculación MUST actualizar los identificadores de
  WhoScored de ligas, equipos y jugadores coincidentes sin modificar los
  identificadores provenientes de Football Data.
- **FR-004**: La fase de estadísticas MUST procesar únicamente jugadores
  activos con identificador WhoScored vinculado y aplicar el rango de temporada
  vigente.
- **FR-005**: El sistema MUST conservar la idempotencia: repetir una carga
  MUST actualizar información existente sin generar vínculos o estadísticas
  duplicados.
- **FR-006**: El flujo automático MUST ejecutarse sin requerir sesión,
  credenciales ni contexto de autorización HTTP.
- **FR-007**: El sistema MUST exponer un punto de administración para iniciar
  una ingesta completa o acotada a liga, equipo o jugador.
- **FR-008**: El punto de inicio manual MUST estar disponible únicamente para
  usuarios autenticados con autoridad `ROLE_ADMIN`.
- **FR-009**: Una solicitud manual aceptada MUST responder `202 Accepted` sin
  bloquear la solicitud hasta que termine la ingesta y MUST devolver una
  referencia que permita identificar la ejecución iniciada.
- **FR-010**: El sistema MUST validar el alcance y el identificador antes de
  aceptar una solicitud manual; los valores inválidos MUST producir un error
  explícito sin iniciar una ejecución.
- **FR-011**: El sistema MUST mantener el rechazo y registro de un nuevo
  intento cuando ya existe una ejecución en estado ACTIVA.
- **FR-012**: El sistema MUST exponer una consulta administrativa para obtener
  la última ejecución de WhoScored.
- **FR-013**: La consulta de estado MUST estar protegida con `ROLE_ADMIN` y
  MUST informar estado, alcance, identificador, inicio, finalización,
  contadores, errores, anomalías y motivo de rechazo cuando existan.
- **FR-014**: Si no existe ninguna ejecución, la consulta de última ejecución
  MUST comunicar esa situación sin inventar un resultado.
- **FR-015**: Los errores parciales de la fuente externa MUST quedar
  registrados en la ejecución y MUST permitir conservar los datos válidos
  obtenidos antes o después del error.
- **FR-016**: El scheduler y el trigger manual MUST reutilizar el mismo caso
  de uso de ingesta y el mismo control de concurrencia, sin duplicar reglas de
  negocio.
- **FR-017**: La integración con WhoScored MUST permanecer aislada del resto
  del sistema mediante el contrato de scraping existente, sin exponer detalles
  de extracción en los endpoints públicos.
- **FR-018**: El sistema MUST registrar en el detalle de la ejecución los
  candidatos, resultados, motivos y ambigüedades del matching, sin crear una
  entidad o tabla independiente para cada caso de vinculación.

### Key Entities

- **Ejecución de ingesta**: Representa una corrida automática o manual, su
  alcance, estado, fechas, temporada aplicada, contadores de procesamiento,
  anomalías, errores y motivo de rechazo.
- **Alcance de ingesta**: Identifica si la ejecución cubre todos los datos, una
  liga, un equipo o un jugador, junto con el identificador requerido cuando
  corresponda.
- **Vinculación WhoScored**: Relación entre una entidad local existente y su
  identificador en WhoScored, conservando separados los identificadores de
  Football Data y WhoScored. Los candidatos, resultados, motivos y
  ambigüedades se auditan en el detalle de la ejecución, sin una entidad
  persistente adicional.
- **Estadística de partido**: Datos de rendimiento de un jugador en un partido
  de la temporada configurada, actualizables sin duplicación.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: En una ejecución con una fuente externa disponible, el 100% de
  las ejecuciones aceptadas realiza la fase de vinculación antes de comenzar
  la fase de estadísticas.
- **SC-002**: Después de una ejecución exitosa sobre las cinco ligas
  configuradas, al menos el 95% de los jugadores locales que tienen una
  coincidencia inequívoca quedan vinculados con un identificador WhoScored.
- **SC-003**: Repetir la misma ingesta dos veces no aumenta la cantidad de
  vínculos ni de estadísticas para las mismas entidades y partidos.
- **SC-004**: El 100% de las solicitudes manuales válidas responde con
  aceptación en menos de 2 segundos, independientemente de la duración total
  de la ingesta.
- **SC-005**: El 100% de los intentos de inicio y consulta realizados por
  usuarios sin autoridad administrativa son rechazados y no inicia ningún
  procesamiento.
- **SC-006**: El estado consultado de una ejecución activa, finalizada o
  rechazada coincide con el resultado observado y contiene los contadores y
  errores disponibles.
- **SC-007**: Ante un error aislado de una liga, equipo o jugador, al menos el
  90% de los datos válidos de los demás alcances se conserva y la ejecución
  deja el error identificado para su recuperación.
- **SC-008**: Los operadores pueden determinar, mediante la consulta de estado,
  si la última carga terminó correctamente o requiere revisión sin consultar
  logs internos.

## Assumptions

- Las cinco ligas objetivo y el rango de temporada continúan definidos por la
  configuración operativa existente; esta feature no agrega nuevas
  competiciones.
- Los jugadores locales ya fueron creados por la sincronización de Football
  Data; esta feature vincula identidades y no crea jugadores nuevos a partir
  de WhoScored.
- La autenticación administrativa existente y la autoridad `ROLE_ADMIN` son
  suficientes para proteger las operaciones manuales y de consulta.
- La respuesta asíncrona incluye una referencia a la ejecución persistida; el
  cliente no necesita mantener abierta la conexión HTTP.
- La configuración del scheduler puede deshabilitarse para tareas automáticas
  sin eliminar la posibilidad de iniciar una ejecución manual.
- Las pausas, límites, reintentos y restricciones anti-bot existentes siguen
  aplicándose sin incorporar mecanismos para evadir protecciones de la fuente.
- PostgreSQL y las credenciales de las fuentes externas están disponibles en el
  entorno operativo antes de iniciar una carga.
