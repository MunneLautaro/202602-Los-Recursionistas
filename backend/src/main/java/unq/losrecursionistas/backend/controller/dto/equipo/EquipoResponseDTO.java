package unq.losrecursionistas.backend.controller.dto.equipo;

import unq.losrecursionistas.backend.model.Equipo;

public record EquipoResponseDTO(
        Long id,
        Long idExterno,
        String nombre,
        String nombreCorto,
        String sigla,
        String escudoUrl,
        Integer fundacion,
        String colores,
        String estadio
) {
    public static EquipoResponseDTO desdeModelo(Equipo equipo) {
        if (equipo == null) {
            return null;
        }

        return new EquipoResponseDTO(
                equipo.getId(),
                equipo.getIdExterno(),
                equipo.getNombre(),
                equipo.getNombreCorto(),
                equipo.getSigla(),
                equipo.getEscudoUrl(),
                equipo.getFundacion(),
                equipo.getColores(),
                equipo.getEstadio()
        );
    }
}