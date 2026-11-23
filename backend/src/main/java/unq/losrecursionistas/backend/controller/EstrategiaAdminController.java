package unq.losrecursionistas.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.estrategia.CambiarEstrategiaRequest;
import unq.losrecursionistas.backend.controller.dto.estrategia.EstrategiaActivaResponse;
import unq.losrecursionistas.backend.controller.dto.estrategia.EstrategiaDisponibleResponse;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.service.interfaces.UsuarioService;
import unq.losrecursionistas.backend.service.interfaces.EstrategiaAdminService;

import java.time.ZoneId;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin/cotizacion")
public class EstrategiaAdminController {
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private final EstrategiaAdminService servicio;
    private final UsuarioService usuarios;

    public EstrategiaAdminController(EstrategiaAdminService servicio, UsuarioService usuarios) {
        this.servicio = servicio;
        this.usuarios = usuarios;
    }

    @GetMapping("/estrategia")
    public EstrategiaResponse consultar() {
        return desde(servicio.consultar());
    }

    @PutMapping("/estrategia")
    public ResponseEntity<CambioResponse> cambiar(@Valid @RequestBody CambiarEstrategiaRequest request,
                                                   @AuthenticationPrincipal UserDetails principal) {
        Usuario usuario = usuarios.buscarPorNombreUsuario(principal.getUsername());
        var resultado = servicio.cambiar(request.estrategia(), usuario);
        return ResponseEntity.ok(new CambioResponse(resultado.mensaje(), resultado.estrategiaActiva(),
                resultado.nombreActivo(), resultado.cambioAplicado(), desde(resultado.ultimoCambio())));
    }

    private EstrategiaResponse desde(EstrategiaAdminService.EstadoEstrategiaCotizacion estado) {
        return new EstrategiaResponse(estado.estrategiaActiva(), estado.nombreActivo(),
                estado.descripcionActiva(), estado.estrategias().stream()
                .map(e -> new EstrategiaDisponibleResponse(e.codigo(), e.nombre(), e.descripcion())).toList(),
                estado.origen(), desde(estado.ultimoCambio()));
    }

    private EstrategiaActivaResponse desde(EstrategiaAdminService.UltimoCambio cambio) {
        if (cambio == null) return null;
        return new EstrategiaActivaResponse(cambio.estrategiaAnterior(), cambio.estrategiaNueva(),
                cambio.usuarioAdmin(), OffsetDateTime.of(cambio.timestampCambio(), ZONA_ARGENTINA.getRules()
                        .getOffset(cambio.timestampCambio())));
    }

    public record EstrategiaResponse(String estrategiaActiva, String nombreActivo, String descripcionActiva,
                                     java.util.List<EstrategiaDisponibleResponse> estrategias, String origen,
                                     EstrategiaActivaResponse ultimoCambio) {
    }

    public record CambioResponse(String mensaje, String estrategiaActiva, String nombreActivo,
                                 boolean cambioAplicado, EstrategiaActivaResponse ultimoCambio) {
    }
}
