package unq.losrecursionistas.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.controller.dto.equipo.EquipoResponseDTO;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.service.interfaces.EquipoService;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping("/{ligaId}")
    public ResponseEntity<List<EquipoResponseDTO>> obtenerEquiposPorLiga(@PathVariable Long ligaId) {
        List<Equipo> equipos = equipoService.obtenerTodosLosEquiposDeLiga(ligaId);
        List<EquipoResponseDTO> equiposResponseDTO = equipos.stream()
                .map(EquipoResponseDTO::desdeModelo)
                .toList();
        return ResponseEntity.ok(equiposResponseDTO);

    }


}
