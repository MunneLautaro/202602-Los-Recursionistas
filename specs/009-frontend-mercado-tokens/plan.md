# Implementation Plan: Frontend del Mercado de Tokens

**Branch**: `009-frontend-mercado-tokens` | **Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/009-frontend-mercado-tokens/spec.md`

## Summary

Completar el frontend de la aplicación de mercado usando los endpoints REST ya implementados. Se conservarán el registro, login, catálogo de jugadores, filtros y catálogos existentes; se ampliará el cliente HTTP para JWT y errores HTTP, se crearán APIs/tipos por feature, se agregarán estadísticas, cotizaciones, ranking, órdenes y cuenta, y se incorporarán rutas públicas y protegidas sin modificar el backend.

## Technical Context

**Language/Version**: TypeScript 6, React 19

**Primary Dependencies**: Vite, react-router-dom 7, Tailwind CSS 4; no nuevas dependencias estructurales

**Storage**: `localStorage` para el JWT y estado mínimo de sesión; no se agrega persistencia propia

**Testing**: `yarn build`, `yarn lint`, pruebas manuales de aceptación con backend activo y mocks/fakes de `fetch` para errores y contratos

**Target Platform**: Navegador web moderno servido por Vite

**Project Type**: Aplicación web frontend integrada con API REST

**Performance Goals**: Renderizar cada vista después de una respuesta disponible, sin solicitudes duplicadas por cambios de filtros o re-renderizados; bloquear acciones mutantes mientras están pendientes

**Constraints**: Solo endpoints existentes; `POST /login` devuelve únicamente `{ token }`; no existe `/me`; las consultas de cuenta requieren un `id` confiable; operaciones administrativas quedan fuera del frontend de usuarios

**Scale/Scope**: Catálogo paginado, ranking paginado, órdenes paginadas, historial de cotizaciones y transacciones; cinco flujos principales de usuario

## Constitution Check

*GATE: Pass*

- Se mantiene React 19, Vite, TypeScript, Tailwind CSS 4 y `react-router-dom`.
- Se reutiliza `frontend/src/lib/fetchClient.ts` como cliente HTTP y se evita agregar una librería estructural.
- La integración queda separada por features (`auth`, `jugadores`, `mercado`, `cuenta`) con APIs, tipos, hooks y componentes.
- No se modifican endpoints, contratos ni lógica del backend.
- No se exponen operaciones administrativas, scraping, ingesta ni recálculo.
- Se respetan YAGNI, manejo explícito de errores, tipado y la identidad visual existente.

## Project Structure

### Documentation (this feature)

```text
specs/009-frontend-mercado-tokens/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── frontend-api-contract.md
└── tasks.md
```

### Source Code

```text
frontend/src/
├── app/
│   └── router.tsx
├── components/
│   ├── Header/
│   └── Layout/
├── features/
│   ├── auth/
│   │   ├── api/
│   │   ├── hooks/
│   │   └── types.ts
│   ├── jugadores/
│   │   ├── api/
│   │   ├── components/
│   │   └── types.ts
│   ├── mercado/
│   │   ├── api/
│   │   ├── components/
│   │   ├── hooks/
│   │   └── types.ts
│   └── cuenta/
│       ├── api/
│       ├── components/
│       ├── hooks/
│       └── types.ts
├── lib/
│   └── fetchClient.ts
└── pages/
    ├── JugadoresPage.tsx
    ├── RankingPage.tsx
    ├── MercadoPage.tsx
    └── CuentaPage.tsx
```

**Structure Decision**: Web application existente con organización por features. Se agregan `mercado` y `cuenta`, se extiende `jugadores`, y se reutilizan componentes, hooks, estilos y paginación existentes.

## Implementation Phases

### Phase 0: Research and decisions

1. Confirmar contratos REST, DTOs, permisos y paginación en `backend/src/main/java/.../controller`, `controller/dto` y `configuration/SecurityConfig.java`.
2. Auditar el frontend actual para preservar lo existente: `fetchClient`, `auth`, `jugadores`, `router`, `Header` y formularios.
3. Fijar la decisión de identidad: no decodificar claims no garantizados ni inventar `/me`; la cuenta solo opera si existe un `userId` confiable, inicialmente el `id` recibido por registro si el flujo lo conserva.

### Phase 1: Design and contracts

1. Definir tipos frontend para páginas Spring (`content`, `totalPages`, `number`, `size`, `totalElements`), estadísticas, cotizaciones, ranking, órdenes, saldo y transacciones.
2. Documentar en `contracts/frontend-api-contract.md` requests, responses, query params, autorización y errores de cada endpoint usado.
3. Documentar en `data-model.md` las entidades de UI, estados de sesión y estados de carga/error.
4. Documentar en `quickstart.md` escenarios de validación manual y comandos de build/lint.

### Phase 2: Implementation sequence

1. **HTTP/auth foundation**: actualizar `fetchClient.ts` para adjuntar `Authorization: Bearer <token>`, conservar headers explícitos, clasificar 401/403 y limpiar/redirigir sesión solo para 401 según una estrategia que no acople el cliente a una página.
2. **Session and route protection**: extender `useAuth`, crear guardia de rutas y actualizar `router.tsx`, `Header.tsx` y `auth/types.ts` para estado de sesión, logout y rutas públicas/privadas.
3. **Player insight**: agregar APIs/tipos/hooks/componentes para `/players/{id}/stats` y `/players/{id}/quotes`, integrándolos en el detalle existente con fechas, paginación, carga, vacío y error.
4. **Ranking**: crear feature API/tipos/página para `/players/ranking`, filtros por liga/posición, paginación y navegación al detalle.
5. **Market orders**: crear feature API/tipos/hooks/componentes para `/orders`, `/orders/buy` y `/orders/sell`; validar cantidad positiva, mostrar campos de `OrdenResponse`, manejar errores funcionales y bloquear doble envío.
6. **Account**: crear API/tipos/componentes para `/users/{id}/credits` y `/users/{id}/transactions`; mostrar saldo y transacciones solo con `userId` confiable, documentando la limitación actual del login.
7. **Navigation and UX consistency**: añadir enlaces/rutas, adaptar estilos `.styles.ts`, estados comunes de carga/error/vacío y revisión visual según la paleta existente.

### Phase 3: Verification

1. Ejecutar `yarn lint` y `yarn build` desde `frontend/`.
2. Validar contratos con backend activo: catálogo, detalle, estadísticas, cotizaciones, ranking, órdenes públicas, compra/venta, 401/403 y cuenta cuando haya `userId`.
3. Verificar que no se agregaron llamadas a endpoints administrativos ni solicitudes no contempladas.

## Endpoint Mapping

| UI capability | Endpoint | Auth |
|---|---|---|
| Registro | `POST /registrar` | Pública |
| Login | `POST /login` | Pública; response `{ token }` |
| Jugadores | `GET /players` | Pública |
| Detalle | `GET /players/{id}` | Pública |
| Estadísticas | `GET /players/{id}/stats` | Pública |
| Cotizaciones | `GET /players/{id}/quotes` | Pública |
| Ranking | `GET /players/ranking` | Pública |
| Ligas/equipos | `GET /leagues`, `GET /teams/{ligaId}` | Pública |
| Órdenes | `GET /orders` | Pública |
| Comprar/vender | `POST /orders/buy`, `POST /orders/sell` | JWT |
| Créditos | `POST /users/{id}/credits` con `{ monto }` | JWT |
| Transacciones | `GET /users/{id}/transactions` | JWT y propia cuenta/admin |

## Risks and Mitigations

- **No hay `/me`**: no inventar identificación; mostrar cuenta limitada si no existe `userId` confiable.
- **Errores variables del backend**: normalizar `message`, `error`, texto y status sin ocultar el fallo.
- **Paginación inconsistente al filtrar**: reiniciar página a cero cuando cambian filtros y validar límites.
- **Doble envío financiero**: estado `pending` por acción y refresco posterior solo tras éxito.
- **Desvío de alcance**: no implementar administración, cancelaciones, vencimientos, comisiones, precios límite ni pagos reales.

## Complexity Tracking

No hay violaciones constitucionales que justificar.
