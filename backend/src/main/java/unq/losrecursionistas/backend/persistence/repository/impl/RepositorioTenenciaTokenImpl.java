package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.TenenciaToken;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTenenciaToken;
import unq.losrecursionistas.backend.persistence.sql.TenenciaTokenDAOSQL;

@Component
public class RepositorioTenenciaTokenImpl implements RepositorioTenenciaToken {
    private final TenenciaTokenDAOSQL dao;
    public RepositorioTenenciaTokenImpl(TenenciaTokenDAOSQL dao) { this.dao = dao; }
    public TenenciaToken buscarBloqueada(Long usuarioId, Long jugadorId) {
        return dao.findByUsuarioIdAndJugadorId(usuarioId, jugadorId).orElse(null);
    }
    public TenenciaToken buscar(Long usuarioId, Long jugadorId) {
        return dao.findByUsuarioIdAndJugadorId(usuarioId, jugadorId).orElse(null);
    }
    public TenenciaToken guardar(TenenciaToken tenencia) { return dao.save(tenencia); }
}
