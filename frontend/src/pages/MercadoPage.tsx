import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../features/auth";
import { OrderFilters } from "../features/mercado/components/OrderFilters";
import { OrderForm } from "../features/mercado/components/OrderForm";
import { OrderList } from "../features/mercado/components/OrderList";
import { useOrders } from "../features/mercado/hooks/useOrders";
import type { OrderFilters as OrderFiltersValue, Order } from "../features/mercado/types";
import { PaginationControls } from "../features/jugadores/components/PaginationControls";

export function MercadoPage() {
  const { isAuthenticated } = useAuth();
  const [filters, setFilters] = useState<OrderFiltersValue>({});
  const [lastOrder, setLastOrder] = useState<Order | null>(null);
  const [orderModalOpen, setOrderModalOpen] = useState(false);
  const { orders, page, totalPages, loading, error, reload, goToPage } = useOrders(filters);

  return (
    <main className="container mx-auto grid gap-8 p-4">
      <section>
        <h1 className="mb-4 text-2xl font-bold">Mercado</h1>
        <div className="flex flex-wrap items-end justify-between gap-4">
          <OrderFilters filters={filters} onChange={(next) => setFilters(next)} />
          {isAuthenticated && (
            <button type="button" className="rounded bg-[var(--primary)] px-4 py-2 text-[var(--pantone-darkest-hour)]" onClick={() => setOrderModalOpen(true)}>
              Crear orden
            </button>
          )}
        </div>
        {loading && <p>Cargando órdenes...</p>}
        {error && <div className="text-[var(--secondary)]"><p>{error}</p><button onClick={reload}>Reintentar</button></div>}
        {!loading && !error && orders.length === 0 && <p>No hay órdenes disponibles.</p>}
        {!loading && !error && orders.length > 0 && <OrderList orders={orders} />}
        <PaginationControls page={page} totalPages={totalPages} onPageChange={goToPage} />
      </section>
      {isAuthenticated ? (
        <section>
          {orderModalOpen && (
            <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4" role="dialog" aria-modal="true">
              <div className="w-full max-w-lg rounded-lg bg-[var(--bg)] p-5 shadow-[var(--shadow)]">
                <OrderForm onSuccess={(order) => { setLastOrder(order); void reload(); }} onClose={() => setOrderModalOpen(false)} />
              </div>
            </div>
          )}
          {lastOrder && <p className="mt-3 text-[var(--accent)]">Orden #{lastOrder.id} creada correctamente.</p>}
        </section>
      ) : (
        <p>Para comprar o vender, <Link className="text-[var(--accent)]" to="/login">iniciá sesión</Link>.</p>
      )}
    </main>
  );
}
