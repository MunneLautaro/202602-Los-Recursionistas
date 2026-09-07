package unq.losrecursionistas.backend.services.impl;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import unq.losrecursionistas.backend.exceptions.ParametrosPaginacionInvalidosException;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.JugadorRepository;
import unq.losrecursionistas.backend.services.interfaces.JugadorService;

@Service
@Transactional
public class JugadorServiceImpl implements JugadorService {

	private final JugadorRepository repository;

	public JugadorServiceImpl(JugadorRepository repository) { this.repository = repository; }

	@Override
	public Page<Jugador> buscar(String liga, String equipo, String posicion, Boolean activo, int pagina) {
		if (pagina < 0) throw new ParametrosPaginacionInvalidosException();
		Pageable pageable = PageRequest.of(pagina, 12);
		return repository.recuperarJugadores(liga, equipo, posicion, activo, pageable);
	}

	@Override
	public Jugador obtener(Long id) {
		return repository.recuperarPorId(id);
	}
}