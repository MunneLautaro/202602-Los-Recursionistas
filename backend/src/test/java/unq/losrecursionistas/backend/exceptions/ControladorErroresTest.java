package unq.losrecursionistas.backend.exceptions;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControladorErroresTest {

    private final ControladorErrores controlador = new ControladorErrores();

    @Mock
    private BindingResult bindingResult;

    @Test
    void manejaLasExcepcionesDeDominio() {
        assertThat(controlador.manejarValidacion(new ExcepcionValidacion("invalido")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(controlador.manejarDominio(new ExcepcionDominio("dominio")).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(controlador.manejarRecursoNoEncontrado(new ExcepcionRecursoNoEncontrado("no")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(controlador.manejarCotizacionActiva(new ExcepcionCotizacionActiva()).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void manejaArgumentosInvalidosConDetalleDeCampos() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.List.of(new FieldError("request", "monto", "debe ser positivo")));

        RespuestaError error = controlador.manejarArgumentoInvalido(exception).getBody();

        assertThat(error).isNotNull();
        assertThat(error.detalle()).isEqualTo("monto: debe ser positivo");
    }

    @Test
    void usaMensajeDeLaExcepcionCuandoNoHayErroresDeCampos() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());
        when(exception.getMessage()).thenReturn("argumento invalido");

        RespuestaError error = controlador.manejarArgumentoInvalido(exception).getBody();

        assertThat(error).isNotNull();
        assertThat(error.detalle()).isEqualTo("argumento invalido");
    }

    @Test
    void manejaErrorSinMensajeYObtieneLaCausaRaiz() {
        Exception root = new IllegalStateException();
        Exception exception = new Exception(null, root);

        RespuestaError error = controlador.manejarErrorNoControlado(exception).getBody();

        assertThat(error).isNotNull();
        assertThat(error.mensaje()).isEqualTo("Exception");
        assertThat(error.detalle()).isEqualTo("java.lang.IllegalStateException");
    }

    @Test
    void conservaMensajeYMensajeDeLaCausaRaiz() {
        RespuestaError error = controlador.manejarErrorNoControlado(
                new IllegalArgumentException("mensaje original")).getBody();

        assertThat(error).isNotNull();
        assertThat(error.mensaje()).isEqualTo("mensaje original");
        assertThat(error.detalle()).isEqualTo("mensaje original");
    }
}
