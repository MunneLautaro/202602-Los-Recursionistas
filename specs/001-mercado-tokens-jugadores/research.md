# Research: Mercado de Tokens de Jugadores

## Decision: Identidad y autorizacion

El usuario de una orden se obtiene del principal autenticado; el request solo contiene jugador y cantidad. Se usaran roles `USER`, `OPERATOR` y `FINANCIAL_ADMIN`. El Superusuario Financiero tendra una identidad tecnica estable por username/configuracion y el rol `FINANCIAL_ADMIN`; su bootstrap sera transaccional e idempotente.

**Rationale:** evita que un cliente suplante a otro usuario y respeta la exigencia de Spring Security. La identidad estable permite repetir bootstrap sin duplicar saldo, posicion u operacion genesis.

**Alternatives considered:** aceptar `usuarioId` en el request (rechazado por riesgo de escalamiento); proveedor de identidad externo (fuera de alcance de la spec).

## Decision: Cotizacion vigente

La aplicacion usara reloj UTC y una vigencia configurable (`quote.validity`). La orden captura la cotizacion al iniciar y vuelve a validarla dentro de la transaccion antes de liquidar. Una cotizacion vencida o reemplazada produce conflicto `409`.

**Rationale:** hace determinista el caso de carrera entre consulta y liquidacion y conserva el precio/version realmente usados.

**Alternatives considered:** usar siempre la ultima cotizacion sin vigencia (contradice la regla de cotizacion vigente); aceptar precio enviado por cliente (no confiable).

## Decision: Dinero, cantidades y formula

Los creditos se representan con `BigDecimal` a escala 2 y rounding `HALF_EVEN`; los tokens son enteros positivos. Cada estrategia produce un score reproducible en rango 0..100 y el precio se deriva de una funcion versionada y configurable, con precio inicial 1.00. La formula y pesos viven en `EstrategiaValuacion`, nunca en el controller.

Se implementan `RendimientoPonderado` y `RendimientoContextoCompetitivo`; ambas registran metricas, pesos, version, posicion y fuente en cada `Cotizacion`.

**Rationale:** evita errores de punto flotante y permite recalcular sin alterar historia. La spec exige dos estrategias, pero no fija formula, por lo que una configuracion versionada hace explicita la decision sin inventar una dependencia externa.

**Alternatives considered:** `double` (rechazado para importes); sobrescribir cotizacion previa (contradice inmutabilidad); una formula fija en codigo (impide configuracion/versionado).

## Decision: Compra, venta e inventario

La compra inicial transfiere saldo y tokens entre el usuario y el Superusuario Financiero. Una venta transfiere tokens del usuario al inventario financiero y el importe al usuario. La restriccion global es `0 <= tokens financieros + tokens de usuarios <= 100` por jugador; las transferencias se bloquean con concurrencia optimista/pesimista segun la operacion.

**Rationale:** define el destino de los tokens vendidos y conserva el maximo de 100 tokens del dominio. La operacion completa queda en una unica transaccion.

**Alternatives considered:** quemar tokens vendidos (perderia inventario negociable); vender a un usuario elegido por cliente (no existe contraparte definida en el contrato).

## Decision: Persistencia y migraciones

PostgreSQL es la fuente de verdad. Se usaran migraciones versionadas y constraints unicos para usuario, jugador, posicion usuario-jugador, cotizacion versionada y operaciones idempotentes. Los indices se justificaran por filtros y consultas del contrato.

**Rationale:** la idempotencia y atomicidad no pueden depender de una comprobacion previa en memoria.

**Alternatives considered:** H2 para integracion (no valida comportamiento real de PostgreSQL); schema generado implicitamente (dificulta reproducibilidad y auditoria).

## Decision: Fallback y cache externo

Cada adapter externo tendra timeout, error tipado y normalizacion. Spring Cache conservara la ultima respuesta valida; si no existe cache, se consultara el snapshot local. Toda respuesta indica `fuente` (`WHO_SCORED`, `FOOTBALL_DATA`, `CACHE` o `LOCAL_FALLBACK`) y la falla se registra con Correlation ID.

**Rationale:** cumple continuidad operativa sin presentar fallback como dato fresco.

**Alternatives considered:** devolver cache sin fuente (rompe trazabilidad); reintentos indefinidos (bloquea SLA).

## Decision: Recalculo y scheduler

El scheduler se configura con una expresion semanal minima y una zona UTC. Cada ejecucion tiene identificador/idempotency key, estado y auditoria. El endpoint manual devuelve `202` cuando inicia una ejecucion asincrona y `409` si existe otra incompatible.

**Rationale:** `202` representa correctamente un job controlado y permite reportar conflicto de ejecuciones.

**Alternatives considered:** `200` siempre (oculta asincronia); permitir jobs duplicados (viola idempotencia).

## Decision: Seguridad y sanitizacion

Bean Validation cubrira forma y limites; un normalizador de DTO hara trimming y rechazo de input no permitido. Spring Security aplicara autenticacion y autorizacion por endpoint. Los errores no controlados no expondran detalles internos.

**Rationale:** separa transporte, autorizacion y reglas de dominio conforme a la Constitucion.

**Alternatives considered:** sanitizar dentro del Model (mezcla concerns); confiar solo en el ORM (no cubre XSS ni mensajes).

## Decision: Rendimiento verificable

La carga de referencia inicial sera un dataset reproducible con cinco ligas, catalogo seed documentado, cuatro usuarios concurrentes y consultas repetidas de catalogo/ranking/portfolio. Se medira p95 y tasa de error en smoke/performance tests; la carga final debe ser aprobada por el equipo antes del cierre.

**Rationale:** transforma el objetivo de p95 de la spec en un check ejecutable sin inventar un volumen de negocio no definido.

**Alternatives considered:** declarar SLA sin dataset (no falsable); optimizar antes de medir.
