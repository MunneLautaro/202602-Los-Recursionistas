package unq.losrecursionistas.backend.controller.dto.jugador;

import unq.losrecursionistas.backend.model.JugadorFiltro;

public record JugadorFiltroRequestDTO(
        String nombre,
        String posicion,
        Long ligaId,
        Long equipoId
) {

    public JugadorFiltro aModelo() {
        return JugadorFiltro.builder()
                .nombre(nombre)
                .posicion(posicion)
                .ligaId(ligaId)
                .equipoId(equipoId)
                .build();
    }
}
