package unq.losrecursionistas.backend.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.orden.OrdenRequest;
import unq.losrecursionistas.backend.controller.dto.orden.OrdenResponse;
import unq.losrecursionistas.backend.controller.dto.orden.TransaccionResponse;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.interfaces.MercadoService;

@RestController
public class MercadoController {
    private static final int PAGE_SIZE_DEFAULT = 12;
    private final MercadoService mercado;
    private final RepositorioUsuario usuarios;

    public MercadoController(MercadoService mercado, RepositorioUsuario usuarios) {
        this.mercado = mercado;
        this.usuarios = usuarios;
    }

    @PostMapping("/orders/buy")
    public ResponseEntity<OrdenResponse> comprar(@Valid @RequestBody OrdenRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        Usuario usuario = usuario(principal);
        return ResponseEntity.ok(OrdenResponse.desde(mercado.crearOrden(
                usuario, request.jugadorId(), request.cantidad(), TipoOrden.COMPRA)));
    }

    @PostMapping("/orders/sell")
    public ResponseEntity<OrdenResponse> vender(@Valid @RequestBody OrdenRequest request,
                                                 @AuthenticationPrincipal UserDetails principal) {
        Usuario usuario = usuario(principal);
        return ResponseEntity.ok(OrdenResponse.desde(mercado.crearOrden(
                usuario, request.jugadorId(), request.cantidad(), TipoOrden.VENTA)));
    }

    @GetMapping("/orders")
    public Page<OrdenResponse> listar(@RequestParam(required = false) Long jugadorId,
                                      @RequestParam(required = false) TipoOrden tipo,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : PAGE_SIZE_DEFAULT);
        return mercado.listarOrdenes(jugadorId, tipo, pageable).map(OrdenResponse::desde);
    }

    @GetMapping("/users/{id}/transactions")
    public Page<TransaccionResponse> transacciones(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "12") int size,
                                                    @AuthenticationPrincipal UserDetails principal) {
        Usuario solicitante = usuario(principal);
        if (!solicitante.getId().equals(id)
                && !solicitante.getAutoridades().contains("ROLE_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No puede consultar otra cuenta");
        }
        Pageable pageable = PageRequest.of(page, size > 0 ? size : PAGE_SIZE_DEFAULT);
        return mercado.listarTransacciones(id, pageable).map(TransaccionResponse::desde);
    }

    private Usuario usuario(UserDetails principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Autenticacion requerida");
        }
        return usuarios.buscarPorNombreUsuario(principal.getUsername());
    }
}
