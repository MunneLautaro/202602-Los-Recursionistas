package unq.losrecursionistas.backend.model;

import java.util.Objects;

public final class Liga {

	private final Long id;
	private final String nombre;
	private final String codigo;
	private final boolean activa;

	public Liga(Long id, String nombre, String codigo, boolean activa) {
		if (nombre == null || nombre.isBlank() || codigo == null || codigo.isBlank()) {
			throw new IllegalArgumentException("La liga requiere nombre y codigo");
		}
		this.id = id;
		this.nombre = nombre.trim();
		this.codigo = codigo.trim().toUpperCase();
		this.activa = activa;
	}

	public Long id() { return id; }
	public String nombre() { return nombre; }
	public String codigo() { return codigo; }
	public boolean activa() { return activa; }

	@Override
	public boolean equals(Object object) {
		return object instanceof Liga other && codigo.equals(other.codigo);
	}

	@Override
	public int hashCode() { return Objects.hash(codigo); }
}