package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.Liga;

import java.util.List;

public interface RepositorioLiga {
    Liga guardar(Liga liga);
    Liga buscarOModificarPorCodigoOIdExterno(String codigo, Long idExterno, String nombre);
    Liga buscarPorCodigo(String codigo);

    List<Liga> obtenerLigas();
}
