# 202602-Los-Recursionistas

## Validacion del backend

El backend utiliza Java 21 y Gradle. Para ejecutar la compilacion y las pruebas desde la raiz del repositorio:

```bash
cd backend
./gradlew clean test
```

En Windows puede ejecutarse el wrapper equivalente:

```powershell
cd backend
.\gradlew.bat clean test
```

El workflow de GitHub Actions en `.github/workflows/ci.yml` configura Java 21, resuelve dependencias, compila el proyecto y ejecuta la suite de pruebas en cada push o pull request hacia `main` o `dev`.

## Limites de arquitectura

La seguridad se concentra en `backend/src/main/java/unq/losrecursionistas/backend/security/` y utiliza Spring Security sin acoplarse a `model`, `service` o `persistence`. La configuracion inicial permite las solicitudes mientras no existan endpoints protegidos; `ManejadorToken` y `PoliticaAcceso` definen puntos de extension para incorporar autenticacion y autorizacion posteriormente.

Las responsabilidades permanecen separadas: `controller` traduce HTTP a DTOs, `service` orquesta entidades, `persistence` accede a datos, `configuration` registra componentes globales, `security` gestiona autenticacion y autorizacion, y `exceptions` traduce errores a respuestas HTTP. Las nuevas clases deben respetar estos limites y usar identificadores ASCII.
