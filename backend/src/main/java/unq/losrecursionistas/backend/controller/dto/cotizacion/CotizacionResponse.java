package unq.losrecursionistas.backend.controller.dto.cotizacion;

import unq.losrecursionistas.backend.model.Cotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CotizacionResponse(Long id, Long jugadorId, BigDecimal valor,
                                  LocalDateTime timestampCalculo, String estrategiaUsada,
                                  LocalDate ventanaInicio, LocalDate ventanaFin) {
    public static CotizacionResponse desde(Cotizacion cotizacion) {
        return new CotizacionResponse(cotizacion.getId(), cotizacion.getJugador().getId(),
                cotizacion.getValor(), cotizacion.getTimestampCalculo(),
                cotizacion.getEstrategiaUsada(), cotizacion.getVentanaInicio(),
                cotizacion.getVentanaFin());
    }
}
