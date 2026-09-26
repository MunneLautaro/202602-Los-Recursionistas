package unq.losrecursionistas.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;

@RestController
@RequestMapping("/admin/sync")
public class SincronizacionController {

    private final SincronizacionService sincronizacionService;

    public SincronizacionController(SincronizacionService sincronizacionService) {
        this.sincronizacionService = sincronizacionService;
    }

    @PostMapping("/jugadores/{equipoExternoId}")
    public ResponseEntity<Void> sincronizarJugadores(@PathVariable Long equipoExternoId) {
        sincronizacionService.sincronizarJugadores(equipoExternoId);
        return ResponseEntity.accepted().build();
    }
}