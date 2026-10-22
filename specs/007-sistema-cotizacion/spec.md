# Feature Specification: Sistema de Cotización de Jugadores

**Feature Branch**: `007-sistema-cotizacion`

**Created**: 2026-10-21

**Status**: Draft

**Input**: User description: "Crear un sistema de cotización de jugadores que calcule periódicamente un valor de mercado a partir de estadísticas de WhoScored, permita consultar historiales y rankings, y admita recálculo manual administrativo mediante estrategias configurables."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calcular cotizaciones periódicas (Priority: P1)

Como operador del sistema, necesito que cada jugador activo con estadísticas de
WhoScored reciba una cotización calculada a partir de su rendimiento reciente,
para mantener actualizado el precio de su token sin intervención manual.

**Why this priority**: Es el valor central de la feature y habilita el historial,
el ranking y las consultas de cotización.

**Independent Test**: Con jugadores activos, estadísticas dentro y fuera de la
ventana, y una estrategia seleccionada, ejecutar el recálculo y verificar que
se persiste una cotización por jugador elegible con sus datos de cálculo.

**Acceptance Scenarios**:

1. **Given** un jugador activo con `idWhoscored` y estadísticas dentro de la
   ventana configurada, **When** se ejecuta el recálculo, **Then** se agregan sus
   métricas, se aplica la estrategia activa y se persiste su nueva cotización.
2. **Given** un jugador inactivo, sin `idWhoscored` o sin estadísticas en la
   ventana, **When** se ejecuta el recálculo, **Then** no se crea una cotización
   para ese jugador y el resto del proceso continúa.
3. **Given** una estrategia activa válida, **When** finaliza el recálculo,
   **Then** cada cotización registra el nombre de la estrategia, la ventana
   utilizada y el timestamp de cálculo.
4. **Given** que el scheduler semanal se ejecuta después de la ingesta de
   WhoScored, **When** llega su horario configurado, **Then** el recálculo se
   ejecuta sin depender de una sesión HTTP ni de un usuario autenticado.

---

### User Story 2 - Consultar historial de cotizaciones (Priority: P1)

Como usuario, necesito consultar el historial de cotizaciones de un jugador en
un rango de fechas opcional, para conocer cómo evolucionó su valor y qué
estrategia produjo cada cálculo.

**Why this priority**: El historial permite auditar los precios publicados y
responde consultas sobre cualquier fecha pasada.

**Independent Test**: Crear varias cotizaciones históricas para un jugador,
consultar el endpoint con y sin fechas, y verificar orden, paginación y campos.

**Acceptance Scenarios**:

1. **Given** un jugador existente con cotizaciones históricas, **When** se
   consulta `GET /players/{id}/quotes`, **Then** se devuelve una página ordenada
   por timestamp de cálculo descendente con valor, estrategia, ventana y fecha.
2. **Given** los parámetros opcionales `desde` y `hasta`, **When** se consulta
   el historial, **Then** solo se incluyen cotizaciones cuyo cálculo esté dentro
   del rango inclusivo indicado.
3. **Given** un jugador inexistente, **When** se consulta su historial,
   **Then** se devuelve un error de recurso no encontrado sin datos ficticios.
4. **Given** que no hay cotizaciones en el rango, **When** se consulta el
   historial, **Then** se devuelve una página vacía con metadatos de paginación
   válidos.

---

### User Story 3 - Consultar ranking de jugadores (Priority: P1)

Como usuario, necesito ver los jugadores ordenados por su cotización más
reciente, con filtros por liga y posición, para comparar oportunidades de
inversión.

**Why this priority**: El ranking convierte las cotizaciones en una vista útil
para tomar decisiones y debe estar disponible junto con el cálculo.

**Independent Test**: Crear jugadores con cotizaciones recientes y anteriores,
consultar el ranking con distintos filtros y verificar orden, variación y
metadatos de página.

**Acceptance Scenarios**:

1. **Given** jugadores elegibles con cotización reciente, **When** se consulta
   `GET /players/ranking`, **Then** se devuelven ordenados por cotización más
   reciente de mayor a menor.
2. **Given** filtros opcionales `ligaId` y `posicion`, **When** se consulta el
   ranking, **Then** solo se incluyen jugadores que cumplen todos los filtros.
3. **Given** que un jugador tiene cotización actual y anterior, **When** aparece
   en el ranking, **Then** la respuesta incluye la variación entre ambas.
4. **Given** jugadores sin cotización, **When** se consulta el ranking, **Then**
   no se incluyen hasta que exista una cotización reciente.

---

### User Story 4 - Solicitar recálculo manual (Priority: P2)

Como administrador, necesito iniciar un recálculo manual sin esperar al
scheduler, para corregir o actualizar las cotizaciones cuando sea necesario.

**Why this priority**: Permite operación y recuperación controlada sin alterar
la ejecución periódica.

**Independent Test**: Invocar el endpoint con credenciales administrativas y
no administrativas, y verificar autorización, respuesta inmediata y ejecución
posterior del job.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado con `ROLE_ADMIN`, **When** realiza
   `POST /quotes/recalculate`, **Then** recibe `202 Accepted` y el recálculo se
   inicia de forma asíncrona.
2. **Given** un usuario sin `ROLE_ADMIN` o no autenticado, **When** invoca el
   endpoint, **Then** la solicitud es rechazada y no se inicia ningún recálculo.
3. **Given** que ya hay un recálculo en curso, **When** se solicita otro,
   **Then** el sistema no ejecuta dos recálculos concurrentes sobre la misma
   ventana y comunica el estado al solicitante.

### Edge Cases

- Si una métrica es nula, se considera cero para el cálculo y no se interrumpe
  el recálculo del jugador.
- Si el máximo de una métrica en la ventana es cero, su valor normalizado es
  cero y no se divide por cero.
- Si `desde` es posterior a `hasta`, la consulta se rechaza con un error de
  validación.
- Si una posición no coincide con las categorías conocidas, se usan los pesos
  generales configurados para la estrategia posicional.
- Si la estrategia configurada no existe o está incompleta, el job falla de
  forma explícita, no persiste cotizaciones parciales y deja un error operativo.
- Si dos cotizaciones tienen el mismo timestamp, el orden secundario es el
  identificador de cotización descendente para mantener resultados estables.
- Un error al calcular un jugador se registra y no impide procesar los demás;
  el resultado del job informa los jugadores omitidos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST calcular cotizaciones únicamente para jugadores
  activos con `idWhoscored` vinculado.
- **FR-002**: El sistema MUST agregar las estadísticas de cada jugador por
  partido dentro de una ventana configurable en días, cuyo valor predeterminado
  sea 7.
- **FR-003**: El sistema MUST seleccionar una única estrategia activa mediante
  `cotizacion.estrategia.activa`, sin que el servicio de cotización contenga
  condicionales específicos para cada estrategia.
- **FR-004**: El sistema MUST incluir al menos las estrategias
  `PERFORMANCE_GENERAL` y `PERFORMANCE_POR_POSICION`, ambas seleccionables por
  configuración.
- **FR-005**: `PERFORMANCE_GENERAL` MUST normalizar cada métrica en el intervalo
  [0,1] usando el máximo observado en la ventana y calcular el score como:
  `0.25*goles + 0.15*asistencias + 0.10*tiros + 0.10*pasesClave +
  0.10*regates + 0.10*tackles + 0.20*rating`.
- **FR-006**: Ambas estrategias MUST calcular el valor como
  `valorBase + (score * factorEscala)`, con `valorBase` y `factorEscala`
  configurables y con resultado monetario de precisión decimal.
- **FR-007**: `PERFORMANCE_POR_POSICION` MUST usar pesos configurables por
  porteros, defensores, mediocampistas y delanteros, incluyendo equivalentes en
  inglés, y MUST contemplar énfasis en las métricas descritas para cada posición.
- **FR-008**: El sistema MUST externalizar en `application.yml`, con fallback
  mediante variables de entorno, la estrategia activa, la ventana, los pesos,
  `valorBase`, `factorEscala` y el cron.
- **FR-009**: Cada cotización persistida MUST registrar jugador, valor calculado,
  timestamp, estrategia usada, inicio de ventana y fin de ventana. El nombre de
  estrategia registrado MUST ser inmutable después de persistirse y no se deben
  crear tablas adicionales de versiones.
- **FR-010**: El sistema MUST ejecutar el recálculo semanal mediante
  `CotizacionScheduler`, con cron configurable, zona
  `America/Argentina/Buenos_Aires` y una ventana posterior a
  `WhoScoredScheduler`.
- **FR-011**: El scheduler y el recálculo manual MUST ejecutarse sin depender de
  sesión HTTP ni `SecurityContext`.
- **FR-012**: `GET /players/{id}/quotes` MUST aceptar `desde`, `hasta`, `page` y
  `size`, devolver historial paginado con tamaño predeterminado 12 y documentar
  sus parámetros y respuestas mediante OpenAPI.
- **FR-013**: `GET /players/ranking` MUST aceptar `ligaId`, `posicion`, `page` y
  `size`, devolver jugadores paginados por cotización reciente descendente e
  incluir id, nombre, posición, equipo, liga, cotización actual y variación
  respecto de la anterior. El tamaño predeterminado MUST ser 12.
- **FR-014**: `POST /quotes/recalculate` MUST requerir `ROLE_ADMIN`, iniciar el
  recálculo de forma asíncrona y responder `202 Accepted` cuando la solicitud
  sea aceptada.
- **FR-015**: El sistema MUST rechazar parámetros inválidos, informar errores
  operativos y evitar respuestas exitosas con datos parciales no identificados.
- **FR-016**: La lógica de cálculo MUST permanecer en estrategias intercambiables
  del módulo de servicios; no debe vivir en entidades, controladores ni
  adaptadores.
- **FR-017**: Deben existir tests de servicio para el servicio de cotización y
  para cada estrategia concreta, cubriendo camino feliz, normalización, límites,
  posiciones, configuración inválida y excepciones relevantes.
- **FR-018**: Los tres endpoints MUST estar documentados con OpenAPI y sus
  respuestas de error MUST ser consistentes con el contrato HTTP del proyecto.

### Key Entities

- **Cotizacion**: valor de mercado de un jugador en un momento determinado;
  incluye jugador, valor, timestamp, estrategia utilizada y ventana de cálculo.
- **Resumen de rendimiento**: métricas agregadas de las estadísticas de partido
  de un jugador dentro de la ventana seleccionada, usado para producir un score.
- **Estrategia de cotización**: regla intercambiable que recibe el resumen de
  rendimiento, la posición y parámetros configurados, y devuelve un valor.
- **Entrada de ranking**: representación de un jugador con su cotización más
  reciente, cotización anterior y variación, además de su liga, equipo y
  posición.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Para un conjunto de 1.000 jugadores elegibles, al menos el 99% de
  los jugadores con datos válidos recibe una cotización en cada ejecución
  completa del job.
- **SC-002**: El 100% de las cotizaciones persistidas contiene estrategia,
  timestamp, valor e inicio y fin de ventana válidos.
- **SC-003**: Al menos el 95% de las consultas de historial y ranking devuelve
  resultados en menos de 2 segundos bajo una carga de 100 solicitudes
  concurrentes.
- **SC-004**: Un administrador puede iniciar un recálculo manual y recibir
  confirmación de aceptación en menos de 1 segundo, sin esperar a que termine el
  procesamiento.
- **SC-005**: El 100% de los casos de prueba de las dos estrategias confirma
  normalización, pesos, valor final y comportamiento ante datos límite.
- **SC-006**: Un usuario puede identificar para cada resultado del ranking la
  cotización actual y su variación anterior sin consultar otra fuente.

## Assumptions

- La autenticación y autorización existentes, incluido `ROLE_ADMIN`, se
  reutilizan sin crear un mecanismo nuevo.
- `EstadisticaPartido`, `Jugador`, equipo y liga ya exponen los datos necesarios
  y sus relaciones se mantienen sin cambios funcionales ajenos a esta feature.
- Las posiciones se normalizan de forma insensible a mayúsculas, acentos,
  guiones y espacios para reconocer español e inglés.
- Como valores iniciales de la estrategia posicional se usarán pesos
  configurables que sumen 1: delanteros priorizan goles 0.35 y tiros 0.20;
  defensores tackles 0.30 e intercepciones 0.20; mediocampistas pasesClave
  0.20 y asistencias 0.20; porteros intercepciones y minutos, con goles y
  asistencias en cero. Los restantes pesos se definirán en la configuración
  inicial respetando esas prioridades.
- El cron predeterminado será el domingo a las 04:00, hora de Argentina, y se
  configurará después del cron vigente de `WhoScoredScheduler`.
- La variación se calculará como diferencia absoluta entre valor actual y
  anterior; el porcentaje no se incluye en esta versión.
- Los valores monetarios se redondean a dos decimales usando una regla
  consistente en todo el sistema.
- El ranking muestra la cotización más reciente disponible, aunque su estrategia
  no coincida con la estrategia actualmente configurada.
- El alcance de esta feature no incluye compra, venta, emisión, liquidez ni
  movimientos de tokens.
- El alcance de esta feature no incluye una interfaz frontend nueva.
