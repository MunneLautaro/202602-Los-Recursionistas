# Modelo de datos: Autenticacion JWT

## Usuario autenticado

Identidad de dominio cargada para login o validacion de un token.

| Campo           | Tipo                    | Reglas                                                                  |
| --------------- | ----------------------- | ----------------------------------------------------------------------- |
| `nombreUsuario` | `String`                | Obligatorio, unico y usado como subject del JWT.                        |
| `contrasena`    | `String`                | Obligatoria; se compara mediante BCrypt y nunca se expone.              |
| `habilitado`    | `boolean`               | Debe ser verdadero para autenticar; un usuario inhabilitado recibe 401. |
| `autoridades`   | conjunto de autoridades | Se convierte a `GrantedAuthority`; no se redefinen roles de negocio.    |

El modelo actual no contiene `habilitado` ni autoridades. La implementacion debe incorporar el minimo soporte persistente o adaptador necesario para cubrir los casos de la spec y definir el valor compatible para usuarios existentes.

## JWT

Credencial firmada y temporal; no es una entidad persistida.

| Claim | Tipo                 | Reglas                                                                  |
| ----- | -------------------- | ----------------------------------------------------------------------- |
| `sub` | `String`             | `nombreUsuario`; obligatorio y no vacio.                                |
| `iat` | fecha Unix/`Instant` | Momento de emision.                                                     |
| `exp` | fecha Unix/`Instant` | Exactamente `iat + 1 hora`; pasado implica token vencido.               |
| firma | HS256                | Usa el secreto configurado por `JWT_SECRET` o su default de desarrollo. |

No se incluyen contrasena, secreto, saldo, authorities internas completas ni datos innecesarios.

## Contexto de seguridad

Estado temporal de una solicitud. Contiene el principal `UserDetails` y sus autoridades cuando el filtro valida el Bearer. Se limpia al terminar la solicitud y no se persiste porque la politica es `STATELESS`.

## Credenciales de login

DTO de request para `POST /login`:

- `nombreUsuario`: obligatorio y no vacio.
- `contrasena`: obligatoria y no vacia.

DTO de response:

- `token`: JWT completo emitido tras autenticacion exitosa.
- No contiene contrasena, secreto ni detalles del usuario que no sean necesarios.

## Respuesta de error de seguridad

Objeto JSON no persistente emitido por los handlers:

```json
{
  "error": "unauthorized",
  "message": "La autenticacion es requerida"
}
```

Para autorizacion:

```json
{
  "error": "forbidden",
  "message": "No tiene permisos suficientes"
}
```

Los campos son obligatorios, no contienen trazas, tokens, credenciales ni secretos, y se corresponden con HTTP 401 y 403 respectivamente.

## Transiciones

1. Credenciales validas y usuario habilitado: `no autenticado -> autenticado`, se emite JWT.
2. Credenciales invalidas, usuario inexistente o inhabilitado: `no autenticado -> rechazado 401`.
3. Bearer valido: `solicitud -> contexto autenticado -> endpoint`.
4. Bearer ausente en ruta privada, malformado, expirado o con firma invalida: `solicitud -> rechazado 401`.
5. Contexto autenticado sin autoridad requerida: `autenticado -> rechazado 403`.
6. Ruta publica: `solicitud -> endpoint` aun si no tiene token; un token opcional invalido no cambia esa publicidad.
