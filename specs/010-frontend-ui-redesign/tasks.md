---

description: "Tareas de implementación del rediseño integral de la interfaz frontend"
---

# Tasks: Rediseño integral de la interfaz frontend

**Input**: Documentos de diseño de `/specs/010-frontend-ui-redesign/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`,
`contracts/` y `quickstart.md`

**Tests**: No se generan tareas de tests automatizados porque la especificación
define escenarios de aceptación, pero no solicita TDD ni una suite nueva.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar las dependencias y conservar la estructura SPA existente.

- [X] T001 Instalar únicamente `lucide-react`, `react-hook-form`, `zod` y `@hookform/resolvers` mediante Yarn y actualizar `frontend/package.json` y `frontend/yarn.lock`
- [X] T002 [P] Auditar las rutas y el wrapper `Layout` sin modificar contratos backend en `frontend/src/app/router.tsx` y `frontend/src/components/Layout/Layout.tsx`
- [X] T003 [P] Registrar la línea base de tokens Pantone, tema claro/oscuro y estilos starter a revisar en `frontend/src/index.css` y `frontend/src/app/App.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Crear las piezas compartidas que bloquean la implementación consistente
de formularios, estados de consulta y accesibilidad.

**⚠️ CRITICAL**: Ninguna historia debe iniciar antes de completar esta fase.

- [X] T004 [P] Crear el componente de campo accesible con `label`, `id`, `aria-describedby`, `aria-invalid`, ayuda, error y sufijo en `frontend/src/components/CampoFormulario/CampoFormulario.tsx`
- [X] T005 [P] Definir estilos Tailwind del campo sin CSS inline en `frontend/src/components/CampoFormulario/CampoFormulario.styles.ts`
- [X] T006 [P] Exponer la API pública del campo desde `frontend/src/components/CampoFormulario/index.ts` y `frontend/src/components/index.ts`
- [X] T007 [P] Crear el componente compartido para estados `cargando`, `exito`, `vacio` y `error`, con acción de recuperación y región dinámica anunciable, en `frontend/src/components/EstadoConsulta/EstadoConsulta.tsx`
- [X] T008 [P] Crear estilos y exportaciones del componente de consulta en `frontend/src/components/EstadoConsulta/EstadoConsulta.styles.ts`, `frontend/src/components/EstadoConsulta/index.ts` y `frontend/src/components/index.ts`
- [X] T009 Implementar el hook genérico `useDebounce` con opción `delayMs` y valor por defecto de 400 ms en `frontend/src/hooks/useDebounce.ts`
- [X] T010 [P] Crear las definiciones de tipos compartidos para formularios y estados, manteniendo compatibilidad con `frontend/src/components/CampoFormulario`, `frontend/src/components/EstadoConsulta` y `frontend/src/hooks/useDebounce.ts`
- [X] T011 Definir esquemas Zod compartidos y su exportación por feature para preservar valores iniciales, errores en español y validación cruzada en `frontend/src/features/auth/schemas.ts`, `frontend/src/features/jugadores/schemas.ts`, `frontend/src/features/mercado/schemas.ts` y `frontend/src/features/cuenta/schemas.ts`

**Checkpoint**: La infraestructura compartida está lista y cada historia puede
implementarse sin introducir una API de estados o formularios incompatible.

---

## Phase 3: User Story 1 - Explorar jugadores y equipos (Priority: P1) 🎯 MVP

**Goal**: Permitir descubrir jugadores y equipos mediante filtros claros,
resultados ordenados, navegación accesible y estados de recuperación.

**Independent Test**: Un visitante recorre equipos, filtra jugadores por nombre,
posición, liga y equipo, espera 400 ms para la búsqueda por nombre, ordena,
pagina, limpia chips, abre un jugador y vuelve al catálogo sin perder orientación.

### Implementation for User Story 1

- [X] T012 [P] [US1] Adaptar `JugadoresFilters` para filtros de nombre, posición, liga, equipo y orden, aplicando el nombre tras 400 ms y dejando los demás filtros con aplicación explícita en `frontend/src/features/jugadores/components/JugadoresFilters.tsx`
- [X] T013 [P] [US1] Añadir estilos responsive, contador, orden visual y estados de foco de filtros en `frontend/src/features/jugadores/components/JugadoresFilters.styles.ts`
- [X] T014 [US1] Integrar chips removibles, reinicio de página ante cambios de filtros y contador de resultados en `frontend/src/pages/JugadoresPage.tsx`
- [X] T015 [P] [US1] Implementar tarjeta de jugador con nombre, posición, edad, liga y equipo, fallback de iniciales sin inventar URLs y navegación por teclado en `frontend/src/features/jugadores/components/JugadorCard.tsx`
- [X] T016 [P] [US1] Ajustar estilos de la tarjeta y sus estados de foco responsive en `frontend/src/features/jugadores/components/JugadorCard.styles.ts`
- [X] T017 [P] [US1] Integrar `EstadoConsulta` para skeleton, vacío con limpieza y error con reintento en `frontend/src/pages/JugadoresPage.tsx` y `frontend/src/pages/EquiposPage.tsx`
- [X] T018 [P] [US1] Mejorar la vista de equipos con selección navegable, escudo o sigla/iniciales y filtro de equipo removible en `frontend/src/pages/EquiposPage.tsx`
- [X] T019 [US1] Conservar paginación coherente cuando filtros o respuestas vacías cambien la página en `frontend/src/features/jugadores/components/PaginationControls.tsx` y `frontend/src/pages/JugadoresPage.tsx`

**Checkpoint**: La exploración de jugadores y equipos es funcional y validable
sin autenticación.

---

## Phase 4: User Story 2 - Consultar detalle y evolución (Priority: P1)

**Goal**: Mostrar estadísticas y cotizaciones de un jugador en un modal accesible,
con rango de fechas validado y paginación del historial.

**Independent Test**: Un visitante abre cualquier jugador, consulta sus totales y
torneos, prueba un rango inválido, pagina cotizaciones y cierra con Escape o el
botón devolviendo el foco al disparador.

### Implementation for User Story 2

- [X] T020 [P] [US2] Implementar el esquema de cotizaciones con fecha inicial no posterior a fecha final y mensajes de error en español en `frontend/src/features/jugadores/schemas.ts`
- [X] T021 [US2] Integrar validación tipada, estadísticas globales y estadísticas por torneo en `frontend/src/features/jugadores/components/JugadorDetailModal.tsx`
- [X] T022 [P] [US2] Implementar focus trap, retorno de foco, cierre con Escape, botón etiquetado y contenido visible dentro del modal en `frontend/src/features/jugadores/components/JugadorDetailModal.tsx`
- [X] T023 [P] [US2] Mostrar estados de carga, vacío y error específicos para estadísticas y cotizaciones sin inventar datos en `frontend/src/features/jugadores/components/JugadorDetailModal.tsx`
- [X] T024 [P] [US2] Ajustar layout responsive, tabla/lista de cotizaciones y estados de foco del modal en `frontend/src/features/jugadores/components/JugadorDetailModal.styles.ts`
- [X] T025 [US2] Mantener paginación y filtros de fecha coherentes al cambiar la consulta en `frontend/src/features/jugadores/components/PaginationControls.tsx` y `frontend/src/features/jugadores/components/JugadorDetailModal.tsx`

**Checkpoint**: El detalle es independiente del catálogo y no permite consultas
con rangos inválidos.

---

## Phase 5: User Story 3 - Comparar ranking del mercado (Priority: P1)

**Goal**: Permitir filtrar, paginar y abrir jugadores desde un ranking con
variaciones comprensibles sin depender solo del color.

**Independent Test**: Un visitante abre Ranking, cambia liga y posición, pagina,
interpreta variaciones positiva/negativa/neutra por icono, signo y texto, y
selecciona una tarjeta para ir al jugador.

### Implementation for User Story 3

- [X] T026 [P] [US3] Integrar filtros automáticos de liga y posición con reinicio de página y estados de consulta en `frontend/src/features/mercado/components/RankingFilters.tsx` y `frontend/src/pages/RankingPage.tsx`
- [X] T027 [P] [US3] Añadir estilos responsive y estados de foco para filtros de ranking en `frontend/src/features/mercado/components/RankingFilters.styles.ts`
- [X] T028 [US3] Renderizar posición, jugador, liga, equipo, valor actual, valor anterior y variación en `frontend/src/pages/RankingPage.tsx`
- [X] T029 [P] [US3] Comunicar cada variación mediante icono Lucide, signo y texto `positiva`, `negativa` o `neutra`, además del color, en `frontend/src/pages/RankingPage.tsx`
- [X] T030 [P] [US3] Integrar estado vacío, error con reintento, skeleton y paginación coherente en `frontend/src/pages/RankingPage.tsx`
- [X] T031 [US3] Hacer navegable cada tarjeta del ranking hacia el detalle del jugador sin cambiar `frontend/src/features/mercado/api/rankingApi.ts`

**Checkpoint**: El ranking es navegable y entendible en ambos temas y sin
depender exclusivamente del color.

---

## Phase 6: User Story 4 - Operar en mercado y consultar cuenta (Priority: P1)

**Goal**: Permitir consultar órdenes, crear compras/ventas y revisar saldo,
recargas, órdenes propias y transacciones conservando contratos y datos ante
errores del servidor.

**Independent Test**: Un usuario autenticado filtra órdenes, crea una orden
válida una sola vez, prueba cantidades inválidas y rechazo de backend, y revisa
balance, recarga, órdenes y transacciones paginadas.

### Implementation for User Story 4

- [X] T032 [P] [US4] Migrar el formulario de orden a `react-hook-form` y Zod con jugador requerido, tipo `COMPRA`/`VENTA` y cantidad entera positiva en `frontend/src/features/mercado/components/OrderForm.tsx` y `frontend/src/features/mercado/schemas.ts`
- [X] T033 [P] [US4] Mostrar errores de campo en español, error del servidor, estado de envío y conservación de valores sin doble envío en `frontend/src/features/mercado/components/OrderForm.tsx`
- [X] T034 [P] [US4] Integrar filtros por jugador/tipo, estados de lista y paginación en `frontend/src/features/mercado/components/OrderFilters.tsx`, `frontend/src/features/mercado/components/OrderList.tsx` y `frontend/src/pages/MercadoPage.tsx`
- [X] T035 [P] [US4] Actualizar estilos de filtros, formulario y lista con layout responsive y foco accesible en `frontend/src/features/mercado/components/OrderFilters.styles.ts`, `frontend/src/features/mercado/components/OrderForm.styles.ts` y `frontend/src/features/mercado/components/OrderList.styles.ts`
- [X] T036 [P] [US4] Migrar recarga a `react-hook-form` y Zod exigiendo monto finito y positivo en `frontend/src/features/cuenta/components/CreditReloadForm.tsx` y `frontend/src/features/cuenta/schemas.ts`
- [X] T037 [US4] Bloquear el botón durante la recarga, conservar valores ante rechazo y actualizar saldo solo tras respuesta exitosa en `frontend/src/features/cuenta/components/CreditReloadForm.tsx` y `frontend/src/features/cuenta/hooks/useAccount.ts`
- [X] T038 [P] [US4] Mostrar balance, identidad, órdenes propias, transacciones, estados vacíos, feedback y paginación en `frontend/src/pages/CuentaPage.tsx`, `frontend/src/features/cuenta/components/BalanceCard.tsx` y `frontend/src/features/cuenta/components/TransactionHistory.tsx`
- [X] T039 [P] [US4] Ajustar estilos de balance, recarga e historial para tema claro/oscuro y viewports responsive en `frontend/src/features/cuenta/components/BalanceCard.styles.ts`, `frontend/src/features/cuenta/components/CreditReloadForm.styles.ts` y `frontend/src/features/cuenta/components/TransactionHistory.styles.ts`

**Checkpoint**: Mercado y cuenta funcionan con autenticación existente, sin
modificar APIs, DTOs ni reglas de negocio del backend.

---

## Phase 7: User Story 5 - Iniciar sesión y navegar con confianza (Priority: P1)

**Goal**: Entregar navegación responsive, home orientada a acciones, formularios
de autenticación accesibles y una identidad visual Pantone consistente.

**Independent Test**: En desktop y mobile se recorren home, menú, login, registro,
logout y rutas protegidas; los campos anuncian errores y los envíos no se duplican.

### Implementation for User Story 5

- [X] T040 [P] [US5] Implementar header responsive con enlace activo, acciones según sesión, Cuenta protegida, iconos Lucide, `aria-expanded` y cierre del menú al seleccionar una ruta en `frontend/src/components/Header/Header.tsx`
- [X] T041 [P] [US5] Ajustar estilos del header, menú móvil, foco visible y breakpoints sin scroll horizontal en `frontend/src/components/Header/Header.styles.ts`
- [X] T042 [P] [US5] Actualizar footer y layout para mantener consistencia visual y wrapper `Layout` en `frontend/src/components/Footer/Footer.tsx`, `frontend/src/components/Footer/Footer.styles.ts` y `frontend/src/components/Layout/Layout.styles.ts`
- [X] T043 [US5] Migrar Login y Registro a `react-hook-form` y Zod con valores iniciales explícitos, errores en español, error general y bloqueo durante envío en `frontend/src/features/auth/components/LoginForm.tsx`, `frontend/src/features/auth/components/RegisterForm.tsx`, `frontend/src/features/auth/hooks/useLogin.ts` y `frontend/src/features/auth/hooks/useRegister.ts`
- [X] T044 [P] [US5] Ajustar estilos y relaciones accesibles de Login y Registro en `frontend/src/features/auth/components/LoginForm.styles.ts`, `frontend/src/features/auth/components/RegisterForm.styles.ts`, `frontend/src/pages/LoginPage.styles.ts` y `frontend/src/pages/RegisterPage.styles.ts`
- [X] T045 [P] [US5] Rediseñar Home para comunicar el propósito y ofrecer CTAs a jugadores, ranking y mercado según sesión en `frontend/src/pages/HomePage.tsx`
- [X] T046 [US5] Normalizar tokens Pantone, espaciado, radios, contraste WCAG AA y eliminar únicamente estilos starter que interfieran en `frontend/src/index.css` y `frontend/src/app/App.css`
- [X] T047 [US5] Revisar labels visibles o accesibles, nombres de botones solo-icono, `aria-describedby`, `aria-invalid`, focus states y regiones dinámicas en `frontend/src/components`, `frontend/src/features/auth` y `frontend/src/pages`

**Checkpoint**: La navegación y autenticación son accesibles en mobile y desktop
y mantienen la identidad visual existente.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Validar integración, documentación y restricciones transversales de
la feature sin cambiar el backend.

- [X] T048 [P] Documentar en `specs/010-frontend-ui-redesign/quickstart.md` el comando Yarn utilizado, decisiones no obvias y sugerencias futuras fuera del alcance
- [X] T049 [P] Revisar que ninguna vista o componente nuevo invoque `fetch` directo y que las llamadas sigan pasando por `frontend/src/lib/fetchClient.ts` y `frontend/src/features/*/api`
- [X] T050 Ejecutar `yarn lint` desde `frontend/` y corregir errores atribuibles al rediseño en los archivos modificados
- [X] T051 Ejecutar `yarn build` desde `frontend/` y corregir errores de `tsc -b` o `vite build` sin introducir `any`
- [X] T052 Verificar manualmente `quickstart.md` en home, jugadores, detalle, ranking, mercado, cuenta, login, registro, teclado, temas y viewports mobile/tablet/desktop
- [X] T053 Confirmar que `frontend/package.json` solo agrega las cuatro dependencias aprobadas y que no se modificaron backend, endpoints, DTOs ni claves `localStorage.token` y `localStorage.nombreUsuario`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001 debe completarse antes de compilar el frontend.
- **Foundational (Phase 2)**: Depende de Phase 1 y bloquea todas las historias.
- **User Stories (Phases 3–7)**: Todas dependen de Phase 2 y pueden ejecutarse en paralelo después de completar la fundación.
- **Polish (Phase 8)**: Depende de las historias que se decidan entregar y de sus integraciones.

### User Story Dependencies

- **US1 (P1)**: Depende de Phase 2; no depende de otra historia.
- **US2 (P1)**: Depende de Phase 2; reutiliza la navegación desde US1, pero es validable con datos de jugador existentes.
- **US3 (P1)**: Depende de Phase 2; no depende de US1/US2 para consultar ranking y navegar a un jugador.
- **US4 (P1)**: Depende de Phase 2; conserva la autenticación y APIs existentes, sin depender de completar US5.
- **US5 (P1)**: Depende de Phase 2; centraliza la experiencia transversal, pero puede implementarse en paralelo con las features de dominio.

### Parallel Opportunities

- T002–T003 pueden ejecutarse en paralelo.
- T004–T010 pueden ejecutarse en paralelo por archivo, coordinando las exportaciones.
- Tras Phase 2, US1, US2, US3, US4 y US5 pueden ejecutarse en paralelo por equipos distintos.
- Dentro de US1, filtros, tarjeta y estados pueden avanzar en paralelo.
- Dentro de US2, estilos, esquema y accesibilidad del modal pueden avanzar en paralelo.
- Dentro de US3, filtros, estilos y representación de variaciones pueden avanzar en paralelo.
- Dentro de US4, formulario de orden, filtros/lista y recarga pueden avanzar en paralelo.
- Dentro de US5, header, footer/layout, autenticación, home y estilos globales pueden avanzar en paralelo.
- T048–T049 pueden ejecutarse en paralelo; T050–T053 deben ejecutarse después de las integraciones.

## Parallel Execution Examples

### User Story 1

```text
T012/T013: filtros y estilos de filtros
T015/T016: tarjeta de jugador y estilos
T017/T018: estados de catálogo y vista de equipos
```

### User Story 2

```text
T020: esquema de cotizaciones
T022/T023/T024: accesibilidad, estados y estilos del modal
```

### User Story 3

```text
T026/T027: filtros y estilos de ranking
T028/T029: datos y señales redundantes de variación
T030: estados y paginación
```

### User Story 4

```text
T032/T033: formulario de orden y feedback
T034/T035: filtros/lista y estilos de mercado
T036/T037: recarga y actualización de cuenta
T038/T039: cuenta e historial y estilos
```

### User Story 5

```text
T040/T041: header y menú responsive
T042: footer/layout
T043/T044: autenticación y estilos de formularios
T045/T046: home y tokens globales
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1 y Phase 2.
2. Completar US1 para catálogo de jugadores y equipos.
3. Validar de forma independiente filtros, debounce, chips, estados, paginación y navegación.
4. Detenerse para demo antes de incorporar las demás historias.

### Incremental Delivery

1. Entregar US1 como MVP.
2. Añadir US2 para detalle y evolución.
3. Añadir US3 para ranking.
4. Añadir US4 para operaciones autenticadas.
5. Añadir US5 para navegación, autenticación y consistencia visual transversal.
6. Ejecutar Phase 8 después de cada incremento relevante y antes de la entrega final.
