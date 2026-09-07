package unq.losrecursionistas.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void validaAltaDeUsuario() throws Exception {
		mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"ana\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.apiKey").isNotEmpty());
	}
}