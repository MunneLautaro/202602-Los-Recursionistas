---
description: "Tareas de implementacion de autenticacion JWT"
---

# Tareas: Autenticacion JWT para la API

**Entrada**: Documentos de diseno en `/specs/004-autenticacion-jwt/`

**Prerequisitos**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/` y `quickstart.md`.

**Organizacion**: Las tareas estan agrupadas por historia de usuario para permitir implementacion y validacion incremental.

## Fase 1: Setup (infraestructura compartida)

**Objetivo**: preparar dependencias y configuracion comun sin crear perfiles ni ApiKey.

- [x] T001 Agregar el soporte JOSE/Nimbus compatible con Spring Security en `backend/build.gradle`, sin agregar JJWT ni dependencias de ApiKey.
- [x] T002 [P] Agregar `jwt.secret=${JWT_SECRET:clave-local-de-desarrollo-de-32-bytes-minimo}` a `backend/src/main/resources/application.properties`, manteniendo una unica configuracion.
- [x] T003 [P] Verificar en `backend/src/test/java/unq/losrecursionistas/backend/ValidacionInfraestructuraTest.java` que los nuevos paquetes, propiedades e identificadores respeten la restriccion ASCII y la ausencia de perfiles adicionales.

## Fase 2: Foundational (prerequisitos bloqueantes)

**Objetivo**: dejar disponibles la identidad, persistencia y beans base que requieren las tres historias.

**Checkpoint**: no comenzar las historias hasta que el usuario pueda cargarse por nombre y existan BCrypt, `AuthenticationManager` y una cadena stateless base.

- [x] T004 Implementar la busqueda por `nombreUsuario` en `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/interfaces/RepositorioUsuario.java`, y delegarla desde `backend/src/main/java/unq/losrecursionistas/backend/persistence/repository/impl/RepositorioUsuarioImpl.java` a `backend/src/main/java/unq/losrecursionistas/backend/persistence/sql/UsuarioDAOSQL.java`, preservando la regla "Obligatorio, unico y usado como subject del JWT".
- [x] T005 Ajustar `backend/src/main/java/unq/losrecursionistas/backend/model/Usuario.java` y su persistencia para exponer `habilitado` y autoridades sin exponer `contrasena`, manteniendo que "Debe ser verdadero para autenticar; un usuario inhabilitado recibe 401".
- [x] T006 [P] Crear el contrato `UsuarioDetailsService` en `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/UsuarioDetailsService.java` y su implementacion `UsuarioDetailsServiceImpl` en `backend/src/main/java/unq/losrecursionistas/backend/service/impl/UsuarioDetailsServiceImpl.java`, cargando por `nombreUsuario` y unificando usuario inexistente, inhabilitado y credencial invalida como fallos no reveladores.
- [x] T007 [P] Crear pruebas de carga de identidad en `backend/src/test/java/unq/losrecursionistas/backend/service/UsuarioDetailsServiceTest.java` para usuario existente, usuario inexistente, usuario inhabilitado y autoridades asignadas.
- [x] T008 Declarar `PasswordEncoder` BCrypt y `AuthenticationManager` mediante `AuthenticationConfiguration` en `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java`.
- [x] T009 Configurar en `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java` CSRF deshabilitado, `SessionCreationPolicy.STATELESS` y una cadena base que no persista autenticacion; mantener `ConfiguracionSeguridad` en `security` para no romper `backend/src/test/java/unq/losrecursionistas/backend/service/ConfiguracionSeguridadTest.java`.
- [x] T010 [P] Preparar fixtures H2 y utilidades de MockMvc en `backend/src/test/java/unq/losrecursionistas/backend/security/SeguridadTestConfig.java` para usuarios BCrypt, autoridades y una ruta privada de prueba sin publicarla en produccion.

## Fase 3: Historia de Usuario 1 - Iniciar sesion y obtener credenciales (Prioridad: P1) 🎯 MVP

**Objetivo**: autenticar credenciales validas en `POST /login` y emitir un JWT HS256 utilizable durante una hora.

**Prueba independiente**: con un usuario registrado, credenciales validas devuelven un token cuyo `sub` es `nombreUsuario`, contiene `iat` y `exp = iat + 1 hora`; credenciales ausentes o invalidas devuelven 401 generico sin revelar si el usuario existe.

### Tests de la Historia de Usuario 1

- [x] T011 [P] [US1] Crear pruebas unitarias de `JwtServiceImpl` en `backend/src/test/java/unq/losrecursionistas/backend/security/JwtServiceImplTest.java` para generacion HS256, `sub`, `iat`, expiracion exacta de una hora, extraccion, subject ausente, token malformado, firma invalida y token expirado.
- [x] T012 [P] [US1] Crear prueba de contrato de `POST /login` en `backend/src/test/java/unq/losrecursionistas/backend/security/LoginAutenticacionTest.java` para respuesta 200 con `{token}`, credenciales invalidas/incompletas con 401 y ausencia de contrasena, secreto o trazas.

### Implementacion de la Historia de Usuario 1

- [x] T013 [US1] Crear el contrato `JwtService` en `backend/src/main/java/unq/losrecursionistas/backend/service/interfaces/JwtService.java` con `generarToken`, `extraerUsername` y `tokenValido`, sin DTOs ni logica de presentacion.
- [x] T014 [US1] Implementar `JwtServiceImpl` en `backend/src/main/java/unq/losrecursionistas/backend/security/jwt/impl/JwtServiceImpl.java` usando Nimbus/JOSE, HS256, `jwt.secret`, claim `sub`, `iat` y `exp` exactamente una hora posterior a `iat`, capturando errores de parsing y firma como validacion fallida.
- [x] T015 [US1] Crear los DTOs `CredencialesLoginDto` y `RespuestaTokenDto` en `backend/src/main/java/unq/losrecursionistas/backend/controller/dto/`, validando que `nombreUsuario` y `contrasena` sean obligatorios y no vacios y sin incluir credenciales en la respuesta.
- [x] T016 [US1] Implementar el controller de login en `backend/src/main/java/unq/losrecursionistas/backend/controller/AutenticacionController.java` para `POST /login`, delegando en `AuthenticationManager` y `JwtService`, y traduciendo todo fallo de autenticacion a 401 generico.
- [x] T017 [US1] Permitir `POST /login` en `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java` y conectar el DTO validado con el `UserDetailsService` BCrypt sin crear un endpoint `/auth/login` adicional.

**Checkpoint**: la Historia de Usuario 1 queda demostrable con login exitoso, token verificable y rechazo seguro de credenciales invalidas.

## Fase 4: Historia de Usuario 2 - Acceder a recursos privados con un token valido (Prioridad: P1)

**Objetivo**: procesar `Authorization: Bearer <jwt>`, cargar la identidad en `SecurityContext` y bloquear antes del endpoint cualquier token invalido.

**Prueba independiente**: una ruta privada recibe la identidad con un Bearer valido; sin header, con esquema incorrecto, Bearer vacio, token malformado, firma invalida, vencido o usuario inexistente recibe 401 y el endpoint no se ejecuta.

### Tests de la Historia de Usuario 2

- [x] T018 [P] [US2] Crear pruebas del filtro en `backend/src/test/java/unq/losrecursionistas/backend/security/JwtAuthFilterTest.java` para header ausente, esquema no Bearer, token vacio, token malformado, firma invalida, expiracion, subject ausente, usuario inexistente, usuario inhabilitado y contexto autenticado.
- [x] T019 [P] [US2] Crear prueba MockMvc de ruta privada en `backend/src/test/java/unq/losrecursionistas/backend/security/AccesoPrivadoJwtTest.java` que compruebe 401 JSON y que el endpoint no se alcanza ante credenciales invalidas.

### Implementacion de la Historia de Usuario 2

- [x] T020 [US2] Implementar `JwtAuthFilter` en `backend/src/main/java/unq/losrecursionistas/backend/security/jwt/impl/JwtAuthFilter.java` como `OncePerRequestFilter`, extrayendo solo el esquema Bearer, validando el token, cargando el usuario y estableciendo `SecurityContext` sin dejar escapar excepciones.
- [x] T021 [US2] Conectar `JwtAuthFilter` antes de `UsernamePasswordAuthenticationFilter` en `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java`, manteniendo que un token invalido en ruta publica no convierta el token opcional en requisito.
- [x] T022 [US2] Configurar `anyRequest().authenticated()` y la respuesta 401 para rutas privadas en `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java`, preservando la politica stateless y evitando cualquier ApiKey.

**Checkpoint**: la Historia de Usuario 2 queda demostrable con autenticacion Bearer valida y rechazo inmediato de todos los casos de borde definidos.

## Fase 5: Historia de Usuario 3 - Aplicar acceso publico y comunicar falta de permisos (Prioridad: P1)

**Objetivo**: mantener publicas las rutas declaradas, diferenciar 401 de 403 y evitar sesiones persistentes.

**Prueba independiente**: `/auth/**`, `/login`, `/public/**`, `/swagger-ui/**` y `/v3/api-docs/**` responden sin token; una identidad autenticada sin autoridad suficiente recibe 403 JSON diferenciado; cualquier ruta privada sin JWT recibe 401.

### Tests de la Historia de Usuario 3

- [x] T023 [P] [US3] Crear pruebas unitarias de handlers en `backend/src/test/java/unq/losrecursionistas/backend/security/JwtHandlersTest.java` para JSON `{error,message}`, status 401/403, `Content-Type` y ausencia de token, secreto, contrasena y stack trace.
- [x] T024 [P] [US3] Crear pruebas MockMvc de rutas publicas y autorizacion en `backend/src/test/java/unq/losrecursionistas/backend/security/RutasSeguridadTest.java` para todas las rutas publicas, Bearer invalido opcional, 401 privado, 403 sin autoridad y ausencia de sesion entre solicitudes.

### Implementacion de la Historia de Usuario 3

- [x] T025 [P] [US3] Implementar `JwtAuthenticationEntryPoint` en `backend/src/main/java/unq/losrecursionistas/backend/security/handlers/JwtAuthenticationEntryPoint.java` con HTTP 401 y JSON simple `{error,message}` sin datos sensibles.
- [x] T026 [P] [US3] Implementar `JwtAccessDeniedHandler` en `backend/src/main/java/unq/losrecursionistas/backend/security/handlers/JwtAccessDeniedHandler.java` con HTTP 403 y JSON diferenciado `{error: forbidden, message: ...}` sin datos sensibles.
- [x] T027 [US3] Configurar ambos handlers y los matchers `permitAll` para `/auth/**`, `/login`, `/public/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`, `/v3/api-docs` y `/actuator/health` en `backend/src/main/java/unq/losrecursionistas/backend/security/ConfiguracionSeguridad.java`.
- [x] T028 [US3] Configurar una autoridad restringida solo para la fixture de pruebas en `backend/src/test/java/unq/losrecursionistas/backend/security/SeguridadTestConfig.java` y verificar 403 para identidades autenticadas sin permiso, sin agregar roles de negocio al modelo productivo.

**Checkpoint**: la Historia de Usuario 3 queda demostrable con rutas publicas, 401/403 diferenciados y estado stateless.

## Fase 6: Polish y preocupaciones transversales

**Objetivo**: completar validacion, documentacion y controles de seguridad sin ampliar el alcance.

- [x] T029 [P] Actualizar la documentacion OpenAPI del login y sus respuestas en `backend/src/main/java/unq/losrecursionistas/backend/controller/AutenticacionController.java`, sin documentar secretos ni tokens completos.
- [x] T030 Revisar `backend/src/main/java/unq/losrecursionistas/backend/security/`, `backend/src/main/java/unq/losrecursionistas/backend/service/` y `backend/src/main/java/unq/losrecursionistas/backend/persistence/` para confirmar que no exista generacion, validacion o asociacion de ApiKey, que los DAOs solo contengan acceso a datos y que todos los identificadores nuevos sean ASCII.
- [x] T031 Ejecutar `backend/gradlew.bat clean test` y corregir solo regresiones de esta feature, preservando los tests existentes.
- [x] T032 Ejecutar los escenarios de `specs/004-autenticacion-jwt/quickstart.md` con H2 y revisar que las respuestas 401/403 no incluyan secretos, credenciales, tokens completos ni trazas.

## Dependencias y orden de ejecucion

### Dependencias por fase

- **Setup (Fase 1)**: no tiene dependencias; T001 debe completarse antes de compilar cualquier clase JOSE y T002 antes de probar configuracion.
- **Foundational (Fase 2)**: depende de T001-T003 y bloquea las tres historias; T004-T007 preparan identidad, T008-T009 beans y politica base, y T010 el arnes de pruebas.
- **Historia 1 (Fase 3)**: depende de Fase 2; produce el login y el servicio JWT que usa la Historia 2.
- **Historia 2 (Fase 4)**: depende de T013-T017 de US1 y de Fase 2; agrega el filtro y protege rutas privadas.
- **Historia 3 (Fase 5)**: depende de T020-T022 de US2 para probar rutas protegidas y contexto; completa handlers, matchers publicos y 403.
- **Polish (Fase 6)**: depende de las tres historias; T031 y T032 son validacion final.

### Dependencias entre historias

- **US1 (P1)**: depende de Foundational; es el MVP y no depende de otra historia.
- **US2 (P1)**: depende de US1 porque reutiliza `JwtService` y el usuario autenticable.
- **US3 (P1)**: depende de US2 para distinguir autenticacion valida de autorizacion insuficiente y verificar rutas privadas.

## Oportunidades de ejecucion paralela

- Tras T001, T002 y T003, T004 y T006 pueden prepararse en paralelo si no modifican el mismo archivo; T007 puede escribirse junto con T006.
- Dentro de US1, T011 y T012 son pruebas independientes; despues T013 y T015 pueden avanzar en paralelo, antes de T014 y T016.
- Dentro de US2, T018 y T019 son pruebas independientes; T020 puede avanzar separado de la preparacion de fixtures de T010, pero la integracion T021-T022 espera a T020.
- Dentro de US3, T023, T025 y T026 pueden avanzar en paralelo porque usan archivos distintos; T024 espera la existencia de handlers y cadena configurada.
- T029 y T030 pueden avanzar en paralelo despues de completar las tres historias.

## Ejemplo de ejecucion paralela

```text
US1: T011 JwtServiceImplTest + T012 LoginAutenticacionTest
US2: T018 JwtAuthFilterTest + T019 AccesoPrivadoJwtTest
US3: T023 JwtHandlersTest + T025 JwtAuthenticationEntryPoint + T026 JwtAccessDeniedHandler
```

## Estrategia de implementacion

### MVP: solo Historia de Usuario 1

1. Completar Fase 1 y Fase 2.
2. Completar Fase 3: login, BCrypt, usuario autenticable y JWT de una hora.
3. Ejecutar T011-T012 y validar el contrato de `POST /login` de forma independiente.

### Entrega incremental

1. Agregar US2 para proteger rutas privadas con Bearer y rechazar tokens invalidos.
2. Agregar US3 para rutas publicas, respuestas 403 y handlers uniformes.
3. Completar Fase 6 con OpenAPI, restricciones de seguridad y quickstart.

## Criterios de completitud por historia

- **US1**: login valido entrega un JWT verificable con `sub`, `iat` y `exp` a una hora; login invalido recibe 401 generico.
- **US2**: Bearer valido carga `SecurityContext`; ausencia, formato incorrecto, firma invalida, malformacion, expiracion o usuario no cargable recibe 401 antes del endpoint.
- **US3**: rutas publicas funcionan sin token; ruta privada sin identidad recibe 401; identidad sin autoridad recibe 403; no hay sesion persistente y los cuerpos no filtran datos sensibles.

## Notas de ejecucion

- Todas las tareas usan checkbox, ID secuencial, etiqueta `[P]` solo cuando corresponde y `[USn]` dentro de su fase de historia.
- Las tareas nombran rutas exactas bajo `backend/` o `specs/004-autenticacion-jwt/`.
- No se modifican ni eliminan tests existentes.
