# Mercado de Tokens de Jugadores

Backend Spring Boot 4.1.1 con Java 21 y Gradle.

## Ejecución

1. Levantar PostgreSQL local con la base `mercado_tokens`.
2. Configurar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` como variables de entorno. En PowerShell, para la sesión actual:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/mercado_tokens"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "TU_PASSWORD_DE_POSTGRES"
$env:JWT_SECRET = "una-clave-local-larga"
```

La contraseña debe ser la que se definió al instalar PostgreSQL; no necesariamente es `postgres`. 3. Ejecutar desde `backend/`:

```powershell
.\gradlew.bat bootRun
```

La documentación OpenAPI queda disponible en `/swagger-ui.html` y `/v3/api-docs`.
El catálogo `GET /players` es público; las operaciones restantes requieren JWT.

## Validación

```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

SonarCloud usa `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION` y `SONAR_TOKEN`; nunca se versionan secretos.

# 202602-Los-Recursionistas
