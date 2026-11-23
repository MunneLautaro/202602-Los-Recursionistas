package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public interface EstrategiaAdminService {
    EstadoEstrategiaCotizacion consultar();
    ResultadoCambioEstrategia cambiar(String estrategia, Usuario usuarioAdmin);

    record EstrategiaDisponible(String codigo, String nombre, String descripcion) {}
    record UltimoCambio(String estrategiaAnterior, String estrategiaNueva, String usuarioAdmin,
                        LocalDateTime timestampCambio) {}
    record EstadoEstrategiaCotizacion(String estrategiaActiva, String nombreActivo,
                                      String descripcionActiva, List<EstrategiaDisponible> estrategias,
                                      String origen, UltimoCambio ultimoCambio) {}
    record ResultadoCambioEstrategia(String mensaje, String estrategiaActiva, String nombreActivo,
                                     boolean cambioAplicado, UltimoCambio ultimoCambio) {}
}
