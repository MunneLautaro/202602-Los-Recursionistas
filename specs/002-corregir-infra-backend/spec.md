# Feature Specification: Correccion de infraestructura del backend

**Feature Branch**: `[002-corregir-infra-backend]`

**Created**: 2026-09-19

**Status**: Draft

**Input**: User description: "Consideraciones e inconsistencias a corregir respecto a la propuesta original: usar Java/Spring Boot y JUnit 5 + AssertJ/Mockito; ubicar entidades en model y acceso a datos en persistence/repository; incluir anotaciones JPA, Lombok y logica de negocio en entidades; configurar PostgreSQL y migraciones en una unica application.properties; y usar nombres en espanol tecnico con identificadores ASCII."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Estructura backend conforme a la arquitectura (Priority: P1)

Como integrante del equipo de desarrollo, necesito que la base del backend respete las capas y ubicaciones definidas por el proyecto para poder agregar funcionalidades sin mezclar dominio, persistencia, servicios y transporte.

**Why this priority**: Una estructura incorrecta contamina todas las funcionalidades posteriores y obliga a refactorizaciones costosas.

**Independent Test**: Revisar la estructura de paquetes y ejecutar una comprobacion automatizada que confirme que las entidades estan en `model`, los repositorios en `persistence/repository` y las dependencias entre capas respetan la arquitectura.

**Acceptance Scenarios**:

1. **Given** la estructura inicial del backend, **When** se inspeccionan las clases de dominio y acceso a datos, **Then** las entidades estan en `model` y los repositorios estan separados en `persistence/repository/interfaces` y `persistence/repository/impl`.
2. **Given** un nuevo caso de uso, **When** se agrega su orquestacion, **Then** el controlador no accede directamente al repositorio y el servicio no expone DTOs de respuesta.

---

### User Story 2 - Modelo persistible con reglas de dominio (Priority: P1)

Como responsable del dominio, necesito que las entidades sean persistibles y custodien sus invariantes para que las reglas de negocio no queden dispersas en servicios o controladores.

**Why this priority**: El modelo es la fuente de verdad de las reglas del dominio y debe ser utilizable tanto por la aplicacion como por las pruebas unitarias.

**Independent Test**: Crear pruebas unitarias de las entidades que verifiquen sus anotaciones de persistencia, construccion y comportamiento ante operaciones validas e invalidas.

**Acceptance Scenarios**:

1. **Given** una entidad de dominio, **When** se inspecciona y se persiste, **Then** cuenta con la configuracion de entidad y tabla requerida y puede ser gestionada por la capa de persistencia.
2. **Given** una operacion que viola una invariante, **When** se ejecuta sobre la entidad, **Then** la propia entidad rechaza la operacion mediante una excepcion de dominio no chequeada.
3. **Given** una entidad con comportamiento de negocio, **When** se ejecuta su suite de pruebas, **Then** se cubren los casos felices y los limites relevantes sin trasladar la regla al servicio.

---

### User Story 3 - Configuracion y validacion reproducibles (Priority: P2)

Como desarrollador, necesito una configuracion unica y un conjunto de pruebas reproducible para verificar que el backend usa PostgreSQL, mantiene compatible su esquema y bloquea regresiones antes de integrar cambios.

**Why this priority**: La configuracion divergente y un framework de pruebas incorrecto producen resultados distintos entre entornos y reducen la confianza en la integracion continua.

**Independent Test**: Ejecutar la validacion del backend y comprobar que carga la unica configuracion, reconoce PostgreSQL, gestiona el esquema mediante JPA/Hibernate y ejecuta pruebas JUnit 5 con AssertJ y Mockito.

**Acceptance Scenarios**:

1. **Given** el proyecto backend, **When** se buscan archivos de configuracion de la aplicacion, **Then** existe una sola `application.properties` y contiene la configuracion base de PostgreSQL y del esquema JPA, sin perfiles ni archivos locales separados.
2. **Given** un cambio en codigo de backend, **When** se ejecuta la suite automatizada, **Then** se usan JUnit 5, AssertJ y Mockito y un fallo de compilacion o prueba marca la validacion como fallida.
3. **Given** una clase nueva del backend, **When** se revisan sus identificadores, **Then** utiliza nombres en espanol tecnico y solamente caracteres ASCII, usando formas como `contrasena`.

### Edge Cases

- Una entidad sin un atributo obligatorio debe ser rechazada antes de persistirse.
- Un esquema incompatible con las entidades debe impedir que la aplicacion se considere lista.
- La presencia accidental de `application-local.properties` o de otro archivo `application*.properties` debe detectarse como incumplimiento.
- Un test escrito con Jest, Vitest o Mocha no debe formar parte de la validacion del backend.
- Un identificador con `n` o `N` en lugar de la forma ASCII equivalente `contrasena` debe ser corregido antes de integrar el cambio.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: El backend MUST mantener la arquitectura por capas definida por el proyecto, con entidades de dominio en `model` y acceso a datos bajo `persistence/repository`, separado en interfaces e implementaciones.
- **FR-002**: Las entidades del dominio MUST incluir la configuracion de persistencia necesaria para representar tablas y relaciones, junto con las anotaciones de productividad y construccion adoptadas por el proyecto.
- **FR-003**: Las entidades MUST contener sus reglas de negocio e invariantes, y MUST rechazar operaciones invalidas mediante excepciones de dominio no chequeadas.
- **FR-004**: La configuracion MUST conectarse a PostgreSQL y MUST mantener el esquema compatible con las entidades JPA durante el arranque de la aplicacion.
- **FR-005**: Toda la configuracion base de la aplicacion, incluyendo conexion y gestion del esquema JPA, MUST residir en una unica `application.properties`; no se deben crear perfiles ni archivos locales separados sin aprobacion explicita.
- **FR-006**: La validacion automatizada del backend MUST usar JUnit 5 para la ejecucion de pruebas y AssertJ y Mockito para aserciones y dobles de prueba cuando correspondan.
- **FR-007**: El backend MUST excluir Jest, Vitest y Mocha de su estrategia de pruebas.
- **FR-008**: Las pruebas MUST permanecer separadas en los espacios de `model` y `service`, y cada metodo publico nuevo de entidades o servicios MUST contar con cobertura de casos felices y de borde.
- **FR-009**: Todo codigo nuevo, incluidos nombres de entidades, atributos, metodos, variables, clases de prueba y mensajes funcionales, MUST usar espanol tecnico cuando corresponda y MUST limitar sus identificadores a caracteres ASCII.
- **FR-010**: La validacion MUST fallar cuando se detecte una entidad fuera de `model`, un repositorio fuera de `persistence/repository`, una configuracion duplicada o una prueba del backend basada en un framework no permitido.

### Key Entities _(include if feature involves data)_

- **Entidad de dominio**: Representa un concepto del negocio, sus atributos persistibles, relaciones, invariantes y operaciones validas.
- **Repositorio de dominio**: Representa el contrato de acceso a entidades y su implementacion separada de la logica de negocio.
- **Configuracion de persistencia**: Representa los datos necesarios para conectar el sistema con PostgreSQL y mantener compatible el esquema con las entidades JPA.
- **Suite de pruebas backend**: Representa la validacion automatizada de modelos y servicios mediante JUnit 5, AssertJ y Mockito.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: El 100% de las entidades y repositorios revisados durante la validacion se ubica en los paquetes establecidos por la arquitectura.
- **SC-002**: El 100% de los metodos publicos nuevos del modelo y los servicios cuenta con al menos un caso feliz y un caso de borde automatizado antes de integrar cambios.
- **SC-003**: La validacion del backend completa compilacion, pruebas y verificacion de compatibilidad del esquema sin usar Jest, Vitest ni Mocha, y cualquier fallo produce un resultado no exitoso.
- **SC-004**: Existe exactamente un archivo de configuracion base `application.properties` y el arranque validado utiliza PostgreSQL y la gestion de esquema JPA declarada en el proyecto.
- **SC-005**: El 100% de los identificadores nuevos inspeccionados usa caracteres ASCII y sigue el vocabulario tecnico en espanol definido por el proyecto.
- **SC-006**: Un desarrollador puede identificar la ubicacion correcta de una entidad, repositorio o prueba en menos de 1 minuto consultando la estructura documentada del backend.

## Assumptions

- La aplicacion backend existente se mantiene en Java y Spring Boot, conforme a la constitucion del proyecto.
- PostgreSQL es el unico motor de persistencia aprobado para esta etapa.
- La gestion inicial del esquema se realiza mediante la configuracion JPA/Hibernate existente; una herramienta de migraciones podra evaluarse en una feature futura si el equipo la necesita.
- La configuracion necesaria para un entorno local se documenta dentro de la unica `application.properties` y no mediante perfiles separados.
- Esta feature corrige la propuesta de infraestructura existente y no incorpora funcionalidades de negocio, autenticacion completa ni integraciones externas.
- Los tests existentes no se eliminan ni modifican para ocultar fallos; cualquier ajuste necesario debe preservar su intencion y trazabilidad.
