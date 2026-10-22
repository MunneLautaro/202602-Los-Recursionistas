package unq.losrecursionistas.backend.controller;


import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.jugador.EstadisticaTemporadaResponseDTO;
import unq.losrecursionistas.backend.controller.dto.jugador.JugadorFiltroRequestDTO;
import unq.losrecursionistas.backend.controller.dto.jugador.JugadorResponseDTO;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.EstadisticaTemporada;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaTemporada;

import java.util.*;

@RestController
@RequestMapping("/players")
public class JugadorController {

    private final JugadorService jugadorService;
    private final RepositorioEstadisticaTemporada estadisticas;
    private final ScrapingConfiguration configuracion;

    public JugadorController(JugadorService jugadorService, RepositorioEstadisticaTemporada estadisticas,
                             ScrapingConfiguration configuracion) {
        this.jugadorService = jugadorService;
        this.estadisticas = estadisticas;
        this.configuracion = configuracion;
    }

    @GetMapping("/{id}")
    public JugadorResponseDTO obtenerJugadorPorId(@PathVariable @Positive Long id) {
        Jugador jugador = jugadorService.obtenerJugadorPorId(id);
        return JugadorResponseDTO.desdeModelo(jugador);
    }

    @GetMapping
    public Page<JugadorResponseDTO> buscarJugadoresConFiltro(
            @ModelAttribute JugadorFiltroRequestDTO filtros,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<Jugador> jugadoresPage = jugadorService.buscarJugadoresConFiltro(filtros.aModelo(), page);
        return jugadoresPage.map(JugadorResponseDTO::desdeModelo);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> estadisticas(@PathVariable Long id) {
        Jugador jugador = jugadorService.obtenerJugadorPorId(id);
        List<EstadisticaTemporada> porTorneo =
                estadisticas.buscarPorJugadorYTemporada(id, configuracion.getTemporadaInicio());
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("jugadorId", id);
        respuesta.put("nombre", jugador.getNombre());
        respuesta.put("posicion", jugador.getPosicion());
        respuesta.put("equipo", jugador.getEquipo() == null ? null : jugador.getEquipo().getNombre());
        respuesta.put("estadisticasPorTorneo", porTorneo.stream()
                .map(EstadisticaTemporadaResponseDTO::desdeModelo)
                .toList());
        respuesta.put("totalesGlobal", totales(porTorneo));
        return respuesta;
    }

    @GetMapping("/ranking")
    public List<EstadisticaTemporadaResponseDTO> ranking(@RequestParam(required = false) String liga,
                                                         @RequestParam(required = false) String posicion,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return estadisticas.buscarRanking(liga, posicion, configuracion.getTemporadaInicio(),
                        Math.max(1, Math.min(limit, 100))).stream()
                .map(EstadisticaTemporadaResponseDTO::desdeModelo)
                .toList();
    }

    @GetMapping("/{id}/quotes")
    public List<Object> cotizaciones(@PathVariable Long id) {
        return List.of();
    }

    private Map<String, Object> totales(List<EstadisticaTemporada> stats) {
        Map<String, Object> total = new LinkedHashMap<>();
        total.put("apps", stats.stream().mapToInt(s -> value(s.getApps())).sum());
        total.put("goles", stats.stream().mapToInt(s -> value(s.getGoles())).sum());
        total.put("asistencias", stats.stream().mapToInt(s -> value(s.getAsistencias())).sum());
        List<java.math.BigDecimal> ratings = stats.stream().map(EstadisticaTemporada::getRatingPromedio)
                .filter(Objects::nonNull).toList();
        total.put("ratingPromedio", ratings.isEmpty() ? null : ratings.stream()
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .divide(java.math.BigDecimal.valueOf(ratings.size()), 2, java.math.RoundingMode.HALF_UP));
        return total;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
