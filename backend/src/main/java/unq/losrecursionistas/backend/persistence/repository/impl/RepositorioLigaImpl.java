package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.persistence.sql.LigaDAOSQL;

import java.util.List;
import java.util.Optional;

@Component
public class RepositorioLigaImpl implements RepositorioLiga {

    private final LigaDAOSQL ligaDAOSQL;

    public RepositorioLigaImpl(LigaDAOSQL ligaDAOSQL) {
        this.ligaDAOSQL = ligaDAOSQL;
    }

    @Override
    public Liga guardar(Liga liga) {
        return ligaDAOSQL.save(liga);
    }

    @Override
    public Liga buscarOModificarPorCodigoOIdExterno(String codigo, Long idExterno, String nombre) {
        Optional<Liga> ligaExistente = ligaDAOSQL.findByCodigo(codigo);
        if (ligaExistente.isPresent()) {
            Liga l = ligaExistente.get();
            if (idExterno != null) {
                l.setIdExterno(idExterno);
            }
            if (nombre != null && !nombre.isBlank()) {
                l.setNombre(nombre);
            }
            return ligaDAOSQL.save(l);
        }

        if (idExterno != null) {
            Optional<Liga> porIdExt = ligaDAOSQL.findByIdExterno(idExterno);
            if (porIdExt.isPresent()) {
                Liga l = porIdExt.get();
                l.setCodigo(codigo);
                if (nombre != null && !nombre.isBlank()) {
                    l.setNombre(nombre);
                }
                return ligaDAOSQL.save(l);
            }
        }

        Liga nuevaLiga = new Liga(idExterno, nombre, codigo);
        return ligaDAOSQL.save(nuevaLiga);
    }

    @Override
    public Liga buscarPorCodigo(String codigo) {
        return ligaDAOSQL.findByCodigo(codigo).orElseThrow(() -> new RuntimeException("Liga no encontrada con código: " + codigo));
    }

    @Override
    public List<Liga> obtenerLigas() {
        return ligaDAOSQL.findAll();
    }
}
