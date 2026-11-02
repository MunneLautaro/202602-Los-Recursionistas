# Research: Frontend del Mercado de Tokens

## Decision: Reutilizar el cliente HTTP existente

- **Decision**: Extender `frontend/src/lib/fetchClient.ts` para agregar el JWT, clasificar 401/403 y conservar el parseo de errores.
- **Rationale**: Es el punto único de comunicación indicado por la constitución y evita duplicar autenticación en cada API.
- **Alternatives considered**: Crear un cliente Axios o una capa global nueva; rechazado porque agrega una dependencia estructural y contradice las convenciones del proyecto.

## Decision: Mantener features separadas

- **Decision**: Implementar APIs, tipos, hooks y componentes dentro de `features/jugadores`, `features/mercado` y `features/cuenta`.
- **Rationale**: El frontend ya usa organización por feature y permite aislar contratos y estados.
- **Alternatives considered**: Centralizar toda la lógica en páginas; rechazado por duplicación y menor testabilidad.

## Decision: No inferir identidad desde el JWT

- **Decision**: La cuenta requiere un `userId` recibido de un contrato confiable; si no está disponible, se muestra la limitación.
- **Rationale**: `/login` solo devuelve `{ token }` y el backend no expone `/me`; decodificar claims no documentados sería un contrato implícito inseguro.
- **Alternatives considered**: Usar el primer usuario registrado, un ID manual o decodificar el token sin contrato; rechazado porque puede consultar o modificar otra cuenta.

## Decision: Paginación y estados locales

- **Decision**: Cada vista conserva filtros y página, reinicia la página ante cambios de filtros y expone estados loading/empty/error.
- **Rationale**: Las respuestas son `Page` y no existe una dependencia de caché en el proyecto.
- **Alternatives considered**: Incorporar una librería de fetching/caché; rechazado por alcance y dependencia innecesaria.
