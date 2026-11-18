import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { PaginationControls } from "../features/jugadores/components/PaginationControls";
import { fetchTokens, fetchTransactions, fetchUserOrders } from "../features/cuenta/api/accountApi";
import type { TokenHolding } from "../features/cuenta/api/accountApi";
import type { Transaction } from "../features/cuenta/types";
import type { Order } from "../features/mercado/types";

function useCurrentUserId() {
  return useMemo(() => {
    const value = localStorage.getItem("userId");
    return value && /^\d+$/.test(value) ? Number(value) : undefined;
  }, []);
}

function AccountPageLayout({ title, children }: { title: string; children: ReactNode }) {
  return (
    <main className="grid gap-5">
      <Link to="/cuenta" className="ui-chip w-fit text-white/70 hover:border-white/20 hover:text-white">← Volver a mi cuenta</Link>
      <h1 className="ui-page-title">{title}</h1>
      {children}
    </main>
  );
}

export function CuentaOrdenesPage() {
  const userId = useCurrentUserId();
  const [orders, setOrders] = useState<Order[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    if (userId === undefined) return;
    void fetchUserOrders(userId, page).then((response) => {
      setOrders(response.content);
      setTotalPages(response.totalPages);
    }).catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "No se pudieron cargar las órdenes."))
      .finally(() => setLoading(false));
  }, [page, userId]);
  return <AccountPageLayout title="Mis órdenes">
    {error && <p role="alert" className="ui-alert-error">{error}</p>}
    {loading && <p className="text-sm text-white/55">Cargando órdenes...</p>}
    {!loading && orders.length === 0 && !error && <p className="text-sm text-white/50">No hay órdenes para mostrar.</p>}
    {orders.map((order) => <article key={order.id} className="ui-tile grid gap-1.5 p-5 text-sm text-white/60 tabular-nums">
      <strong className="text-base font-semibold text-white">{order.tipo} - {order.jugadorNombre ?? `Jugador #${order.jugadorId}`}</strong>
      <p>{order.cantidadDisponible}/{order.cantidadOriginal} tokens · Precio {order.precioUnitario} · {order.estado}</p>
    </article>)}
    <PaginationControls page={page} totalPages={totalPages} onPageChange={setPage} />
  </AccountPageLayout>;
}

export function CuentaTokensPage() {
  const userId = useCurrentUserId();
  const [tokens, setTokens] = useState<TokenHolding[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    if (userId === undefined) return;
    void fetchTokens(userId, page).then((response) => {
      setTokens(response.content);
      setTotalPages(response.totalPages);
    })
      .catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "No se pudieron cargar los tokens."))
      .finally(() => setLoading(false));
  }, [page, userId]);
  return <AccountPageLayout title="Mis tokens">
    {error && <p role="alert" className="ui-alert-error">{error}</p>}
    {loading && <p className="text-sm text-white/55">Cargando tokens...</p>}
    {!loading && tokens.length === 0 && !error && <p className="text-sm text-white/50">No tenés tokens disponibles.</p>}
    {tokens.map((token) => <article key={token.id} className="ui-tile grid gap-1.5 p-5 text-sm text-white/60 tabular-nums">
      <strong className="text-base font-semibold text-white">{token.jugadorNombre}</strong><p>{token.equipoNombre}</p>
      <p>Total: {token.cantidadTotal} · Reservados: {token.cantidadReservada} · Disponibles: {token.cantidadDisponible}</p>
    </article>)}
    <PaginationControls page={page} totalPages={totalPages} onPageChange={setPage} />
  </AccountPageLayout>;
}

export function CuentaTransaccionesPage() {
  const userId = useCurrentUserId();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    if (userId === undefined) return;
    void fetchTransactions(userId, page).then((response) => {
      setTransactions(response.content);
      setTotalPages(response.totalPages);
    })
      .catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "No se pudieron cargar las transacciones."))
      .finally(() => setLoading(false));
  }, [page, userId]);
  return <AccountPageLayout title="Mis transacciones">
    {error && <p role="alert" className="ui-alert-error">{error}</p>}
    {loading && <p className="text-sm text-white/55">Cargando transacciones...</p>}
    {!loading && transactions.length === 0 && !error && <p className="text-sm text-white/50">No hay transacciones realizadas.</p>}
    {transactions.map((transaction) => <article key={transaction.id} className="ui-tile grid gap-1.5 p-5 text-sm text-white/60 tabular-nums">
      <strong className="text-base font-semibold text-white">{transaction.jugadorNombre ?? `Jugador #${transaction.jugadorId}`}</strong>
      <p>{transaction.cantidad} tokens · Total {transaction.total} · {new Date(transaction.ejecutadaEn).toLocaleString("es-AR")}</p>
    </article>)}
    <PaginationControls page={page} totalPages={totalPages} onPageChange={setPage} />
  </AccountPageLayout>;
}
