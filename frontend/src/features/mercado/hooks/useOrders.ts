import { useCallback, useEffect, useState } from "react";
import { fetchOrders } from "../api/ordersApi";
import type { Order, OrderFilters } from "../types";

export function useOrders(filters: OrderFilters) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (requestedPage: number) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetchOrders(filters, requestedPage);
      setOrders(response.content);
      setPage(response.number);
      setTotalPages(response.totalPages);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Error al cargar órdenes");
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    const timer = window.setTimeout(() => void load(0), 0);
    return () => window.clearTimeout(timer);
  }, [load]);

  return {
    orders,
    page,
    totalPages,
    loading,
    error,
    reload: () => load(page),
    goToPage: (nextPage: number) => load(nextPage),
  };
}
