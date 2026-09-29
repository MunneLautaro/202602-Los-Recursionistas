package unq.losrecursionistas.backend.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CredencialesLoginDto(
		@NotBlank String nombreUsuario,
		@NotBlank String contrasena) {
}