package unq.losrecursionistas.backend.services.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import unq.losrecursionistas.backend.controller.dto.JugadorPageResponse;
import unq.losrecursionistas.backend.controller.dto.JugadorResponse;
import unq.losrecursionistas.backend.exceptions.DomainException;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.JugadorRepository;
import unq.losrecursionistas.backend.services.interfaces.JugadorService;

@Service
public class JugadorServiceImpl implements JugadorService {

	private final JugadorRepository repository;

	public JugadorServiceImpl(JugadorRepository repository) { this.repository = repository; }

	@Override
	public JugadorPageResponse buscar(String liga, String equipo, String posicion, Boolean activo,
			int pagina, int tamano) {
		if (pagina < 0 || tamano < 1 || tamano > 100) {
			throw new DomainException("PARAMETROS_INVALIDOS", "La paginacion no es valida");
		}
		List<Jugador> filtrados = repository.findAll().stream()
				.filter(jugador -> liga == null || jugador.liga().nombre().equalsIgnoreCase(liga.trim()))
				.filter(jugador -> equipo == null || jugador.equipo().equalsIgnoreCase(equipo.trim()))
				.filter(jugador -> posicion == null || jugador.posicion().equalsIgnoreCase(posicion.trim()))
				.filter(jugador -> activo == null || jugador.activo() == activo)
				.sorted(Comparator.comparing(Jugador::id))
				.toList();
		int inicio = Math.min(pagina * tamano, filtrados.size());
		int fin = Math.min(inicio + tamano, filtrados.size());
		return new JugadorPageResponse(filtrados.subList(inicio, fin).stream().map(JugadorResponse::from).toList(),
				pagina, tamano, filtrados.size());
	}

	@Override
	public JugadorResponse obtener(Long id) {
		return repository.findById(id).map(JugadorResponse::from)
				.orElseThrow(() -> new DomainException("RECURSO_NO_ENCONTRADO", "El jugador no existe"));
	}
}