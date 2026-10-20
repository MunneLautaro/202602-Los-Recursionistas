package unq.losrecursionistas.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.whoscored.*;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.service.interfaces.EjecucionIngestaService;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/whoscored/ingestas")
public class WhoscoredIngestaController {
    private final IngestaWhoscoredService ingesta;
    private final EjecucionIngestaService ejecuciones;

    public WhoscoredIngestaController(IngestaWhoscoredService ingesta, EjecucionIngestaService ejecuciones) {
        this.ingesta = ingesta;
        this.ejecuciones = ejecuciones;
    }

    @PostMapping
    public ResponseEntity<IniciarIngestaResponse> iniciar(@Valid @RequestBody IniciarIngestaRequest request) {
        EjecucionIngesta ejecucion = ingesta.iniciar(request.alcance(), request.id());
        return ResponseEntity.accepted()
                .location(URI.create("/api/admin/whoscored/ingestas/" + ejecucion.getId()))
                .body(IniciarIngestaResponse.from(ejecucion));
    }

    @GetMapping("/ultima")
    public UltimaEjecucionResponse ultima() {
        return UltimaEjecucionResponse.from(ejecuciones.ultima());
    }
}
