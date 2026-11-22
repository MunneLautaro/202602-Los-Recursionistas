package unq.losrecursionistas.backend.service.interfaces;

/**
 * Carga inicial de datos de WhoScored al iniciar la aplicación.
 */
public interface WhoscoredBootstrapService {
    /** Indica si la carga inicial de WhoScored está habilitada por configuración. */
    boolean cargaInicialHabilitada();

    void cargarDatosIniciales();
}
