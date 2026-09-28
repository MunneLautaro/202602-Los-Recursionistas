package unq.losrecursionistas.backend.controller.dto.liga;

import unq.losrecursionistas.backend.model.Liga;

import java.time.LocalDate;

public record LigaResponseDTO(
        Long id,
        Long idExterno,
        String nombre,
        String codigo,
        LocalDate fechaCreacion
) {
    public static LigaResponseDTO desdeModelo(Liga liga) {
        return new LigaResponseDTO(
                liga.getId(),
                liga.getIdExterno(),
                liga.getNombre(),
                liga.getCodigo(),
                liga.getFechaCreacion()
        );
    }
}
