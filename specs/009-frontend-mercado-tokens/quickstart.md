# Quickstart: Frontend del Mercado de Tokens

## Prerequisites

1. Start the backend with its database and seed data.
2. From `frontend/`, install dependencies with Yarn.
3. Set `VITE_API_URL` if the backend is not at `http://localhost:8080`.

## Static validation

```powershell
cd frontend
yarn lint
yarn build
```

Expected result: both commands finish successfully without TypeScript or ESLint errors.

## Acceptance walkthrough

1. Open `/jugadores`, filter by league/team/position, paginate, and open a player.
2. From the player detail, load `/players/{id}/stats` and `/players/{id}/quotes`; verify empty, loading, date-range and error states.
3. Open ranking, filter by league and position, paginate, and navigate to a player.
4. Open the public market and filter `/orders` by player/type.
5. Register or log in, reload the browser, and verify the JWT is sent on a protected operation.
6. Submit a valid buy or sell order; verify the request contains only `jugadorId` and positive `cantidad`.
7. Submit zero/negative quantity and verify no request is sent.
8. Use an invalid token and verify 401 clears the session and routes to login; use a forbidden response and verify the session remains.
9. Open account. If a trusted user id exists, validate balance, credit reload and paginated transactions; otherwise verify the documented limitation is shown.
10. Confirm no normal navigation exposes admin ingestion, synchronization or quote-recalculation actions.
