package unq.losrecursionistas.backend.controller.dto.usuario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import unq.losrecursionistas.backend.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioRequestDTO(
        @NotNull(message = "El nombre de usuario es requerido")
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String nombreUsuario,
        @NotNull(message = "La contraseña es requerida")
        @NotBlank(message = "La contraseña no puede estar vacía")
        String contrasena,
        @NotNull(message = "El saldo es requerido")
        @DecimalMin(value = "0.0", inclusive = true, message = "El saldo no puede ser negativo")
        Double saldo,
        @NotNull(message = "El estado de habilitación es requerido")
        Boolean habilitado




) {
    public Usuario aModelo() {
        return Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .contrasena(contrasena)
                .saldo(saldo)
                .habilitado(habilitado)
                .build();
    }

    public Usuario aModeloRegister() {
        return Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .contrasena(contrasena)
                .saldo(saldo)
                .habilitado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

}