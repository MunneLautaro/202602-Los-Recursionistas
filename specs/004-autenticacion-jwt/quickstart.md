# Guia rapida de validacion

## Prerrequisitos

- Java 21 disponible.
- Ejecutar desde `backend/`.
- Usar H2 para la suite; no hace falta PostgreSQL para las pruebas automatizadas.
- Proveer `JWT_SECRET` en una validacion de configuracion personalizada; sin ella debe funcionar el default de desarrollo.

## Compilar y probar

En Windows:

```powershell
.\gradlew.bat clean test
```

En Linux/CI:

```bash
./gradlew clean test
```

Resultado esperado: suite existente y pruebas de autenticacion exitosas, sin modificar ni eliminar tests previos.

## Escenarios automatizados minimos

1. Generar un token con `JwtService` y comprobar `sub`, `iat`, HS256 y expiracion exacta de una hora.
2. Extraer el usuario y validar tokens correctos; rechazar token vencido, malformado, sin subject y con firma invalida.
3. Autenticar con `POST /login` usando credenciales validas y recibir solo el token; credenciales invalidas reciben 401 generico.
4. Acceder a una ruta privada sin token, con esquema incorrecto, Bearer vacio, token invalido, usuario inexistente o inhabilitado: siempre 401 y nunca llega al endpoint.
5. Acceder a rutas publicas sin token y con token invalido: siguen disponibles.
6. Acceder autenticado sin autoridad suficiente: 403 con JSON diferenciado.
7. Confirmar `SessionCreationPolicy.STATELESS`: no hay autenticacion reutilizable en una segunda solicitud sin Bearer.
8. Inspeccionar respuestas para confirmar ausencia de token completo, secreto, contrasena y stack trace.

## Validacion manual opcional

Levantar con el default de desarrollo:

```powershell
.\gradlew.bat bootRun
```

Probar el login y luego usar el token devuelto:

```powershell
$body = '{"nombreUsuario":"usuario-prueba","contrasena":"contrasena-prueba"}'
$login = Invoke-RestMethod http://localhost:8080/login -Method Post -ContentType 'application/json' -Body $body
Invoke-WebRequest http://localhost:8080/ruta-privada -Headers @{ Authorization = "Bearer $($login.token)" }
```

La ruta privada concreta depende de los controllers existentes al implementar. Swagger, OpenAPI y `/public/**` deben continuar accesibles sin credenciales.

## Referencias

- Contratos: [contracts/api-publica.md](contracts/api-publica.md)
- Modelo: [data-model.md](data-model.md)
