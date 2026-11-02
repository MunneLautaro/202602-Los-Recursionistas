package unq.losrecursionistas.backend.controller.dto.orden;

import unq.losrecursionistas.backend.model.TransaccionMercado;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransaccionResponse(Long id, Long jugadorId, Long compradorId, Long vendedorId,
                                  Integer cantidad, BigDecimal precioUnitario,
                                  BigDecimal total, OffsetDateTime ejecutadaEn) {
    public static TransaccionResponse desde(TransaccionMercado tx) {
        return new TransaccionResponse(tx.getId(), tx.getJugador().getId(),
                tx.getComprador().getId(), tx.getVendedor().getId(), tx.getCantidad(),
                tx.getPrecioUnitario(), tx.getTotal(), tx.getEjecutadaEn());
    }
}
