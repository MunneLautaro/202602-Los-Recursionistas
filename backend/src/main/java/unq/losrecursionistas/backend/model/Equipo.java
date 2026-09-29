package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idExterno;
    private String nombre;
    private String nombreCorto;
    private String sigla;
    private String escudoUrl;
    private Integer fundacion;
    private String colores;
    private String estadio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liga_id")
    private Liga liga;

    @Builder.Default
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Jugador> jugadores = new ArrayList<>();

    public Equipo(String nombre, String nombreCorto, String sigla, String escudoUrl, Integer fundacion, String colores, String estadio) {
        this.nombre = nombre;
        this.nombreCorto = nombreCorto;
        this.sigla = sigla;
        this.escudoUrl = escudoUrl;
        this.fundacion = fundacion;
        this.colores = colores;
        this.estadio = estadio;
    }
}
