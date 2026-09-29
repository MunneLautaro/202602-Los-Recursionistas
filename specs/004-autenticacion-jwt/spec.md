# Feature Specification: Autenticacion JWT para la API

**Feature Branch**: `[004-autenticacion-jwt]`

**Created**: 2026-09-22

**Status**: Draft

**Input**: User description: "Proveer autenticacion basada en JWT para proteger los endpoints privados de la API, con login, validacion de Bearer Token, manejo consistente de errores 401/403 y sin incluir ApiKey."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Iniciar sesion y obtener credenciales (Priority: P1)

Como usuario registrado, necesito iniciar sesion con mis credenciales para obtener una credencial temporal que me permita utilizar los endpoints privados de la API.

**Why this priority**: Sin una forma confiable de autenticarse, los usuarios no pueden acceder de manera segura a las funcionalidades protegidas.

**Independent Test**: Enviar credenciales validas y comprobar que se recibe una credencial utilizable; repetir con credenciales invalidas y comprobar que el acceso es rechazado sin exponer informacion sensible.

**Acceptance Scenarios**:

1. **Given** un usuario registrado con credenciales validas, **When** inicia sesion, **Then** recibe un JWT firmado con su identificador como sujeto, fecha de emision y fecha de expiracion de una hora.
2. **Given** credenciales invalidas o incompletas, **When** el usuario intenta iniciar sesion, **Then** la solicitud es rechazada con una respuesta de autenticacion consistente y sin revelar si el usuario existe.
3. **Given** un JWT emitido correctamente, **When** el usuario revisa su vigencia, **Then** la credencial deja de ser aceptada al cumplirse su fecha de expiracion.

---

### User Story 2 - Acceder a recursos privados con un token valido (Priority: P1)

Como usuario autenticado, necesito enviar mi JWT en cada solicitud privada para que la API reconozca mi identidad y autorice el acceso correspondiente.

**Why this priority**: La validacion uniforme del token es la barrera principal que protege los recursos privados.

**Independent Test**: Acceder a un endpoint privado con un JWT valido, uno ausente y uno invalido, y verificar que cada caso produce el resultado de acceso esperado.

**Acceptance Scenarios**:

1. **Given** un JWT valido en el encabezado Bearer, **When** el usuario solicita un endpoint privado, **Then** la identidad queda disponible para la autorizacion y la solicitud continua hacia el recurso.
2. **Given** una solicitud a un endpoint privado sin encabezado de autenticacion, **When** se procesa la solicitud, **Then** responde HTTP 401 con JSON consistente.
3. **Given** un token invalido, malformado o expirado, **When** se procesa la solicitud, **Then** responde HTTP 401 con JSON consistente y no llega al endpoint privado.

---

### User Story 3 - Aplicar acceso publico y comunicar falta de permisos (Priority: P1)

Como consumidor de la API, necesito que las rutas publicas permanezcan disponibles sin token y que las rutas protegidas distingan autenticacion de autorizacion para recibir una respuesta accionable.

**Why this priority**: La separacion clara entre rutas publicas, autenticacion y autorizacion evita bloqueos indebidos y facilita el diagnostico de accesos rechazados.

**Independent Test**: Consultar rutas publicas sin credenciales y una ruta protegida con una identidad autenticada sin permisos suficientes, verificando acceso publico y respuesta 403 respectivamente.

**Acceptance Scenarios**:

1. **Given** una solicitud a `/auth/**`, `/login`, `/public/**`, `/swagger-ui/**` o `/v3/api-docs/**`, **When** se realiza sin token, **Then** la ruta permanece disponible.
2. **Given** una solicitud a cualquier otra ruta sin un JWT valido, **When** se procesa, **Then** responde HTTP 401.
3. **Given** una identidad autenticada sin permisos suficientes, **When** intenta acceder a un recurso restringido, **Then** responde HTTP 403 con un JSON consistente y diferenciado del error 401.
4. **Given** cualquier solicitud autenticada, **When** se establece el contexto de seguridad, **Then** la API opera sin crear una sesion persistente entre solicitudes.

---

### Edge Cases

- El encabezado `Authorization` esta presente pero no usa el esquema `Bearer` o no contiene un token.
- El JWT tiene firma invalida, estructura malformada, sujeto ausente o fecha de expiracion superada.
- El usuario indicado por un token valido ya no existe o esta inhabilitado al momento de cargar su identidad.
- Una ruta publica recibe un token invalido; la ruta debe conservar el acceso publico sin convertir una credencial opcional en un requisito.
- Un usuario autenticado intenta acceder a un recurso para el que no tiene permisos; debe obtener 403, no 401.
- Las respuestas 401 y 403 no deben incluir el token, el secreto de firma, credenciales ni trazas internas.
- La ausencia de `JWT_SECRET` debe utilizar el valor de desarrollo definido para la aplicacion, sin impedir el arranque; los entornos productivos deben poder proporcionar su propio secreto mediante configuracion.
- La solucion no debe crear, validar ni asociar ApiKey.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: El sistema MUST ofrecer un login que autentique a un usuario registrado y devuelva un JWT firmado cuando las credenciales sean validas.
- **FR-002**: El JWT MUST incluir como minimo el username del usuario, la fecha de emision y una fecha de expiracion exactamente una hora posterior a su emision.
- **FR-003**: El sistema MUST firmar los JWT con HS256 y utilizar un secreto configurable mediante `JWT_SECRET`, con un valor de desarrollo por defecto.
- **FR-004**: El sistema MUST leer el encabezado `Authorization` con esquema `Bearer`, extraer el token, identificar al usuario, validar la firma y la vigencia, y establecer el contexto de seguridad para solicitudes validas.
- **FR-005**: El sistema MUST rechazar con HTTP 401 y una respuesta JSON consistente toda solicitud a una ruta protegida que no tenga un JWT valido.
- **FR-006**: El sistema MUST responder HTTP 401 directamente cuando el token sea invalido, malformado o expirado, sin permitir que la solicitud alcance el endpoint protegido.
- **FR-007**: El sistema MUST responder HTTP 403 con una respuesta JSON consistente y diferenciada cuando una identidad autenticada no tenga permisos suficientes.
- **FR-008**: Las rutas `/auth/**`, `/login`, `/public/**`, `/swagger-ui/**` y `/v3/api-docs/**` MUST ser publicas y accesibles sin autenticacion.
- **FR-009**: Toda ruta fuera del conjunto publico MUST requerir un JWT valido para continuar.
- **FR-010**: La autenticacion MUST utilizar sesiones sin estado y no conservar una sesion de servidor entre solicitudes.
- **FR-011**: Las respuestas de error de autenticacion y autorizacion MUST contener JSON simple con un campo `error` y un mensaje descriptivo, sin exponer secretos, tokens, credenciales ni trazas internas.
- **FR-012**: La implementacion MUST exponer un codificador de contrasenas BCrypt y un gestor de autenticacion para que el login pueda validar credenciales de manera segura.
- **FR-013**: El contrato `UsuarioDetailsService` MUST ubicarse en `service/interfaces` y su implementacion `UsuarioDetailsServiceImpl` MUST ubicarse en `service/impl`. La configuracion de autenticacion, autorizacion, tokens, filtros y handlers MUST permanecer dentro de `security`.
- **FR-014**: La implementacion MUST incluir la configuracion de seguridad, handlers para 401 y 403, filtro de validacion JWT y servicio JWT conforme a la estructura de paquetes definida en el alcance.
- **FR-014a**: El acceso de autenticacion a usuarios MUST depender del contrato `persistence/repository/interfaces/RepositorioUsuario`; su implementacion MUST delegar mediante composicion en `persistence/sql/UsuarioDAOSQL`, sin acceso directo desde el servicio a JPA o `EntityManager`.
- **FR-015**: La feature MUST excluir la generacion, asociacion y validacion de ApiKey.
- **FR-016**: Los nombres de clases, metodos, propiedades y mensajes nuevos MUST respetar el idioma tecnico del proyecto y utilizar identificadores ASCII.

### Key Entities _(include if feature involves data)_

- **Usuario autenticado**: Representa la identidad cargada desde las credenciales o desde el sujeto del JWT, junto con sus autoridades y estado habilitado.
- **JWT**: Representa la credencial temporal firmada que contiene sujeto, emision y expiracion.
- **Contexto de seguridad**: Representa la identidad y autoridades disponibles durante el procesamiento de una solicitud autenticada.
- **Respuesta de error de seguridad**: Representa el JSON consistente emitido cuando falla la autenticacion o la autorizacion, distinguiendo 401 de 403.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: El 100% de los intentos de login con credenciales validas produce una credencial temporal aceptable por los endpoints protegidos durante su vigencia.
- **SC-002**: El 100% de las solicitudes protegidas sin credencial valida recibe HTTP 401 y ninguna alcanza el recurso protegido.
- **SC-003**: El 100% de las solicitudes de identidades autenticadas sin permisos suficientes recibe HTTP 403, diferenciado de HTTP 401.
- **SC-004**: El 100% de las rutas publicas definidas permanece accesible sin autenticacion y el 100% de las rutas restantes exige credencial valida.
- **SC-005**: El 100% de los JWT emitidos expira una hora despues de su emision y deja de permitir acceso una vez vencido.
- **SC-006**: En una inspeccion de respuestas de error y credenciales, no se encuentran secretos, contrasenas, tokens completos ni trazas internas.
- **SC-007**: Los consumidores pueden distinguir el motivo de un rechazo de autenticacion frente a uno de autorizacion leyendo los campos del JSON y el codigo HTTP, sin depender de detalles internos.
- **SC-008**: La suite de pruebas existente y las pruebas de los casos felices y de borde de seguridad finalizan sin regresiones.

## Assumptions

- Existe un usuario registrado y un mecanismo vigente para cargar sus `UserDetails`; esta feature no define registro de usuarios ni recuperacion de contrasenas.
- El endpoint de login conserva el contrato de entrada y salida compatible con los controladores existentes o se incorpora solo si aun no existe un login.
- El valor por defecto de `JWT_SECRET` es exclusivamente de desarrollo; los entornos productivos proporcionaran un secreto propio mediante la configuracion disponible.
- Las autoridades y permisos existentes del usuario son la fuente de verdad para decidir autorizacion; esta feature no redefine roles de negocio.
- Las rutas de Swagger y OpenAPI deben permanecer publicas porque forman parte del conjunto de rutas indicado por el alcance.
- La unica credencial admitida por esta feature es JWT; ApiKey queda fuera de alcance.
- La solucion conserva una unica configuracion base `application.properties` y no crea perfiles adicionales.
