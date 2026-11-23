# Investigacion: gestion de estrategia de cotizacion

## Relevamiento de `CotizacionServiceImpl`

**Hallazgo:** `CotizacionServiceImpl` recibe por constructor `@Value("${cotizacion.estrategia.activa:PERFORMANCE_GENERAL}") String estrategiaActiva`. Ese valor se conserva como campo fijo y `obtenerEstrategiaActiva()` lo busca en el mapa de estrategias.

**Impacto:** modificar la configuracion persistida no cambia los recálculos posteriores sin reiniciar. La implementacion tambien guarda `estrategiaActiva` como codigo historico al crear cada `Cotizacion`.

**Decision:** eliminar el campo fijo derivado de `@Value`; inyectar solo el mapa de estrategias y un repositorio de configuracion activa. En cada ejecucion de recálculo se resolvera primero la fila persistida; si no existe se usara el `@Value` como fallback local. El codigo `estrategiaUsada` obtenido en el inicio del recálculo se conservara para todo ese proceso.

**Alternativas consideradas:** mantener el bean fijo y volver a cargarlo desde un servicio; se descartó porque la resolución seguiria dependiendo de inyeccion y no cumpliria el nivel BD/fallback. Consultar la configuracion dentro de cada jugador; se descartó por fragmentar transacciones y permitir combinacion de estrategias durante un mismo job.

## Relevamiento de `CotizacionScheduler`

**Hallazgo:** el scheduler llama directamente a `servicio.recalcular()` y el servicio contiene un `AtomicBoolean recalculoActivo`, pero el scheduler no reserva ni libera esa marca y el recálculo manual async tampoco la usa al iniciar el scheduler.

**Decision:** compartir el mismo `CotizacionService.reservarRecalculo()` para manual y scheduler. Manual/scheduler reservan al inicio y liberan al finalizar mediante `finally`. `EstrategiaAdminService` consulta `CotizacionService.estaRecalculoActivo()` o reutiliza un mecanismo de consulta equivalente antes de persistir.

**Rationale:** la alternativa elegida se limita a una JVM y coordina los dos caminos existentes sin crear una entidad extra. Es consistente con el alcance actual y la decision del equipo. En despliegues multi-instancia no ofrece exclusion global; quedará como limitacion documentada del MVP.

**Alternativas consideradas:** flag persistente en BD; coordina instancias y permite reinicios, pero agrega estado de ejecucion y recuperacion de estados huérfanos. Lock pesimista sobre configuracion; evita cambios solo si el recálculo adquiere el mismo lock, pero acoplaría el ciclo completo de job a una fila. Ambos exceden el MVP aunque son validos para una evolucion futura.

## Relevamiento de `Cotizacion`

**Hallazgo:** `Cotizacion.estrategiaUsada` ya es una columna `String` nullable=false, append-only mediante `updatable=false`, y se persiste en `cotizaciones`. No requiere modificar su estructura para guardar el codigo interno.

**Decision:** preservar el campo y su inmutabilidad. Solo los DTOs REST mapearan el codigo a nombre legible.

## Endpoints existentes de historial, grafico y detalle

**Hallazgo:** existen tres superficies de cotizacion:

- `GET /players/{id}/quotes`: devuelve `Page<CotizacionResponse>` y actualmente expone `estrategiaUsada` con el codigo.
- `GET /players/{id}/quotes/series`: devuelve `List<PuntoCotizacionResponse>` y actualmente expone `estrategiaUsada` con el codigo.
- La tabla/detalle de la UI consume `Quote.estrategiaUsada` dentro de `JugadorDetailModal`; no hay DTO backend separado de detalle de cotizacion.
- `GET /players/ranking` no representa la estrategia usada y no es una superficie incluida en el alcance historico.

**Decision:** mantener los nombres de campos de respuesta, pero convertir `CotizacionResponse.estrategiaUsada` y `PuntoCotizacionResponse.estrategiaUsada` a nombre legible en el controller. La vista no debera contener lookup de codigos: mostrará exactamente el valor recibido. `JugadorDetailModal`, su tabla y `QuoteHistoryChart` seguiran consumiendo el mismo DTO ya que el grafico usa la serie y el detalle usa la pagina.

## Panel administrativo actual

**Hallazgo:** `AdminPage` ya contiene acciones administrativas y feedback de carga/error. `features/admin/api/adminApi.ts` centraliza llamadas con `fetchClient`.

**Decision:** agregar a `adminApi.ts` las operaciones GET/PUT de estrategia, un hook o estado local dentro de `AdminPage`, y un componente dedicado si el bloque crece. Mantener estilos en archivos `.styles.ts`. La confirmacion debera advertir que no se ejecutara recálculo.

## Persistencia elegida

**Decision:** dos entidades separadas:

- `ConfiguracionEstrategia`: un registro singleton con la estrategia activa persistida.
- `CambioEstrategia`: registro append-only de auditoria.

**Rationale:** conserva una fuente primaria simple para consultar el activo, separa el estado de su historial y permite que el ultimo cambio sea consultado sin paginacion. La implementacion debe inicializar o buscar el identificador fijo `1`.

**Alternativas consideradas:** usar solo auditoría como activo omite una entidad y una actualizacion, pero requiere ordenar el ultimo registro y volver una consulta historica en fuente del estado; dificulta idempotencia y no modela la ausencia inicial. Solo configuracion mas auditoria en otra entidad es mas simple que politicas generales de versionado.

## Coordinacion elegida

**Decision:** `AtomicBoolean` compartido dentro de `CotizacionServiceImpl` y reserva comun a scheduler, manual sync/async y admin. Es suficiente para el despliegue en una instancia.

**Limite:** esta eleccion no coordinara instancias JVM independientes. Esta limitacion no se convierte en soporte multi-instancia sin requerimiento adicional.

## Archivos relevantes

### Nuevos backend
- `model/ConfiguracionEstrategia.java`
- `model/CambioEstrategia.java`
- `persistence/sql/ConfiguracionEstrategiaDAOSQL.java`
- `persistence/sql/CambioEstrategiaDAOSQL.java`
- `persistence/repository/interfaces/RepositorioConfiguracionEstrategia.java`
- `persistence/repository/interfaces/RepositorioCambioEstrategia.java`
- `persistence/repository/impl/RepositorioConfiguracionEstrategiaImpl.java`
- `persistence/repository/impl/RepositorioCambioEstrategiaImpl.java`
- `service/interfaces/EstrategiaAdminService.java`
- `service/impl/EstrategiaAdminServiceImpl.java`
- `controller/EstrategiaAdminController.java`
- `controller/dto/estrategia/EstrategiaDisponibleResponse.java`
- `controller/dto/estrategia/EstrategiaActivaResponse.java`
- `controller/dto/estrategia/CambiarEstrategiaRequest.java`
- `exceptions/ExcepcionEstrategiaEnRecalculo.java` o excepcion equivalente traducida a 409

### Nuevos frontend
- `features/admin/api/estrategiaApi.ts` o ampliacion de `adminApi.ts`
- `features/admin/types.ts`
- `features/admin/hooks/useEstrategiaCotizacion.ts`
- `features/admin/components/EstrategiaCotizacionPanel.tsx`
- `features/admin/components/EstrategiaCotizacionPanel.styles.ts`

### Backend existente a modificar
- `service/impl/CotizacionServiceImpl.java`
- `service/interfaces/CotizacionService.java`
- `service/impl/CotizacionAsyncWorker.java`
- `scheduler/CotizacionScheduler.java`
- `controller/CotizacionController.java`
- `controller/dto/cotizacion/CotizacionResponse.java`
- `controller/dto/cotizacion/PuntoCotizacionResponse.java`
- `configuration/SecurityConfig.java`
- `exceptions/ControladorErrores.java` solo si se agrega el handler necesario
- `src/test/java/.../service/CotizacionServiceImplTest.java`
- `application.yml` solo se conserva el fallback ya existente

### Frontend existente a modificar
- `pages/AdminPage.tsx`
- `features/admin/api/adminApi.ts`
- `features/jugadores/types.ts`: preferir renombre del campo DTO si se decide en implementacion, pero mantener compatibilidad
- `features/jugadores/components/JugadorDetailModal.tsx`: solo si requiere eliminar una muestra de codigo o normalizar texto
- `features/jugadores/components/QuoteHistoryChart.tsx`: solo si requiere ajustar el tipo/label recibido
