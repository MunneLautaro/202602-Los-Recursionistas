# Investigacion y decisiones: Correccion de infraestructura del backend

## Decision 1: Gestion del esquema con JPA/Hibernate

- **Decision**: No incorporar una herramienta externa de migraciones en esta feature. Mantener la gestion automatica del esquema mediante JPA/Hibernate y configurar `spring.jpa.hibernate.ddl-auto=update` en la unica `application.properties`.
- **Rationale**: El usuario no considera necesaria una herramienta de migraciones para esta etapa y el backend ya tiene JPA/Hibernate configurado. Se evita agregar una dependencia y un flujo operativo que no aporta valor al alcance actual.
- **Alternatives considered**: Una herramienta externa de versionado y `ddl-auto=validate`. La herramienta externa agrega dependencias, archivos y una estrategia de versionado que el equipo no necesita ahora; `validate` requiere que el esquema ya exista y vuelve mas costoso el arranque inicial.
- **Consecuencia**: La aplicacion gestionara la compatibilidad basica entre entidades y esquema al arrancar. Si el proyecto requiere historial, rollback o despliegues con cambios controlados, se debera crear una feature posterior para evaluar una estrategia de versionado.

## Decision 2: Relaciones entre entidades

- **Decision**: Modelar referencias directas y acotadas: `Liga` contiene equipos, `Equipo` pertenece a una liga, `Jugador` pertenece a un equipo, `CotizacionJugador` pertenece a un jugador, `PosicionPortfolio` pertenece a un usuario y a un jugador, y `TransaccionAuditoria` referencia al usuario autor y, cuando corresponda, a la posicion afectada.
- **Rationale**: Estas relaciones representan el dominio indicado sin introducir una entidad de asociacion adicional para la cartera en esta etapa. Las colecciones se mantienen bajo demanda para evitar grafos persistentes innecesarios y ciclos de serializacion.
- **Alternatives considered**: Modelar todas las relaciones como identificadores escalares o agregar una entidad `Portfolio` intermedia. Los identificadores escalares pierden integridad declarativa; `Portfolio` amplia el alcance de la feature y no aparece en la lista solicitada.
- **Consecuencia**: Las relaciones JPA deben definir cardinalidad, nulabilidad y estrategia de carga de forma explicita. La capa controller no serializa entidades directamente en esta feature.

## Decision 3: Propiedad de las reglas de negocio

- **Decision**: Las entidades validan sus invariantes en metodos de dominio; los servicios resuelven entidades existentes, construyen `Pageable` y orquestan repositorios, sin trasladar calculos del dominio.
- **Rationale**: Cumple el Principio II y separa las responsabilidades: el modelo conoce saldo, tokens, cotizaciones y estados; el servicio conoce existencia, paginacion y coordinacion.
- **Alternatives considered**: Centralizar todas las validaciones en servicios o usar validadores externos. Esas opciones dejan objetos de dominio invalidos cuando se usan fuera del flujo HTTP y contradicen la constitucion.
- **Invariantes iniciales**:
  - `Usuario`: saldo no negativo y contrasena almacenada como dato protegido, sin exponerla en representaciones.
  - `Liga`, `Equipo` y `Jugador`: nombres requeridos y relaciones obligatorias donde aplique.
  - `CotizacionJugador`: valor de cotizacion positivo y tokens disponibles no negativos.
  - `PosicionPortfolio`: cantidad de tokens positiva y operaciones que no permiten saldo o tokens insuficientes.
  - `TransaccionAuditoria`: autor, detalle, timestamp y estados anterior/posterior inmutables despues de crear el registro.

## Decision 4: Contratos de repositorio y paginacion

- **Decision**: Cada contrato publico vive en `persistence/repository/interfaces`; los contratos de consulta multiple retornan `Page<T>` y reciben `Pageable`. Las implementaciones viven en `persistence/repository/impl` y delegan en DAOs Spring Data bajo `persistence/sql`.
- **Rationale**: Mantiene la separacion exigida por el Principio I y permite que el service cree `PageRequest.of(page, 12)` antes de invocar al repositorio, como exige el Principio IV.
- **Alternatives considered**: Retornar `List<T>` y paginar en memoria, o exponer directamente repositorios desde controllers. Ambas alternativas violan la constitucion y degradan el comportamiento con volumen.

## Decision 5: Pruebas del backend

- **Decision**: Usar JUnit 5 como motor, AssertJ para aserciones y Mockito para dobles cuando una unidad necesite colaboraciones. Las pruebas de modelo se ubican en `backend/src/test/java/unq/losrecursionistas/backend/model/` y las de servicio en `.../service/`.
- **Rationale**: Es la combinacion solicitada y ya existe `useJUnitPlatform()` en Gradle. Las entidades se prueban como unidades sin levantar Spring; los servicios usan Mockito para repositorios y verifican paginacion y fail-fast.
- **Alternatives considered**: Jest, Vitest, Mocha o pruebas exclusivamente de contexto Spring. Los tres primeros no corresponden al backend Java y la ultima opcion vuelve lentas las pruebas y no aisla las invariantes del modelo.

## Decision 6: Lombok y JPA

- **Decision**: Mantener Lombok como apoyo de construccion y acceso controlado, con constructor sin argumentos requerido por JPA, constructor completo y builder cuando sea seguro. Los identificadores de igualdad se basan en la identidad persistente y no en todas las relaciones.
- **Rationale**: El proyecto ya declara Lombok y la constitucion exige anotaciones JPA/Lombok. Evitar igualdad sobre asociaciones previene ciclos y consultas inesperadas.
- **Alternatives considered**: Escribir todos los metodos manualmente o usar `@Data` sin controlar igualdad. La primera alternativa duplica codigo; la segunda puede incluir relaciones y provocar ciclos.
