package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.ActividadDiariaMercado;
import unq.losrecursionistas.backend.model.TransaccionMercado;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTransaccionMercado;
import unq.losrecursionistas.backend.persistence.sql.TransaccionMercadoDAOSQL;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class RepositorioTransaccionMercadoImpl implements RepositorioTransaccionMercado {
    private final TransaccionMercadoDAOSQL dao;
    public RepositorioTransaccionMercadoImpl(TransaccionMercadoDAOSQL dao) { this.dao = dao; }
    public TransaccionMercado crear(TransaccionMercado transaccion) { return dao.save(transaccion); }
    public Page<TransaccionMercado> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        return dao.findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc(usuarioId, usuarioId, pageable);
    }
    public List<ActividadDiariaMercado> resumirTransaccionesPorDia(LocalDate desde, LocalDate hasta) {
        AgregadosDiarios.validarRango(desde, hasta);
        return dao.resumirPorDia(desde, hasta).stream()
                .map(fila -> new ActividadDiariaMercado(AgregadosDiarios.dia(fila.getDia()), 0, 0,
                        AgregadosDiarios.entero(fila.getCantidad()), AgregadosDiarios.decimal(fila.getVolumen(), 2)))
                .sorted(Comparator.comparing(ActividadDiariaMercado::dia))
                .toList();
    }
}
