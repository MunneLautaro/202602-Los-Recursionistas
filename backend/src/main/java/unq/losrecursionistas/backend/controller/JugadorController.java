package unq.losrecursionistas.backend.controller;


import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.controller.dto.jugador.JugadorFiltroRequestDTO;
import unq.losrecursionistas.backend.controller.dto.jugador.JugadorResponseDTO;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;

@RestController
@RequestMapping("/players")
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
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
}
