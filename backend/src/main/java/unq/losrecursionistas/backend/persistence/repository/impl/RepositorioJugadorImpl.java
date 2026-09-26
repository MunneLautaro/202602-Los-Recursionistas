package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.persistence.sql.JugadorDAOSQL;

import java.util.List;

@Component
public class RepositorioJugadorImpl implements RepositorioJugador {

    private final JugadorDAOSQL jugadorDAOSQL;

    public RepositorioJugadorImpl(JugadorDAOSQL jugadorDAOSQL) {
        this.jugadorDAOSQL = jugadorDAOSQL;
    }

    @Override
    public void guardarTodos(List<Jugador> jugadores) {
        for (Jugador j : jugadores) {
            if (j.getIdExterno() != null) {
                jugadorDAOSQL.findByIdExterno(j.getIdExterno())
                        .ifPresent(existente -> j.setId(existente.getId()));
            }
        }
        jugadorDAOSQL.saveAll(jugadores);
    }
}
