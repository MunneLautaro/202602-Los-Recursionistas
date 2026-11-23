# Feature Specification: Gestión de estrategia de cotización

**Feature Branch**: `011-gestion-estrategia-cotizacion`

**Created**: 2026-11-22

**Status**: Draft

**Input**: User description: "Permitir que un administrador vea y cambie la estrategia activa de cotización desde el panel, persistir el cambio y su auditoría, proteger la operación y mostrar nombres legibles de la estrategia en todo el historial."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar la estrategia activa (Priority: P1)

Como administrador, quiero ver en mi panel cuál es la estrategia de cotización
activa y qué alternativas existen, junto con una explicación breve de cómo
calcula el valor, para decidir cuál utilizar sin interpretar códigos técnicos.

**Why this priority**: Es la capacidad mínima que permite a la administración
conocer el estado actual y las opciones disponibles antes de realizar cambios.

**Independent Test**: Iniciar sesión como administrador, abrir el panel junto
a la opción de recálculo y verificar que se muestran la estrategia activa, las
estrategias disponibles, sus nombres legibles y sus descripciones.

**Acceptance Scenarios**:

1. **Given** un administrador autenticado en el panel, **When** consulta la
   sección de estrategia de cotización, **Then** se muestra la estrategia
   activa con su nombre legible y descripción.
2. **Given** existen las estrategias generales y por posición, **When** se
   muestra la sección, **Then** ambas aparecen con un nombre legible y una
   descripción comprensible de su cálculo.
3. **Given** una estrategia activa elegida previamente, **When** el panel se
   recarga, **Then** continúa mostrando esa misma estrategia mientras no haya
   sido reemplazada.

---

### User Story 2 - Cambiar la estrategia activa (Priority: P1)

Como administrador, quiero seleccionar otra estrategia y confirmarla desde el
panel, para que los próximos recálculos utilicen la nueva estrategia sin
reiniciar el sistema.

**Why this priority**: Cambiar la estrategia en operación es el objetivo
principal de la feature y evita cambios que requieran intervención técnica.

**Independent Test**: Iniciar sesión como administrador, elegir una estrategia
diferente, confirmar el diálogo y ejecutar el recálculo manual o esperar al
siguiente recálculo programado.

**Acceptance Scenarios**:

1. **Given** el administrador selecciona una estrategia disponible diferente,
   **When** confirma el cambio, **Then** la estrategia activa cambia y el panel
   confirma el resultado.
2. **Given** se confirmó un cambio, **When** el administrador ejecuta el
   próximo recálculo manual o llega el siguiente recálculo programado,
   **Then** las nuevas cotizaciones usan la estrategia recién activa.
3. **Given** se confirmó un cambio, **When** el cambio finaliza,
   **Then** las cotizaciones anteriores permanecen iguales y conservan la
   estrategia con la que fueron calculadas.
4. **Given** el administrador selecciona la estrategia que ya está activa,
   **When** confirma la operación, **Then** se conserva el estado sin mostrar
   error y no se crea un nuevo registro de auditoría.
5. **Given** se inicia un cambio mientras hay un recálculo en curso,
   **When** el sistema lo procesa, **Then** rechaza el cambio, conserva la
   estrategia activa y muestra el mensaje que explica el conflicto.

---

### User Story 3 - Auditar el último cambio (Priority: P1)

Como administrador, quiero conocer quién realizó el último cambio de estrategia
y cuándo, para explicar por qué podían variar las cotizaciones.

**Why this priority**: La auditoría aporta trazabilidad operativa y ayuda a
distinguir cambios administrativos de variaciones normales del mercado.

**Independent Test**: Realizar un cambio efectivo con un administrador,
recargar el panel y verificar que aparecen el usuario responsable y la fecha
y hora; también verificar el estado inicial cuando nunca hubo cambios.

**Acceptance Scenarios**:

1. **Given** nunca se cambió la estrategia desde el sistema,
   **When** el administrador consulta el panel, **Then** se indica que la
   estrategia proviene del valor predeterminado de configuración.
2. **Given** un administrador cambió efectivamente la estrategia,
   **When** finaliza el cambio y se consulta el panel, **Then** se muestran el
   usuario que realizó el cambio y su fecha y hora.
3. **Given** se presenta la fecha del último cambio,
   **When** se muestra al administrador, **Then** corresponde a la zona horaria
   America/Argentina/Buenos_Aires.
4. **Given** se produjo un cambio efectivo,
   **When** el sistema reinicia y se consulta el panel, **Then** se mantiene el
   registro del último cambio y la estrategia activa persistida.

---

### User Story 4 - Identificar estrategias en el historial (Priority: P2)

Como usuario, quiero ver el nombre legible de la estrategia utilizada en cada
cotización dentro del historial, el gráfico y el detalle del jugador, para
entender cuándo cambió el criterio de cálculo.

**Why this priority**: La nomenclatura legible evita confusión técnica y
permite interpretar el historial sin conocer los identificadores internos.

**Independent Test**: Abrir el historial de un jugador con cotizaciones creadas
por estrategias distintas y verificar tabla, gráfico, tooltip y detalle.

**Acceptance Scenarios**:

1. **Given** una cotización registrada con una estrategia conocida,
   **When** aparece en la tabla de historial, **Then** muestra el nombre
   legible en lugar del código interno.
2. **Given** el usuario pasa sobre un punto del gráfico,
   **When** se muestra el tooltip, **Then** incluye el nombre legible de la
   estrategia usada para ese punto.
3. **Given** el usuario abre el detalle de una cotización,
   **When** se muestra su estrategia, **Then** se presenta el nombre legible
   sin exponer el código interno.
4. **Given** una pantalla muestra varias cotizaciones o herramientas del
   mercado, **When** se informa la estrategia asociada,
   **Then** nunca reemplaza el nombre legible por el código interno.

---

### Edge Cases

- Una persona no autenticada no puede consultar ni solicitar el cambio si la
  operación requiere autenticación; el cambio queda denegado sin modificar el
  estado.
- Una persona autenticada sin rol de administrador no puede cambiar la
  estrategia; el sistema rechaza la operación y el panel no ofrece un resultado
  exitoso.
- Si se envía una estrategia vacía, desconocida o inexistente, el cambio se
  rechaza con un mensaje claro de validación y sin generar auditoría.
- Si hay un recálculo concurrente, el cambio se rechaza con el mensaje de
  conflicto y la estrategia activa permanece sin cambios.
- Si el cambio es idempotente porque la estrategia ya está activa, no genera
  auditoría ni modifica cotizaciones.
- Después de reiniciar el sistema, la estrategia activa persiste y, si no existe
  ninguna estrategia guardada, se usa el valor inicial de configuración.
- Cambiar la estrategia no recalcula cotizaciones existentes ni vuelve a
  procesar el historial.
- Los mensajes de error deben conservar el texto comprensible recibido del
  sistema, incluido el conflicto por recálculo en curso.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir a un administrador consultar la
  estrategia de cotización activa.
- **FR-002**: La consulta MUST incluir todas las estrategias disponibles,
  identificadas con nombre legible y descripción breve.
- **FR-003**: Las descripciones MUST explicar por separado el cálculo general
  y el cálculo ponderado por posición, incluyendo que la posición desconocida
  utiliza los pesos generales.
- **FR-004**: El sistema MUST permitir únicamente a usuarios con rol
  administrador cambiar la estrategia activa.
- **FR-005**: Un intento de cambio sin autenticación MUST ser rechazado y una
  solicitud autenticada de usuario sin rol administrador MUST ser rechazada
  sin modificar datos.
- **FR-006**: El sistema MUST rechazar una estrategia vacía o que no exista
  entre las disponibles y comunicar claramente el motivo de la validación.
- **FR-007**: El sistema MUST persistir la estrategia elegida y mantenerla
  activa después de reiniciar.
- **FR-008**: La propiedad de configuración inicial MUST utilizarse únicamente
  cuando todavía no existe una estrategia persistida.
- **FR-009**: Cambiar la estrategia MUST NOT iniciar automáticamente un
  recálculo ni modificar cotizaciones ya calculadas.
- **FR-010**: Los recálculos manuales existentes y los recálculos programados
  MUST leer la estrategia activa persistida al iniciar el proceso.
- **FR-011**: El sistema MUST rechazar un cambio efectivo si existe un recálculo
  en curso y MUST dejar intacta la estrategia activa.
- **FR-012**: El sistema MUST registrar todo cambio efectivo con estrategia
  anterior, estrategia nueva, usuario administrador y fecha y hora.
- **FR-013**: La fecha de auditoría MUST representarse en
  America/Argentina/Buenos_Aires.
- **FR-014**: Si nunca se cambió la estrategia, la consulta MUST indicar que
  proviene del valor predeterminado de configuración.
- **FR-015**: Seleccionar la estrategia ya activa MUST ser una operación
  idempotente, exitosa y sin generar una nueva auditoría.
- **FR-016**: El panel MUST presentar confirmación antes de aplicar un cambio y
  advertir que las cotizaciones no se recalcularán hasta el recálculo manual o
  programado correspondiente.
- **FR-017**: El panel MUST mostrar estados de carga y mensajes de éxito o error
  provenientes del sistema, especialmente el conflicto por recálculo en curso.
- **FR-018**: El historial, el gráfico y el detalle de cotizaciones MUST mostrar
  nombres legibles de la estrategia usada y MUST NOT mostrar su código interno.
- **FR-019**: Los nombres legibles MUST aplicarse a cotizaciones existentes y
  nuevas sin alterar los datos históricos ni su estrategia registrada.
- **FR-020**: Los endpoints o interfaces de consulta y cambio MUST seguir la
  documentación y el formato estándar de errores disponible en el sistema.

### Key Entities

- **Estrategia de cotización**: Criterio disponible para calcular valores,
  identificado internamente por un código estable y mostrado al usuario con
  nombre legible y descripción.
- **Configuración activa**: Estrategia vigente utilizada por los próximos
  recálculos y persistida independientemente del reinicio del sistema.
- **Cambio de estrategia**: Registro histórico inmutable con estrategia anterior,
  estrategia nueva, usuario administrador y fecha y hora.
- **Cotización**: Resultado histórico existente que conserva la estrategia con
  la que fue calculada y no se modifica al cambiar la configuración activa.
- **Recálculo**: Ejecución manual o programada que fija su estrategia al iniciar
  y evita mezclas con cambios concurrentes.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un administrador puede conocer la estrategia activa y completar
  el cambio efectivo en menos de tres interacciones desde el panel.
- **SC-002**: Después de reiniciar el sistema, el 100% de las pruebas verifica
  que permanece la última estrategia elegida por un administrador.
- **SC-003**: El 100% de los intentos sin autenticación o sin rol administrador
  queda rechazado sin cambiar la estrategia activa.
- **SC-004**: En el 100% de los intentos realizados durante un recálculo en
  curso, el cambio se rechaza y la estrategia activa no varía.
- **SC-005**: Todo cambio efectivo queda registrado una sola vez con usuario,
  estrategia anterior, estrategia nueva y fecha; las operaciones idempotentes
  no agregan registros.
- **SC-006**: El primer recálculo posterior a un cambio utiliza la nueva
  estrategia tanto en ejecución manual como programada.
- **SC-007**: El 100% de las cotizaciones previas conserva su valor y su
  estrategia histórica después de cambiar la configuración activa.
- **SC-008**: En historial, gráfico y detalle, el 100% de las estrategias
  conocidas se presenta con nombre legible y ninguna pantalla expone el código
  interno.

## Assumptions

- Las estrategias existentes son las dos descritas en el alcance; no se
  requieren crear, eliminar ni editar estrategias desde esta feature.
- La configuración inicial ya existe y puede seguir sirviendo como valor
  predeterminado hasta que haya una estrategia persistida.
- Los usuarios disponen del mecanismo de autenticación y roles ya establecido
  por el sistema.
- Los recálculos manual y programado ya conocen el estado de ejecución y pueden
  ser coordinados para rechazar cambios concurrentes.
- Las zonas horarias del sistema se presentan al usuario en
  America/Argentina/Buenos_Aires sin alterar los datos históricos almacenados.
