import React, { useCallback, useEffect, useRef, useState } from "react";
import {
  fetchJugadorQuotes,
  fetchJugadorStats,
  type QuoteFilters,
} from "../api/jugadoresApi";
import type { JugadorResponseDTO, PlayerStats, Quote } from "../types";
import { calcularEdad } from "../utils";
import { quoteFiltersSchema, type QuoteFiltersValues } from "../schemas";
import { jugadorDetailModalStyles } from "./JugadorDetailModal.styles";

interface JugadorDetailModalProps {
  jugador: JugadorResponseDTO;
  onClose: () => void;
}

const focusableSelector =
  'button:not([disabled]), input:not([disabled]), [tabindex]:not([tabindex="-1"])';

const getErrorMessage = (error: unknown, fallback: string): string =>
  error instanceof Error && error.message ? error.message : fallback;

export const JugadorDetailModal: React.FC<JugadorDetailModalProps> = ({
  jugador,
  onClose,
}) => {
  const modalRef = useRef<HTMLDivElement>(null);
  const previouslyFocusedRef = useRef<HTMLElement | null>(null);
  const [stats, setStats] = useState<PlayerStats | null>(null);
  const [quotes, setQuotes] = useState<Quote[]>([]);
  const [quotesPage, setQuotesPage] = useState(0);
  const [quotesTotalPages, setQuotesTotalPages] = useState(0);
  const [quoteFilters, setQuoteFilters] = useState<QuoteFiltersValues>({});
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
      setStatsError(getErrorMessage(error, "Error al cargar estadísticas."));
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
        setQuotesError(getErrorMessage(error, "Error al cargar cotizaciones."));
      } finally {
        setLoadingQuotes(false);
      }
    },
    [jugador.id],
  );

  useEffect(() => {
    previouslyFocusedRef.current =
      document.activeElement instanceof HTMLElement ? document.activeElement : null;
    const firstFocusable = modalRef.current?.querySelector<HTMLElement>(
      focusableSelector,
    );
    (firstFocusable ?? modalRef.current)?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
        return;
      }
      if (event.key !== "Tab" || !modalRef.current) return;
      const focusable = Array.from(
        modalRef.current.querySelectorAll<HTMLElement>(focusableSelector),
      );
      if (focusable.length === 0) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      previouslyFocusedRef.current?.focus();
    };
  }, [onClose]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadStats();
      void loadQuotes();
    }, 0);
    return () => window.clearTimeout(timer);
  }, [loadQuotes, loadStats]);

  const applyQuoteFilters = (event: React.FormEvent) => {
    event.preventDefault();
    const result = quoteFiltersSchema.safeParse(quoteFilters);
    if (!result.success) {
      setDateError(result.error.issues[0]?.message ?? "Revisa las fechas.");
      return;
    }
    setDateError(null);
    void loadQuotes(0, result.data);
  };

  const updateFilter = (key: keyof QuoteFiltersValues, value: string) => {
    setQuoteFilters((previous) => ({ ...previous, [key]: value || undefined }));
    setDateError(null);
  };

  return (
    <div
      className={jugadorDetailModalStyles.overlay}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div
        ref={modalRef}
        className={jugadorDetailModalStyles.modal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="jugador-detail-title"
        tabIndex={-1}
      >
        <button
          type="button"
          className={jugadorDetailModalStyles.closeButton}
          onClick={onClose}
          aria-label="Cerrar detalle del jugador"
        >
          <span aria-hidden="true">×</span>
        </button>
        <h2 id="jugador-detail-title" className={jugadorDetailModalStyles.title}>
          {jugador.nombre}
        </h2>
        <p className={jugadorDetailModalStyles.field}>Posición: {jugador.posicion}</p>
        <p className={jugadorDetailModalStyles.field}>
          Edad: {calcularEdad(jugador.fechaNacimiento) ?? "No disponible"}
        </p>
        {jugador.equipo && <p className={jugadorDetailModalStyles.field}>Equipo: {jugador.equipo.nombre}</p>}
        {jugador.liga && <p className={jugadorDetailModalStyles.field}>Liga: {jugador.liga.nombre}</p>}

        <section className={jugadorDetailModalStyles.section} aria-labelledby="stats-title">
          <h3 id="stats-title" className={jugadorDetailModalStyles.subtitle}>Estadísticas</h3>
          {loadingStats && <p className={jugadorDetailModalStyles.status}>Cargando estadísticas...</p>}
          {statsError && (
            <div className={jugadorDetailModalStyles.error} role="alert">
              <p>{statsError}</p>
              <button type="button" className={jugadorDetailModalStyles.actionButton} onClick={() => void loadStats()}>
                Reintentar
              </button>
            </div>
          )}
          {!loadingStats && !statsError && !stats && (
            <p className={jugadorDetailModalStyles.status}>No hay estadísticas disponibles.</p>
          )}
          {!loadingStats && !statsError && stats && (
            <>
              <p className={jugadorDetailModalStyles.field}>
                Totales: {stats.totalesGlobal.apps} partidos, {stats.totalesGlobal.goles} goles,{" "}
                {stats.totalesGlobal.asistencias} asistencias
                {stats.totalesGlobal.ratingPromedio !== null &&
                  `, rating ${stats.totalesGlobal.ratingPromedio}`}
              </p>
              {stats.estadisticasPorTorneo.length === 0 ? (
                <p className={jugadorDetailModalStyles.status}>No hay estadísticas por torneo disponibles.</p>
              ) : (
                <ul>
                  {stats.estadisticasPorTorneo.map((season) => (
                    <li key={season.id} className={jugadorDetailModalStyles.listItem}>
                      <strong>{season.torneo}</strong>: {season.apps ?? 0} partidos, {season.goles ?? 0} goles,
                      rating {season.ratingPromedio ?? "N/D"}
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </section>

        <section className={jugadorDetailModalStyles.section} aria-labelledby="quotes-title">
          <h3 id="quotes-title" className={jugadorDetailModalStyles.subtitle}>Cotizaciones</h3>
          <form onSubmit={applyQuoteFilters} className={jugadorDetailModalStyles.filters} noValidate>
            <label className={jugadorDetailModalStyles.label}>
              Desde
              <input
                className={jugadorDetailModalStyles.input}
                type="date"
                value={quoteFilters.desde ?? ""}
                aria-invalid={Boolean(dateError)}
                onChange={(event) => updateFilter("desde", event.target.value)}
              />
            </label>
            <label className={jugadorDetailModalStyles.label}>
              Hasta
              <input
                className={jugadorDetailModalStyles.input}
                type="date"
                value={quoteFilters.hasta ?? ""}
                aria-invalid={Boolean(dateError)}
                onChange={(event) => updateFilter("hasta", event.target.value)}
              />
            </label>
            <button type="submit" className={jugadorDetailModalStyles.actionButton}>Filtrar</button>
          </form>
          {dateError && <p className={jugadorDetailModalStyles.error} role="alert">{dateError}</p>}
          {loadingQuotes && <p className={jugadorDetailModalStyles.status}>Cargando cotizaciones...</p>}
          {quotesError && (
            <div className={jugadorDetailModalStyles.error} role="alert">
              <p>{quotesError}</p>
              <button type="button" className={jugadorDetailModalStyles.actionButton} onClick={() => void loadQuotes(quotesPage, quoteFilters)}>
                Reintentar
              </button>
            </div>
          )}
          {!loadingQuotes && !quotesError && quotes.length === 0 && (
            <p className={jugadorDetailModalStyles.status}>No hay cotizaciones para el rango seleccionado.</p>
          )}
          {!loadingQuotes && !quotesError && quotes.length > 0 && (
            <div className={jugadorDetailModalStyles.tableWrapper}>
              <table className={jugadorDetailModalStyles.table}>
                <caption className="sr-only">Historial de cotizaciones de {jugador.nombre}</caption>
                <thead>
                  <tr>
                    <th className={jugadorDetailModalStyles.tableHeader}>Fecha</th>
                    <th className={jugadorDetailModalStyles.tableHeader}>Valor</th>
                    <th className={jugadorDetailModalStyles.tableHeader}>Estrategia</th>
                    <th className={jugadorDetailModalStyles.tableHeader}>Ventana</th>
                  </tr>
                </thead>
                <tbody>
                  {quotes.map((quote) => (
                    <tr key={quote.id}>
                      <td className={jugadorDetailModalStyles.listItem}>{new Date(quote.timestampCalculo).toLocaleString()}</td>
                      <td className={jugadorDetailModalStyles.listItem}>{quote.valor}</td>
                      <td className={jugadorDetailModalStyles.listItem}>{quote.estrategiaUsada}</td>
                      <td className={jugadorDetailModalStyles.listItem}>{quote.ventanaInicio} a {quote.ventanaFin}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {quotesTotalPages > 1 && (
                <div className={jugadorDetailModalStyles.pagination}>
                  <button type="button" className={jugadorDetailModalStyles.actionButton} disabled={quotesPage === 0} onClick={() => void loadQuotes(quotesPage - 1, quoteFilters)}>Anterior</button>
                  <span aria-live="polite">Página {quotesPage + 1} de {quotesTotalPages}</span>
                  <button type="button" className={jugadorDetailModalStyles.actionButton} disabled={quotesPage >= quotesTotalPages - 1} onClick={() => void loadQuotes(quotesPage + 1, quoteFilters)}>Siguiente</button>
                </div>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
};
