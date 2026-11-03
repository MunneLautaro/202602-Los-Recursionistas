# Feature Specification: Rediseño integral de la interfaz frontend

**Feature Branch**: `010-frontend-ui-redesign`
**Created**: 2026-11-03
**Status**: Draft
**Input**: User description: "Rediseñar toda la interfaz de Los Recursionistas, una plataforma de fantasy football con equipos, jugadores, ranking y mercado de tokens, manteniendo la lógica existente y mejorando la experiencia visual, los formularios, la accesibilidad y los estados de carga."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Explorar jugadores y equipos (Priority: P1)

Como visitante quiero explorar jugadores y equipos con filtros claros, estados comprensibles y detalles útiles para descubrir oportunidades del mercado.

**Why this priority**: El catálogo es la principal puerta de entrada al producto y debe entregar valor aun sin autenticación.

**Independent Test**: Un visitante puede recorrer equipos, filtrar jugadores por nombre, posición, liga y equipo, ordenar los resultados, abrir el detalle de un jugador y volver al catálogo sin perder orientación.

**Acceptance Scenarios**:

1. **Given** un visitante en Jugadores, **When** aplica filtros y cambia de página, **Then** ve el contador, los chips de filtros activos y únicamente los resultados correspondientes.
2. **Given** un visitante escribe un nombre en el filtro, **When** deja de escribir durante al menos 400 ms, **Then** la búsqueda se actualiza sin exigir una acción adicional y los demás filtros conservan su comportamiento explícito de aplicar.
3. **Given** no existen resultados, **When** termina la búsqueda, **Then** aparece un estado vacío con una acción para limpiar los filtros.
4. **Given** ocurre un error al cargar el catálogo, **When** el visitante selecciona reintentar, **Then** se vuelve a intentar la carga y se muestra un mensaje accionable si vuelve a fallar.
5. **Given** un visitante selecciona un equipo, **When** navega a sus jugadores, **Then** el catálogo muestra el filtro de equipo como un chip removible.

### User Story 2 - Consultar el detalle y la evolución de un jugador (Priority: P1)

Como visitante quiero consultar estadísticas, cotizaciones e información contextual de un jugador en un modal legible para decidir si seguirlo o negociar sus tokens.

**Why this priority**: El detalle conecta los datos deportivos con la valoración del mercado.

**Independent Test**: Un visitante abre cualquier jugador, consulta sus totales y estadísticas por torneo, filtra cotizaciones por rango de fechas, pagina el historial y cierra el modal con teclado o controles visibles.

**Acceptance Scenarios**:

1. **Given** un jugador seleccionado, **When** se abre su detalle, **Then** el modal muestra nombre, posición, estadísticas globales y estadísticas por torneo.
2. **Given** un rango de fechas inválido, **When** el visitante intenta consultar cotizaciones, **Then** no se realiza la consulta y se muestra el error junto al campo correspondiente.
3. **Given** no hay estadísticas o cotizaciones, **When** termina la carga, **Then** se muestra un estado vacío específico sin presentar datos inventados.
4. **Given** el detalle está abierto, **When** el visitante presiona Escape o el botón de cerrar, **Then** el modal se cierra y el foco vuelve al elemento que lo abrió.

### User Story 3 - Comparar el ranking del mercado (Priority: P1)

Como visitante quiero revisar el ranking de jugadores y filtrarlo por liga o posición para identificar variaciones relevantes de valor.

**Why this priority**: El ranking resume el comportamiento del mercado y facilita descubrir jugadores.

**Independent Test**: Un visitante abre Ranking, cambia ambos filtros, navega por sus páginas y selecciona una tarjeta para ir al jugador correspondiente.

**Acceptance Scenarios**:

1. **Given** el ranking disponible, **When** el visitante lo abre, **Then** ve posición, jugador, liga, equipo, valor actual, valor anterior y variación.
2. **Given** el visitante cambia liga o posición, **When** el cambio es válido, **Then** el ranking se actualiza automáticamente y conserva una paginación coherente.
3. **Given** una variación positiva, negativa o neutra, **When** se muestra una tarjeta, **Then** el sentido se comunica mediante icono, signo y texto además del color.
4. **Given** no hay resultados para los filtros, **When** termina la consulta, **Then** se muestra un estado vacío con el mensaje correspondiente.

### User Story 4 - Operar en el mercado y consultar la cuenta (Priority: P1)

Como usuario autenticado quiero consultar órdenes, crear compras o ventas y revisar mi saldo e historial para participar en el mercado con créditos ficticios.

**Why this priority**: La operación y el seguimiento de la cuenta son el objetivo central de la plataforma.

**Independent Test**: Un usuario autenticado consulta órdenes, filtra por jugador y tipo, crea una orden válida, recibe errores funcionales sin perder lo ingresado y revisa saldo, órdenes propias y transacciones.

**Acceptance Scenarios**:

1. **Given** el mercado, **When** un usuario filtra órdenes por nombre o tipo, **Then** la lista y la paginación reflejan los filtros aplicados.
2. **Given** un usuario autenticado completa jugador, cantidad y tipo válidos, **When** confirma la orden, **Then** se crea una sola operación y se muestra confirmación con los datos resultantes.
3. **Given** una cantidad cero, negativa, decimal no permitido o incompleta, **When** intenta enviar, **Then** la solicitud no se envía y el error aparece junto al campo.
4. **Given** el backend rechaza una operación por saldo, tokens o reglas de negocio, **When** se recibe la respuesta, **Then** se muestra el mensaje del servidor y se conservan los datos ingresados.
5. **Given** un usuario autenticado abre Cuenta, **When** consulta el balance, órdenes o transacciones, **Then** ve datos paginados y estados vacíos claros cuando no existen registros.
6. **Given** el usuario inicia una recarga válida, **When** confirma el monto, **Then** el saldo actualizado se muestra después de una respuesta exitosa y el botón permanece bloqueado mientras la operación está pendiente.

### User Story 5 - Iniciar sesión y navegar con confianza (Priority: P1)

Como visitante o usuario quiero una navegación coherente, responsive y accesible para entender qué acciones están disponibles y completar formularios sin confusión.

**Why this priority**: La confianza y la accesibilidad atraviesan todas las funciones públicas y protegidas.

**Independent Test**: Se puede recorrer la home, abrir el menú móvil, registrarse, iniciar sesión, cerrar sesión y usar las rutas públicas y protegidas en viewport de escritorio y móvil.

**Acceptance Scenarios**:

1. **Given** una ruta pública o protegida, **When** el usuario la visita, **Then** el header marca el enlace activo, muestra las acciones de autenticación correctas y ofrece Cuenta solo cuando corresponde.
2. **Given** un viewport menor al breakpoint de escritorio, **When** el usuario abre el menú, **Then** los enlaces y acciones aparecen en una columna, el botón cambia entre abrir y cerrar y seleccionar un enlace cierra el menú.
3. **Given** el usuario completa Login o Registro, **When** un campo es inválido, **Then** recibe un mensaje en español asociado al campo y el formulario conserva los valores válidos.
4. **Given** el usuario envía un formulario, **When** la operación está pendiente, **Then** el botón se deshabilita, comunica el estado de carga y no permite envíos duplicados.
5. **Given** el usuario navega en tema claro u oscuro, **When** usa cualquier pantalla, **Then** la interfaz mantiene contraste WCAG AA y la identidad visual basada en la paleta Pantone existente.

### Edge Cases

- Los resultados vacíos, errores de red, respuestas sin mensaje y fallos de carga deben mostrar estados explícitos con una acción de reintento o limpieza cuando sea aplicable.
- Si un filtro deja al usuario en una página inexistente, la vista debe volver a una página válida sin mostrar resultados de otra consulta.
- Los datos opcionales de jugador, equipo, liga, escudo o fotografía deben tener una representación textual o de iniciales sin inventar URLs.
- Las fechas deben impedir rangos donde la fecha inicial sea posterior a la final.
- Los botones de operación y recarga deben bloquear envíos duplicados durante la espera.
- El modal debe mantener foco, permitir cierre accesible y no mostrar contenido fuera del área visible.
- Los estados del ranking sin variación deben distinguirse de los positivos y negativos sin depender exclusivamente del color.
- La interfaz debe conservar las llamadas y contratos actuales del backend; no se agregan operaciones de negocio, pagos reales, favoritos ni notificaciones en esta versión.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La interfaz MUST aplicar una identidad visual consistente para home, jugadores, equipos, ranking, mercado, cuenta, login, registro y administración, con soporte para tema claro y oscuro usando los tokens de color Pantone existentes.
- **FR-002**: La interfaz MUST conservar la lógica existente de carga, filtros, paginación, autenticación, navegación y comunicación con el backend, modificando únicamente presentación, estados de interacción y manejo de formularios.
- **FR-003**: El header MUST mostrar navegación responsive, enlace activo, acciones de autenticación según el estado de sesión y Cuenta únicamente para usuarios autenticados.
- **FR-004**: El menú móvil MUST ser operable por teclado y lector de pantalla, informar su estado y cerrarse al seleccionar un enlace.
- **FR-005**: Jugadores MUST ofrecer búsqueda por nombre con actualización automática tras 400 ms de inactividad, filtros de posición, liga, equipo y orden, chips removibles y contador de resultados.
- **FR-006**: Jugadores MUST mostrar tarjetas con identidad del jugador, posición, edad, liga y equipo, con fallback de iniciales cuando falten imágenes y navegación por teclado.
- **FR-007**: Jugadores, equipos, ranking y mercado MUST mostrar estados de carga, error y vacío consistentes, con skeletons durante la carga y acciones de recuperación cuando corresponda.
- **FR-008**: El detalle de jugador MUST mostrar estadísticas globales, estadísticas por torneo, historial de cotizaciones filtrable por fechas y paginación dentro de un modal accesible.
- **FR-009**: Ranking MUST permitir filtrar automáticamente por liga y posición, mostrar variaciones positivas, negativas y neutras con señales redundantes, y navegar al detalle del jugador seleccionado.
- **FR-010**: Mercado MUST permitir consultar y filtrar órdenes, y a usuarios autenticados crear órdenes de compra o venta conservando las reglas y contratos actuales.
- **FR-011**: Cuenta MUST mostrar identidad del usuario, saldo, recarga, órdenes propias y transacciones con estados vacíos, paginación y feedback de operación.
- **FR-012**: Login, Registro, filtros, detalle de cotizaciones, órdenes y recarga MUST validar sus datos antes de enviar, mostrar errores de campo en español y mostrar errores del servidor sin perder datos ingresados.
- **FR-013**: Todos los formularios MUST usar un comportamiento consistente de valores iniciales, validación cruzada cuando corresponda, feedback de envío y prevención de doble envío.
- **FR-014**: Todos los controles interactivos MUST tener etiquetas visibles o accesibles, estados de foco, nombres para botones solo-icono, relaciones entre errores y campos, y regiones dinámicas anunciables.
- **FR-015**: La interfaz MUST usar exclusivamente iconografía de la biblioteca aprobada para esta feature, sin emojis ni iconos SVG escritos manualmente, y los iconos decorativos no deben interferir con lectores de pantalla.
- **FR-016**: La solución MUST mantener el patrón de estilos del proyecto, el sistema de espaciado y radios definido para la experiencia, y no introducir una librería de componentes o dependencia externa fuera de las cuatro dependencias de UI y formularios especificadas.
- **FR-017**: La implementación MUST añadir únicamente las dependencias aprobadas para iconos y formularios, verificando compatibilidad entre el validador y su adaptador, y usar el gestor de paquetes establecido por el proyecto.
- **FR-018**: Home MUST comunicar el propósito de la plataforma, ofrecer accesos a jugadores, ranking y mercado, y adaptar sus llamadas a la acción al estado autenticado.
- **FR-019**: La implementación MUST entregar los componentes compartidos de campos y estados, los esquemas de validación por feature, el hook de debounce y los componentes de catálogo especificados, manteniendo imports y tipos existentes compatibles.
- **FR-020**: La documentación de la entrega MUST incluir el comando de instalación utilizado, las decisiones de diseño no obvias y sugerencias futuras separadas del alcance actual.

### Key Entities

- **Jugador**: Persona deportiva mostrada en el catálogo, con nombre, posición, edad, equipo, liga, estadísticas y cotizaciones.
- **Equipo**: Equipo agrupado dentro de una liga y punto de entrada para filtrar jugadores.
- **RankingPlayer**: Representación de un jugador en el ranking con valores actual y anterior y su variación.
- **Orden**: Intención de compra o venta de tokens, con jugador, tipo, cantidad, precio, estado y fecha.
- **Cuenta**: Información del usuario autenticado, sus saldos, órdenes propias y transacciones.
- **Formulario**: Datos introducidos por el usuario, reglas de validación, errores de campo y estado de envío.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: En una prueba de navegación, al menos 90% de los participantes puede encontrar un jugador, aplicar un filtro y abrir su detalle sin asistencia.
- **SC-002**: En una prueba de responsive, el 100% de las funciones principales de home, jugadores, ranking, mercado, cuenta, login y registro se puede completar en viewport móvil y de escritorio sin scroll horizontal involuntario.
- **SC-003**: El 100% de los formularios definidos en el alcance muestra validación antes del envío para entradas inválidas y conserva los datos ingresados ante un error del servidor.
- **SC-004**: La búsqueda por nombre actualiza los resultados dentro de 500 ms después de cumplidos los 400 ms de espera, en al menos 95% de las interacciones medidas bajo condiciones normales.
- **SC-005**: Al menos 95% de las solicitudes de carga de listas muestra un estado visible de carga, éxito, vacío o error; ninguna pantalla queda sin feedback durante una espera.
- **SC-006**: El 100% de los botones solo-icono, campos y modales relevantes tiene nombre accesible, relación de error o etiqueta verificable mediante inspección automatizada y manual.
- **SC-007**: Al menos 90% de los usuarios autenticados puede crear una orden válida en su primer intento y recibe confirmación o un error comprensible en menos de 3 segundos después de la respuesta del servicio.
- **SC-008**: En revisión visual, todas las vistas usan la paleta Pantone definida, mantienen contraste WCAG AA y no introducen la paleta violeta alternativa excluida.
- **SC-009**: La entrega no agrega dependencias fuera del conjunto aprobado y el frontend conserva compilación, lint y pruebas existentes sin regresiones atribuibles al rediseño.

## Assumptions

- El backend y sus endpoints actuales permanecen disponibles y no se modifican como parte de esta feature.
- Las reglas de autenticación, autorización, validación de dominio y formatos de datos existentes son la fuente de verdad; el frontend no inventa reglas incompatibles.
- La información opcional sin imagen se representa con iniciales o texto, sin solicitar nuevos datos al backend.
- El gestor de paquetes oficial del frontend es Yarn, por lo que las dependencias aprobadas se agregan mediante el flujo existente del proyecto.
- El alcance visual incluye desktop, tablet y mobile, pero no agrega una preferencia manual de tema, favoritos, comparador o notificaciones.
- Los umbrales de rendimiento se verifican bajo condiciones normales de red y datos representativos del entorno de desarrollo.
- La administración conserva sus capacidades actuales y recibe las mismas reglas de consistencia visual y formularios que las demás vistas cuando tenga formularios.
