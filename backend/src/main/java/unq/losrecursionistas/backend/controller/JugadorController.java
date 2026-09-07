package unq.losrecursionistas.backend.controller;

import org.springframework.http.ResponseEntity;
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
		return ResponseEntity.ok(service.buscar(liga, equipo, posicion, activo, page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<JugadorResponse> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(service.obtener(id));
	}
}