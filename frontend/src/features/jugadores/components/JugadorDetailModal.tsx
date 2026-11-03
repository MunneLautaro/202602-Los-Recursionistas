import React, { useCallback, useEffect, useState } from "react";
import {
  fetchJugadorQuotes,
  fetchJugadorStats,
  type QuoteFilters,
} from "../api/jugadoresApi";
import type { JugadorResponseDTO, PlayerStats, Quote } from "../types";
import { calcularEdad } from "../utils";
import { jugadorDetailModalStyles } from "./JugadorDetailModal.styles";

interface JugadorDetailModalProps {
  jugador: JugadorResponseDTO;
  onClose: () => void;
}

export const JugadorDetailModal: React.FC<JugadorDetailModalProps> = ({
  jugador,
  onClose,
}) => {
  const [stats, setStats] = useState<PlayerStats | null>(null);
  const [quotes, setQuotes] = useState<Quote[]>([]);
  const [quotesPage, setQuotesPage] = useState(0);
  const [quotesTotalPages, setQuotesTotalPages] = useState(0);
  const [quoteFilters, setQuoteFilters] = useState<QuoteFilters>({});
  const [loadingStats, setLoadingStats] = useState(true);
  const [loadingQuotes, setLoadingQuotes] = useState(true);
  const [statsError, setStatsError] = useState<string | null>(null);
  const [quotesError, setQuotesError] = useState<string | null>(null);
  const [dateError, setDateError] = useState<string | null>(null);

  const loadStats = useCallback(async () => {
    setLoadingStats(true);
    setStatsError(null);
    try {
      setStats(await fetchJugadorStats(jugador.id));
    } catch (error) {
      setStatsError(
        error instanceof Error ? error.message : "Error al cargar estadísticas",
      );
    } finally {
      setLoadingStats(false);
    }
  }, [jugador.id]);

  const loadQuotes = useCallback(
    async (page = 0, filters: QuoteFilters = {}) => {
    setLoadingQuotes(true);
    setQuotesError(null);
    try {
      const response = await fetchJugadorQuotes(jugador.id, filters, page);
      setQuotes(response.content);
      setQuotesPage(response.number);
      setQuotesTotalPages(response.totalPages);
    } catch (error) {
      setQuotesError(
        error instanceof Error ? error.message : "Error al cargar cotizaciones",
      );
    } finally {
      setLoadingQuotes(false);
    }
    },
    [jugador.id],
  );

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadStats();
      void loadQuotes(0, {});
    }, 0);
    return () => window.clearTimeout(timer);
  }, [loadQuotes, loadStats]);

  const applyQuoteFilters = (event: React.FormEvent) => {
    event.preventDefault();
    if (
      quoteFilters.desde &&
      quoteFilters.hasta &&
      quoteFilters.desde > quoteFilters.hasta
    ) {
      setDateError("La fecha desde no puede ser posterior a hasta.");
      return;
    }
    setDateError(null);
    void loadQuotes(0, quoteFilters);
  };

  return (
    <div className={jugadorDetailModalStyles.overlay} onClick={onClose}>
      <div
        className={jugadorDetailModalStyles.modal}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <button
          className={jugadorDetailModalStyles.closeButton}
          onClick={onClose}
        >
          ×
        </button>
        <h2 className={jugadorDetailModalStyles.title}>{jugador.nombre}</h2>
        <p className={jugadorDetailModalStyles.field}>
          Posición: {jugador.posicion}
        </p>
        <p className={jugadorDetailModalStyles.field}>
          Edad: {calcularEdad(jugador.fechaNacimiento) ?? "No disponible"}
        </p>
        {jugador.equipo && (
          <p className={jugadorDetailModalStyles.field}>
            Equipo: {jugador.equipo.nombre}
          </p>
        )}
        {jugador.liga && (
          <p className={jugadorDetailModalStyles.field}>
            Liga: {jugador.liga.nombre}
          </p>
        )}
        <section className={jugadorDetailModalStyles.section}>
          <h3 className={jugadorDetailModalStyles.subtitle}>Estadísticas</h3>
          {loadingStats && <p>Cargando estadísticas...</p>}
          {statsError && (
            <div className={jugadorDetailModalStyles.error}>
              <p>{statsError}</p>
              <button onClick={loadStats}>Reintentar</button>
            </div>
          )}
          {!loadingStats && !statsError && stats && (
            <>
              <p className={jugadorDetailModalStyles.field}>
                Totales: {stats.totalesGlobal.apps} partidos,{" "}
                {stats.totalesGlobal.goles} goles,{" "}
                {stats.totalesGlobal.asistencias} asistencias
              </p>
              {stats.estadisticasPorTorneo.length === 0 ? (
                <p>No hay estadísticas disponibles.</p>
              ) : (
                <ul>
                  {stats.estadisticasPorTorneo.map((season) => (
                    <li key={season.id} className={jugadorDetailModalStyles.listItem}>
                      {season.torneo}: {season.apps ?? 0} partidos,{" "}
                      {season.goles ?? 0} goles, rating{" "}
                      {season.ratingPromedio ?? "N/D"}
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </section>
        <section className={jugadorDetailModalStyles.section}>
          <h3 className={jugadorDetailModalStyles.subtitle}>Cotizaciones</h3>
          <form onSubmit={applyQuoteFilters} className={jugadorDetailModalStyles.filters}>
            <label>
              Desde
              <input
                type="date"
                value={quoteFilters.desde ?? ""}
                onChange={(event) =>
                  setQuoteFilters((previous) => ({
                    ...previous,
                    desde: event.target.value || undefined,
                  }))
                }
              />
            </label>
            <label>
              Hasta
              <input
                type="date"
                value={quoteFilters.hasta ?? ""}
                onChange={(event) =>
                  setQuoteFilters((previous) => ({
                    ...previous,
                    hasta: event.target.value || undefined,
                  }))
                }
              />
            </label>
            <button type="submit">Filtrar</button>
          </form>
          {dateError && <p className={jugadorDetailModalStyles.error}>{dateError}</p>}
          {loadingQuotes && <p>Cargando cotizaciones...</p>}
          {quotesError && (
            <div className={jugadorDetailModalStyles.error}>
              <p>{quotesError}</p>
              <button onClick={() => loadQuotes(quotesPage, quoteFilters)}>
                Reintentar
              </button>
            </div>
          )}
          {!loadingQuotes && !quotesError && quotes.length === 0 && (
            <p>No hay cotizaciones para el rango seleccionado.</p>
          )}
          {!loadingQuotes && !quotesError && quotes.length > 0 && (
            <>
              <ul>
                {quotes.map((quote) => (
                  <li key={quote.id} className={jugadorDetailModalStyles.listItem}>
                    {quote.valor} - {new Date(quote.timestampCalculo).toLocaleString()}
                    <small>
                      {" "}
                      ({quote.estrategiaUsada}, {quote.ventanaInicio} a{" "}
                      {quote.ventanaFin})
                    </small>
                  </li>
                ))}
              </ul>
              {quotesTotalPages > 1 && (
                <div className={jugadorDetailModalStyles.pagination}>
                  <button
                    disabled={quotesPage === 0}
                    onClick={() => loadQuotes(quotesPage - 1, quoteFilters)}
                  >
                    Anterior
                  </button>
                  <span>
                    Página {quotesPage + 1} de {quotesTotalPages}
                  </span>
                  <button
                    disabled={quotesPage >= quotesTotalPages - 1}
                    onClick={() => loadQuotes(quotesPage + 1, quoteFilters)}
                  >
                    Siguiente
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  );
};
