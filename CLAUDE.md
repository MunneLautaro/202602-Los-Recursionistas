# CLAUDE.md

Guía para Claude Code en este repositorio. El proyecto se desarrolla con
**spec-kit** (Spec-Driven Development), compartido con GitHub Copilot.

## Fuente de verdad

- **Constitution:** [.specify/memory/constitution.md](.specify/memory/constitution.md).
  Leerla antes de especificar, planificar o implementar. Prevalece sobre
  cualquier spec, plan, tarea o instrucción de este archivo. No modificarla
  salvo mediante `/speckit-constitution` y con pedido explícito.
- **Specs:** cada feature vive en `specs/NNN-nombre-feature/` con `spec.md`,
  `plan.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/`,
  `checklists/` y `tasks.md`. Respetar la numeración secuencial y la estructura
  existente; los artefactos se generan desde `.specify/templates/`.
- **Feature activa:** `.specify/feature.json` (`feature_directory`).

## Flujo spec-kit

Los comandos están en `.claude/skills/speckit-*` (copias de `.github/skills/`,
que es el origen gestionado por spec-kit):

1. `/speckit-constitution` — enmendar la constitution (solo si se pide).
2. `/speckit-specify <descripción>` — crear la spec de una feature nueva.
3. `/speckit-clarify` — resolver ambigüedades de la spec.
4. `/speckit-plan` — plan técnico y artefactos de diseño.
5. `/speckit-checklist` — checklists de calidad de requisitos.
6. `/speckit-tasks` — desglose en `tasks.md`.
7. `/speckit-analyze` — consistencia entre spec, plan y tareas.
8. `/speckit-implement` — ejecutar las tareas.
9. `/speckit-converge`, `/speckit-taskstoissues` — según necesidad.

Los scripts de soporte son PowerShell (`.specify/scripts/powershell/`).

## Reglas clave (resumen; el detalle está en la constitution)

- Toda la documentación, comentarios y nombres de dominio en español técnico;
  identificadores solo ASCII (sin `ñ`).
- YAGNI estricto: no crear código, tests ni configuración no pedidos.
- Ante ambigüedad arquitectónica o nuevas fuentes externas: detenerse y
  proponer al menos dos alternativas antes de codificar.
- Calidad: seguir `.agents/skills/sonarcloud-quality/SKILL.md`.

## Comandos

```powershell
# Backend (Java 21, Gradle) — incluye ArquitecturaCapasTest (ArchUnit)
cd backend; .\gradlew.bat clean test

# Frontend (React 19, Vite, TypeScript, Yarn)
cd frontend; yarn lint; yarn build
```

Informar cualquier verificación que no se haya podido ejecutar y su motivo.
