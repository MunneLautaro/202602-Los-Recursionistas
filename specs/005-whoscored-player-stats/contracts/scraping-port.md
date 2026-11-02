# Contrato `ScrapingPort`

El servicio de aplicacion depende unicamente de
`unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort`. Sus retornos son
DTOs inmutables del subpaquete `adapter/whoscored/dto`; no expone Playwright ni
entidades JPA.

## Operaciones

- Descubrir equipos y ligas con identificador WhoScored, nombre, slug y
  temporada.
- Descubrir jugadores de un equipo con `idWhoscored`, nombre, slug/URL, fecha de
  nacimiento, nacionalidad, equipo y posiciones.
- Obtener las apariciones del jugador para la temporada activa, agrupadas por
  `partidoWhoscoredId`.
- Obtener o combinar las cuatro categorias `Summary`, `Defensive`,
  `Offensive` y `Passing` en una aparicion, con metricas tipadas y el mapa
  `statsCrudas`.

## Invariantes del contrato

- El ID numerico de jugador y partido es obligatorio cuando la fuente lo
  informa; un slug percent-encoded no puede reemplazarlo.
- Solo una aparicion con `minutos > 0` cruza al servicio para persistencia;
  banco, cero minutos y ausencia de registro se descartan.
- Campos no disponibles son `null`, nunca cero inventado.
- Torneo, fecha, equipo/rival y local/visitante se conservan tal como los
  informa la fuente, sujetos a validacion del parser.
- El adapter no propaga excepciones de Playwright: las transforma en
  `ScrapingException` con causa, URL, fase y mensaje operativo.
- El contrato no garantiza estabilidad de URLs ni endpoints internos de
  WhoScored; los patrones y selectores son detalles internos centralizados.
