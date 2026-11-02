package unq.losrecursionistas.backend.controller.dto.usuario;

import unq.losrecursionistas.backend.model.Usuario;
import java.math.BigDecimal;

public record SaldoResponse(Long usuarioId, BigDecimal saldo, BigDecimal saldoReservado,
                            BigDecimal saldoDisponible) {
    public static SaldoResponse desde(Usuario usuario) {
        return new SaldoResponse(usuario.getId(), dinero(usuario.getSaldo()),
                dinero(usuario.getSaldoReservado()), dinero(usuario.saldoDisponible()));
    }

    private static BigDecimal dinero(Double valor) {
        return BigDecimal.valueOf(valor == null ? 0.0 : valor);
    }
}
