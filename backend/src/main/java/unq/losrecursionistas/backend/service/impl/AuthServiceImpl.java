package unq.losrecursionistas.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.service.interfaces.AuthService;
import unq.losrecursionistas.backend.service.interfaces.JwtService;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Override
	public RespuestaTokenDto login(CredencialesLoginDto credenciales) {
		Authentication autenticacion = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(credenciales.nombreUsuario(), credenciales.contrasena()));
		UserDetails usuario = (UserDetails) autenticacion.getPrincipal();
		String token = jwtService.generarToken(usuario);
		return new RespuestaTokenDto(token);
	}
}

