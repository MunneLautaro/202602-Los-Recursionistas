package unq.losrecursionistas.backend.controller.dto.orden;

import unq.losrecursionistas.backend.model.Orden;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrdenResponse(Long id, Long jugadorId, String tipo, Integer cantidadOriginal,
                            Integer cantidadDisponible, BigDecimal precioUnitario,
                            BigDecimal precioTotalRemanente, String estado,
                            OffsetDateTime fechaCreacion) {
    public static OrdenResponse desde(Orden orden) {
        return new OrdenResponse(orden.getId(), orden.getJugador().getId(),
                orden.getTipo().name(), orden.getCantidadOriginal(),
                orden.getCantidadDisponible(), orden.getPrecioUnitario(),
                orden.precioTotalRemanente(), orden.getEstado().name(),
                orden.getFechaCreacion());
    }
}
