# Contrato publico de autenticacion JWT

## POST `/login`

- **Acceso**: publico.
- **Request**: JSON con `nombreUsuario` y `contrasena`, ambos obligatorios.
- **Respuesta exitosa**: `200 OK` con JSON `{ "token": "<jwt>" }`.
- **JWT**: HS256; `sub` es el nombre de usuario; contiene `iat`; `exp` es exactamente una hora posterior a `iat`.
- **Credenciales invalidas**: `401 Unauthorized` con `{ "error": "unauthorized", "message": "..." }` sin indicar si el usuario existe.

## Rutas publicas

No requieren Authorization:

- `/auth/**`
- `/login`
- `/public/**`
- `/swagger-ui/**` y `/swagger-ui.html`
- `/v3/api-docs/**` y `/v3/api-docs`

Una ruta publica conserva el acceso aun cuando reciba un Bearer invalido.

## Rutas privadas

Cualquier ruta fuera del conjunto publico requiere:

```http
Authorization: Bearer <jwt>
```

Un token valido carga la identidad y sus autoridades en `SecurityContext`. La sesion de servidor no se conserva entre solicitudes.

## Errores de autenticacion

Para token ausente en ruta privada, esquema distinto de Bearer, token vacio, malformado, sin subject, expirado, con firma invalida, usuario inexistente o usuario inhabilitado:

- **HTTP**: `401 Unauthorized`.
- **Cuerpo**: JSON con campos `error` y `message`.
- **Restricciones**: no incluir token, secreto, contrasena, stack trace ni indicador sensible sobre la existencia del usuario.

## Errores de autorizacion

Para una identidad autenticada sin autoridad suficiente:

- **HTTP**: `403 Forbidden`.
- **Cuerpo**: `{ "error": "forbidden", "message": "No tiene permisos suficientes" }` o mensaje equivalente no sensible.
- **Diferencia**: el codigo y el campo `error` deben distinguirlo de 401.

## Fuera de contrato

No se aceptan, generan ni validan ApiKey. Tampoco se exponen secretos o claims adicionales como contrasenas, saldo o informacion interna.
