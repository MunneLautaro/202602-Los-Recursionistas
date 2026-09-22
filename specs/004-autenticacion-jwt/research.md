# Investigacion: Autenticacion JWT

## Estado actual verificado

- El backend usa Java 21, Spring Boot 4.1.1 y Gradle Wrapper 9.7.1.
- `spring-boot-starter-security` ya existe, pero no hay dependencia JWT, filtro Bearer, `UserDetailsService`, login ni controller REST.
- `security/ConfiguracionSeguridad.java` existe y actualmente deshabilita CSRF y permite toda solicitud.
- El modelo `Usuario` tiene `nombreUsuario` y `contrasena`, pero no tiene estado habilitado ni autoridades; el repositorio no expone busqueda por nombre.
- Ya existe un test que importa `security.ConfiguracionSeguridad`; moverla romperia una prueba vigente.
- `application.properties` es la unica configuracion y los tests usan H2.

## Decision: ubicacion de paquetes

**Decision**: conservar `security/ConfiguracionSeguridad.java`, ubicar los contratos de servicios en `service/interfaces`, sus implementaciones de caso de uso en `service/impl`, y mantener configuracion, implementacion JWT, filtro y handlers bajo `security`. El repositorio de usuario delega en `persistence/sql/UsuarioDAOSQL`.

**Rationale**: la constitution exige contratos e implementaciones separadas. `UsuarioDetailsServiceImpl` es un adaptador de identidad y por eso pertenece a `service/impl`; la cadena, los filtros y los tokens son infraestructura transversal de `security`. La persistencia queda aislada tras `RepositorioUsuario` y su DAO SQL.

**Alternatives considered**:

- `services.interfaces` + `security.config`: descartado porque introduce `services` plural, contradice la constitution y no existe en el repositorio.
- Mover todo a `configuration` y `service/impl`: descartado porque mezclaria la configuracion transversal con la responsabilidad de seguridad y haria que un servicio de dominio emita JWT.
- Mantener `UsuarioDetailsService` en `security`: descartado porque mezcla un contrato de servicio con la configuracion y los filtros.

## Decision: implementacion JWT

**Decision**: usar Nimbus mediante `spring-security-oauth2-jose`, con algoritmo HS256 y una clave derivada del valor `jwt.secret`.

**Rationale**: Spring Security ya es una dependencia estructural; JOSE integra decoder/parser y validacion de firma con el stack existente, evitando una segunda abstraccion JWT. El BOM de Spring Boot gestiona versiones compatibles.

**Alternatives considered**:

- JJWT: descartado para esta feature porque requiere varias dependencias y un parser separado que duplica integracion con Spring Security.
- Implementacion criptografica propia: descartada por riesgo y por incumplir el uso de una libreria probada.

## Decision: endpoint y seguridad HTTP

**Decision**: `POST /login` es el endpoint canonico. Son publicas `/auth/**`, `/login`, `/public/**`, `/swagger-ui/**`, `/v3/api-docs/**` y las variantes exactas existentes de Swagger/OpenAPI; el resto requiere autenticacion.

**Rationale**: `/login` esta explicitamente definido por la spec y no existe un endpoint previo que preservar. La cadena unica stateless evita sesiones y mantiene la configuracion observable.

**Alternatives considered**:

- Exponer tambien `/auth/login`: descartado para no crear dos contratos para la misma operacion; `/auth/**` sigue publico para futuras rutas.
- Mantener `anyRequest().permitAll()`: descartado porque deja sin proteccion todos los endpoints privados.

## Decision: secreto y claims

**Decision**: configurar `jwt.secret=${JWT_SECRET:clave-local-de-desarrollo-de-32-bytes-minimo}` en la unica `application.properties`; emitir `sub`, `iat` y `exp`, con `exp` exactamente una hora posterior a `iat`.

**Rationale**: permite desarrollo sin bloquear el arranque y permite secreto propio en produccion. Los claims cubren identificacion y vigencia requeridas sin incluir datos sensibles.

**Alternatives considered**:

- Secreto hardcodeado en Java: descartado porque impide rotacion y configuracion por entorno.
- Agregar perfiles `local`/`prod`: descartado por la constitution.

## Decision: identidad y autoridades

**Decision**: completar el adaptador de usuario para cargar por `nombreUsuario`, verificar existencia y habilitacion, y exponer las autoridades que el modelo/persistencia soporte. Si no hay roles persistidos, se usara solo la autoridad minima acordada para usuarios autenticados y se verificara 403 con una autoridad adicional no concedida; no se redefinen roles de negocio.

**Rationale**: la feature exige usuario inexistente/inhabilitado y diferencia 401 de 403, pero el modelo actual no contiene esos datos. El plan explicita el cambio minimo necesario en vez de ocultar la carencia.

**Alternatives considered**:

- Considerar todo usuario habilitado y sin autoridades: no cubre el caso de usuario inhabilitado ni permite probar autorizacion diferenciada.
- Inventar un sistema completo de roles de negocio: descartado por exceder el alcance.

## Riesgos y controles

- Contraseñas existentes posiblemente no estan hasheadas: el login debe asumir almacenamiento BCrypt para nuevos datos y documentar cualquier migracion de datos necesaria; nunca devolver la contrasena.
- No hay endpoints privados de negocio: las pruebas deben usar un endpoint de prueba o una configuracion de test que no se publique en produccion.
- Tokens invalidos en rutas publicas no deben bloquear la ruta; el filtro debe dejar continuar cuando la cadena no exige autenticacion.
- Las excepciones de parsing, firma y expiracion deben capturarse antes del endpoint y traducirse a 401 sin stack trace.
