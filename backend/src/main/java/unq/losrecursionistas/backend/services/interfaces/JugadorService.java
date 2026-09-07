package unq.losrecursionistas.backend.services.interfaces;

import unq.losrecursionistas.backend.controller.dto.JugadorPageResponse;
import unq.losrecursionistas.backend.controller.dto.JugadorResponse;

public interface JugadorService {
	JugadorPageResponse buscar(String liga, String equipo, String posicion, Boolean activo,
			int pagina, int tamano);

	JugadorResponse obtener(Long id);
}
