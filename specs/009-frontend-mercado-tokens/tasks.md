---
description: "Tareas de implementación del frontend del mercado de tokens"
---

# Tasks: Frontend del Mercado de Tokens

**Input**: Design documents from `specs/009-frontend-mercado-tokens/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/frontend-api-contract.md`, `quickstart.md`

**Tests**: No se agregan tareas TDD porque la spec no lo solicita. La validación se realiza con `yarn lint`, `yarn build` y los escenarios de `quickstart.md`.

## Phase 1: Setup

**Purpose**: Preparar la estructura frontend sin alterar la funcionalidad existente.

- [x] T001 Crear las carpetas `frontend/src/features/mercado/api`, `frontend/src/features/mercado/components`, `frontend/src/features/mercado/hooks`, `frontend/src/features/cuenta/api`, `frontend/src/features/cuenta/components` y `frontend/src/features/cuenta/hooks` según `specs/009-frontend-mercado-tokens/plan.md`.
- [x] T002 [P] Revisar y conservar las convenciones de exportación de `frontend/src/features/auth/index.ts`, `frontend/src/components/index.ts` y las APIs existentes antes de agregar nuevos módulos.
- [x] T003 [P] Verificar que `frontend/package.json` mantenga React, TypeScript, Vite, Tailwind y `react-router-dom` sin incorporar dependencias estructurales nuevas.

---

## Phase 2: Foundational

**Purpose**: Habilitar contratos HTTP y errores compartidos antes de implementar historias de usuario.

- [x] T004 Implementar en `frontend/src/lib/fetchClient.ts` la inclusión automática de `Authorization: Bearer <token>` cuando exista un token en `localStorage`, sin sobrescribir un header explícito.
- [x] T005 Implementar en `frontend/src/lib/fetchClient.ts` errores tipados o clasificables por status para distinguir 401, 403, errores JSON con `message`/`error` y respuestas de texto o red sin mensaje.
- [x] T006 [P] Completar en `frontend/src/features/auth/types.ts` los tipos de sesión necesarios para representar `token`, `nombreUsuario`, `userId` opcional y estado de sesión invalidada, sin asumir claims no documentados del JWT.
- [x] T007 Ejecutar `yarn lint` y `yarn build` desde `frontend/` después de los cambios del cliente HTTP y corregir cualquier regresión de tipado o lint.

**Checkpoint**: El cliente HTTP conserva las llamadas públicas actuales, adjunta JWT en solicitudes protegidas y expone 401/403 sin ocultar errores.

---

## Phase 3: User Story 1 - Explorar jugadores y su valor (Priority: P1) 🎯 MVP

**Goal**: Permitir consultar jugadores, estadísticas y evolución de cotizaciones desde el catálogo público.

**Independent Test**: Un visitante filtra y pagina jugadores, abre un detalle, carga estadísticas y consulta cotizaciones con rango de fechas, incluyendo estados vacío y error.

### Implementation for User Story 1

- [x] T008 [P] [US1] Agregar en `frontend/src/features/jugadores/types.ts` los tipos `PlayerStats`, `SeasonStats`, `GlobalStats`, `Quote` y sus campos correspondientes al contrato de `/players/{id}/stats` y `/players/{id}/quotes`.
- [x] T009 [P] [US1] Agregar en `frontend/src/features/jugadores/api/jugadoresApi.ts` `fetchJugadorStats(id)` para `GET /players/{id}/stats` y `fetchJugadorQuotes(id, filters, page, size)` para `GET /players/{id}/quotes`, omitiendo parámetros vacíos.
- [x] T010 [US1] Extender `frontend/src/features/jugadores/components/JugadorDetailModal.tsx` para cargar y mostrar datos del jugador, `totalesGlobal`, `estadisticasPorTorneo`, valores de cotización, `timestampCalculo`, estrategia y ventana de cálculo.
- [x] T011 [US1] Agregar en `frontend/src/features/jugadores/components/JugadorDetailModal.tsx` filtros de fechas inclusivos para cotizaciones, validando que `desde` no sea posterior a `hasta` antes de solicitar datos.
- [x] T012 [US1] Agregar estados loading, empty, error y reintento para estadísticas y cotizaciones en `frontend/src/features/jugadores/components/JugadorDetailModal.tsx`, evitando que un fallo secundario cierre el detalle.
- [x] T013 [US1] Actualizar `frontend/src/pages/JugadoresPage.tsx` y `frontend/src/features/jugadores/components/JugadorDetailModal.styles.ts` para integrar la carga de estadísticas/cotizaciones sin romper filtros, paginación ni detalle actual.
- [x] T014 [US1] Ejecutar los escenarios de catálogo, detalle, estadísticas, cotizaciones, rango de fechas, cero resultados y error descritos en `specs/009-frontend-mercado-tokens/quickstart.md`.

**Checkpoint**: US1 funciona de forma pública e independiente.

---

## Phase 4: User Story 2 - Comparar el ranking del mercado (Priority: P1)

**Goal**: Permitir consultar y filtrar el ranking paginado de jugadores por cotización.

**Independent Test**: Un visitante abre el ranking, aplica liga/posición, pagina resultados y navega al detalle de un jugador.

### Implementation for User Story 2

- [x] T015 [P] [US2] Crear `frontend/src/features/mercado/types.ts` con `RankingPlayer`, `PaginatedResponse<T>` y filtros de ranking (`ligaId`, `posicion`, `page`, `size`) según `RankingJugadorResponse`.
- [x] T016 [P] [US2] Crear `frontend/src/features/mercado/api/rankingApi.ts` con `fetchRanking(filters, page, size)` para `GET /players/ranking`, omitiendo filtros no definidos.
- [x] T017 [P] [US2] Crear `frontend/src/features/mercado/components/RankingFilters.tsx` y `frontend/src/features/mercado/components/RankingFilters.styles.ts` reutilizando ligas y posiciones disponibles en el frontend.
- [x] T018 [US2] Crear `frontend/src/pages/RankingPage.tsx` para mostrar nombre, posición, equipo, liga, valor actual, valor anterior y variación, con carga, vacío, error y paginación.
- [x] T019 [US2] Agregar navegación desde una fila del ranking hacia `/jugadores` o el detalle existente del jugador usando `jugadorId`, sin duplicar la lógica de consulta de jugadores.
- [x] T020 [US2] Ejecutar la validación del ranking sin autenticación, incluyendo filtros, cambio de página, cero resultados y navegación al detalle.

**Checkpoint**: US2 funciona de forma pública e independiente después de la base HTTP.

---

## Phase 5: User Story 3 - Operar en el mercado (Priority: P1)

**Goal**: Permitir consultar órdenes públicamente y crear compras/ventas autenticadas con cantidades válidas.

**Independent Test**: Un visitante consulta órdenes; un usuario autenticado crea una compra o venta válida, y las cantidades inválidas o reglas rechazadas muestran errores sin confirmar falsamente la operación.

### Implementation for User Story 3

- [x] T021 [P] [US3] Completar `frontend/src/features/mercado/types.ts` con `Order`, `OrderFilters`, `OrderRequest` y los estados `tipo`/`estado` usados por `OrdenResponse`.
- [x] T022 [P] [US3] Crear `frontend/src/features/mercado/api/ordersApi.ts` con `fetchOrders(filters, page, size)`, `createBuyOrder({ jugadorId, cantidad })` y `createSellOrder({ jugadorId, cantidad })` para los endpoints del contrato.
- [x] T023 [P] [US3] Crear `frontend/src/features/mercado/hooks/useOrders.ts` para consultar órdenes paginadas, reiniciar página al cambiar filtros y exponer loading/empty/error.
- [x] T024 [US3] Crear `frontend/src/features/mercado/components/OrderFilters.tsx`, `OrderFilters.styles.ts`, `OrderList.tsx` y `OrderList.styles.ts` para filtros de jugador/tipo y campos de orden: cantidades, precios, total remanente, estado y fecha.
- [x] T025 [US3] Crear `frontend/src/features/mercado/components/OrderForm.tsx` y `OrderForm.styles.ts` para compra/venta, enviando exactamente `{ jugadorId, cantidad }`, validando cantidad entera positiva y bloqueando doble envío.
- [x] T026 [US3] Crear `frontend/src/pages/MercadoPage.tsx` integrando consulta pública, formulario autenticado, redirección al login y mensajes para saldo insuficiente, tokens insuficientes, jugador inactivo, 401, 403 y demás errores del backend.
- [x] T027 [US3] Ejecutar la validación de órdenes públicas y autenticadas: compra, venta, cantidad cero/negativa, error funcional, token inválido, respuesta prohibida y confirmación únicamente tras respuesta exitosa.

**Checkpoint**: US3 ofrece el flujo central de mercado sin cancelaciones, vencimientos, comisiones, precios límite ni pagos reales.

---

## Phase 6: User Story 5 - Mantener una sesión segura y navegable (Priority: P1)

**Goal**: Proteger rutas y acciones autenticadas, persistir sesión válida y reaccionar correctamente a 401/403.

**Independent Test**: Un usuario inicia sesión, recarga la aplicación, accede a una ruta protegida, cierra sesión y prueba tokens rechazados.

### Implementation for User Story 5

- [x] T028 [US5] Extender `frontend/src/features/auth/hooks/useAuth.ts` para exponer un estado de sesión reactivo, conservar `token`/usuario conocido tras recarga y limpiar sesión en logout o invalidación.
- [x] T029 [US5] Actualizar `frontend/src/features/auth/hooks/useLogin.ts` y `frontend/src/features/auth/hooks/useRegister.ts` para mantener los contratos `/login` y `/registrar`, guardar solo datos confiables y no inferir identidad desde claims no documentados.
- [x] T030 [US5] Crear `frontend/src/components/ProtectedRoute.tsx` para redirigir usuarios no autenticados al login y preservar la ruta de retorno cuando corresponda.
- [x] T031 [US5] Actualizar `frontend/src/app/router.tsx` con rutas públicas para ranking/mercado de consulta y rutas protegidas para acciones autenticadas/cuenta, sin romper inicio, registro, login y jugadores.
- [x] T032 [US5] Actualizar `frontend/src/components/Header/Header.tsx` para enlaces a jugadores, ranking y mercado, acceso a cuenta cuando corresponda y logout visible.
- [x] T033 [US5] Conectar la respuesta 401 del cliente HTTP con la invalidación de sesión y navegación al login sin acoplar la API a componentes; conservar la sesión ante 403 y mostrar acceso denegado en la vista.
- [x] T034 [US5] Ejecutar la validación de login, recarga del navegador, ruta protegida, logout, token inválido/401 y respuesta 403 descrita en `quickstart.md`.

**Checkpoint**: US5 mantiene una sesión coherente y bloquea acciones protegidas sin afectar consultas públicas.

---

## Phase 7: User Story 4 - Consultar cuenta e historial (Priority: P2)

**Goal**: Mostrar saldo, recarga ficticia e historial de transacciones solo cuando exista un `userId` confiable.

**Independent Test**: Con un identificador confiable se consultan saldo, recarga e historial paginado; sin él se informa la limitación sin consultar una cuenta arbitraria.

### Implementation for User Story 4

- [x] T035 [P] [US4] Crear `frontend/src/features/cuenta/types.ts` con `Balance`, `Transaction`, `CreditReloadRequest` y las páginas correspondientes a `SaldoResponse` y `TransaccionResponse`.
- [x] T036 [P] [US4] Crear `frontend/src/features/cuenta/api/accountApi.ts` con `reloadCredits(userId, monto)` para `POST /users/{id}/credits` y `fetchTransactions(userId, page, size)` para `GET /users/{id}/transactions`.
- [x] T037 [P] [US4] Crear `frontend/src/features/cuenta/hooks/useAccount.ts` para cargar saldo/transacciones, reiniciar página, bloquear recargas pendientes y exponer errores 401/403.
- [x] T038 [US4] Crear `frontend/src/features/cuenta/components/BalanceCard.tsx`, `BalanceCard.styles.ts`, `CreditReloadForm.tsx` y `CreditReloadForm.styles.ts` para saldo, reservado, disponible y monto positivo ficticio.
- [x] T039 [US4] Crear `frontend/src/features/cuenta/components/TransactionHistory.tsx` y `TransactionHistory.styles.ts` para mostrar jugador, comprador, vendedor, cantidad, precio unitario, total, fecha y paginación.
- [x] T040 [US4] Crear `frontend/src/pages/CuentaPage.tsx` para usar únicamente `userId` confiable, mostrar la limitación del contrato cuando solo exista `{ token }` y no inventar `/me`, claims ni identificadores.
- [x] T041 [US4] Integrar el acceso a cuenta en `frontend/src/app/router.tsx` y `frontend/src/components/Header/Header.tsx` como ruta protegida, sin permitir consultar otra cuenta desde la interfaz normal.
- [x] T042 [US4] Ejecutar la validación de cuenta con `userId` confiable y sin `userId`, incluyendo saldo, recarga positiva, error de recarga, historial paginado, 401 y 403.

**Checkpoint**: US4 funciona de forma segura o informa claramente la limitación de identidad.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Integrar visualmente las historias, verificar alcance y cerrar regresiones.

- [x] T043 [P] Revisar `frontend/src/index.css`, `frontend/src/app/App.styles.ts` y los nuevos archivos `.styles.ts` para conservar la paleta Pantone, responsive design y estados visuales coherentes.
- [x] T044 [P] Revisar `frontend/src/features/auth`, `frontend/src/features/jugadores` y `frontend/src/components` para asegurar que las funcionalidades existentes de registro, login, filtros, catálogos y detalle no se rompan.
- [x] T045 Verificar que no existan llamadas ni enlaces normales a `/api/admin/whoscored/**`, `/admin/sync/**`, `/ingesta/**` o `/quotes/recalculate`, y que no se hayan implementado cancelaciones, vencimientos, comisiones, precios límite ni pagos reales.
- [x] T046 Ejecutar `yarn lint` y `yarn build` desde `frontend/` y corregir todos los errores antes de considerar terminada la feature.
- [x] T047 Ejecutar todos los escenarios de `specs/009-frontend-mercado-tokens/quickstart.md` con backend activo y registrar cualquier contrato incompatible como bloqueo explícito, sin inventar una solución de frontend.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: no depende de otras fases.
- **Phase 2 (Foundational)**: depende de Phase 1 y bloquea todas las historias.
- **Phase 3 (US1)**: depende de Phase 2; es el MVP recomendado.
- **Phase 4 (US2)**: depende de Phase 2; puede ejecutarse en paralelo con US1.
- **Phase 5 (US3)**: depende de Phase 2 y de la base de sesión de US5 para acciones autenticadas; la consulta pública puede desarrollarse en paralelo.
- **Phase 6 (US5)**: depende de Phase 2 y habilita rutas/acciones protegidas de US3 y US4.
- **Phase 7 (US4)**: depende de Phase 6 y del identificador confiable disponible; no depende de que exista `/me`.
- **Phase 8 (Polish)**: depende de todas las historias incluidas en la entrega.

### User Story Dependencies

- **US1 (P1)**: solo depende de Foundational.
- **US2 (P1)**: solo depende de Foundational; puede ejecutarse en paralelo con US1.
- **US3 (P1)**: su consulta pública depende de Foundational; sus mutaciones requieren US5.
- **US5 (P1)**: depende de Foundational y es prerequisito de las rutas protegidas de US3 y US4.
- **US4 (P2)**: depende de US5 y de disponer de un `userId` confiable.

### Parallel Opportunities

- T008 y T009 pueden ejecutarse en paralelo en US1; T010-T012 dependen de sus contratos.
- T015, T016 y T017 pueden ejecutarse en paralelo en US2; T018 integra sus resultados.
- T021-T024 pueden ejecutarse en paralelo en US3; T025-T026 dependen de APIs y tipos.
- T035 y T036 pueden ejecutarse en paralelo en US4; T038-T039 dependen de los tipos.
- US1 y US2 pueden desarrollarse en paralelo después de Phase 2.
- La consulta pública de US3 puede desarrollarse en paralelo con US5; las acciones protegidas se integran después.
- T043 y T044 pueden ejecutarse en paralelo durante Polish.

## Implementation Strategy

### MVP First (User Story 1)

1. Completar Phase 1 y Phase 2.
2. Implementar US1: catálogo existente, estadísticas y cotizaciones.
3. Ejecutar su prueba independiente y `yarn lint`/`yarn build`.
4. Entregar el flujo de exploración antes de agregar operaciones financieras.

### Incremental Delivery

1. Agregar US2 para descubrimiento mediante ranking.
2. Agregar US5 para consolidar sesión y protección.
3. Agregar US3 para órdenes públicas y compra/venta autenticada.
4. Agregar US4 solo con identificación confiable.
5. Ejecutar Phase 8 y el quickstart completo.
