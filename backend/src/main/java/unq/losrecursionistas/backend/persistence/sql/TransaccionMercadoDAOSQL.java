package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.TransaccionMercado;

@Repository
public interface TransaccionMercadoDAOSQL extends JpaRepository<TransaccionMercado, Long> {
    Page<TransaccionMercado> findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc(
            Long compradorId, Long vendedorId, Pageable pageable);
}
