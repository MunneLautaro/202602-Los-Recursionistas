package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.TenenciaToken;

public interface RepositorioTenenciaToken {
    TenenciaToken buscarBloqueada(Long usuarioId, Long jugadorId);
    TenenciaToken buscar(Long usuarioId, Long jugadorId);
    TenenciaToken guardar(TenenciaToken tenencia);
}
