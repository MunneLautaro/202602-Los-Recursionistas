import { useCallback, useEffect, useState } from "react";
import { fetchTransactions, reloadCredits } from "../api/accountApi";
import type { Balance, Transaction } from "../types";

export function useAccount(userId?: number) {
  const [balance, setBalance] = useState<Balance | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [reloadingCredits, setReloadingCredits] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [errorStatus, setErrorStatus] = useState<number | null>(null);

  const loadTransactions = useCallback(
    async (requestedPage: number) => {
      if (userId === undefined) {
        return;
      }

      setLoading(true);
      setError(null);
      setErrorStatus(null);
      try {
        const response = await fetchTransactions(userId, requestedPage);
        setTransactions(response.content);
        setPage(response.number);
        setTotalPages(response.totalPages);
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : "No se pudo cargar el historial.");
        if (typeof reason === "object" && reason !== null && "status" in reason) {
          const status = (reason as { status?: unknown }).status;
          setErrorStatus(typeof status === "number" ? status : null);
        }
      } finally {
        setLoading(false);
      }
    },
    [userId],
  );

  useEffect(() => {
    if (userId !== undefined) {
      const timer = window.setTimeout(() => void loadTransactions(0), 0);
      return () => window.clearTimeout(timer);
    }
    return undefined;
  }, [loadTransactions, userId]);

  const reload = useCallback(
    async (monto: number) => {
      if (userId === undefined) {
        return;
      }

      setReloadingCredits(true);
      setError(null);
      setErrorStatus(null);
      try {
        setBalance(await reloadCredits(userId, monto));
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : "No se pudo recargar créditos.");
        if (typeof reason === "object" && reason !== null && "status" in reason) {
          const status = (reason as { status?: unknown }).status;
          setErrorStatus(typeof status === "number" ? status : null);
        }
      } finally {
        setReloadingCredits(false);
      }
    },
    [userId],
  );

  return {
    balance,
    transactions,
    page,
    totalPages,
    loading,
    reloadingCredits,
    error,
    errorStatus,
    reload,
    reloadTransactions: () => loadTransactions(page),
    goToPage: (nextPage: number) => loadTransactions(nextPage),
  };
}
