package unq.losrecursionistas.backend.controller;

import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;
import unq.losrecursionistas.backend.service.interfaces.ResultadoIngesta;

@RestController
@RequestMapping("/ingesta")
public class IngestaController {
    private final IngestaWhoscoredService servicio;

    public IngestaController(IngestaWhoscoredService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/linking")
    public ResultadoIngesta linking() {
        return servicio.ejecutarLinking();
    }

    @PostMapping("/estadisticas")
    public ResultadoIngesta estadisticas() {
        return servicio.ejecutarIngesta();
    }

    @PostMapping("/jugador/{id}/estadisticas")
    public ResultadoIngesta estadisticasJugador(@PathVariable Long id) {
        return servicio.ingestarEstadisticasJugador(id);
    }
}
