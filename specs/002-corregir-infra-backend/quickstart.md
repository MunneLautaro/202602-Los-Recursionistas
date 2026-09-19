# Guia de validacion: Correccion de infraestructura del backend

## Prerrequisitos

- Java 21 disponible en el entorno.
- PostgreSQL disponible en `localhost:5432`.
- Base `football_market` creada y credenciales configuradas en la unica `backend/src/main/resources/application.properties`.
- Acceso de red a Maven Central para resolver dependencias.

## Validacion de estructura y nombres

Desde la raiz del repositorio:

```powershell
Get-ChildItem backend/src/main/java/unq/losrecursionistas/backend/model -Filter *.java
Get-ChildItem backend/src/main/java/unq/losrecursionistas/backend/persistence/repository -Recurse -Filter *.java
Get-ChildItem backend/src/test/java/unq/losrecursionistas/backend/model -Filter *.java
Get-ChildItem backend/src/test/java/unq/losrecursionistas/backend/service -Filter *.java
```

Resultado esperado:

- Las entidades estan en `model`.
- Los contratos e implementaciones de repositorio estan bajo `persistence/repository`.
- Las pruebas estan separadas en `model` y `service`.
- Los identificadores nuevos usan ASCII y espanol tecnico, por ejemplo `contrasena`.

## Validacion de configuracion unica

```powershell
Get-ChildItem backend/src/main/resources -Filter 'application*.properties'
Select-String -Path backend/src/main/resources/application.properties -Pattern 'spring.datasource.url|spring.jpa.hibernate.ddl-auto'
```

Resultado esperado:

- Solo existe `application.properties`.
- La URL apunta a PostgreSQL.
- `spring.jpa.hibernate.ddl-auto=update` gestiona el esquema mediante JPA/Hibernate.

## Compilacion y pruebas unitarias

```powershell
Set-Location backend
.\gradlew.bat clean test
```

Resultado esperado:

- Compilacion exitosa con Java 21.
- JUnit Platform ejecuta todos los tests.
- Los tests de `model` cubren invariantes y casos de borde, incluidos saldo insuficiente y falta de tokens.
- Los tests de `service` verifican fail-fast, interacciones con repositorios y `PageRequest.of(page, 12)` mediante Mockito.
- AssertJ se usa para aserciones y no aparecen dependencias ni tests de Jest, Vitest o Mocha.

## Validacion del esquema

```powershell
.\gradlew.bat bootRun
```

Resultado esperado:

- La aplicacion conecta con PostgreSQL.
- JPA/Hibernate crea o actualiza las tablas necesarias a partir de las entidades.
- Una incompatibilidad entre las entidades y PostgreSQL impide un arranque exitoso.
- No se agregan dependencias, propiedades ni archivos de una herramienta externa de migraciones.

## Criterio de cierre

La feature se considera validada cuando la estructura, la configuracion, la gestion del esquema y la suite de pruebas cumplen los resultados anteriores y el workflow de CI ejecuta el mismo comando de pruebas sin pasos de Node.js.
