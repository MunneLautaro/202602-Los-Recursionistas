# Modelo de datos

## ConfiguracionEstrategia

Representa la estrategia activa persistida. Se usará un único registro lógico identificado por `id=1`.

| Campo | Tipo | Restriccion |
|---|---|---|
| id | Long | Primary key; valor fijo 1 |
| estrategiaActiva | String | Not null, length acotado |
| actualizadoEn | LocalDateTime | Actualizado solo junto con el cambio efectivo |

Invariantes:
- Solo acepta `PERFORMANCE_GENERAL` o `PERFORMANCE_POR_POSICION`.
- No existe creación/eliminación pública.
- Si no existe la fila, `CotizacionServiceImpl` usa `cotizacion.estrategia.activa`.
- La actualizacion no dispara recálculo.

## CambioEstrategia

Registro append-only e inmutable del ultimo cambio efectivo.

| Campo | Tipo | Restriccion |
|---|---|---|
| id | Long | Primary key identity |
| estrategiaAnterior | String | Nullable si la fuente inicial era configuración |
| estrategiaNueva | String | Not null |
| usuarioAdmin | String | Nombre del usuario autenticado |
| timestampCambio | LocalDateTime | Instant exacto representado con zona Buenos Aires |

Invariantes:
- La entidad no expone update/delete de dominio.
- Solo se crea cuando el codigo nuevo difiere del activo efectivo.
- `estrategiaAnterior` puede ser null para documentar el primer cambio desde fallback.
- El ultimo registro se obtiene por `timestampCambio DESC, id DESC`.
- La zona se presenta mediante `America/Argentina/Buenos_Aires`; los datos no cambian su semantica historica.

## Relacion con Cotizacion

`Cotizacion.estrategiaUsada` permanece como `String`, inmutable y no normalizada. No se agrega FK ni se modifica su historial.

Estrategias conocidas:

| Codigo interno | Nombre legible | Descripcion |
|---|---|---|
| `PERFORMANCE_GENERAL` | Rendimiento general | Calcula segun pesos generales para todas las métricas. |
| `PERFORMANCE_POR_POSICION` | Rendimiento por posicion | Calcula segun pesos específicos de la posicion; usa pesos generales si la posicion es desconocida. |

## Transaccion de cambio

Dentro de `@Transactional` de `EstrategiaAdminServiceImpl`:

1. Rechazar si el flag `recalculoActivo` está activo.
2. Validar rol administrador recibido desde el contexto autenticado.
3. Validar codigo permitido.
4. Resolver activo efectivo (fila persistida o fallback).
5. Si es igual, retornar éxito idempotente sin escribir ni auditar.
6. Si difiere, actualizar/crear la fila singleton y guardar el registro append-only.

Cualquier excepcion de persistencia hace rollback de ambas escrituras.

