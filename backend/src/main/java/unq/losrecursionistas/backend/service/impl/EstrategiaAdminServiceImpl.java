package unq.losrecursionistas.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.exceptions.ExcepcionEstrategiaEnRecalculo;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.model.CambioEstrategia;
import unq.losrecursionistas.backend.model.ConfiguracionEstrategia;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCambioEstrategia;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioConfiguracionEstrategia;
import unq.losrecursionistas.backend.service.estrategia.CatalogoEstrategias;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;
import unq.losrecursionistas.backend.service.interfaces.EstrategiaAdminService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class EstrategiaAdminServiceImpl implements EstrategiaAdminService {
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private final RepositorioConfiguracionEstrategia configuracion;
    private final RepositorioCambioEstrategia cambios;
    private final CotizacionService cotizaciones;
    private final String fallback;

    public EstrategiaAdminServiceImpl(RepositorioConfiguracionEstrategia configuracion,
                                      RepositorioCambioEstrategia cambios,
                                      CotizacionService cotizaciones,
                                      @Value("${cotizacion.estrategia.activa:PERFORMANCE_GENERAL}") String fallback) {
        this.configuracion = configuracion;
        this.cambios = cambios;
        this.cotizaciones = cotizaciones;
        this.fallback = fallback;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoEstrategiaCotizacion consultar() {
        String activa = activo();
        CatalogoEstrategias.Disponible disponible = disponible(activa);
        return new EstadoEstrategiaCotizacion(activa, disponible.nombre(), disponible.descripcion(),
                CatalogoEstrategias.todas().stream()
                        .map(e -> new EstrategiaDisponible(e.codigo(), e.nombre(), e.descripcion()))
                        .toList(),
                configuracion.buscar() == null ? "CONFIGURACION" : "PERSISTIDA",
                convertir(cambios.ultimo()));
    }

    @Override
    @Transactional
    public ResultadoCambioEstrategia cambiar(String estrategia, Usuario usuarioAdmin) {
        if (usuarioAdmin == null || !usuarioAdmin.getAutoridades().contains("ROLE_ADMIN")) {
            throw new ExcepcionValidacion("El usuario no tiene permisos de administrador");
        }
        String codigo = estrategia == null ? "" : estrategia.trim().toUpperCase(Locale.ROOT);
        CatalogoEstrategias.Disponible nueva = disponible(codigo);
        if (!cotizaciones.estaRecalculoActivo()) {
            String anterior = activo();
            if (anterior.equals(codigo)) {
                return resultado(codigo, false, cambios.ultimo());
            }
            LocalDateTime ahora = LocalDateTime.now(ZONA_ARGENTINA);
            ConfiguracionEstrategia existente = configuracion.buscar();
            if (existente == null) {
                configuracion.guardar(new ConfiguracionEstrategia(codigo, ahora));
            } else {
                existente.actualizar(codigo, ahora);
                configuracion.guardar(existente);
            }
            cambios.guardar(new CambioEstrategia(anterior, codigo, usuarioAdmin.getNombreUsuario(), ahora));
            return resultado(nueva.codigo(), true, cambios.ultimo());
        }
        throw new ExcepcionEstrategiaEnRecalculo();
    }

    private ResultadoCambioEstrategia resultado(String codigo, boolean aplicado, CambioEstrategia ultimo) {
        CatalogoEstrategias.Disponible disponible = disponible(codigo);
        return new ResultadoCambioEstrategia("Estrategia activa cambiada correctamente.", codigo,
                disponible.nombre(), aplicado, convertir(ultimo));
    }

    private String activo() {
        ConfiguracionEstrategia guardada = configuracion.buscar();
        return guardada == null ? fallback : guardada.getEstrategiaActiva();
    }

    private CatalogoEstrategias.Disponible disponible(String codigo) {
        CatalogoEstrategias.Disponible disponible = CatalogoEstrategias.obtener(codigo);
        if (disponible == null) {
            throw new ExcepcionValidacion("Estrategia de cotizacion desconocida: " + codigo);
        }
        return disponible;
    }

    private UltimoCambio convertir(CambioEstrategia cambio) {
        return cambio == null ? null : new UltimoCambio(cambio.getEstrategiaAnterior(),
                cambio.getEstrategiaNueva(), cambio.getUsuarioAdmin(), cambio.getTimestampCambio());
    }
}
