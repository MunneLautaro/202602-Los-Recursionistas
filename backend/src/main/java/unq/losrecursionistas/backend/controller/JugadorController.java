package unq.losrecursionistas.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import unq.losrecursionistas.backend.controller.dto.JugadorPageResponse;
import unq.losrecursionistas.backend.controller.dto.JugadorResponse;
import unq.losrecursionistas.backend.services.interfaces.JugadorService;

@RestController
@RequestMapping("/players")
public class JugadorController {

	private final JugadorService service;

	public JugadorController(JugadorService service) { this.service = service; }

	@GetMapping
	public ResponseEntity<JugadorPageResponse> listar(
			@RequestParam(required = false) String liga,
			@RequestParam(required = false) String equipo,
			@RequestParam(required = false) String posicion,
			@RequestParam(required = false) Boolean activo,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Page<unq.losrecursionistas.backend.model.Jugador> resultado = service.buscar(liga, equipo, posicion, activo, page);
		Page<JugadorResponse> paginaDTOs = resultado.map(JugadorResponse::from);
		JugadorPageResponse response = new JugadorPageResponse(paginaDTOs.getContent(), paginaDTOs.getNumber(),
				paginaDTOs.getSize(), paginaDTOs.getTotalElements());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<JugadorResponse> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(JugadorResponse.from(service.obtener(id)));
	}
}