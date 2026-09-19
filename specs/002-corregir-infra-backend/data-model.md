# Modelo de datos: Correccion de infraestructura del backend

## Alcance

Este modelo define las entidades JPA requeridas por la feature y sus invariantes iniciales. Las clases se ubicaran en `backend/src/main/java/unq/losrecursionistas/backend/model/` y usaran identificadores ASCII en espanol tecnico.

## Entidades

### Usuario

- **Tabla**: `usuarios`.
- **Campos**: `id`, `nombreUsuario`, `contrasena`, `saldo`, `fechaCreacion`.
- **Reglas**: `nombreUsuario` y `contrasena` son obligatorios; `saldo` no puede ser negativo; las operaciones de debito deben rechazar saldo insuficiente.
- **Persistencia**: identidad generada, columnas no nulas para datos obligatorios y precision definida para dinero.
- **Relaciones**: uno a muchos con `PosicionPortfolio` y `TransaccionAuditoria`, con carga controlada.

### Liga

- **Tabla**: `ligas`.
- **Campos**: `id`, `nombre`, `codigo`, `fechaCreacion`.
- **Reglas**: `nombre` y `codigo` son obligatorios y unicos; no se permite crear una liga con valores vacios.
- **Relaciones**: uno a muchos con `Equipo`.

### Equipo

- **Tabla**: `equipos`.
- **Campos**: `id`, `nombre`, `codigo`, `ligaId`.
- **Reglas**: `nombre` es obligatorio; `liga` es obligatoria; el codigo identifica al equipo dentro del dominio.
- **Relaciones**: muchos a uno con `Liga` y uno a muchos con `Jugador`.

### Jugador

- **Tabla**: `jugadores`.
- **Campos**: `id`, `nombre`, `posicion`, `equipoId`, `activo`.
- **Reglas**: `nombre`, `posicion` y `equipo` son obligatorios; un jugador debe estar activo para admitir nuevas operaciones de portfolio.
- **Relaciones**: muchos a uno con `Equipo`, uno a muchos con `CotizacionJugador` y uno a muchos con `PosicionPortfolio`.

### CotizacionJugador

- **Tabla**: `cotizaciones_jugador`.
- **Campos**: `id`, `jugadorId`, `valor`, `tokensDisponibles`, `fechaCotizacion`.
- **Reglas**: `valor` debe ser positivo; `tokensDisponibles` no puede ser negativo; una cotizacion nueva conserva la trazabilidad temporal del jugador.
- **Relaciones**: muchos a uno con `Jugador`.

### PosicionPortfolio

- **Tabla**: `posiciones_portfolio`.
- **Campos**: `id`, `usuarioId`, `jugadorId`, `cantidadTokens`, `precioCompra`, `fechaAdquisicion`, `fechaActualizacion`.
- **Reglas**: usuario y jugador obligatorios; `cantidadTokens` y `precioCompra` positivos; vender o reducir tokens no puede superar la cantidad disponible; las operaciones que requieran saldo deben rechazar saldo insuficiente.
- **Relaciones**: muchos a uno con `Usuario` y muchos a uno con `Jugador`.
- **Restriccion**: unicidad de la posicion abierta por usuario y jugador, si el caso de uso de portfolio la requiere.

### TransaccionAuditoria

- **Tabla**: `transacciones_auditoria`.
- **Campos**: `id`, `usuarioId`, `tipoOperacion`, `detalle`, `fechaHora`, `estadoAnterior`, `estadoPosterior`.
- **Reglas**: autor, detalle, timestamp y estados son obligatorios; una vez creada, la transaccion es inmutable; debe registrar toda operacion financiera.
- **Relaciones**: muchos a uno con `Usuario`; puede guardar referencia a `PosicionPortfolio` cuando la auditoria corresponda a una posicion.

## Relaciones y restricciones

```text
Liga 1 ---- N Equipo 1 ---- N Jugador 1 ---- N CotizacionJugador
Usuario 1 ---- N PosicionPortfolio N ---- 1 Jugador
Usuario 1 ---- N TransaccionAuditoria
PosicionPortfolio 1 ---- N TransaccionAuditoria (opcional)
```

- Las claves foraneas deben impedir referencias a registros inexistentes.
- Las columnas monetarias deben usar precision y escala explicitas.
- Los timestamps deben generarse en la creacion de la entidad y no depender de la capa controller.
- Las asociaciones no deben formar `toString`, `equals` ni `hashCode` recursivos.
- La configuracion JPA/Hibernate debe crear o actualizar tablas, restricciones, indices y claves foraneas de forma compatible con estas relaciones.

## Repositorios

Cada repositorio tendra un contrato en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/` y una implementacion o adaptador en `.../impl/`.

- `RepositorioUsuario`: busqueda por nombre de usuario y consultas paginadas.
- `RepositorioLiga`: busqueda por codigo y listado paginado.
- `RepositorioEquipo`: listado paginado por liga.
- `RepositorioJugador`: busqueda y listado paginado por equipo, liga o estado.
- `RepositorioCotizacionJugador`: historial paginado por jugador y ultima cotizacion.
- `RepositorioPosicionPortfolio`: posiciones paginadas por usuario.
- `RepositorioTransaccionAuditoria`: auditoria paginada por usuario y rango temporal.

Toda consulta multiple debe retornar `Page<T>` y recibir `Pageable`. El service construira `PageRequest.of(page, 12)` antes de invocar cada contrato.
