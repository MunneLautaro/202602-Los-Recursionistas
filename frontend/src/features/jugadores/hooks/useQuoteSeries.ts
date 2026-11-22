import { useCallback, useRef, useState } from "react";
import { fetchJugadorQuoteSeries, type QuoteFilters } from "../api/jugadoresApi";
import type { QuotePoint } from "../types";

/**
 * Carga la serie de cotizaciones de un jugador para el gráfico.
 * Si se dispara una carga nueva antes de que termine la anterior,
 * la respuesta vieja se descarta para no pisar el rango más reciente.
 */
export function useQuoteSeries(jugadorId: number) {
  const [points, setPoints] = useState<QuotePoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const lastRequest = useRef(0);

  const load = useCallback(
    async (filters: QuoteFilters = {}) => {
      const requestId = ++lastRequest.current;
      setLoading(true);
      setError(null);
      try {
        const response = await fetchJugadorQuoteSeries(jugadorId, filters);
        if (requestId === lastRequest.current) setPoints(response);
      } catch (reason) {
        if (requestId === lastRequest.current) {
          setError(
            reason instanceof Error && reason.message
              ? reason.message
              : "Error al cargar el gráfico de cotizaciones.",
          );
        }
      } finally {
        if (requestId === lastRequest.current) setLoading(false);
      }
    },
    [jugadorId],
  );

  return { points, loading, error, load };
}
