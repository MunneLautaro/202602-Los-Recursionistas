package unq.losrecursionistas.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.EstadisticaTemporada;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.JugadorFiltro;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaTemporada;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;

import java.util.List;

@Service
@Transactional
public class JugadorServiceImpl implements JugadorService {

    private final RepositorioJugador repositorioJugador;
    private final RepositorioEstadisticaTemporada repositorioEstadisticas;
    private final ScrapingConfiguration configuracionScraping;

    public JugadorServiceImpl(RepositorioJugador repositorioJugador,
                              RepositorioEstadisticaTemporada repositorioEstadisticas,
                              ScrapingConfiguration configuracionScraping) {
        this.repositorioJugador = repositorioJugador;
        this.repositorioEstadisticas = repositorioEstadisticas;
        this.configuracionScraping = configuracionScraping;
    }

    @Override
    public void guardarTodos(List<Jugador> jugadores) {
        repositorioJugador.guardarTodos(jugadores);
    }

    @Override
    public Jugador obtenerJugadorPorId(Long id) {
        return repositorioJugador.obtenerJugadorPorId(id);
    }

    @Override
    public Page<Jugador> buscarJugadoresConFiltro(JugadorFiltro filtro, int page) {
        Pageable pageable = PageRequest.of(page, 12, Sort.by("nombre").ascending());
        return repositorioJugador.buscarJugadoresConFiltro(filtro, pageable);
    }

    @Override
    public List<Jugador> obtenerJugadoresActivos() {
        return repositorioJugador.obtenerJugadoresActivos();
    }

    @Override
    public List<EstadisticaTemporada> obtenerEstadisticasTemporadaActual(Long jugadorId) {
        return repositorioEstadisticas.buscarPorJugadorYTemporada(jugadorId,
                configuracionScraping.getTemporadaInicio());
    }
}
