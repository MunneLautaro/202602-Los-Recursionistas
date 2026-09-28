package unq.losrecursionistas.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.controller.dto.liga.LigaResponseDTO;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.service.interfaces.LigaService;

import java.util.List;

@RestController
@RequestMapping("/leagues")
public class LigaController {
    private final LigaService ligaService;

    public LigaController(LigaService ligaService) {
        this.ligaService = ligaService;
    }

    @GetMapping
    public ResponseEntity<List<LigaResponseDTO>> obtenerLigas(){
        List<Liga> ligas = ligaService.obtenerLigas();
        List<LigaResponseDTO> ligasResponseDTO = ligas.stream()
                .map(LigaResponseDTO::desdeModelo)
                .toList();
        return ResponseEntity.ok(ligasResponseDTO);

    }

}
