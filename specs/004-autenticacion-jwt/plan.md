# Implementation Plan: Autenticacion JWT para la API

**Branch**: `[004-autenticacion-jwt]` | **Date**: 2026-09-22 | **Spec**: [spec.md](spec.md)

**Input**: Especificacion de autenticacion JWT para proteger los endpoints privados de la API.

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Resumen

Se incorporara autenticacion stateless con JWT HS256, login con credenciales de `Usuario`, filtro Bearer y respuestas JSON uniformes para 401 y 403. Se usara Nimbus mediante el soporte JOSE de Spring Security, con un secreto configurable por `JWT_SECRET` y expiracion de una hora. La configuracion actual se reemplazara de `permitAll` general a rutas publicas explicitas y `authenticated()` para el resto.

La estructura se alinea con la arquitectura constitucional: `security/ConfiguracionSeguridad.java` continua siendo la configuracion de Spring Security; los contratos e implementaciones de servicios viven en `service/interfaces` y `service/impl`; la implementacion y los componentes que emiten, validan o interceptan tokens permanecen en `security`. El acceso a usuarios usa el contrato de repositorio y un DAO SQL compuesto por su adaptador. No se crean `services` plural ni `security/config`.

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.7.1

**Primary Dependencies**: Spring Security existente; agregar soporte JOSE de Spring Security para Nimbus y HS256; Spring MVC, Spring Data JPA, Validation, JUnit 5, AssertJ, Mockito y MockMvc.

**Storage**: PostgreSQL en ejecucion normal; H2 en pruebas de contexto. Se conserva la unica `application.properties`.

**Testing**: JUnit 5, AssertJ, Mockito, `@SpringBootTest` y MockMvc. Tests unitarios para servicio JWT y handlers/filtro; pruebas de contexto para beans, rutas, estado stateless y login.

**Target Platform**: Servicio web Spring Boot sobre JVM 21, desarrollo Windows y CI Linux.

**Project Type**: Backend web REST MVC.

**Performance Goals**: Validacion local del JWT sin acceso a red y sin sesiones persistentes; no se fija un throughput nuevo. El login queda sujeto al coste normal de BCrypt.

**Constraints**: expiracion exacta de una hora; identificadores ASCII; una sola configuracion; sin ApiKey; no exponer secretos, tokens ni trazas; usuarios inexistentes o inhabilitados deben producir 401; no modificar ni eliminar tests existentes.

**Scale/Scope**: Toda ruta REST existente y futura fuera de las rutas publicas declaradas; el adaptador `UserDetailsService` se ubica en `service/impl` y delega en `RepositorioUsuario`.

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

Los gates pasan con las decisiones explicitadas en este plan:

- **Principio I**: configuracion, filtro, tokens y handlers permanecen en `security`; `UsuarioDetailsService` se separa en `service/interfaces` y `service/impl`; el acceso a usuarios pasa por `persistence/repository` y `persistence/sql`, sin controller directo a repository.
- **Principio III**: cada servicio tiene contrato e implementacion separados. `JwtService` no retorna DTOs ni contiene presentacion; su implementacion de seguridad permanece en `security`, mientras `UsuarioDetailsServiceImpl` vive en `service/impl` por ser un adaptador de identidad.
- **Principio V**: los errores HTTP de seguridad se resuelven con `AuthenticationEntryPoint` y `AccessDeniedHandler` bajo `security`; las excepciones de dominio nuevas, si fueran necesarias, permanecen en `exceptions`.
- **Principio VI**: DTO de login valida forma; autenticacion resuelve credenciales; el dominio conserva invariantes de usuario.
- **Principio VII**: se agregan pruebas felices y de borde para JWT, filtro, handlers, login, usuario inexistente, expiracion, firma y acceso publico; no se borran tests existentes.
- **Principio IX**: la discrepancia `service`/`services`, la ubicacion del adaptador de identidad y `configuration`/`security.config` queda resuelta explicitamente en la seccion Resumen y en `research.md`. La ambiguedad `/login` frente a `/auth/login` se resuelve con `/login` como endpoint canonico.
- **Principio X**: nombres nuevos en espanol tecnico y ASCII; nombres oficiales JWT, HS256, BCrypt y Spring Security se conservan.
- **Configuracion unica**: solo se modifica `backend/src/main/resources/application.properties`, agregando `jwt.secret=${JWT_SECRET:...}` sin perfiles.
- **ApiKey**: no se agrega, valida ni asocia ninguna ApiKey.

No hay violaciones constitucionales ni gates bloqueados.

## Estructura del proyecto

### Documentacion de esta feature

```text
specs/004-autenticacion-jwt/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Codigo fuente

```text
backend/
├── src/
│   ├── main/java/unq/losrecursionistas/backend/
│   │   ├── security/
│   │   │   ├── handlers/
│   │   │   ├── jwt/impl/
│   │   │   └── ConfiguracionSeguridad.java
│   │   ├── service/interfaces/JwtService.java
│   │   ├── service/interfaces/UsuarioDetailsService.java
│   │   ├── service/impl/UsuarioDetailsServiceImpl.java
│   │   ├── persistence/repository/interfaces/RepositorioUsuario.java
│   │   ├── persistence/repository/impl/RepositorioUsuarioImpl.java
│   │   └── persistence/sql/UsuarioDAOSQL.java
│   │   ├── controller/dto/
│   │   └── model/Usuario.java
│   ├── main/resources/application.properties
│   └── test/java/unq/losrecursionistas/backend/
│       ├── service/
│       └── security/
└── build.gradle
```

**Structure Decision**: Se mantiene el backend Gradle existente. `ConfiguracionSeguridad`, handlers, filtro y la implementacion concreta de JWT permanecen en `security`. Los contratos e implementaciones de servicios se separan en `service/interfaces` y `service/impl`; `UsuarioDetailsServiceImpl` delega en `RepositorioUsuario`. `RepositorioUsuarioImpl` recibe `UsuarioDAOSQL` por constructor y el DAO vive en `persistence/sql`.

## Decisiones de diseno

### Paquetes y responsabilidades

- `security/ConfiguracionSeguridad.java`: beans `SecurityFilterChain`, `PasswordEncoder` y `AuthenticationManager`; politica stateless y matchers publicos.
- `security/handlers/JwtAuthenticationEntryPoint.java`: respuesta 401 para autenticacion ausente o fallida.
- `security/handlers/JwtAccessDeniedHandler.java`: respuesta 403 para identidad autenticada sin autoridad.
- `security/jwt/impl/JwtAuthFilter.java`: `OncePerRequestFilter` para Bearer; una credencial invalida responde 401 inmediatamente.
- `service/interfaces/JwtService.java`: contrato `generarToken`, `extraerUsername` y `tokenValido`, sin DTOs ni presentacion.
- `service/interfaces/UsuarioDetailsService.java`: contrato del adaptador de identidad basado en Spring Security.
- `service/impl/UsuarioDetailsServiceImpl.java`: carga usuarios por nombre y construye `UserDetails` delegando exclusivamente en `RepositorioUsuario`.
- `persistence/repository/impl/RepositorioUsuarioImpl.java`: adaptador de repositorio por composicion; delega CRUD y consultas paginadas en `UsuarioDAOSQL`.
- `persistence/sql/UsuarioDAOSQL.java`: contrato Spring Data JPA para persistencia de `Usuario`.
- `security/jwt/impl/JwtServiceImpl.java`: firma, lectura y validacion HS256, con composicion de la configuracion JWT. No extiende una clase base de servicio.
- `controller`: login y sus DTOs, con `AuthenticationManager`; el controller no crea JWT directamente sino que invoca el componente de seguridad.

### Decisiones de ambiguedad

- **Opcion elegida para paquetes**: seguir la constitution y separar `service/interfaces` de `service/impl`; `security` conserva solo la infraestructura transversal de seguridad. La alternativa de dejar `UsuarioDetailsService` en `security` queda descartada porque mezcla el adaptador de identidad con la configuracion y contradice la division de servicios.
- **Endpoint de login**: `/login` es canonico porque la spec lo menciona explicitamente y coincide con la semantica de autenticacion de Spring Security. `/auth/**` se mantiene publico, pero no se crea un segundo endpoint `/auth/login` en esta feature.
- **Libreria JWT**: usar Nimbus a traves de `spring-security-oauth2-jose`, gestionado por el BOM de Spring Boot. JJWT queda descartada por introducir varias dependencias y un parser paralelo a Spring Security.
- **Identidad y permisos**: agregar al modelo/adaptador solo el estado y autoridades necesarios para `UserDetails` (usuario habilitado y autoridades existentes). Si la persistencia actual no tiene roles, el usuario autenticado recibe la autoridad minima definida por el proyecto y los endpoints con autoridad adicional deben producir 403. No se inventan roles de negocio nuevos fuera de esta frontera.
- **Secreto**: `jwt.secret=${JWT_SECRET:clave-local-de-desarrollo-de-32-bytes-minimo}` en la unica configuracion. El default permite desarrollo; documentar que produccion debe proveer `JWT_SECRET`.

### Flujo de autenticacion

1. `POST /login` valida un DTO de credenciales y delega en `AuthenticationManager`.
2. `UsuarioDetailsServiceImpl` busca por `nombreUsuario` a traves de `RepositorioUsuario`; usuario inexistente, inhabilitado o contrasena incorrecta se traduce a 401 generico.
3. El servicio JWT emite `sub`, `iat` y `exp = iat + 1h` usando HS256.
4. El filtro lee solo `Authorization: Bearer <token>`. Header ausente deja que la cadena produzca 401 en rutas privadas; header malformado, firma invalida, token vencido o usuario no cargable responde 401 directamente.
5. En rutas publicas, un Bearer invalido no vuelve obligatorio el token: el filtro continua cuando la solicitud no necesita autenticacion, sin establecer un contexto falso.
6. `SessionCreationPolicy.STATELESS` impide conservar autenticacion entre solicitudes.

### Testing previsto

- Unitarios: generacion, subject, `iat`, expiracion exacta, secreto configurable, extraccion, usuario ausente, firma invalida, malformacion y vencimiento.
- Seguridad HTTP: header ausente, esquema incorrecto, Bearer vacio, token invalido/expirado, usuario inexistente/inhabilitado, ruta publica con token invalido, 401 y 403 con JSON `{error,message}` y sin secretos.
- Contexto: beans BCrypt y `AuthenticationManager`, rutas publicas, ruta privada, filtro antes de autenticacion, sesiones stateless y login exitoso/fallido.
- No se modifican ni eliminan tests existentes; se reutiliza H2 y se evita depender de PostgreSQL para la suite.

## Plan de trabajo

### 1. Dependencias y configuracion

1. Agregar soporte JOSE/Nimbus de Spring Security en `backend/build.gradle`, sin introducir ApiKey ni una libreria JWT paralela.
2. Agregar `jwt.secret` con placeholder `JWT_SECRET` y default de desarrollo a la unica `application.properties`.
3. Implementar los beans de password encoder, authentication manager, servicio de usuarios, `SecurityFilterChain` stateless y handlers.
4. Cambiar `anyRequest().permitAll()` a `anyRequest().authenticated()` y declarar exactamente las rutas publicas de la spec, preservando Swagger/OpenAPI y health.

### 2. Servicio y flujo de login

1. Crear `service/interfaces/JwtService` con el contrato definido.
2. Crear la implementacion JWT en `security/jwt/impl`, con HS256, `JWT_SECRET`, `iat`, expiracion de una hora y manejo seguro de excepciones.
3. Completar la busqueda de usuario y el adaptador `UserDetails` respetando el modelo/persistencia existente, incluyendo el estado habilitado y autoridades necesarios.
4. Crear DTOs ASCII de login y el endpoint `POST /login`, mapeando credenciales invalidas a una respuesta generica 401.
5. Crear filtro Bearer y handlers JSON sin reutilizar un formato que agregue campos distintos de `error` y `message`.

### 3. Pruebas y verificacion

1. Agregar tests unitarios de `JwtServiceImpl` y tests de handlers/filtro según los casos borde de la spec.
2. Agregar pruebas MockMvc de login, rutas publicas, ruta privada, 401, 403 y ausencia de sesion.
3. Ejecutar `./gradlew clean test` o `gradlew.bat clean test` y la validacion de infraestructura existente.
4. Revisar que no se creen perfiles, ApiKey, secretos en respuestas ni cambios en tests existentes.

## Artefactos de diseno

- [research.md](research.md): decisiones de paquetes, dependencia JWT, estado del repositorio y riesgos resueltos.
- [data-model.md](data-model.md): entidades de identidad, claims, contexto y error de seguridad.
- [contracts/README.md](contracts/README.md) y [contracts/api-publica.md](contracts/api-publica.md): contrato HTTP de login, rutas y errores 401/403.
- [quickstart.md](quickstart.md): validacion reproducible de la feature.

## Reevaluacion constitucional posterior al diseno

- **Principios I y III**: cumplidos mediante la separacion explicita entre contratos e implementaciones de servicios, repositorio y DAO SQL, y componentes de seguridad; no se usa `services` plural ni herencia generica.
- **Principios V y VI**: cumplidos; handlers traducen seguridad en HTTP y el login valida transporte antes de autenticar.
- **Principio VII**: cumplido con cobertura de servicio, filtro, handlers, login y casos borde sin tocar tests existentes.
- **Principio IX**: las alternativas de paquetes, libreria JWT y endpoint de login quedan decididas y justificadas; no quedan aclaraciones pendientes.
- **Principio X**: toda documentacion y nombres propios nuevos son espanoles tecnicos y ASCII.
- **Configuracion unica y seguridad**: solo se modifica `application.properties`, se mantiene STATELESS y ApiKey permanece fuera de alcance.

Resultado: gates aprobados despues del diseno, sin desviaciones constitucionales pendientes.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation                  | Why Needed         | Simpler Alternative Rejected Because                                       |
| -------------------------- | ------------------ | -------------------------------------------------------------------------- |
| N/A                        | N/A                | No hay violaciones constitucionales que requieran una desviacion aprobada. |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient]                                        |
