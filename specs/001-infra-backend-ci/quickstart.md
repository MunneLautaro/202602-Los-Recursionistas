# Quickstart: validacion de la infraestructura base

## Requisitos previos

- Java 21
- Gradle wrapper disponible en `backend/`
- PostgreSQL instalado y ejecutandose
- Credenciales locales esperadas: usuario `postgres`, password `root`

## Configuracion local

1. Verificar que el archivo `backend/src/main/resources/application.properties` contiene la configuracion base del proyecto.
2. Mantener una sola fuente de configuracion para esta etapa.
3. Confirmar que el servicio apunta a PostgreSQL con la configuracion local esperada.

## Validacion de compilacion y pruebas

Desde la raiz del repositorio:

```bash
cd backend
./gradlew clean test
```

**Resultado esperado**:

- la compilacion del proyecto finaliza sin errores
- la suite de pruebas base ejecuta correctamente
- el workflow de GitHub Actions refleja un estado exitoso para el build

## Verificacion de estructura

Validar que existan los paquetes requeridos:

- `model`
- `controller` y `dto`
- `service/interfaces`
- `service/impl`
- `persistence/repository/interfaces`
- `persistence/repository/impl`
- `configuration`
- `security`
- `exceptions`

## Verificacion de manejo de errores

- garantizar que las excepciones sean runtime
- confirmar que existe un `@ControllerAdvice` global
- validar que cualquier error de validacion se traduzca a una respuesta HTTP consistente

## Verificacion de CI

El workflow en `.github/workflows/ci.yml` debe ejecutar la validacion automatica del proyecto y marcar fallo si la compilacion o la suite de pruebas fracasan.

## Resultado

Si estas verificaciones terminan sin errores, la infraestructura base del backend queda lista para continuar con nuevas funcionalidades, seguridad y persistencia de dominio.
