package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import unq.losrecursionistas.backend.exceptions.JugadorNoEncontradoException;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.JugadorRepository;
import unq.losrecursionistas.backend.persistence.sql.JugadorDAOSQL;

@Repository
public class JugadorRepositoryImpl implements JugadorRepository {

	private final JugadorDAOSQL jugadorDAOSQL;

	public JugadorRepositoryImpl(JugadorDAOSQL jugadorDAOSQL) {
		this.jugadorDAOSQL = jugadorDAOSQL;
	}

	@Override
	public Page<Jugador> recuperarJugadores(String liga, String equipo, String posicion,
			Boolean activo, Pageable pageable) {
		return jugadorDAOSQL.buscarPorFiltros(normalizar(liga), normalizar(equipo),
				normalizar(posicion), activo, pageable);
	}

	@Override
	public Jugador recuperarPorId(Long id) {
		return jugadorDAOSQL.findById(id)
				.orElseThrow(() -> new JugadorNoEncontradoException(id));
	}

	private String normalizar(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}
}