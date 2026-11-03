package unq.losrecursionistas.backend.security.jwt.impl;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import unq.losrecursionistas.backend.service.interfaces.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuaSinBearer() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UserDetailsService users = mock(UserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void autenticaTokenValido() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UserDetailsService users = mock(UserDetailsService.class);
        var user = User.withUsername("ana").password("x").roles("USER").build();
        when(jwt.extraerUsername("token")).thenReturn(java.util.Optional.of("ana"));
        when(users.loadUserByUsername("ana")).thenReturn(user);
        when(jwt.tokenValido("token", user)).thenReturn(true);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("ana");
    }

    @Test
    void ignoraTokenVacioUsuarioDeshabilitadoYContextoExistente() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UserDetailsService users = mock(UserDetailsService.class);
        FilterChain chain = mock(FilterChain.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest empty = new MockHttpServletRequest();
        empty.addHeader("Authorization", "Bearer ");
        filter.doFilter(empty, new MockHttpServletResponse(), chain);
        verify(chain).doFilter(any(), any());

        SecurityContextHolder.getContext().setAuthentication(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                        "existing", null, java.util.List.of()));
        MockHttpServletRequest existing = new MockHttpServletRequest();
        existing.addHeader("Authorization", "Bearer token");
        filter.doFilter(existing, new MockHttpServletResponse(), chain);
        verifyNoInteractions(jwt, users);
    }

    @Test
    void limpiaContextoCuandoElServicioFalla() throws Exception {
        JwtService jwt = mock(JwtService.class);
        when(jwt.extraerUsername("bad")).thenThrow(new IllegalStateException("bad"));
        UserDetailsService users = mock(UserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");

        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
