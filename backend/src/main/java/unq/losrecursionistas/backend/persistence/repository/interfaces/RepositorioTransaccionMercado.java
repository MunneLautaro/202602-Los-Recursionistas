package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.ActividadDiariaMercado;
import unq.losrecursionistas.backend.model.TransaccionMercado;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioTransaccionMercado {
    TransaccionMercado crear(TransaccionMercado transaccion);
    Page<TransaccionMercado> buscarPorUsuario(Long usuarioId, Pageable pageable);
    /**
     * Emparejamientos y volumen por dia (America/Argentina/Buenos_Aires), solo los dias con datos.
     * El rango esta acotado a 90 dias.
     */
    List<ActividadDiariaMercado> resumirTransaccionesPorDia(LocalDate desde, LocalDate hasta);
}
