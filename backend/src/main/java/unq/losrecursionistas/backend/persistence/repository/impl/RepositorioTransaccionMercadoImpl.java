package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.TransaccionMercado;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTransaccionMercado;
import unq.losrecursionistas.backend.persistence.sql.TransaccionMercadoDAOSQL;

@Component
public class RepositorioTransaccionMercadoImpl implements RepositorioTransaccionMercado {
    private final TransaccionMercadoDAOSQL dao;
    public RepositorioTransaccionMercadoImpl(TransaccionMercadoDAOSQL dao) { this.dao = dao; }
    public TransaccionMercado crear(TransaccionMercado transaccion) { return dao.save(transaccion); }
    public Page<TransaccionMercado> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        return dao.findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc(usuarioId, usuarioId, pageable);
    }
}
