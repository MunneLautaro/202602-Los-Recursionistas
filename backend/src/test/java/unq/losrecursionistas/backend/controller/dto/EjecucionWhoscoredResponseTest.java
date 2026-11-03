package unq.losrecursionistas.backend.controller.dto;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.controller.dto.whoscored.EjecucionWhoscoredResponse;
import unq.losrecursionistas.backend.model.EjecucionIngesta;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EjecucionWhoscoredResponseTest {

    @Test
    void convierteEjecucionYContadores() {
        EjecucionIngesta ejecucion = EjecucionIngesta.builder()
                .id(4L).estado(EjecucionIngesta.Estado.FINALIZADA)
                .alcance(EjecucionIngesta.Alcance.LIGA).ligaId(8L)
                .inicio(OffsetDateTime.now()).temporadaInicio(LocalDate.of(2025, 1, 1))
                .temporadaFin(LocalDate.of(2025, 12, 31)).jugadoresProcesados(10)
                .jugadoresVinculados(8).jugadoresNoVinculados(2).partidosInsertados(20)
                .partidosActualizados(3).errores(1).anomalias(2).motivoRechazo("ninguno")
                .detalle(Map.of("ok", true)).build();

        EjecucionWhoscoredResponse response = EjecucionWhoscoredResponse.from(ejecucion);

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.contadores().jugadoresProcesados()).isEqualTo(10);
        assertThat(response.contadores().partidosActualizados()).isEqualTo(3);
    }
}
