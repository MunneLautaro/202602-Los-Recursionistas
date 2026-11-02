# Feature Specification: Frontend del Mercado de Tokens

**Feature Branch**: `009-frontend-mercado-tokens`

**Created**: 2026-11-02

**Status**: Draft

**Input**: User description: "Completar en el frontend la funcionalidad principal de la aplicación de mercado de tokens de jugadores de fútbol utilizando exclusivamente los endpoints REST existentes en el backend."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Explorar jugadores y su valor (Priority: P1)

Como visitante quiero buscar jugadores, consultar sus estadísticas y revisar la evolución de su cotización para entender su valor de mercado antes de operar.

**Why this priority**: Es la entrada principal al producto y permite tomar decisiones aun sin autenticarse.

**Independent Test**: Con el backend disponible, un visitante puede filtrar jugadores, abrir un detalle, consultar estadísticas, historial de cotizaciones y paginar los resultados sin iniciar sesión.

**Acceptance Scenarios**:

1. **Given** un visitante en el catálogo, **When** filtra por nombre, posición, liga o equipo, **Then** ve únicamente los jugadores coincidentes y puede cambiar de página.
2. **Given** un jugador existente, **When** abre su detalle, **Then** ve sus datos, estadísticas globales y estadísticas por torneo.
3. **Given** un jugador existente, **When** consulta su historial de cotizaciones y establece fechas desde/hasta, **Then** ve los valores paginados dentro del período solicitado.
4. **Given** una consulta sin resultados o con un error, **When** termina la carga, **Then** la interfaz muestra un estado vacío o un mensaje accionable sin romper la navegación.

---

### User Story 2 - Comparar el ranking del mercado (Priority: P1)

Como visitante quiero consultar un ranking paginado de jugadores por cotización y filtrarlo por liga o posición para identificar oportunidades.

**Why this priority**: Conecta la información de jugadores con el propósito de valoración y permite descubrir activos relevantes.

**Independent Test**: Un visitante puede abrir el ranking, aplicar filtros, paginarlo y navegar al detalle de cualquier jugador.

**Acceptance Scenarios**:

1. **Given** el ranking disponible, **When** el visitante lo abre, **Then** ve nombre, posición, equipo, liga, valor actual, valor anterior y variación.
2. **Given** filtros de liga o posición, **When** el visitante los aplica, **Then** el ranking se actualiza conservando una paginación consistente.
3. **Given** una fila del ranking, **When** el visitante la selecciona, **Then** navega al detalle del jugador correspondiente.

---

### User Story 3 - Operar en el mercado (Priority: P1)

Como usuario autenticado quiero revisar órdenes disponibles y crear órdenes de compra o venta de tokens usando créditos ficticios.

**Why this priority**: Es la acción central que transforma la consulta de cotizaciones en participación en el mercado.

**Independent Test**: Un usuario autenticado puede consultar órdenes, completar una compra o venta con una cantidad positiva y recibir confirmación o un error funcional claro.

**Acceptance Scenarios**:

1. **Given** el mercado, **When** un visitante consulta las órdenes, **Then** ve resultados paginados y puede filtrarlos por jugador y tipo.
2. **Given** un usuario autenticado y un jugador válido, **When** envía una compra o venta con cantidad positiva, **Then** el frontend envía solamente el identificador del jugador y la cantidad y muestra la orden creada.
3. **Given** una cantidad cero, negativa o inválida, **When** el usuario intenta operar, **Then** la solicitud no se envía y se muestra una validación junto al campo.
4. **Given** saldo insuficiente, tokens insuficientes, jugador inactivo u otra regla de negocio incumplida, **When** el backend rechaza la operación, **Then** se muestra el mensaje de error y se conservan los datos ingresados.
5. **Given** un visitante no autenticado, **When** intenta crear una orden, **Then** se le solicita iniciar sesión sin mostrar la operación como exitosa.

---

### User Story 4 - Consultar cuenta e historial (Priority: P2)

Como usuario autenticado quiero consultar mis créditos e historial de transacciones para conocer el estado de mis operaciones.

**Why this priority**: Da trazabilidad y confianza al uso del mercado, aunque depende de que el backend permita identificar al usuario después del login.

**Independent Test**: Con un identificador de usuario conocido, un usuario puede consultar saldo, recargar créditos ficticios y revisar transacciones paginadas.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado cuyo identificador está disponible, **When** abre su cuenta, **Then** ve saldo total, saldo reservado y saldo disponible.
2. **Given** una recarga con monto válido, **When** el usuario la confirma, **Then** ve el saldo actualizado devuelto por el backend.
3. **Given** transacciones existentes, **When** el usuario abre el historial, **Then** ve jugador, comprador, vendedor, cantidad, precio unitario, total y fecha, con paginación.
4. **Given** un login que solo devuelve un JWT y no permite obtener el identificador del usuario, **When** el usuario abre la cuenta, **Then** la interfaz explica que esa información no puede cargarse con el contrato actual y no inventa un identificador ni consulta otra cuenta.

---

### User Story 5 - Mantener una sesión segura y navegable (Priority: P1)

Como usuario quiero que la navegación distinga claramente las funciones públicas de las protegidas y que una sesión inválida no genere acciones aparentes de éxito.

**Why this priority**: Evita operaciones no autorizadas y mantiene coherencia entre el estado visual y la seguridad del backend.

**Independent Test**: Se puede probar iniciando sesión, recargando la aplicación, accediendo a rutas privadas, cerrando sesión y usando un token vencido o rechazado.

**Acceptance Scenarios**:

1. **Given** un login exitoso, **When** el usuario navega a una vista protegida o realiza una solicitud protegida, **Then** se envía el JWT según el contrato de autenticación.
2. **Given** una respuesta 401, **When** finaliza la solicitud, **Then** se limpia la sesión inválida y se redirige al login con un mensaje comprensible.
3. **Given** una respuesta 403, **When** el usuario intenta una acción sin permisos, **Then** se muestra acceso denegado sin borrar una sesión que sigue siendo válida.
4. **Given** el usuario autenticado, **When** selecciona cerrar sesión, **Then** el token se elimina y las acciones protegidas dejan de estar disponibles.

### Edge Cases

- Los listados paginados deben manejar cero resultados, una sola página, cambio de filtros con una página anterior inválida y tamaños de página rechazados.
- Las fechas de cotización deben validarse como un rango válido y conservar el formato esperado por el backend.
- Los valores monetarios, cantidades y variaciones deben mostrarse sin perder precisión ni convertir errores de formato en operaciones válidas.
- Las acciones de compra, venta y recarga deben bloquearse mientras están pendientes para evitar envíos duplicados.
- Un error de red, respuesta no JSON o error sin mensaje debe producir un mensaje genérico útil y permitir reintentar.
- La vista no debe exponer operaciones administrativas de ingesta o recálculo de cotizaciones a usuarios finales.
- No deben implementarse cancelaciones, vencimientos, comisiones, precios límite ni pagos reales porque no forman parte del contrato disponible.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El frontend MUST conservar el registro y login actuales usando `/registrar` y `/login`, incluyendo sus validaciones y mensajes de error.
- **FR-002**: El frontend MUST adjuntar el JWT almacenado a cada solicitud que requiera autenticación y MUST distinguir respuestas 401 de respuestas 403.
- **FR-003**: El frontend MUST ofrecer navegación pública para catálogo, detalle, estadísticas, cotizaciones, ranking y consulta de órdenes.
- **FR-004**: El frontend MUST mantener los filtros existentes de jugadores por nombre, posición, liga y equipo, con paginación y estados de carga, vacío y error.
- **FR-005**: El frontend MUST consultar `GET /players/{id}/stats` y mostrar estadísticas globales y por torneo.
- **FR-006**: El frontend MUST consultar `GET /players/{id}/quotes` con filtros opcionales `desde`, `hasta`, `page` y `size`, mostrando valor, fecha de cálculo, estrategia y ventana utilizada.
- **FR-007**: El frontend MUST ofrecer un ranking mediante `GET /players/ranking`, soportando `ligaId`, `posicion`, `page` y `size`, y mostrar todos los campos de su respuesta.
- **FR-008**: El frontend MUST ofrecer una consulta paginada de órdenes mediante `GET /orders`, soportando `jugadorId`, `tipo`, `page` y `size`.
- **FR-009**: El frontend MUST permitir a usuarios autenticados crear compras y ventas mediante `/orders/buy` y `/orders/sell`, enviando exactamente `jugadorId` y `cantidad`.
- **FR-010**: El frontend MUST validar que la cantidad de una orden sea un número entero positivo antes de enviarla y MUST impedir envíos duplicados mientras la solicitud esté pendiente.
- **FR-011**: El frontend MUST representar correctamente tipo, cantidades, precio unitario, total remanente, estado y fecha de cada orden, y mostrar los errores funcionales devueltos por el backend.
- **FR-012**: El frontend MUST proteger las acciones de compra, venta, recarga y consulta de transacciones, redirigiendo al login cuando corresponda.
- **FR-013**: El frontend MUST ofrecer la consulta de saldo mediante `POST /users/{id}/credits` únicamente cuando disponga de un identificador de usuario válido y mostrar saldo, saldo reservado y saldo disponible.
- **FR-014**: El frontend MUST permitir una recarga de créditos ficticios con un monto válido, sin presentarla como dinero real.
- **FR-015**: El frontend MUST ofrecer el historial mediante `GET /users/{id}/transactions` con `page` y `size`, respetando que el backend solo autoriza la propia cuenta salvo administración.
- **FR-016**: Cuando el login no proporcione el identificador de usuario y no exista otro contrato disponible para obtenerlo, el frontend MUST informar la limitación y MUST NOT inventar un endpoint, decodificar datos no garantizados del JWT ni consultar una cuenta arbitraria.
- **FR-017**: El frontend MUST agregar rutas y enlaces para jugadores, ranking, mercado y cuenta sin romper las rutas actuales de inicio, registro y login.
- **FR-018**: El frontend MUST reutilizar el cliente HTTP, tipos, hooks, componentes y convenciones de estilos existentes, sin agregar dependencias estructurales innecesarias.
- **FR-019**: El frontend MUST excluir de la experiencia normal las operaciones administrativas `/api/admin/whoscored/ingestas`, `/admin/sync/jugadores/{equipoExternoId}`, `/ingesta/**` y `/quotes/recalculate`.
- **FR-020**: El frontend MUST mostrar estados de carga, éxito, vacío, validación, error de red, 401, 403 y conflicto sin ocultar fallos ni generar confirmaciones falsas.

### Key Entities

- **Jugador**: Futbolista consultable por identidad, posición, equipo y liga.
- **Estadísticas de jugador**: Totales globales y registros por torneo, incluyendo apariciones, goles, asistencias y rating cuando estén disponibles.
- **Cotización**: Valor histórico de un jugador, fecha de cálculo, estrategia y ventana temporal.
- **Ranking de jugador**: Posición comparativa con valor actual, valor anterior y variación.
- **Orden**: Intención de compra o venta con jugador, cantidades, precio capturado, estado y fecha.
- **Saldo**: Créditos ficticios totales, reservados y disponibles de un usuario.
- **Transacción**: Ejecución del mercado con jugador, comprador, vendedor, cantidad, precio, total y fecha.
- **Sesión**: Estado local de autenticación basado en el JWT entregado por el backend.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un visitante puede pasar del catálogo al detalle, estadísticas e historial de cotización de un jugador en no más de 4 interacciones principales.
- **SC-002**: Al menos el 95% de las consultas exitosas de catálogo, ranking, estadísticas, cotizaciones y órdenes muestran sus datos o un estado vacío correcto, sin pantalla en blanco.
- **SC-003**: Un usuario autenticado puede completar una orden válida de compra o venta en menos de 3 minutos desde la selección del jugador.
- **SC-004**: El 100% de las órdenes inválidas por cantidad no genera una solicitud al backend.
- **SC-005**: El 100% de las respuestas 401 elimina o invalida la sesión local y conduce al flujo de login; ninguna respuesta 401/403 se muestra como éxito.
- **SC-006**: El usuario puede identificar saldo total, reservado y disponible, cuando el identificador de cuenta está disponible, sin consultar manualmente otra pantalla.
- **SC-007**: El 100% de las operaciones administrativas queda inaccesible desde la navegación normal de usuarios finales.
- **SC-008**: Las pruebas de aceptación cubren al menos un flujo exitoso y uno fallido para autenticación, consulta de jugadores, ranking, órdenes y cuenta.

## Assumptions

- El backend permanece disponible en la URL configurada por `VITE_API_URL` y conserva los contratos REST actualmente implementados.
- El JWT de login se almacena y reutiliza según el mecanismo ya presente en el frontend; el login actualmente devuelve únicamente `token`.
- La respuesta de registro contiene un identificador de usuario, pero no se asume que dicho dato esté disponible después de un login posterior ni que el JWT sea un contrato para obtenerlo.
- Las órdenes usan la cotización vigente capturada por el backend; el frontend no calcula precios ni implementa matching.
- Los créditos son ficticios y no representan dinero real.
- El alcance está orientado a usuarios finales; no incluye panel administrativo ni operaciones de ingesta, sincronización o recálculo.
- Se mantiene el stack y la identidad visual establecidos por la constitución del proyecto.

## Existing Functionality and Scope Boundary

Ya existe parcialmente el registro, login, persistencia básica del JWT, catálogo paginado de jugadores, filtros por nombre/posición/liga/equipo, detalle de jugador y catálogos de ligas/equipos. La feature debe completar e integrar esas capacidades con estadísticas, cotizaciones, ranking, órdenes, sesión protegida y cuenta.

Quedan fuera de alcance los cambios al backend, la creación de nuevos endpoints, la modificación del contrato de login para agregar un usuario, operaciones administrativas, cambios en el cálculo de cotizaciones, cancelación o vencimiento de órdenes, comisiones, pagos reales y cualquier funcionalidad financiera real.
