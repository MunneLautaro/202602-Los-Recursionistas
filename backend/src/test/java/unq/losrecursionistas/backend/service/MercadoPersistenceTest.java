package unq.losrecursionistas.backend.service;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import unq.losrecursionistas.backend.model.EstadoOrden;
import unq.losrecursionistas.backend.model.TenenciaToken;
import unq.losrecursionistas.backend.model.TipoOrden;
import unq.losrecursionistas.backend.persistence.sql.OrdenDAOSQL;
import unq.losrecursionistas.backend.persistence.sql.TransaccionMercadoDAOSQL;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoPersistenceTest {
    @Test
    void holdingsHaveUniqueUserPlayerConstraint() {
        assertThat(TenenciaToken.class.getAnnotation(jakarta.persistence.Table.class)
                .uniqueConstraints()).anyMatch(constraint ->
                constraint.name().equals("uk_tenencia_usuario_jugador"));
    }

    @Test
    void activeOrderQueriesUseLockedMatchingAndFifo() throws NoSuchMethodException {
        var method = OrdenDAOSQL.class.getMethod("buscarCompatiblesBloqueadas",
                Long.class, TipoOrden.class, java.math.BigDecimal.class,
                EstadoOrden.class, Long.class);
        assertThat(method.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(method.getAnnotation(Query.class).value())
                .contains("o.estado = :estado", "order by o.fechaCreacion asc, o.id asc");
        assertThat(OrdenDAOSQL.class.getMethod(
                "findFirstByUsuarioIdAndJugadorIdAndEsLiquidezInicialTrue",
                Long.class, Long.class).getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void transactionQueriesCoverBuyerAndSellerHistory() throws NoSuchMethodException {
        assertThat(TransaccionMercadoDAOSQL.class.getMethod(
                "findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc",
                Long.class, Long.class, org.springframework.data.domain.Pageable.class))
                .isNotNull();
    }
}
