# Implementation Plan: Infraestructura base del backend y CI

**Branch**: `[001-infra-backend-ci]` | **Date**: 2026-09-19 | **Spec**: [spec.md](../spec.md)

**Input**: Feature specification from `/specs/001-infra-backend-ci/spec.md`

## Summary

Se define la estructura inicial del backend Java con Spring Boot, separando responsabilidades por capas y dejando una base para seguridad, excepciones y validaciones. Ademas, se incorpora la automatizacion de integracion continua para validar compilacion y pruebas antes de aceptar cambios.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.1.1, Spring Data JPA, Spring Validation, Spring WebMVC, PostgreSQL, Lombok, JUnit 5, Gradle

**Storage**: PostgreSQL

**Testing**: Gradle + JUnit 5 (spring-boot-starter-validation-test y spring-boot-starter-webmvc-test)

**Target Platform**: Servidor Java ejecutado en entorno Linux GitHub Actions y entornos locales de desarrollo

**Project Type**: web-service

**Performance Goals**: NEEDS CLARIFICATION; se asume un objetivo operativo basico para flujo interno con tiempos de respuesta aceptables para consultas y validaciones sin depender de un servicio externo.

**Constraints**: Un unico archivo `application.properties`, arquitectura en capas estricta, uso de nombres ASCII y manejo centralizado de errores.

**Scale/Scope**: Backend inicial con estructura extensible para futuras entidades, autenticacion, seguridad y servicios de dominio.

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

- **Arquitectura en capas**: PASS. La estructura propuesta sigue `model`, `controller`, `service`, `persistence`, `configuration`, `security` y `exceptions`.
- **Configuracion unica**: PASS. Se mantiene un solo `application.properties` y se evita perfiles separados por defecto.
- **Seguridad y Spring Security**: PASS. El paquete `security` queda reservado para autenticacion y autorizacion futuras.
- **Manejo de errores**: PASS. Se define una jerarquia runtime en `exceptions` y un `@ControllerAdvice` para respuestas consistentes.
- **Testing obligatorio**: PASS. Se define estructura base de tests en `model` y `service`.
- **CI/CD**: PASS. Se incluye un workflow de GitHub Actions en el repositorio para compilacion y tests.
- **Idioma e identificadores**: PASS. Los nombres y rutas se mantendran en espanol tecnico con ASCII seguro para la implementacion.

No se detectan violaciones de la constitution que requieran justificacion adicional.

## Project Structure

### Documentation (this feature)

```text
specs/001-infra-backend-ci/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md
└── spec.md              # Feature specification
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── unq/
│   │   │       └── losrecursionistas/
│   │   │           └── backend/
│   │   │               ├── configuration/
│   │   │               ├── controller/
│   │   │               │   └── dto/
│   │   │               ├── exceptions/
│   │   │               ├── model/
│   │   │               ├── persistence/
│   │   │               │   └── repository/
│   │   │               │       ├── interfaces/
│   │   │               │       └── impl/
│   │   │               ├── security/
│   │   │               ├── service/
│   │   │               │   ├── interfaces/
│   │   │               │   └── impl/
│   │   │               └── BackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── unq/
│               └── losrecursionistas/
│                   └── backend/
│                       ├── model/
│                       └── service/
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle

.github/
└── workflows/
    └── ci.yml
```

**Structure Decision**: Se adopta una arquitectura monolito backend con un unico modulo Java y una carpeta `backend` como punto de entrada principal. La estructura real del proyecto ya dispone de `build.gradle` y `gradlew`, por lo que el pipeline se integra con Gradle y no se introduce Maven para evitar un cambio de toolchain innecesario.

## Complexity Tracking

No hay violaciones de la constitution que requieran complejidad adicional ni justificaciones de excepcion.
