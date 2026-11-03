package unq.losrecursionistas.backend.controller.dto.usuario;

import unq.losrecursionistas.backend.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioPerfilResponse(Long id, String nombreUsuario, Double saldo,
                                    Double saldoReservado, Double saldoDisponible,
                                    Boolean habilitado, LocalDateTime fechaCreacion) {
    public static UsuarioPerfilResponse desdeModelo(Usuario usuario) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getSaldo(),
                usuario.getSaldoReservado(),
                usuario.saldoDisponible(),
                usuario.isHabilitado(),
                usuario.getFechaCreacion());
    }
}
