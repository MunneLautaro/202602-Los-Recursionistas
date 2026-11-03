# Plan de implementación: Rediseño integral de la interfaz frontend

**Branch**: `010-frontend-ui-redesign` | **Fecha**: 2026-11-03 | **Spec**: [spec.md](./spec.md)

**Entrada**: Especificación de feature en `specs/010-frontend-ui-redesign/spec.md`

## Summary

Se modernizará la interfaz SPA sin modificar el backend ni sus contratos: se
mantendrán las APIs, hooks de dominio, autenticación, paginación y rutas
existentes, y se refactorizarán componentes y páginas para una experiencia
responsive, accesible y consistente con la paleta Pantone. Se incorporarán
únicamente `lucide-react`, `react-hook-form`, `zod` y `@hookform/resolvers`,
aprobados por el equipo, además de componentes compartidos de campos, estados
de carga/error/vacío, esquemas tipados y `useDebounce`.

El trabajo se ejecutará en dos fases de este plan. Phase 0 documenta la
auditoría y las decisiones técnicas en `research.md`. Phase 1 define entidades,
contratos y validación en `data-model.md`, `contracts/` y `quickstart.md`.
Phase 2 queda reservada para `/speckit-tasks`.

## Technical Context

**Language/Version**: TypeScript 6 / React 19

**Primary Dependencies**: Vite 8, Tailwind CSS 4 mediante `@tailwindcss/vite`,
`react-router-dom` 7, `lucide-react`, `react-hook-form`, `zod` y
`@hookform/resolvers`

**Storage**: `localStorage` con las claves existentes `token` y
`nombreUsuario`; el estado de cada feature permanece en sus hooks. No se
introducen nuevas fuentes persistentes.

**Testing**: `yarn lint`, `yarn build` (incluye `tsc -b`), y las pruebas
existentes sin regresiones. Se revisará el escaneo SonarCloud disponible; toda
verificación no ejecutada se informará.

**Target Platform**: Web responsive para desktop, tablet y mobile; no se agrega
PWA ni almacenamiento offline.

**Project Type**: SPA web-service frontend con backend REST existente.

**Performance Goals**: La búsqueda por nombre actualiza resultados dentro de
500 ms después del debounce de 400 ms (SC-004); al menos 95% de solicitudes de
listas muestra carga, éxito, vacío o error (SC-005).

**Constraints**: Sin scroll horizontal involuntario (SC-002), contraste WCAG AA
en claro y oscuro (SC-008), llamadas HTTP solamente desde módulos `api/` vía
`fetchClient`, estilos de componente en `.styles.ts`, sin librería de
componentes externa y sin cambios al backend.

**Scale/Scope**: Nueve áreas de página (home, jugadores, equipos, ranking,
mercado, cuenta, login, registro y administración), componentes compartidos y
cinco features de UI existentes (`auth`, `jugadores`, `mercado`, `cuenta` y
`admin`).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Gate inicial: PASS

- Dependencias nuevas limitadas a las cuatro aprobadas en FR-017 y agregadas
  mediante Yarn.
- Se conserva la jerarquía `features/` → `components/` → `pages/`; cada
  componente nuevo tendrá directorio, `.tsx`, `.styles.ts` e `index.ts`.
- Las llamadas HTTP existentes permanecen en `features/**/api` mediante
  `src/lib/fetchClient.ts`; ningún componente invocará `fetch` directamente.
- Los estilos nuevos se definirán en `.styles.ts` con clases Tailwind; no habrá
  CSS inline ni hojas CSS por componente. Se retirará únicamente el CSS de
  starter que interfiera con el alcance visual.
- Se mantienen `localStorage.token` y `localStorage.nombreUsuario`.
- Las páginas seguirán montadas bajo `Layout` desde `app/router.tsx`; no se
  envolverán individualmente.
- No se modifican backend, endpoints ni DTOs de servicio.
- Se reutilizan los tokens Pantone existentes de `frontend/src/index.css` y se
  verificará contraste WCAG AA en ambos temas; no se introduce una paleta
  violeta alternativa.
- El código nuevo cumplirá `.agents/skills/sonarcloud-quality/SKILL.md`: sin
  `any`, comparaciones estrictas, dependencias exhaustivas de hooks y lógica
  compartida extraída.

### Trazabilidad de historias

- **US-1 Explorar jugadores y equipos**: FR-005, FR-006 y FR-007 mediante
  `JugadoresFilters`, `useDebounce`, chips/contador, tarjetas accesibles,
  estados compartidos y vistas de equipos.
- **US-2 Detalle y evolución**: FR-008 y FR-012 mediante el modal accesible,
  filtros de fechas, paginación, estados vacíos y esquema de cotizaciones.
- **US-3 Ranking**: FR-009 y FR-007 mediante filtros automáticos, tarjeta de
  variación redundante, navegación interna y estado vacío.
- **US-4 Mercado / Cuenta**: FR-010, FR-011, FR-012 y FR-013 mediante
  formularios tipados, validación, preservación de datos, estados de operación
  y paginación existente.
- **US-5 Sesión y navegación accesible**: FR-001, FR-003, FR-004, FR-014,
  FR-015 y FR-018 mediante Header responsive, iconos aprobados, campos
  accesibles, tema Pantone y CTAs de Home.

### Gate posterior a Phase 1: PASS

- Los contratos compartidos están limitados a campo, estados, esquemas y
  debounce solicitados por FR-019; no agregan abstracciones de negocio.
- La decisión aprobada mantiene exactamente cuatro dependencias nuevas y el
  comando Yarn está documentado en `quickstart.md`.
- El modelo y los contratos no cambian endpoints, DTOs ni persistencia del
  backend, y mantienen `fetchClient`, `localStorage` y el wrapper `Layout`.
- La organización propuesta conserva `app/`, `features/`, `components/`,
  `pages/` y `lib/`; cada componente nuevo tendrá su API pública mediante
  `index.ts`.
- Los artefactos exigen `.styles.ts`, señales redundantes para estados,
  etiquetas ARIA y contraste Pantone WCAG AA.
- No quedan valores `NEEDS CLARIFICATION`; la única decisión ambigua de
  dependencias fue presentada con alternativas y aprobada antes de Phase 1.

## Project Structure

### Documentation (this feature)

```text
specs/010-frontend-ui-redesign/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md             # Phase 2, generado por /speckit-tasks
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── app/                  # main, providers, router; sin cambio estructural
│   ├── components/
│   │   ├── Header/
│   │   ├── Footer/
│   │   ├── Layout/
│   │   ├── CampoFormulario/
│   │   └── EstadoConsulta/
│   ├── features/
│   │   ├── auth/
│   │   ├── jugadores/
│   │   ├── mercado/
│   │   ├── cuenta/
│   │   └── admin/
│   ├── hooks/
│   │   └── useDebounce.ts
│   ├── lib/
│   │   └── fetchClient.ts
│   └── pages/
└── package.json
```

**Structure Decision**: Se mantiene el SPA actual bajo `frontend/src`. Los
componentes visuales compartidos viven en `components/`, la lógica de dominio
visual y sus esquemas permanecen en cada `features/`, las páginas solo
orquestan, y `app/router.tsx` conserva el wrapper `Layout`. `fetchClient.ts`,
los endpoints y los tipos de backend se reutilizan sin cambios contractuales.

## Plan por fases

### Phase 0 — Research

1. Auditar páginas, componentes, hooks, APIs, estilos y rutas existentes, y
   clasificar cada elemento como refactorización, conservación o creación.
2. Confirmar que `fetchClient`, `localStorage`, Tailwind 4 y los tokens Pantone
   son las únicas bases transversales necesarias.
3. Presentar y aprobar la combinación de iconos y formularios. La decisión
   aprobada es `lucide-react` + `react-hook-form` + `zod` +
   `@hookform/resolvers`; `research.md` registra alternativas, licencias,
   bundle orientativo, tree-shaking y reglas ARIA.
4. Registrar gaps FR-001..FR-020 y riesgos de regresión sin modificar código.

**Salida**: `research.md`, sin NEEDS CLARIFICATION pendiente.

### Phase 1 — Design / Contratos

1. Definir `CampoFormulario`, `EstadoConsulta`, `EstadoCarga` y
   `useDebounce` como contratos tipados y reutilizables.
2. Definir esquemas Zod por feature para auth, jugadores/cotizaciones,
   mercado y cuenta, manteniendo las reglas de dominio del backend como fuente
   de verdad.
3. Mapear FR-001..FR-020 a ubicaciones exactas de `src/`, distinguiendo
   refactorizaciones de componentes nuevos.
4. Modelar `Jugador`, `Equipo`, `RankingPlayer`, `Orden`, `Cuenta` y
   `Formulario` en `data-model.md`.
5. Crear contratos TypeScript públicos en `contracts/` y una guía de ejecución
   en `quickstart.md`.
6. Re-ejecutar el Constitution Check: dependencias, jerarquía, HTTP, estilos,
   storage, Layout, backend, Pantone, WCAG AA y SonarCloud.

**Salida**: `data-model.md`, `contracts/`, `quickstart.md` y gate posterior
PASS. La lista ejecutable de tareas queda para `/speckit-tasks`.

### Phase 2 — Tasks

Será generado exclusivamente por `/speckit-tasks` a partir de este plan y de
los artefactos de diseño.

## Sugerencias futuras fuera del alcance (FR-020)

- Medir el bundle de producción con un analizador dedicado y evaluar
  `zod/mini` solo si la medición real lo justifica.
- Añadir pruebas automatizadas de accesibilidad visual y navegación con
  Playwright si el equipo incorpora esa infraestructura.
- Evaluar preferencia manual de tema, favoritos, comparador o notificaciones
  únicamente mediante una nueva especificación.
