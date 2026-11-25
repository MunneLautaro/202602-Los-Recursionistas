# Feature Specification: Métricas de negocio y panel de observabilidad con gráficos

**Feature Branch**: `013-metricas-negocio-panel`

**Created**: 2026-11-24

**Status**: Draft

**Input**: User description: "Agregar métricas de negocio de mercado (órdenes creadas y emparejadas, volumen operado en créditos, tokens en circulación, órdenes rechazadas por saldo insuficiente) y de cotización (recálculos ejecutados, duración, jugadores cotizados, estrategia activa, variación promedio) en el backend, y un panel para administradores con gráficos que las muestre junto con las métricas técnicas de la feature 012 (salud, requests y errores, JVM y recursos, pool de base de datos). Enfoque mixto: métricas en vivo más gráficos históricos calculados desde los datos persistidos."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver la actividad del mercado (Priority: P1)

Como administrador, quiero ver en un panel cuántas órdenes se crean y se
emparejan, cuánto volumen en créditos se opera, cuántos tokens están en
circulación y cuántas órdenes se rechazan por saldo insuficiente, con su
evolución diaria en gráficos, para entender cómo se está usando el mercado.

**Why this priority**: Es el núcleo del pedido. El mercado es la actividad
principal de la plataforma y hoy no hay forma de verla agregada.

**Independent Test**: Con datos de mercado cargados, abrir el panel como
administrador y verificar que las tarjetas muestran los totales y que los
gráficos diarios coinciden con las órdenes y transacciones registradas en el
período elegido.

**Acceptance Scenarios**:

1. **Given** órdenes y transacciones registradas en los últimos 30 días,
   **When** el administrador abre el panel, **Then** ve un gráfico diario de
   órdenes creadas separado por compra y venta, y un gráfico diario de
   emparejamientos y volumen operado en créditos.
2. **Given** el mismo panel, **When** el administrador consulta las
   tarjetas de mercado, **Then** ve los totales del período: órdenes creadas
   (compra/venta), emparejamientos, volumen operado y tokens en circulación
   actuales.
3. **Given** compras rechazadas por saldo insuficiente desde el último
   reinicio, **When** el administrador consulta el panel, **Then** ve la
   cantidad de rechazos, indicada como "desde el último reinicio".
4. **Given** que el administrador cambia el período a 7 o 90 días, **When**
   se actualiza la vista, **Then** los gráficos y totales históricos
   reflejan solo ese período.
5. **Given** un día sin actividad dentro del período, **When** se dibuja el
   gráfico, **Then** ese día aparece con valor cero y no se omite.

---

### User Story 2 - Ver el comportamiento de la cotización (Priority: P1)

Como administrador, quiero ver cuántos recálculos de cotización se
ejecutaron (exitosos y fallidos), cuánto duraron, cuántos jugadores se
cotizaron, cuál es la estrategia activa y cuál fue la variación promedio de
las cotizaciones, para controlar que el proceso de valoración funcione y
detectar cambios bruscos.

**Why this priority**: La cotización define el precio de los tokens; un
recálculo fallido o una variación anómala impacta directamente en el
mercado.

**Independent Test**: Ejecutar un recálculo desde el panel administrativo
existente y verificar que el panel de observabilidad refleja un recálculo
exitoso más, su duración, la cantidad de jugadores cotizados y la variación
promedio; y que el gráfico histórico incluye el día del recálculo.

**Acceptance Scenarios**:

1. **Given** un recálculo terminado con éxito, **When** el administrador
   abre el panel, **Then** ve el contador de recálculos exitosos
   incrementado, la duración del último recálculo y la duración promedio.
2. **Given** un recálculo que falló, **When** el administrador consulta el
   panel, **Then** el contador de recálculos fallidos aumenta y el de
   exitosos no.
3. **Given** cotizaciones persistidas en el período, **When** el
   administrador consulta el gráfico de cotización, **Then** ve por día la
   cantidad de jugadores cotizados y la variación promedio porcentual.
4. **Given** la estrategia activa configurada, **When** el administrador
   abre el panel, **Then** ve el nombre legible de la estrategia activa, y
   si la estrategia cambia, el panel la refleja en la siguiente
   actualización.

---

### User Story 3 - Ver la salud técnica de la aplicación (Priority: P2)

Como administrador, quiero ver en el mismo panel el estado de salud
(global y por componente), los requests por endpoint con su tasa de error y
latencia media, el uso de memoria, threads, CPU y tiempo activo, y el
estado del pool de conexiones a la base, para detectar problemas técnicos
sin usar herramientas externas.

**Why this priority**: Reutiliza la observabilidad técnica de la feature
012; aporta diagnóstico, pero el valor nuevo principal está en las
métricas de negocio.

**Independent Test**: Abrir el panel como administrador, generar tráfico
contra la API y verificar que, tras la siguiente actualización, el gráfico
de requests por endpoint, la tasa de error y la latencia media reflejan ese
tráfico, y que salud, memoria y pool muestran valores coherentes.

**Acceptance Scenarios**:

1. **Given** la aplicación saludable, **When** el administrador abre el
   panel, **Then** ve el estado global "UP" y el estado de cada componente
   (base de datos, disco).
2. **Given** tráfico reciente a la API, **When** el panel se actualiza,
   **Then** muestra los endpoints con más requests, su tasa de error y su
   latencia media.
3. **Given** el panel abierto, **When** transcurren varias actualizaciones,
   **Then** los gráficos de memoria, CPU y conexiones del pool muestran su
   evolución durante la sesión.
4. **Given** la base de datos caída, **When** el panel se actualiza,
   **Then** el panel muestra el estado global "DOWN" (obtenido del estado de
   salud público) y el aviso "Servicio no disponible". El administrador no
   pierde la sesión y el panel se recupera solo en la siguiente
   actualización con la base disponible.

---

### User Story 4 - Exportar métricas de negocio a Prometheus (Priority: P2)

Como administrador, quiero que las métricas de negocio en vivo estén
disponibles junto con las técnicas en la exposición Prometheus existente,
para poder conectarlas en el futuro a herramientas de monitoreo.

**Why this priority**: Mantiene la observabilidad centralizada y cumple la
política vigente, pero no es necesaria para el panel.

**Independent Test**: Crear órdenes y ejecutar un recálculo; consultar la
exposición Prometheus como administrador y verificar series de órdenes,
emparejamientos, volumen, rechazos, tokens en circulación, recálculos,
duración, jugadores cotizados, estrategia activa y variación promedio, todas
con la etiqueta de la aplicación.

**Acceptance Scenarios**:

1. **Given** actividad de mercado y un recálculo desde el último reinicio,
   **When** el administrador consulta la exposición Prometheus, **Then**
   encuentra todas las métricas de negocio de esta feature.
2. **Given** las métricas de negocio expuestas, **When** se inspeccionan
   sus etiquetas, **Then** ninguna identifica usuarios ni jugadores
   individuales.

---

### User Story 5 - Restringir el panel a administradores (Priority: P1)

Como responsable de seguridad, quiero que el panel y sus datos solo sean
accesibles para administradores, para no exponer información operativa ni
de negocio a usuarios comunes.

**Why this priority**: Los datos agregados del mercado y el estado técnico
son información sensible para la operación.

**Independent Test**: Intentar acceder al panel y a sus datos sin sesión,
con un usuario común y con un administrador, y verificar que solo el
administrador obtiene los datos.

**Acceptance Scenarios**:

1. **Given** un usuario sin sesión, **When** intenta abrir el panel,
   **Then** es redirigido al login y no obtiene datos.
2. **Given** un usuario con `ROLE_USUARIO`, **When** intenta abrir el panel
   o consultar sus datos, **Then** se le deniega el acceso.
3. **Given** un administrador, **When** abre el panel, **Then** ve todas
   las secciones.

### Edge Cases

- **Reinicio del backend**: las métricas en vivo (rechazos por saldo
  insuficiente, recálculos exitosos/fallidos, duración) vuelven a cero; el
  panel las rotula "desde el último reinicio" y muestra el tiempo activo.
  Los gráficos históricos no se ven afectados.
- **Sin recálculos desde el reinicio**: duración y contadores muestran
  "sin datos" en lugar de cero engañoso; el histórico de cotizaciones sigue
  disponible desde la base.
- **Primera cotización de un jugador**: no tiene valor previo, por lo que no
  participa del cálculo de variación promedio.
- **Órdenes de liquidez inicial creadas por el sistema**: no se cuentan como
  órdenes creadas por usuarios ni sus tokens como circulación.
- **Orden emparejada parcialmente en varias transacciones**: cada
  transacción cuenta como un emparejamiento y suma su total al volumen.
- **Rechazo por tokens insuficientes en una venta**: no se cuenta como
  rechazo por saldo insuficiente (fuera de alcance).
- **Fallo al cargar una sección**: la sección afectada muestra un mensaje de
  error y las demás siguen visibles; la siguiente actualización reintenta.
- **Base de datos caída**: la autenticación de cada request necesita la
  base, así que los datos del panel no se pueden obtener. Antes de pedirlos,
  el panel consulta el estado de salud público. Si es "DOWN", muestra
  "Servicio no disponible" con ese estado, no pide los datos protegidos y no
  invalida la sesión.
- **Período sin datos**: los gráficos se muestran vacíos con un mensaje
  "sin actividad en el período".
- **Sesión expirada durante la actualización periódica**: el panel deja de
  actualizar y redirige al login.
- **Zona horaria**: los días de los gráficos se agrupan en
  `America/Argentina/Buenos_Aires`.

## Requirements *(mandatory)*

### Functional Requirements

**Métricas de mercado**

- **FR-001**: El sistema MUST contar las órdenes creadas por usuarios,
  diferenciadas por tipo (compra/venta), excluyendo las órdenes de liquidez
  inicial creadas por el sistema.
- **FR-002**: El sistema MUST contar los emparejamientos ejecutados, donde
  cada transacción de mercado cuenta como un emparejamiento.
- **FR-003**: El sistema MUST acumular el volumen operado en créditos como
  la suma del total de las transacciones de mercado.
- **FR-004**: El sistema MUST informar los tokens en circulación como la
  suma de tokens en poder de usuarios, excluyendo la cuenta custodio del
  sistema.
- **FR-005**: El sistema MUST contar las órdenes de compra rechazadas
  porque el usuario no tiene saldo disponible suficiente.

**Métricas de cotización**

- **FR-006**: El sistema MUST contar los recálculos de cotización
  ejecutados, diferenciando exitosos y fallidos.
- **FR-007**: El sistema MUST medir la duración de cada recálculo y
  permitir consultar la duración del último y la duración promedio.
- **FR-008**: El sistema MUST informar la cantidad de jugadores cotizados
  en el último recálculo.
- **FR-009**: El sistema MUST informar la estrategia de cotización activa
  con su nombre legible.
- **FR-010**: El sistema MUST calcular la variación promedio porcentual de
  las cotizaciones del último recálculo respecto de la cotización previa de
  cada jugador, excluyendo jugadores sin cotización previa.

**Históricos**

- **FR-011**: El sistema MUST proveer series diarias, para un período
  seleccionable de 7, 30 o 90 días (30 por defecto), de: órdenes creadas por
  tipo, emparejamientos, volumen operado, jugadores cotizados y variación
  promedio, calculadas desde los datos ya persistidos, sin almacenar
  snapshots nuevos.
- **FR-012**: Las series diarias MUST incluir todos los días del período,
  con cero en los días sin actividad, agrupadas en la zona horaria del
  proyecto.

**Exposición y seguridad**

- **FR-013**: Las métricas de negocio en vivo (FR-001 a FR-008 y FR-010)
  MUST registrarse con el mecanismo de métricas aprobado y aparecer en la
  exposición Prometheus existente con la etiqueta común de la aplicación,
  sin crear endpoints de Actuator nuevos. La estrategia activa (FR-009) se
  muestra solo en el panel y no se exporta como etiqueta de métrica: la
  constitución prohíbe exponer valores de propiedades de configuración en
  métricas, y la estrategia puede provenir de `application.yml`.
- **FR-014**: Ninguna métrica ni dato del panel MUST identificar usuarios o
  jugadores individuales ni exponer credenciales, tokens o datos personales.
- **FR-015**: Los datos del panel MUST ser accesibles solo con
  `ROLE_ADMIN`; sin sesión MUST responder 401 y con `ROLE_USUARIO` 403, con
  el formato de error vigente.

**Panel**

- **FR-016**: El panel MUST estar dentro del área administrativa existente
  y accesible solo para administradores.
- **FR-017**: El panel MUST mostrar las métricas de negocio con tarjetas de
  valor actual y gráficos históricos diarios, y un selector de período.
- **FR-018**: El panel MUST mostrar las métricas técnicas: salud global y
  por componente; endpoints con más requests, con su tasa de error y
  latencia media; memoria, threads, CPU y tiempo activo; y conexiones
  activas, inactivas y pendientes del pool de base de datos.
- **FR-019**: El panel MUST actualizarse automáticamente cada 30 segundos
  mientras esté visible, y las métricas técnicas MUST mostrar su evolución
  durante la sesión.
- **FR-020**: Las métricas en vivo que se reinician con el backend MUST
  rotularse "desde el último reinicio" en el panel.
- **FR-021**: Cada sección del panel MUST manejar su error de forma
  independiente, sin impedir la visualización de las demás.

### Key Entities *(include if feature involves data)*

- **Métrica de negocio en vivo**: Contador, medidor o tiempo acumulado desde
  el último reinicio (órdenes, emparejamientos, volumen, rechazos,
  recálculos, duración, jugadores cotizados, estrategia activa, variación).
- **Serie diaria histórica**: Para cada día del período, la cantidad o valor
  agregado de una métrica de negocio, calculada desde órdenes,
  transacciones y cotizaciones persistidas.
- **Resumen técnico**: Instantánea de salud, requests por endpoint, recursos
  de la JVM y del pool de conexiones obtenida de la observabilidad de la
  feature 012.
- **Período**: Rango de 7, 30 o 90 días que filtra las series históricas.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un administrador puede responder en menos de 1 minuto,
  usando solo el panel, cuántas órdenes se crearon, cuántas se emparejaron y
  qué volumen se operó en los últimos 30 días.
- **SC-002**: Los totales históricos del panel coinciden al 100% con los
  registros de órdenes, transacciones y cotizaciones del período elegido.
- **SC-003**: Tras crear una orden, emparejarla o ejecutar un recálculo, el
  cambio se ve en el panel en a lo sumo 30 segundos sin recargar la página.
- **SC-004**: El 100% de las métricas de negocio en vivo de FR-013 aparece en la
  exposición Prometheus con la etiqueta de la aplicación y sin etiquetas que
  identifiquen usuarios o jugadores.
- **SC-005**: Un usuario sin rol administrador no obtiene datos del panel
  en el 100% de los intentos.
- **SC-006**: El panel completo se carga en menos de 3 segundos con 90 días
  de historia en un entorno de desarrollo.
- **SC-007**: La suite de tests del backend se completa sin fallos ni
  regresiones y el frontend compila y pasa el lint sin errores nuevos.

## Assumptions

- Continúa la feature 012: Actuator, Micrometer y la exposición Prometheus
  ya existen y no se agregan endpoints de Actuator nuevos. Los endpoints
  administrativos del panel (`/api/admin/observabilidad/**`) están
  habilitados por la constitución v2.9.0.
- Enfoque mixto aprobado por el usuario: las métricas en vivo se reinician
  con el backend; los gráficos históricos se calculan desde los datos
  persistidos de órdenes, transacciones y cotizaciones, sin tablas nuevas.
  Rechazos por saldo, recálculos fallidos y duraciones no tienen histórico
  persistido, por lo que solo se muestran en vivo.
- La cuenta custodio y las órdenes de liquidez inicial son mecanismos
  internos del sistema y no representan actividad de usuarios.
- La variación promedio se calcula como el promedio simple, entre jugadores,
  de `(valor nuevo - valor previo) / valor previo`.
- Las métricas técnicas del panel son las de la feature 012; su evolución se
  muestra solo durante la sesión del navegador (se pierde al recargar).
- El panel se ubica en el área administrativa existente; la librería de
  gráficos ya usada en el frontend se reutiliza, sin dependencias nuevas.
- Los tests siguen el Principio VII: tests unitarios de `service` y
  `model` para la lógica nueva; no se agregan tests de `controller`,
  `configuration` ni `security`.
- Fuera de alcance: servidor Prometheus o Grafana, alertas, percentiles e
  histogramas, rechazos por tokens insuficientes, métricas de ingesta y de
  usuarios, logs estructurados y Correlation ID.
