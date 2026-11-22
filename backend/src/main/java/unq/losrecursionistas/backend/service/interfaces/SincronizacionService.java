package unq.losrecursionistas.backend.service.interfaces;

import java.util.List;

/**
 * Sincroniza ligas, equipos y planteles desde la API de Football Data.
 */
public interface SincronizacionService {
    void sincronizarTodasLasLigas(List<String> codigosLigas);

    void sincronizarLiga(String codigoLiga);

    void sincronizarJugadores(Long equipoExternoId);
}
