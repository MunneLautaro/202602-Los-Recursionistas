package unq.losrecursionistas.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import unq.losrecursionistas.backend.configuration.CorrelationIdFilter;

@SpringBootTest
@AutoConfigureMockMvc
class JugadorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listaJugadoresYDevuelveCorrelationId() throws Exception {
		mockMvc.perform(get("/players").header(CorrelationIdFilter.HEADER, "test-correlation"))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.HEADER, "test-correlation"))
				.andExpect(jsonPath("$.contenido[0].nombre").value("Jugador Demo"));
	}

	@Test
	void rechazaJugadorInexistente() throws Exception {
		mockMvc.perform(get("/players/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
	}

	@Test
	void publicaOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Mercado de Tokens de Jugadores API"));
	}
}