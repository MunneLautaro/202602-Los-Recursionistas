package unq.losrecursionistas.backend.service.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCotizacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioOrden;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTenenciaToken;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTransaccionMercado;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;

/** Repositorios que consulta el resumen de negocio del panel de observabilidad. */
@Component
public record RepositoriosObservabilidad(RepositorioOrden ordenes, RepositorioTransaccionMercado transacciones,
                                         RepositorioTenenciaToken tenencias, RepositorioUsuario usuarios,
                                         RepositorioCotizacion cotizaciones) {
}
