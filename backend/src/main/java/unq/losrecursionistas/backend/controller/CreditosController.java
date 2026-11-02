package unq.losrecursionistas.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.usuario.RecargaCreditosRequest;
import unq.losrecursionistas.backend.controller.dto.usuario.SaldoResponse;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.interfaces.MercadoService;

@RestController
public class CreditosController {
    private final MercadoService mercado;
    private final RepositorioUsuario usuarios;

    public CreditosController(MercadoService mercado, RepositorioUsuario usuarios) {
        this.mercado = mercado;
        this.usuarios = usuarios;
    }

    @PostMapping("/users/{id}/credits")
    public ResponseEntity<SaldoResponse> recargar(@PathVariable Long id,
                                                   @Valid @RequestBody RecargaCreditosRequest request,
                                                   @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Autenticacion requerida");
        }
        Usuario solicitante = usuarios.buscarPorNombreUsuario(principal.getUsername());
        return ResponseEntity.ok(SaldoResponse.desde(
                mercado.recargarCreditos(solicitante, id, request.monto())));
    }
}
