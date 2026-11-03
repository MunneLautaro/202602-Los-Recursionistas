package unq.losrecursionistas.backend.controller.dto.orden;

import unq.losrecursionistas.backend.model.TenenciaToken;

public record TenenciaTokenResponse(Long id, Long jugadorId, String jugadorNombre,
                                    String equipoNombre, Integer cantidadTotal,
                                    Integer cantidadReservada, Integer cantidadDisponible) {
    public static TenenciaTokenResponse desde(TenenciaToken tenencia) {
        return new TenenciaTokenResponse(tenencia.getId(), tenencia.getJugador().getId(),
                tenencia.getJugador().getNombre(),
                tenencia.getJugador().getEquipo().getNombre(),
                tenencia.getCantidadTotal(), tenencia.getCantidadReservada(),
                tenencia.cantidadDisponible());
    }
}
