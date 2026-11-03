# Investigación — Rediseño integral de la interfaz frontend

## Decisión de dependencias aprobadas

**Decisión**: usar `lucide-react`, `react-hook-form`, `zod` y
`@hookform/resolvers`, instaladas con:

```bash
yarn add lucide-react react-hook-form zod @hookform/resolvers
```

**Racional**:

- `lucide-react` ofrece iconos SVG individuales, estilo consistente, imports
  ESM y tree-shaking. Su licencia es ISC y admite React 19. Se estima entre
  1–3 kB gzip por icono usado, sujeto a verificación con `vite build`.
- Los iconos aceptan `className`, `size` y `currentColor`, por lo que se
  integran con Tailwind 4 sin plugin. Los decorativos usarán `aria-hidden`;
  los controles solo-icono tendrán texto visible o `aria-label`.
- `react-hook-form` reduce renders y ofrece tipado de campos para React 19.
  `zod` permite inferir tipos desde esquemas y `@hookform/resolvers` aporta
  `zodResolver`. El conjunto estima RHF 9–15 kB gzip, Zod 8–15 kB y
  resolver 2–4 kB, sujeto a medición final.
- Las cuatro dependencias tienen licencia permisiva (ISC/MIT), soportan
  imports optimizables y no constituyen una librería de componentes.

## Alternativas consideradas

| Opción | Ventajas | Costes o riesgos |
|---|---|---|
| `@heroicons/react` + RHF/Zod | Imports individuales, MIT y buena integración Tailwind | Menor flexibilidad de estilo y no fue la opción aprobada |
| `react-icons` + RHF/Yup | Catálogo amplio, MIT y API conocida | El tree-shaking depende más estrictamente del subpaquete; Yup ofrece inferencia menos directa |
| `lucide-react` + TanStack Form/Zod | API potente para formularios complejos | Mayor superficie y sobre-ingeniería para los formularios actuales |

El equipo aprobó la primera opción (`lucide-react` + RHF/Zod/resolver) y no
aprobó introducir `zod/mini` sin una medición de bundle que lo justifique.

## Auditoría del código actual

- `frontend/src/app/router.tsx` ya monta las rutas bajo `Layout` y separa
  rutas públicas, protegidas y de administración. Se conserva esa estructura.
- `frontend/src/lib/fetchClient.ts` centraliza URL base, JWT y errores. Los
  módulos `features/**/api` son los únicos que deben invocarlo; no se agregan
  llamadas HTTP en componentes.
- `frontend/src/index.css` ya define tokens Pantone y tema claro/oscuro.
  `app/App.css` conserva estilos del starter y debe revisarse para evitar
  conflictos, sin reemplazar los tokens globales.
- `Header` necesita menú móvil, enlace activo, estados ARIA y cierre al
  seleccionar una ruta. `Footer` y `Layout` requieren principalmente
  consistencia visual.
- `JugadoresFilters` ya carga ligas y equipos, pero el nombre solo se aplica
  al enviar; faltan debounce, orden, chips, contador, labels y recuperación
  accesible. `JugadorCard` requiere fallback de iniciales y navegación
  semántica. El modal requiere Escape, focus trap/focus return, botón
  etiquetado y estados explícitos.
- Ranking, mercado y cuenta conservan la lógica principal, pero necesitan
  estados compartidos, feedback uniforme, variaciones redundantes y
  validación tipada. Los esquemas no deben duplicar reglas de negocio del
  backend.
- Login, registro, órdenes y recarga tienen validación ad hoc. Se migrarán
  gradualmente a `react-hook-form` y esquemas Zod por feature, preservando
  valores ante error de servidor y bloqueando doble envío.

## Mapa de gaps y superficies

| FR | Superficie prevista | Acción |
|---|---|---|
| FR-001, FR-016 | `index.css`, `App.css`, todos los `.styles.ts` | Normalizar tokens, espaciado, radios y contraste |
| FR-002 | `features/**/api`, hooks y páginas | Conservar contratos; refactor solo de presentación |
| FR-003, FR-004, FR-015 | `components/Header/` | Menú responsive, iconos Lucide y ARIA |
| FR-005, FR-006, FR-007 | `features/jugadores/`, `pages/EquiposPage.tsx` | Debounce, filtros/chips, tarjetas, fallback y estados |
| FR-008, FR-012, FR-014 | `JugadorDetailModal/`, esquemas jugadores | Modal accesible, fechas y errores asociados |
| FR-009 | `features/mercado/`, `pages/RankingPage.tsx` | Filtros, variación con icono/signo/texto y navegación |
| FR-010, FR-011, FR-012, FR-013 | `features/mercado/`, `features/cuenta/` | Formularios tipados, estados y preservación de datos |
| FR-018 | `pages/HomePage.tsx` | CTAs públicos y autenticados |
| FR-019 | `components/CampoFormulario/`, `components/EstadoConsulta/`, `hooks/useDebounce.ts` | Crear API pública compartida |
| FR-020 | `quickstart.md` y documentación de entrega | Comando Yarn y futuras sugerencias separadas |

## Riesgos y controles

- **Regresión de contratos**: no editar `api/`, DTOs ni endpoints salvo ajustes
  de tipos estrictamente necesarios; validar compilación y lint.
- **Pérdida de estado de formulario**: los errores de servidor se mostrarán
  como error general o de campo sin resetear valores.
- **Accesibilidad incompleta**: cada campo tendrá `label`, `id`,
  `aria-describedby` y `aria-invalid` cuando corresponda; modales gestionarán
  foco y Escape.
- **Contraste**: revisar combinaciones Pantone en ambos temas con WCAG AA; el
  amarillo Sun Glare usará texto Darkest Hour.

