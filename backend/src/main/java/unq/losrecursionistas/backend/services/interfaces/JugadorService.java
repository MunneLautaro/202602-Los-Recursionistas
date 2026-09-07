package unq.losrecursionistas.backend.services.interfaces;

import org.springframework.data.domain.Page;
import unq.losrecursionistas.backend.model.Jugador;

public interface JugadorService {
	Page<Jugador> buscar(String liga, String equipo, String posicion, Boolean activo, int pagina);

	Jugador obtener(Long id);
}
