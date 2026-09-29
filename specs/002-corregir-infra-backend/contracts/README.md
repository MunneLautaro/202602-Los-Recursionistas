# Contratos de la feature

Esta feature no agrega endpoints REST ni contratos publicos de integracion externa. Su superficie contractual es interna al backend:

- Los repositorios exponen contratos Java bajo `persistence/repository/interfaces`.
- Sus adaptadores viven bajo `persistence/repository/impl` y delegan en DAOs de persistencia bajo `persistence/sql` mediante inyeccion por constructor.
- Las consultas de multiples registros retornan `Page<T>` y reciben `Pageable`.
- Los servicios construyen el `Pageable` con tamano por defecto 12 y retornan entidades o paginas de entidades.
- El mapeo a DTO y la documentacion OpenAPI pertenecen a la capa controller cuando se incorporen endpoints funcionales.

Los detalles de entidades, relaciones e invariantes estan en [data-model.md](../data-model.md).
