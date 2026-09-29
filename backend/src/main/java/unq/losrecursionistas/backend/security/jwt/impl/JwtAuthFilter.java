package unq.losrecursionistas.backend.security.jwt.impl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import unq.losrecursionistas.backend.service.interfaces.JwtService;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String PREFIJO_BEARER = "Bearer ";

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith(PREFIJO_BEARER)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorization.substring(PREFIJO_BEARER.length()).trim();
		if (token.isEmpty() || SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			jwtService.extraerUsername(token)
					.map(userDetailsService::loadUserByUsername)
					.filter(UserDetails::isEnabled)
					.filter(usuario -> jwtService.tokenValido(token, usuario))
					.ifPresent(usuario -> autenticar(request, usuario));
		} catch (RuntimeException excepcion) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private void autenticar(HttpServletRequest request, UserDetails usuario) {
		var autenticacion = UsernamePasswordAuthenticationToken.authenticated(
				usuario, null, usuario.getAuthorities());
		autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(autenticacion);
	}
}