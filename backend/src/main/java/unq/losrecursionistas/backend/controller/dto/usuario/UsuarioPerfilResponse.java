package unq.losrecursionistas.backend.controller.dto.usuario;

import unq.losrecursionistas.backend.model.Usuario;

import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioPerfilResponse(Long id, String nombreUsuario, Double saldo,
                                    Double saldoReservado, Double saldoDisponible,
                                    Boolean habilitado, LocalDateTime fechaCreacion,
                                    Set<String> autoridades) {
    public static UsuarioPerfilResponse desdeModelo(Usuario usuario) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getSaldo(),
                usuario.getSaldoReservado(),
                usuario.saldoDisponible(),
                usuario.isHabilitado(),
                usuario.getFechaCreacion(),
                usuario.getAutoridades());
    }
}
