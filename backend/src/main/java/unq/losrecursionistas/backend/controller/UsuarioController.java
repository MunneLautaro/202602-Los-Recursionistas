package unq.losrecursionistas.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.controller.dto.usuario.UsuarioPerfilResponse;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;

@RestController
@RequestMapping({"", "/users"})
public class UsuarioController {
    private final RepositorioUsuario usuarios;

    public UsuarioController(RepositorioUsuario usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfil(
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null || principal.getUsername().isBlank()) {
            throw new AccessDeniedException("Autenticacion requerida");
        }
        return ResponseEntity.ok(UsuarioPerfilResponse.desdeModelo(
                usuarios.buscarPorNombreUsuario(principal.getUsername())));
    }
}
