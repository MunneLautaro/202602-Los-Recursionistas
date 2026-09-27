package unq.losrecursionistas.backend.controller.dto.usuario;

import unq.losrecursionistas.backend.model.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String nombreUsuario,
        String saldo,
        String fechaCreacion,
        Boolean habilitado

) {

    public static UsuarioResponseDTO desdeModelo(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getSaldo().toString(),
                usuario.getFechaCreacion().toString(),
                usuario.isHabilitado()
        );
    }
}
