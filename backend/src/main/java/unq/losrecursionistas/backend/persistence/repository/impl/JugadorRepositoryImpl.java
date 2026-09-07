package unq.losrecursionistas.backend.persistence.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.JugadorRepository;

@Repository
public class JugadorRepositoryImpl implements JugadorRepository {

	private final Map<Long, Jugador> jugadores = new ConcurrentHashMap<>();

	public JugadorRepositoryImpl() {
		Liga premier = new Liga(1L, "Premier League", "PREMIER", true);
		Liga liga = new Liga(2L, "La Liga", "LA_LIGA", true);
		jugadores.put(1L, new Jugador(1L, "Jugador Demo", "Equipo Demo", "DELANTERO", premier, true, true));
		jugadores.put(2L, new Jugador(2L, "Jugador Reserva", "Equipo Demo", "MEDIOCAMPISTA", liga, false, false));
	}

	@Override
	public List<Jugador> findAll() { return jugadores.values().stream().toList(); }

	@Override
	public Optional<Jugador> findById(Long id) { return Optional.ofNullable(jugadores.get(id)); }
}