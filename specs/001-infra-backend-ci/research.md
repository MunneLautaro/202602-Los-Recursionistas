# Research: Infraestructura base del backend y CI

## Decision 1: mantener el stack actual de Java y Gradle

**Decision**: Se conserva Java 21 con Spring Boot y Gradle, porque el repositorio ya define `build.gradle`, `settings.gradle` y el wrapper de Gradle.

**Rationale**: El proyecto ya tiene una base funcional y una configuracion inicial. Cambiar a Maven o a otro stack introduciria costo adicional sin beneficio para la infraestructura base solicitada.

**Alternatives considered**:

- Maven con `mvnw`: se descarta por inconsistencia con la base actual del repositorio.
- Spring Boot con perfiles separados: se descarta porque la constitution exige una sola configuracion base.

## Decision 2: una sola configuracion base

**Decision**: La aplicacion usa un unico archivo `backend/src/main/resources/application.properties` para la configuracion inicial.

**Rationale**: La constitution exige no perfiles ni archivos adicionales salvo aprobacion expresa. Esto reduce riesgo de errores ambientales y facilita mantener valores base en un solo lugar.

**Alternatives considered**:

- application-dev.properties y application-prod.properties: se descarta por incumplir la regla del proyecto.
- variables de entorno exclusivamente: se descarta como estrategia inicial por falta de estandar en el repositorio.

## Decision 3: dividir la arquitectura en capas

**Decision**: Se implementa la estructura en paquetes: `model`, `controller/dto`, `service/interfaces`, `service/impl`, `persistence/repository/interfaces`, `persistence/repository/impl`, `configuration`, `security` y `exceptions`.

**Rationale**: La constitution exige un esquema claro para evitar acoplamientos y facilitar crecimiento sin reestructuracion.

**Alternatives considered**:

- mezclar servicios y repositorios directamente en `controller`: se rechaza por violar la separacion de responsabilidades.
- poner excepciones y seguridad en un paquete unico `common`: se rechaza por no reflejar la arquitectura requerida.

## Decision 4: manejo inicial de errores y seguridad

**Decision**: La infraestructura inicial define una jerarquia de runtime exceptions y un `@ControllerAdvice` dentro de `exceptions`. El paquete `security` queda preparado para autenticacion y autorizacion futuras.

**Rationale**: Esto permite recolectar fallas de validacion y dominio con una respuesta HTTP consistente sin bloquear el crecimiento futuro.

**Alternatives considered**:

- devolver errores planos sin formato: se rechaza porque dificulta diagnostico y adopcion por clientes.
- implementar autenticacion completa desde el inicio: se rechaza como alcance adicional no requerido para la base del sistema.

## Decision 5: CI con GitHub Actions y Gradle

**Decision**: El workflow usa `./gradlew clean test` dentro de `backend` y valida la compilacion y pruebas en GitHub Actions.

**Rationale**: El repositorio ya usa Gradle, por lo que se mantiene la herramienta actual y se reduce el riesgo de cambios de build system.

**Alternatives considered**:

- Maven con `./mvnw clean test`: se descarta por no coincidir con la base existente.
- ejecutar solo compilacion: se descarta porque la constitution exige pruebas iniciales.

## Decision 6: credenciales de base de datos por defecto

**Decision**: Para la configuracion inicial de desarrollo se asume PostgreSQL con usuario `postgres` y password `root`.

**Rationale**: El usuario del feature especifica estos valores como configuracion base, y la estructura del proyecto ya prevé un backend con PostgreSQL.

**Alternatives considered**:

- no documentar credenciales: se descarta porque el entorno local no quedaria operativo de manera reproducible.
- perfiles separados: se descarta por la regla unica de configuracion.

## Resultado de clarificaciones

No quedan `NEEDS CLARIFICATION` pendientes. Las decisiones tecnicas criticas se resolvieron con supuestos basados en la base actual del repositorio y en la constitution del proyecto.
