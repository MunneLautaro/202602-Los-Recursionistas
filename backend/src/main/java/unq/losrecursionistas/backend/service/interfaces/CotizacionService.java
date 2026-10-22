package unq.losrecursionistas.backend.service.interfaces;

import org.springframework.data.domain.Page;
import unq.losrecursionistas.backend.controller.dto.cotizacion.CotizacionResponse;
import unq.losrecursionistas.backend.controller.dto.cotizacion.RankingJugadorResponse;
import unq.losrecursionistas.backend.model.Jugador;

import java.time.LocalDate;
import java.util.List;

public interface CotizacionService {
    void recalcular();
    void recalcular(LocalDate ventanaInicio, LocalDate ventanaFin);
    void recalcular(List<Jugador> jugadores, LocalDate ventanaInicio, LocalDate ventanaFin);
    Page<CotizacionResponse> historial(Long jugadorId, LocalDate desde, LocalDate hasta,
                                       int pagina, int tamano);
    Page<RankingJugadorResponse> ranking(Long ligaId, String posicion, int pagina, int tamano);
    boolean reservarRecalculo();
    void liberarRecalculo();
}
