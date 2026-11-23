package unq.losrecursionistas.backend.service.interfaces;

import org.springframework.data.domain.Page;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.RankingCotizacion;

import java.time.LocalDate;
import java.util.List;

public interface CotizacionService {
    void recalcular();
    void recalcular(LocalDate ventanaInicio, LocalDate ventanaFin);
    void recalcular(List<Jugador> jugadores, LocalDate ventanaInicio, LocalDate ventanaFin);
    Page<Cotizacion> historial(Long jugadorId, LocalDate desde, LocalDate hasta,
                                int pagina, int tamano);
    List<Cotizacion> serie(Long jugadorId, LocalDate desde, LocalDate hasta);
    Page<RankingCotizacion> ranking(Long ligaId, String posicion, int pagina, int tamano);
    boolean reservarRecalculo();
    void liberarRecalculo();
    boolean estaRecalculoActivo();
    void asegurarCotizacionInicial(Jugador jugador);
}
