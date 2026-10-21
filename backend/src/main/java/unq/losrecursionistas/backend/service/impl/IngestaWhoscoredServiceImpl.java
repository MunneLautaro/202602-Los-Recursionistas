package unq.losrecursionistas.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;
import unq.losrecursionistas.backend.model.CasoVinculacion;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.model.EstadisticaTemporada;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCasoVinculacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaPartido;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaTemporada;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;
import unq.losrecursionistas.backend.service.interfaces.ResultadoIngesta;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class IngestaWhoscoredServiceImpl implements IngestaWhoscoredService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IngestaWhoscoredServiceImpl.class);

    private final RepositorioEjecucionIngesta ejecuciones;
    private final RepositorioJugador jugadores;
    private final RepositorioCasoVinculacion casos;
    private final RepositorioEstadisticaPartido estadisticas;
    private final MatchingWhoscoredService matching;
    private final ScrapingPort scraping;
    private final ScrapingConfiguration configuracion;
    private final RepositorioLiga ligas;
    private final RepositorioEquipo equipos;
    private final RepositorioEstadisticaTemporada estadisticasTemporada;

    @Autowired
    public IngestaWhoscoredServiceImpl(RepositorioEjecucionIngesta ejecuciones,
                                       RepositorioJugador jugadores,
                                       RepositorioCasoVinculacion casos,
                                       RepositorioEstadisticaPartido estadisticas,
                                       MatchingWhoscoredService matching,
                                       ScrapingPort scraping,
                                       ScrapingConfiguration configuracion,
                                       @Autowired(required = false) RepositorioLiga ligas,
                                       @Autowired(required = false) RepositorioEquipo equipos,
                                       @Autowired(required = false) RepositorioEstadisticaTemporada estadisticasTemporada) {
        this.ejecuciones = ejecuciones;
        this.jugadores = jugadores;
        this.casos = casos;
        this.estadisticas = estadisticas;
        this.matching = matching;
        this.scraping = scraping;
        this.configuracion = configuracion;
        this.ligas = ligas;
        this.equipos = equipos;
        this.estadisticasTemporada = estadisticasTemporada;
    }

    public IngestaWhoscoredServiceImpl(RepositorioEjecucionIngesta ejecuciones,
                                       RepositorioJugador jugadores,
                                       RepositorioCasoVinculacion casos,
                                       RepositorioEstadisticaPartido estadisticas,
                                       MatchingWhoscoredService matching,
                                       ScrapingPort scraping,
                                       ScrapingConfiguration configuracion) {
        this(ejecuciones, jugadores, casos, estadisticas, matching, scraping, configuracion, null, null, null);
    }

    /**
     * Constructor de compatibilidad para escenarios unitarios de sólo vinculación.
     */
    public IngestaWhoscoredServiceImpl(RepositorioEjecucionIngesta ejecuciones,
                                       RepositorioJugador jugadores,
                                       RepositorioCasoVinculacion casos,
                                       MatchingWhoscoredService matching,
                                       ScrapingPort scraping) {
        this(ejecuciones, jugadores, casos, null, matching, scraping, null, null, null, null);
    }

    public IngestaWhoscoredServiceImpl(RepositorioEjecucionIngesta ejecuciones,
                                       RepositorioJugador jugadores, RepositorioCasoVinculacion casos,
                                       RepositorioEstadisticaPartido estadisticas, MatchingWhoscoredService matching,
                                       ScrapingPort scraping, ScrapingConfiguration configuracion,
                                       RepositorioLiga ligas, RepositorioEquipo equipos) {
        this(ejecuciones, jugadores, casos, estadisticas, matching, scraping, configuracion,
                ligas, equipos, null);
    }

    @Override
    public ResultadoIngesta ejecutarLinking() {
        EjecucionIngesta ejecucion = crearEjecucion(EjecucionIngesta.Alcance.COMPLETA);
        ejecutarFaseVinculacion(EjecucionIngesta.Alcance.COMPLETA, null, ejecucion);
        finalizar(ejecucion);
        return resultado(ejecucion);
    }

    @Override
    public ResultadoIngesta ejecutarIngesta() {
        EjecucionIngesta ejecucion = crearEjecucion(EjecucionIngesta.Alcance.COMPLETA);
        for (Jugador jugador : seleccionarJugadores(EjecucionIngesta.Alcance.COMPLETA, null)) {
            procesarJugadorTemporada(jugador, ejecucion);
        }
        finalizar(ejecucion);
        return resultado(ejecucion);
    }

    @Override
    public ResultadoIngesta ingestarEstadisticasJugador(Long jugadorId) {
        Jugador jugador = jugadores.obtenerJugadorPorId(jugadorId);
        if (jugador.getIdWhoscored() == null) {
            throw new IllegalStateException("El jugador " + jugadorId + " no tiene idWhoscored vinculado");
        }
        EjecucionIngesta ejecucion = crearEjecucion(EjecucionIngesta.Alcance.JUGADOR);
        procesarJugadorTemporada(jugador, ejecucion);
        finalizar(ejecucion);
        return resultado(ejecucion);
    }

    private EjecucionIngesta crearEjecucion(EjecucionIngesta.Alcance alcance) {
        EjecucionIngesta ejecucion = EjecucionIngesta.builder().estado(EjecucionIngesta.Estado.ACTIVA)
                .alcance(alcance).temporadaInicio(configuracion.getTemporadaInicio())
                .temporadaFin(configuracion.getTemporadaFin()).inicio(OffsetDateTime.now()).build();
        return ejecuciones.guardar(ejecucion);
    }

    private ResultadoIngesta resultado(EjecucionIngesta ejecucion) {
        return new ResultadoIngesta(ejecucion.getJugadoresProcesados(), ejecucion.getPartidosInsertados(),
                ejecucion.getPartidosActualizados(), ejecucion.getErrores());
    }

    private void procesarJugadorTemporada(Jugador jugador, EjecucionIngesta ejecucion) {
        ejecucion.setJugadoresProcesados(ejecucion.getJugadoresProcesados() + 1);
        long inicio = System.nanoTime();
        try {
            List<AparicionWhoscoredDto> apariciones = scraping.obtenerApariciones(jugador.getIdWhoscored());
            if (apariciones.isEmpty()) {
                String slug = jugador.getSlug();
                if (slug == null || slug.isBlank()) {
                    slug = MatchingWhoscoredService.normalizar(jugador.getNombre()).replace(' ', '-');
                } else {
                    slug = slug.replaceFirst("^.*/players/\\d+/(?:show/|matchstatistics/)?", "")
                            .replaceAll("[?#].*$", "");
                }
                apariciones = scraping.obtenerAparicionesTemporada(jugador.getIdWhoscored(), slug);
            }
            if (apariciones.isEmpty()) {
                LOGGER.warn("WhoScored no devolvió apariciones para el jugador {} (idWhoscored: {})",
                        jugador.getId(), jugador.getIdWhoscored());
                ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
                return;
            }
            int descartadas = 0;
            for (AparicionWhoscoredDto aparicion : apariciones) {
                if (!esPersistible(aparicion)) {
                    descartadas++;
                    continue;
                }
                if (estadisticas != null) {
                    boolean existente = estadisticas.buscarPorJugadorYPartido(
                            jugador.getId(), aparicion.partidoWhoscoredId()) != null;
                    estadisticas.upsert(construirEstadistica(jugador, aparicion, ejecucion));
                    if (existente) ejecucion.setPartidosActualizados(ejecucion.getPartidosActualizados() + 1);
                    else ejecucion.setPartidosInsertados(ejecucion.getPartidosInsertados() + 1);
                }
            }
            if (descartadas > 0) {
                ejecucion.setAnomalias(ejecucion.getAnomalias() + descartadas);
                LOGGER.warn("Se descartaron {} apariciones no persistibles del jugador {}",
                        descartadas, jugador.getId());
            }
            recalcularTemporada(jugador);
        } catch (RuntimeException ex) {
            ejecucion.setErrores(ejecucion.getErrores() + 1);
            LOGGER.warn("Error al procesar estadísticas del jugador {}: {}", jugador.getId(), ex.getMessage());
        } finally {
            LOGGER.info("Scraping de estadísticas del jugador {} finalizado en {} ms",
                    jugador.getId(), (System.nanoTime() - inicio) / 1_000_000);
        }
    }

    private void recalcularTemporada(Jugador jugador) {
        if (estadisticasTemporada == null) return;
        List<EstadisticaPartido> partidos = estadisticas.buscarPorJugadorYTemporada(
                jugador.getId(), configuracion.getTemporadaInicio());
        Map<String, List<EstadisticaPartido>> grupos = partidos.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getTorneo() + "|" + p.getTorneoWhoscoredId()));
        List<EstadisticaTemporada> agregadas = grupos.values().stream().map(this::agregar).toList();
        estadisticasTemporada.reemplazarPorJugadorYTemporada(jugador.getId(),
                configuracion.getTemporadaInicio(), agregadas);
    }

    private EstadisticaTemporada agregar(List<EstadisticaPartido> partidos) {
        EstadisticaPartido primero = partidos.get(0);
        return EstadisticaTemporada.builder().jugador(primero.getJugador()).torneo(primero.getTorneo())
                .torneoWhoscoredId(primero.getTorneoWhoscoredId())
                .temporadaInicio(configuracion.getTemporadaInicio()).temporadaFin(configuracion.getTemporadaFin())
                .ultimaActualizacion(java.time.LocalDateTime.now()).apps(partidos.size())
                .minutosTotales(suma(partidos, EstadisticaPartido::getMinutos))
                .goles(suma(partidos, EstadisticaPartido::getGoles)).asistencias(suma(partidos, EstadisticaPartido::getAsistencias))
                .tirosAlArco(suma(partidos, EstadisticaPartido::getTirosAlArco)).pasesClave(suma(partidos, EstadisticaPartido::getPasesClave))
                .regates(suma(partidos, EstadisticaPartido::getRegates)).tackles(suma(partidos, EstadisticaPartido::getTackles))
                .intercepciones(suma(partidos, EstadisticaPartido::getIntercepciones)).amarillas(suma(partidos, EstadisticaPartido::getAmarillas))
                .rojas(suma(partidos, EstadisticaPartido::getRojas)).ratingPromedio(promedio(partidos, EstadisticaPartido::getRating))
                .precisionPasesPromedio(promedio(partidos, EstadisticaPartido::getPrecisionPases)).build();
    }

    private static Integer suma(List<EstadisticaPartido> partidos,
                                java.util.function.Function<EstadisticaPartido, Integer> getter) {
        return partidos.stream().map(getter).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private static BigDecimal promedio(List<EstadisticaPartido> partidos,
                                       java.util.function.Function<EstadisticaPartido, BigDecimal> getter) {
        List<BigDecimal> valores = partidos.stream().map(getter).filter(java.util.Objects::nonNull).toList();
        if (valores.isEmpty()) return null;
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(valores.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public synchronized EjecucionIngesta iniciar(EjecucionIngesta.Alcance alcance, Long alcanceId) {
        if (ejecuciones.existeActiva()) {
            return guardarRechazo(alcance, alcanceId);
        }
        EjecucionIngesta ejecucion = EjecucionIngesta.builder()
                .estado(EjecucionIngesta.Estado.ACTIVA)
                .alcance(alcance)
                .temporadaInicio(configuracion == null ? null : configuracion.getTemporadaInicio())
                .temporadaFin(configuracion == null ? null : configuracion.getTemporadaFin())
                .inicio(OffsetDateTime.now())
                .build();
        ejecucion.setIdAlcance(alcanceId);
        EjecucionIngesta reservada = ejecuciones.reservar(ejecucion);
        if (reservada != null) {
            ejecucion = reservada;
        } else {
            ejecucion = ejecuciones.guardar(ejecucion);
        }
        if (ejecucion.getEstado() == EjecucionIngesta.Estado.RECHAZADA) {
            return ejecucion;
        }
        ejecutar(ejecucion);
        return ejecucion;
    }

    @Override
    public void ejecutar(EjecucionIngesta ejecucion) {
        EjecucionIngesta.Alcance alcance = ejecucion.getAlcance();
        Long alcanceId = ejecucion.getIdAlcance();

        // FASE 1: Descubrimiento y vinculación jerárquica de ligas, equipos y jugadores
        try {
            ejecutarFaseVinculacion(alcance, alcanceId, ejecucion);
        } catch (Exception ex) {
            LOGGER.error("Error durante la fase de vinculación de WhoScored: {}", ex.getMessage(), ex);
            ejecucion.setErrores(ejecucion.getErrores() + 1);
            ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
        }

        // FASE 2: Carga de estadísticas de partidos
        try {
            List<Jugador> objetivo = seleccionarJugadores(alcance, alcanceId);
            for (Jugador jugador : objetivo) {
                procesarJugador(jugador, ejecucion);
            }
        } catch (Exception ex) {
            LOGGER.error("Error durante la fase de estadísticas de WhoScored: {}", ex.getMessage(), ex);
            ejecucion.setErrores(ejecucion.getErrores() + 1);
            ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
        }

        finalizar(ejecucion);
    }

    private void ejecutarFaseVinculacion(EjecucionIngesta.Alcance alcance, Long alcanceId, EjecucionIngesta ejecucion) {
        if (scraping == null || matching == null) {
            return;
        }

        switch (alcance) {
            case COMPLETA -> vincularCompleta(ejecucion);
            case LIGA -> vincularPorLiga(alcanceId, ejecucion);
            case EQUIPO -> vincularPorEquipo(alcanceId, ejecucion);
            case JUGADOR -> vincularPorJugador(alcanceId, ejecucion);
        }
    }

    private void vincularCompleta(EjecucionIngesta ejecucion) {
        List<LigaWhoscoredDto> ligasDescubiertas;
        try {
            ligasDescubiertas = scraping.descubrirLigas();
        } catch (RuntimeException ex) {
            LOGGER.warn("No se pudieron descubrir ligas en WhoScored: {}", ex.getMessage());
            ejecucion.setErrores(ejecucion.getErrores() + 1);
            ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
            ligasDescubiertas = List.of();
        }

        if (ligas != null && !ligasDescubiertas.isEmpty()) {
            List<Liga> ligasLocales = ligas.obtenerLigas();
            for (LigaWhoscoredDto ligaDto : ligasDescubiertas) {
                try {
                    Liga ligaLocal = ligasLocales.stream()
                            .filter(l -> coincideLiga(l.getNombre(), ligaDto.nombre()))
                            .findFirst()
                            .orElse(null);

                    if (ligaLocal != null) {
                        if (ligaLocal.getIdWhoscored() == null || !ligaLocal.getIdWhoscored().equals(ligaDto.idWhoscored())) {
                            ligaLocal.setIdWhoscored(ligaDto.idWhoscored());
                            ligas.guardar(ligaLocal);
                        }
                        vincularEquiposDeLiga(ligaLocal, ligaDto.idWhoscored(), ejecucion);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error al procesar liga {}: {}", ligaDto.nombre(), ex.getMessage());
                    ejecucion.setErrores(ejecucion.getErrores() + 1);
                    ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
                }
            }
        } else if (equipos != null) {
            for (Equipo equipo : equipos.obtenerTodos()) {
                if (equipo.getIdWhoscored() != null) {
                    try {
                        vincularJugadores(equipo.getIdWhoscored(), ejecucion);
                    } catch (Exception ex) {
                        LOGGER.warn("Error al procesar jugadores de equipo {}: {}", equipo.getNombre(), ex.getMessage());
                        ejecucion.setErrores(ejecucion.getErrores() + 1);
                        ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
                    }
                }
            }
        }
    }

    private void vincularPorLiga(Long ligaId, EjecucionIngesta ejecucion) {
        if (ligas == null || ligaId == null) return;
        Liga liga = ligas.obtenerLigas().stream()
                .filter(l -> ligaId.equals(l.getId()))
                .findFirst()
                .orElse(null);
        if (liga != null && liga.getIdWhoscored() != null) {
            vincularEquiposDeLiga(liga, liga.getIdWhoscored(), ejecucion);
        }
    }

    private void vincularPorEquipo(Long equipoId, EjecucionIngesta ejecucion) {
        if (equipos == null || equipoId == null) return;
        Equipo equipo = equipos.obtenerTodos().stream()
                .filter(e -> equipoId.equals(e.getId()))
                .findFirst()
                .orElse(null);
        if (equipo != null && equipo.getIdWhoscored() != null) {
            try {
                vincularJugadores(equipo.getIdWhoscored(), ejecucion);
            } catch (Exception ex) {
                ejecucion.setErrores(ejecucion.getErrores() + 1);
                ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
            }
        }
    }

    private void vincularPorJugador(Long jugadorId, EjecucionIngesta ejecucion) {
        if (jugadorId == null) return;
        Jugador jugador = jugadores.obtenerJugadorPorId(jugadorId);
        if (jugador != null && jugador.getIdWhoscored() == null && jugador.getEquipo() != null && jugador.getEquipo().getIdWhoscored() != null) {
            try {
                vincularJugadores(jugador.getEquipo().getIdWhoscored(), ejecucion);
            } catch (Exception ex) {
                ejecucion.setErrores(ejecucion.getErrores() + 1);
                ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
            }
        }
    }

    private void vincularEquiposDeLiga(Liga ligaLocal, Long ligaWhoscoredId, EjecucionIngesta ejecucion) {
        if (equipos == null) return;
        List<EquipoWhoscoredDto> equiposDescubiertos;
        try {
            equiposDescubiertos = scraping.descubrirEquipos(ligaWhoscoredId);
        } catch (Exception ex) {
            LOGGER.warn("No se pudieron descubrir equipos para liga {}: {}", ligaWhoscoredId, ex.getMessage());
            ejecucion.setErrores(ejecucion.getErrores() + 1);
            ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
            return;
        }

        List<Equipo> equiposLocales = equipos.obtenerEquiposDeLiga(ligaLocal.getId());
        for (EquipoWhoscoredDto eqDto : equiposDescubiertos) {
            try {
                Equipo eqLocal = equiposLocales.stream()
                        .filter(e -> coincideEquipo(e.getNombre(), eqDto.nombre()))
                        .findFirst()
                        .orElse(null);

                if (eqLocal != null) {
                    if (eqLocal.getIdWhoscored() == null || !eqLocal.getIdWhoscored().equals(eqDto.idWhoscored())) {
                        // Chequeo estricto de unicidad antes de persistir
                        Equipo existente = equipos.buscarPorIdWhoscored(eqDto.idWhoscored());
                        if (existente != null && !existente.getId().equals(eqLocal.getId())) {
                            LOGGER.warn("El ID WhoScored {} ya está asignado al equipo '{}' (id: {}). Se omite asignación al equipo '{}' (id: {})",
                                    eqDto.idWhoscored(), existente.getNombre(), existente.getId(), eqLocal.getNombre(), eqLocal.getId());
                            ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
                        } else {
                            eqLocal.setIdWhoscored(eqDto.idWhoscored());
                            equipos.guardar(eqLocal);
                        }
                    }
                    vincularJugadores(eqDto.idWhoscored(), ejecucion);
                }
            } catch (Exception ex) {
                LOGGER.error("Error al procesar equipo descubierto '{}' (id: {}): {}", eqDto.nombre(), eqDto.idWhoscored(), ex.getMessage());
                ejecucion.setErrores(ejecucion.getErrores() + 1);
                ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
            }
        }
    }

    private List<Jugador> seleccionarJugadores(EjecucionIngesta.Alcance alcance, Long alcanceId) {
        List<Jugador> activos = jugadores.obtenerJugadoresActivos();
        if (alcanceId == null || alcance == EjecucionIngesta.Alcance.COMPLETA) {
            return activos.stream().filter(jugador -> jugador.getIdWhoscored() != null).toList();
        }
        Predicate<Jugador> filtro = switch (alcance) {
            case JUGADOR -> jugador -> alcanceId.equals(jugador.getId());
            case EQUIPO -> jugador -> jugador.getEquipo() != null
                    && alcanceId.equals(jugador.getEquipo().getId());
            case LIGA -> jugador -> jugador.getEquipo() != null
                    && jugador.getEquipo().getLiga() != null
                    && alcanceId.equals(jugador.getEquipo().getLiga().getId());
            case COMPLETA -> jugador -> true;
        };
        return activos.stream().filter(filtro).filter(jugador -> jugador.getIdWhoscored() != null).toList();
    }

    private void procesarJugador(Jugador jugador, EjecucionIngesta ejecucion) {
        procesarJugadorTemporada(jugador, ejecucion);
    }

    private boolean esPersistible(AparicionWhoscoredDto aparicion) {
        LocalDate inicio = configuracion == null ? null : configuracion.getTemporadaInicio();
        LocalDate fin = configuracion == null ? null : configuracion.getTemporadaFin();
        return aparicion != null
                && aparicion.partidoWhoscoredId() != null
                && aparicion.fechaPartido() != null
                && aparicion.minutos() != null
                && aparicion.minutos() > 0
                && (inicio == null || !aparicion.fechaPartido().isBefore(inicio))
                && (fin == null || !aparicion.fechaPartido().isAfter(fin));
    }

    private EstadisticaPartido construirEstadistica(Jugador jugador, AparicionWhoscoredDto aparicion,
                                                     EjecucionIngesta ejecucion) {
        Map<String, Object> raw = aparicion.statsCrudas();
        return EstadisticaPartido.builder()
                .jugador(jugador)
                .partidoWhoscoredId(aparicion.partidoWhoscoredId())
                .fechaPartido(aparicion.fechaPartido())
                .torneo(aparicion.torneo())
                .torneoWhoscoredId(aparicion.torneoWhoscoredId())
                .temporadaInicio(ejecucion.getTemporadaInicio())
                .temporadaFin(ejecucion.getTemporadaFin())
                .equipoWhoscoredId(aparicion.equipoWhoscoredId())
                .equipoNombre(aparicion.equipoNombre())
                .rivalWhoscoredId(aparicion.rivalWhoscoredId())
                .rivalNombre(aparicion.rivalNombre())
                .localVisitante(aparicion.localVisitante())
                .minutos(aparicion.minutos())
                .rating(aparicion.rating())
                .goles(decimalOrInteger(raw, "goals", "goles"))
                .asistencias(integer(raw, "assists", "asistencias"))
                .tiros(integer(raw, "shots", "tiros"))
                .tirosAlArco(integer(raw, "shotsOnTarget", "tirosAlArco"))
                .pases(integer(raw, "passes", "pases"))
                .pasesClave(integer(raw, "keyPasses", "pasesClave"))
                .regates(integer(raw, "dribbles", "regates"))
                .tackles(integer(raw, "tackles"))
                .intercepciones(integer(raw, "interceptions", "intercepciones"))
                .faltasCometidas(integer(raw, "fouls", "foulsCommitted", "faltasCometidas"))
                .amarillas(integer(raw, "yellowCards", "amarillas"))
                .rojas(integer(raw, "redCards", "rojas"))
                .precisionPases(decimal(raw, "passSuccess", "passAccuracy", "precisionPases"))
                .statsCrudas(raw)
                .build();
    }

    private static Integer integer(Map<String, Object> raw, String... keys) {
        Object value = first(raw, keys);
        if (value instanceof Number number) return number.intValue();
        try { return value == null ? null : Integer.valueOf(value.toString()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static Integer decimalOrInteger(Map<String, Object> raw, String... keys) {
        return integer(raw, keys);
    }

    private static BigDecimal decimal(Map<String, Object> raw, String... keys) {
        Object value = first(raw, keys);
        try { return value == null ? null : new BigDecimal(value.toString()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static Object first(Map<String, Object> raw, String... keys) {
        for (String key : keys) {
            if (raw.containsKey(key)) return raw.get(key);
        }
        return null;
    }

    private EjecucionIngesta guardarRechazo(EjecucionIngesta.Alcance alcance, Long alcanceId) {
        return ejecuciones.guardar(EjecucionIngesta.builder()
                .estado(EjecucionIngesta.Estado.RECHAZADA)
                .alcance(alcance)
                .ligaId(alcance == EjecucionIngesta.Alcance.LIGA ? alcanceId : null)
                .equipoId(alcance == EjecucionIngesta.Alcance.EQUIPO ? alcanceId : null)
                .jugadorId(alcance == EjecucionIngesta.Alcance.JUGADOR ? alcanceId : null)
                .temporadaInicio(configuracion == null ? null : configuracion.getTemporadaInicio())
                .temporadaFin(configuracion == null ? null : configuracion.getTemporadaFin())
                .inicio(OffsetDateTime.now())
                .fin(OffsetDateTime.now())
                .motivoRechazo("Ya existe una ejecucion activa")
                .build());
    }

    private void finalizar(EjecucionIngesta ejecucion) {
        ejecucion.setEstado(EjecucionIngesta.Estado.FINALIZADA);
        ejecucion.setFin(OffsetDateTime.now());
        ejecuciones.guardar(ejecucion);
    }

    public void vincularJugadores(Long equipoWhoscoredId, EjecucionIngesta ejecucion) {
        List<JugadorWhoscoredDto> descubiertos = scraping.descubrirJugadores(equipoWhoscoredId);
        for (JugadorWhoscoredDto descubierto : descubiertos) {
            try {
                ejecucion.setJugadoresProcesados(ejecucion.getJugadoresProcesados() + 1);
                MatchingWhoscoredService.ResultadoMatching resultado = matching.vincular(descubierto);
                if (resultado.estado() == MatchingWhoscoredService.Estado.VINCULADO) {
                    jugadores.guardar(resultado.jugador());
                    ejecucion.setJugadoresVinculados(ejecucion.getJugadoresVinculados() + 1);
                } else {
                    guardarCaso(descubierto, resultado, ejecucion);
                    ejecucion.setJugadoresNoVinculados(ejecucion.getJugadoresNoVinculados() + 1);
                }
            } catch (Exception ex) {
                LOGGER.warn("Error al vincular jugador {} (id: {}): {}", descubierto.nombre(), descubierto.idWhoscored(), ex.getMessage());
                ejecucion.setErrores(ejecucion.getErrores() + 1);
                ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
            }
        }
    }

    private void guardarCaso(JugadorWhoscoredDto descubierto,
                             MatchingWhoscoredService.ResultadoMatching resultado,
                             EjecucionIngesta ejecucion) {
        if (casos == null) {
            return;
        }
        try {
            CasoVinculacion.Motivo motivo = resultado.estado() == MatchingWhoscoredService.Estado.AMBIGUO
                    ? CasoVinculacion.Motivo.AMBIGUO : CasoVinculacion.Motivo.NO_ENCONTRADO;
            casos.guardar(CasoVinculacion.builder()
                    .idWhoscored(descubierto.idWhoscored())
                    .nombre(descubierto.nombre())
                    .slugUrl(descubierto.slug())
                    .fechaNacimiento(descubierto.fechaNacimiento())
                    .nacionalidad(descubierto.nacionalidad())
                    .equipoWhoscoredId(descubierto.equipoWhoscoredId())
                    .motivo(motivo)
                    .estado(CasoVinculacion.Estado.PENDIENTE)
                    .candidatos(Map.of("jugadores", resultado.candidatos().stream().map(Jugador::getId).toList()))
                    .ejecucion(ejecucion)
                    .creadoEn(OffsetDateTime.now())
                    .detalleOperativo("Jugador descubierto sin vinculacion automatica")
                    .build());
        } catch (Exception ex) {
            LOGGER.warn("Error al guardar caso de vinculación para jugador {}: {}", descubierto.nombre(), ex.getMessage());
        }
    }

    private static boolean coincideLiga(String local, String descubierta) {
        if (local == null || descubierta == null) return false;
        String normLocal = normalizar(local);
        String normDesc = normalizar(descubierta);
        if (normLocal.equals(normDesc) || normDesc.contains(normLocal) || normLocal.contains(normDesc)) {
            return true;
        }
        boolean esLocalEspana = normLocal.contains("primera division") || normLocal.contains("la liga") || normLocal.contains("laliga");
        boolean esDescEspana = normDesc.contains("la liga") || normDesc.contains("laliga") || normDesc.contains("primera division");
        return esLocalEspana && esDescEspana;
    }

    private static boolean coincideEquipo(String local, String descubierto) {
        if (local == null || descubierto == null) return false;
        String normLocal = normalizar(local);
        String normDesc = normalizar(descubierto);
        if (normLocal.equals(normDesc)) return true;

        String cleanLocal = limpiarNombreEquipo(normLocal);
        String cleanDesc = limpiarNombreEquipo(normDesc);
        if (cleanLocal.equals(cleanDesc)) return true;

        // Desambiguaciones críticas para clubes que comparten ciudad o nombre
        if (sonEquiposEnConflicto(cleanLocal, cleanDesc)) {
            return false;
        }

        if (cleanLocal.length() >= 5 && cleanDesc.length() >= 5) {
            if (cleanLocal.startsWith(cleanDesc) || cleanDesc.startsWith(cleanLocal)) {
                return true;
            }
        }
        return false;
    }

    private static String limpiarNombreEquipo(String nombre) {
        return nombre.replaceAll("\\b(fc|cf|afc|ssc|ac|as|rb|vfl|bvb|cd|ud|ca|de|del|la|el)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean sonEquiposEnConflicto(String a, String b) {
        // "inter" / "internazionale" vs "milan" / "ac milan"
        boolean aEsInter = a.contains("inter");
        boolean bEsInter = b.contains("inter");
        boolean aEsMilan = a.contains("milan") && !aEsInter;
        boolean bEsMilan = b.contains("milan") && !bEsInter;
        if ((aEsInter && bEsMilan) || (bEsInter && aEsMilan)) return true;

        // "manchester city" vs "manchester united"
        boolean aCity = a.contains("city");
        boolean bCity = b.contains("city");
        boolean aUnited = a.contains("united");
        boolean bUnited = b.contains("united");
        if ((aCity && bUnited) || (bCity && aUnited)) return true;

        // "atletico" vs "real madrid"
        boolean aAtletico = a.contains("atletico");
        boolean bAtletico = b.contains("atletico");
        boolean aReal = a.contains("real");
        boolean bReal = b.contains("real");
        if ((aAtletico && bReal) || (bAtletico && aReal)) return true;

        return false;
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
