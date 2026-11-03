package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.TenenciaToken;

public interface RepositorioTenenciaToken {
    TenenciaToken buscarBloqueada(Long usuarioId, Long jugadorId);
    TenenciaToken buscar(Long usuarioId, Long jugadorId);
    TenenciaToken guardar(TenenciaToken tenencia);
    Page<TenenciaToken> buscarPorUsuario(Long usuarioId, Pageable pageable);
}
