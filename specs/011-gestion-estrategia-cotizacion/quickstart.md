# Guia rapida de validacion

## Prerrequisitos

- PostgreSQL activo y variables de `application.yml` disponibles.
- Backend levantado con Java 21.
- Frontend levantado con Vite.
- Usuario autenticado con `ROLE_ADMIN` y usuario sin rol administrador para pruebas negativas.

## Cambiar estrategia

1. Abrir el panel administrativo.
2. Consultar la seccion de estrategia de cotizacion.
3. Confirmar que se muestran las dos alternativas con descripcion.
4. Seleccionar una estrategia distinta y confirmar.
5. Verificar que el panel muestra la nueva estrategia y el ultimo cambio.

Resultado esperado: cambio persistido sin recálculo automatico.

## Idempotencia

1. Seleccionar la estrategia que ya está activa.
2. Confirmar.

Resultado esperado: `200`, `cambioAplicado=false` y cero registros nuevos de auditoria.

## Concurrencia

1. Iniciar recálculo manual.
2. Intentar cambiar la estrategia antes de que finalice.

Resultado esperado: `409` con mensaje de recálculo en curso y activo sin cambios.

## Reinicio

1. Cambiar efectivamente la estrategia.
2. Reiniciar el backend.
3. Consultar el panel y ejecutar un recálculo manual.

Resultado esperado: la estrategia persistida permanece activa y el recálculo la usa.

## Fallback

1. Asegurar que no exista fila persistida en `ConfiguracionEstrategia`.
2. Consultar el activo y recálcular.

Resultado esperado: se usa `cotizacion.estrategia.activa` de `application.yml`.

## Historial y nombres

1. Crear o conservar cotizaciones con ambas estrategias.
2. Abrir historial, grafico y detalle del jugador.

Resultado esperado: `Rendimiento general` y `Rendimiento por posicion`, sin mostrar codigos internos.

## Verificacion automatica

Backend:

```powershell
cd backend
.\gradlew test
```

Frontend:

```powershell
cd frontend
yarn lint
yarn typecheck
```
