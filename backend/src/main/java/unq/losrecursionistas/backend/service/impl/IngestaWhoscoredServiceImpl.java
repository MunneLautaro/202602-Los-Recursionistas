package unq.losrecursionistas.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.model.CasoVinculacion;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCasoVinculacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaPartido;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class IngestaWhoscoredServiceImpl implements IngestaWhoscoredService {
    private final RepositorioEjecucionIngesta ejecuciones;
    private final RepositorioJugador jugadores;
    private final RepositorioCasoVinculacion casos;
    private final RepositorioEstadisticaPartido estadisticas;
    private final MatchingWhoscoredService matching;
    private final ScrapingPort scraping;
    private final ScrapingConfiguration configuracion;

    @Autowired
    public IngestaWhoscoredServiceImpl(RepositorioEjecucionIngesta ejecuciones,
                                       RepositorioJugador jugadores,
                                       RepositorioCasoVinculacion casos,
                                       RepositorioEstadisticaPartido estadisticas,
                                       MatchingWhoscoredService matching,
                                       ScrapingPort scraping,
                                       ScrapingConfiguration configuracion) {
        this.ejecuciones = ejecuciones;
        this.jugadores = jugadores;
        this.casos = casos;
        this.estadisticas = estadisticas;
        this.matching = matching;
        this.scraping = scraping;
        this.configuracion = configuracion;
    }

    /**
     * Compatibility constructor for the linking-only use case used by older callers.
     */
    public IngestaWhoscoredServiceImpl(RepositorioEjecucionIngesta ejecuciones,
                                       RepositorioJugador jugadores,
                                       RepositorioCasoVinculacion casos,
                                       MatchingWhoscoredService matching,
                                       ScrapingPort scraping) {
        this(ejecuciones, jugadores, casos, null, matching, scraping, null);
    }

    @Override
    @Transactional
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public synchronized EjecucionIngesta iniciar(EjecucionIngesta.Alcance alcance, Long alcanceId) {
        if (ejecuciones.existeActiva()) {
            return guardarRechazo(alcance, alcanceId);
        }

        EjecucionIngesta ejecucion = ejecuciones.guardar(EjecucionIngesta.builder()
                .estado(EjecucionIngesta.Estado.ACTIVA)
                .alcance(alcance)
                .ligaId(alcance == EjecucionIngesta.Alcance.LIGA ? alcanceId : null)
                .equipoId(alcance == EjecucionIngesta.Alcance.EQUIPO ? alcanceId : null)
                .jugadorId(alcance == EjecucionIngesta.Alcance.JUGADOR ? alcanceId : null)
                .temporadaInicio(configuracion == null ? null : configuracion.getTemporadaInicio())
                .temporadaFin(configuracion == null ? null : configuracion.getTemporadaFin())
                .inicio(OffsetDateTime.now())
                .build());

        List<Jugador> objetivo = seleccionarJugadores(alcance, alcanceId);
        for (Jugador jugador : objetivo) {
            procesarJugador(jugador, ejecucion);
        }
        finalizar(ejecucion);
        return ejecucion;
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
        ejecucion.setJugadoresProcesados(ejecucion.getJugadoresProcesados() + 1);
        try {
            List<AparicionWhoscoredDto> apariciones = scraping.obtenerApariciones(jugador.getIdWhoscored());
            for (AparicionWhoscoredDto aparicion : apariciones) {
                if (!esPersistible(aparicion)) {
                    ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
                    continue;
                }
                EstadisticaPartido estadistica = construirEstadistica(jugador, aparicion, ejecucion);
                boolean existente = estadisticas.buscarPorJugadorYPartido(
                        jugador.getId(), aparicion.partidoWhoscoredId()) != null;
                estadisticas.upsert(estadistica);
                if (existente) {
                    ejecucion.setPartidosActualizados(ejecucion.getPartidosActualizados() + 1);
                } else {
                    ejecucion.setPartidosInsertados(ejecucion.getPartidosInsertados() + 1);
                }
            }
        } catch (RuntimeException exception) {
            ejecucion.setErrores(ejecucion.getErrores() + 1);
            ejecucion.setAnomalias(ejecucion.getAnomalias() + 1);
        }
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

    @Transactional
    public void vincularJugadores(Long equipoWhoscoredId, EjecucionIngesta ejecucion) {
        List<JugadorWhoscoredDto> descubiertos = scraping.descubrirJugadores(equipoWhoscoredId);
        for (JugadorWhoscoredDto descubierto : descubiertos) {
            ejecucion.setJugadoresProcesados(ejecucion.getJugadoresProcesados() + 1);
            MatchingWhoscoredService.ResultadoMatching resultado = matching.vincular(descubierto);
            if (resultado.estado() == MatchingWhoscoredService.Estado.VINCULADO) {
                jugadores.guardar(resultado.jugador());
                ejecucion.setJugadoresVinculados(ejecucion.getJugadoresVinculados() + 1);
            } else {
                guardarCaso(descubierto, resultado, ejecucion);
                ejecucion.setJugadoresNoVinculados(ejecucion.getJugadoresNoVinculados() + 1);
            }
        }
    }

    private void guardarCaso(JugadorWhoscoredDto descubierto,
                             MatchingWhoscoredService.ResultadoMatching resultado,
                             EjecucionIngesta ejecucion) {
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
    }
}
