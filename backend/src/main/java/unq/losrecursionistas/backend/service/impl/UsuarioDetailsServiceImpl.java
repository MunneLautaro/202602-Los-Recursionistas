package unq.losrecursionistas.backend.service.impl;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.interfaces.UsuarioDetailsService;

@Service
public class UsuarioDetailsServiceImpl implements UsuarioDetailsService {

	private final RepositorioUsuario repositorioUsuario;

	public UsuarioDetailsServiceImpl(RepositorioUsuario repositorioUsuario) {
		this.repositorioUsuario = repositorioUsuario;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = repositorioUsuario.buscarPorNombreUsuario(username);
		if (usuario == null) {
			throw new UsernameNotFoundException("Credenciales invalidas");
		}
		var autoridades = usuario.getAutoridades().stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());
		return User.withUsername(usuario.getNombreUsuario())
				.password(usuario.getContrasena())
				.authorities(autoridades)
				.disabled(!usuario.isHabilitado())
				.build();
	}
}