package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Usuario;

/**
 * Resultado de un login exitoso: el JWT emitido y el usuario autenticado.
 * {@code usuario} puede ser {@code null} si el principal autenticado no tiene un usuario persistido.
 */
public record ResultadoLogin(String token, String nombreUsuario, Usuario usuario) {
}
