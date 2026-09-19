# Contratos de la infraestructura inicial

Este proyecto no expone un conjunto de contratos externos definidos para la base inicial. La capa REST se desarrolla cuando existan endpoints concretos del dominio o de integracion.

## Convencion actual

- Los controladores deben responder con DTOs de salida bien definidos
- Todas las respuestas de error deben seguir un formato consistente
- Las excepciones de negocio se gestionan en `exceptions` y se traducen con `@ControllerAdvice`
- Los contratos de endpoints se documentaran aqui a medida que se agreguen funcionalidades

## Proximos pasos

Cuando se defina la primera API funcional, se agregaran contratos especificos para:

- request payload
- response payload
- codigos HTTP
- escenarios de error
- validaciones de negocio
