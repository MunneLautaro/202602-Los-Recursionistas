package unq.losrecursionistas.backend.controller.dto;

import unq.losrecursionistas.backend.model.Usuario;

import java.time.LocalDateTime;

public record LoginUsuarioResponse(Long id, String nombreUsuario, Double saldo,
                                   Boolean habilitado, LocalDateTime fechaCreacion) {
    public static LoginUsuarioResponse desde(Usuario usuario) {
        return new LoginUsuarioResponse(usuario.getId(), usuario.getNombreUsuario(),
                usuario.getSaldo(), usuario.isHabilitado(), usuario.getFechaCreacion());
    }
}
