package unq.losrecursionistas.backend.controller.dto;

import unq.losrecursionistas.backend.service.interfaces.ResultadoLogin;

import java.util.Set;

public record RespuestaTokenDto(String token, LoginUsuarioResponse usuario) implements RespuestaDto {
	public static RespuestaTokenDto desde(ResultadoLogin resultado) {
		LoginUsuarioResponse datosUsuario = resultado.usuario() == null
				? new LoginUsuarioResponse(null, resultado.nombreUsuario(), null, null, null, Set.of())
				: LoginUsuarioResponse.desde(resultado.usuario());
		return new RespuestaTokenDto(resultado.token(), datosUsuario);
	}
}