package unq.losrecursionistas.backend.model;

import java.util.Objects;

public final class Jugador {

	private final Long id;
	private final String nombre;
	private final String equipo;
	private final String posicion;
	private final Liga liga;
	private final boolean activo;
	private final boolean disponible;

	public Jugador(Long id, String nombre, String equipo, String posicion, Liga liga,
			boolean activo, boolean disponible) {
		if (nombre == null || nombre.isBlank() || liga == null) {
			throw new IllegalArgumentException("El jugador requiere nombre y liga");
		}
		this.id = id;
		this.nombre = nombre.trim();
		this.equipo = normalize(equipo);
		this.posicion = normalize(posicion);
		this.liga = liga;
		this.activo = activo;
		this.disponible = disponible;
	}

	private String normalize(String value) { return value == null ? "" : value.trim(); }
	public Long id() { return id; }
	public String nombre() { return nombre; }
	public String equipo() { return equipo; }
	public String posicion() { return posicion; }
	public Liga liga() { return liga; }
	public boolean activo() { return activo; }
	public boolean disponible() { return disponible; }

	@Override
	public boolean equals(Object object) {
		return object instanceof Jugador other && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() { return Objects.hash(id); }
}