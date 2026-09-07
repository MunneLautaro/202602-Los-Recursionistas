package unq.losrecursionistas.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rechazaApiKeyInvalida() throws Exception {
		mockMvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"no-existe\",\"apiKey\":\"invalid\"}"))
				.andExpect(status().isUnauthorized());
	}
}