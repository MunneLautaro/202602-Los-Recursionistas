package unq.losrecursionistas.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.exceptions.ControladorErrores;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursoNoEncontrado;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;
import unq.losrecursionistas.backend.service.interfaces.MercadoService;
import unq.losrecursionistas.backend.service.interfaces.UsuarioService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControladorErroresTest {

    private final JugadorService jugadorService = mock(JugadorService.class);
    private final UsuarioService usuarioService = mock(UsuarioService.class);
    private final MercadoService mercadoService = mock(MercadoService.class);

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new JugadorController(jugadorService),
                        new CreditosController(mercadoService, usuarioService),
                        new ControllerDePrueba())
                .setControllerAdvice(new ControladorErrores())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    // ---------- Bugs reportados ----------

    @Test
    void jugadorInexistenteDevuelve404() throws Exception {
        when(jugadorService.obtenerJugadorPorId(999L))
                .thenThrow(new ExcepcionRecursoNoEncontrado("No existe el jugador con el id: 999"));

        mvc.perform(get("/players/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe el jugador con el id: 999"));
    }

    @Test
    void estadisticasDeJugadorInexistenteDevuelven404() throws Exception {
        when(jugadorService.obtenerJugadorPorId(999L))
                .thenThrow(new ExcepcionRecursoNoEncontrado("No existe el jugador con el id: 999"));

        mvc.perform(get("/players/999/stats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    void usuarioComunConsultandoCreditosDeOtraCuentaRecibe403() throws Exception {
        Usuario comun = new Usuario("comun", "clave", 0.0);
        comun.setId(1L);
        when(usuarioService.buscarPorNombreUsuario("comun")).thenReturn(comun);
        autenticarComo("comun", "ROLE_USUARIO");

        mvc.perform(get("/users/2/credits"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403))
                .andExpect(jsonPath("$.mensaje").value("No puede consultar otra cuenta"));
        verify(usuarioService, never()).buscarPorId(2L);
    }

    @Test
    void accesoDenegadoSinUsuarioAutenticadoDevuelve401() throws Exception {
        mvc.perform(get("/prueba/acceso-denegado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    // ---------- Excepciones estándar de Spring MVC ----------

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/players/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    void idNoPositivoDevuelve400() throws Exception {
        mvc.perform(get("/players/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    void parametroRequeridoFaltanteDevuelve400() throws Exception {
        mvc.perform(get("/prueba/parametro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    void cuerpoIlegibleDevuelve400() throws Exception {
        mvc.perform(post("/prueba/cuerpo").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    void metodoNoSoportadoDevuelve405ConHeaderAllow() throws Exception {
        mvc.perform(put("/players/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.codigo").value(405));
    }

    @Test
    void mediaTypeNoSoportadoDevuelve415() throws Exception {
        mvc.perform(post("/prueba/cuerpo").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value(415));
    }

    // ---------- Otras excepciones ----------

    @Test
    void argumentoIlegalDevuelve400() throws Exception {
        mvc.perform(get("/prueba/argumento-ilegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El monto debe ser positivo"));
    }

    @Test
    void violacionDeIntegridadDevuelve409() throws Exception {
        mvc.perform(get("/prueba/integridad"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(409));
    }

    @Test
    void conflictoDeConcurrenciaDevuelve409() throws Exception {
        mvc.perform(get("/prueba/concurrencia"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(409));
    }

    @Test
    void errorNoControladoDevuelve500SinExponerDetallesInternos() throws Exception {
        mvc.perform(get("/prueba/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value(500))
                .andExpect(jsonPath("$.mensaje").value("Ocurrio un error inesperado"))
                .andExpect(jsonPath("$.detalle").value("Internal Server Error"));
    }

    private static void autenticarComo(String nombreUsuario, String autoridad) {
        UserDetails principal = User.withUsername(nombreUsuario).password("x").authorities(autoridad).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(autoridad))));
    }

    @RestController
    static class ControllerDePrueba {

        @GetMapping("/prueba/acceso-denegado")
        void accesoDenegado() {
            throw new AccessDeniedException("Prohibido");
        }

        @GetMapping("/prueba/parametro")
        String parametro(@RequestParam String requerido) {
            return requerido;
        }

        @PostMapping(value = "/prueba/cuerpo", consumes = MediaType.APPLICATION_JSON_VALUE)
        Map<String, Object> cuerpo(@RequestBody Map<String, Object> cuerpo) {
            return cuerpo;
        }

        @GetMapping("/prueba/argumento-ilegal")
        void argumentoIlegal() {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }

        @GetMapping("/prueba/integridad")
        void integridad() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
        }

        @GetMapping("/prueba/concurrencia")
        void concurrencia() {
            throw new PessimisticLockingFailureException("lock timeout");
        }

        @GetMapping("/prueba/inesperado")
        void inesperado() {
            throw new IllegalStateException("password=secreto en la base");
        }
    }
}
