package unq.losrecursionistas.backend.model;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ApiKey {

	private Long id;
	private Long usuarioId;
	@ToString.Exclude
	private String valor;

	public ApiKey(Long usuarioId, String valor) {
		this.usuarioId = usuarioId;
		this.valor = valor;
	}

	public String apiKey() {
		return valor;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof ApiKey other)) return false;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}