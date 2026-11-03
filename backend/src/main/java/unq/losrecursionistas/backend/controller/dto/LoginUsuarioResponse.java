package unq.losrecursionistas.backend.controller.dto;

import unq.losrecursionistas.backend.model.Usuario;

import java.time.LocalDateTime;
import java.util.Set;

public record LoginUsuarioResponse(Long id, String nombreUsuario, Double saldo,
                                   Boolean habilitado, LocalDateTime fechaCreacion,
                                   Set<String> autoridades) {
    public static LoginUsuarioResponse desde(Usuario usuario) {
        return new LoginUsuarioResponse(usuario.getId(), usuario.getNombreUsuario(),
                usuario.getSaldo(), usuario.isHabilitado(), usuario.getFechaCreacion(),
                usuario.getAutoridades());
    }
}
