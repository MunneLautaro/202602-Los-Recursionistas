package unq.losrecursionistas.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ArquitecturaBaseTest {

	private static final Path RUTA_FUENTES = Path.of("src", "main", "java");

	@Test
	void lasDeclaracionesDePaqueteCoincidenConLaRutaDeLaFuente() throws IOException {
		try (Stream<Path> fuentes = Files.walk(RUTA_FUENTES)) {
			fuentes.filter(this::esFuenteJava).forEach(this::verificarPaquete);
		}
	}

	@Test
	void losControladoresNoImportanRepositoriosDirectamente() throws IOException {
		Path rutaController = RUTA_FUENTES.resolve(Path.of(
				"unq", "losrecursionistas", "backend", "controller"));

		if (!Files.exists(rutaController)) {
			return;
		}

		try (Stream<Path> fuentes = Files.walk(rutaController)) {
			fuentes.filter(this::esFuenteJava).forEach(this::verificarSinAccesoDirectoARepositorio);
		}
	}

	private boolean esFuenteJava(Path ruta) {
		return Files.isRegularFile(ruta) && ruta.toString().endsWith(".java");
	}

	private void verificarPaquete(Path ruta) {
		try {
			String contenido = Files.readString(ruta);
			String paqueteEsperado = paqueteDesdeRuta(ruta);
			assertThat(contenido)
					.contains("package " + paqueteEsperado + ";");
		} catch (IOException excepcion) {
			throw new AssertionError("No se pudo leer " + ruta, excepcion);
		}
	}

	private void verificarSinAccesoDirectoARepositorio(Path ruta) {
		try {
			assertThat(Files.readString(ruta))
					.doesNotContain("import unq.losrecursionistas.backend.persistence.repository");
		} catch (IOException excepcion) {
			throw new AssertionError("No se pudo leer " + ruta, excepcion);
		}
	}

	private String paqueteDesdeRuta(Path ruta) {
		Path relativo = RUTA_FUENTES.relativize(ruta.getParent());
		return relativo.toString()
				.replace('\\', '.')
				.replace('/', '.');
	}
}
