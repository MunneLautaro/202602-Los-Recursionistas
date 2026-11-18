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
    <main className="grid gap-8">
      <section className="grid gap-6">
        <header className="grid gap-3">
        <span className="ui-eyebrow flex items-center gap-3"><span className="ui-dot" aria-hidden="true" />Órdenes</span>
        <h1 className="ui-page-title">Mercado</h1>
      </header>
        <div className="flex flex-wrap items-end gap-4">
          <OrderFilters filters={filters} onChange={(next) => setFilters(next)} />
          {isAuthenticated && (
            <button type="button" className="ui-btn-primary min-h-12 px-6" onClick={() => setOrderModalOpen(true)}>
              Crear orden
            </button>
          )}
        </div>
        {loading && <p className="text-sm text-white/55">Cargando órdenes...</p>}
        {error && <div className="ui-alert-error flex flex-wrap items-center justify-between gap-3"><p>{error}</p><button className="ui-btn-ghost px-4 py-2 text-sm" onClick={reload}>Reintentar</button></div>}
        {!loading && !error && orders.length === 0 && <p className="ui-card p-8 text-center text-sm text-white/55">No hay órdenes disponibles.</p>}
        {!loading && !error && orders.length > 0 && <OrderList orders={orders} />}
        <PaginationControls page={page} totalPages={totalPages} onPageChange={goToPage} />
      </section>
      {isAuthenticated ? (
        <section>
          {orderModalOpen && (
            <div className="ui-overlay fixed inset-0 z-[60] grid place-items-center p-4" role="dialog" aria-modal="true">
              <div className="ui-modal w-full max-w-lg p-6 sm:p-8">
                <OrderForm onSuccess={(order) => { setLastOrder(order); void reload(); }} onClose={() => setOrderModalOpen(false)} />
              </div>
            </div>
          )}
          {lastOrder && <p className="ui-alert-success">Orden #{lastOrder.id} creada correctamente.</p>}
        </section>
      ) : (
        <p className="ui-card p-6 text-sm text-white/60">Para comprar o vender, <Link className="font-semibold text-mint hover:text-white" to="/login">iniciá sesión</Link>.</p>
      )}
    </main>
  );
}
