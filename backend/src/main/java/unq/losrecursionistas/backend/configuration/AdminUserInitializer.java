package unq.losrecursionistas.backend.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;

@Component
@Order(1)
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(RepositorioUsuario repositorioUsuario, PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!repositorioUsuario.existeElUsuario(DEFAULT_ADMIN_USERNAME)) {
            LOGGER.info("Inicializando usuario administrador por defecto...");
            Usuario admin = new Usuario(
                    DEFAULT_ADMIN_USERNAME,
                    passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                    100000.0
            );
            admin.getAutoridades().add(ROLE_ADMIN);
            repositorioUsuario.crearUsuario(admin);
            LOGGER.info("Usuario administrador '{}' creado exitosamente con rol {}.", DEFAULT_ADMIN_USERNAME, ROLE_ADMIN);
        } else {
            LOGGER.info("El usuario administrador '{}' ya existe.", DEFAULT_ADMIN_USERNAME);
        }
    }
}

