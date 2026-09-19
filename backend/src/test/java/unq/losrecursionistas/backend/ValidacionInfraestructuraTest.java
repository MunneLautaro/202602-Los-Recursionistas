package unq.losrecursionistas.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ValidacionInfraestructuraTest {

	private static final Path RAIZ_BACKEND = Path.of("src");

	@Test
	void mantieneUnaUnicaConfiguracionDeAplicacion() throws Exception {
		try (Stream<Path> archivos = Files.walk(RAIZ_BACKEND.resolve("main/resources"))) {
			List<Path> configuraciones = archivos.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().matches("application.*\\.properties"))
					.toList();
			assertThat(configuraciones).extracting(path -> path.getFileName().toString())
					.containsExactly("application.properties");
		}
	}

	@Test
	void rechazaIdentificadoresNoAsciiYFrameworksDePruebaNoPermitidos() throws Exception {
		Pattern identificadorNoAscii = Pattern.compile("\\b[A-Za-z_$][A-Za-z0-9_$]*[^\\x00-\\x7F][A-Za-z0-9_$]*\\b");
		Pattern frameworkProhibido = Pattern.compile("\\b(Jest|Vitest|Mocha)\\b");
		try (Stream<Path> archivos = Files.walk(RAIZ_BACKEND)) {
			for (Path archivo : archivos.filter(path -> Files.isRegularFile(path)
					&& !path.equals(Path.of("src/test/java/unq/losrecursionistas/backend/ValidacionInfraestructuraTest.java"))
					&& (path.toString().endsWith(".java") || path.toString().endsWith(".properties"))).toList()) {
				String contenido = Files.readString(archivo);
				assertThat(identificadorNoAscii.matcher(contenido).find())
						.as("identificador no ASCII en %s", archivo).isFalse();
				assertThat(frameworkProhibido.matcher(contenido).find())
						.as("framework de pruebas no permitido en %s", archivo).isFalse();
			}
		}
	}
}
