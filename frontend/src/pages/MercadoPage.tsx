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
  const { orders, page, totalPages, loading, error, reload, goToPage } = useOrders(filters);

  return (
    <main className="container mx-auto grid gap-8 p-4">
      <section>
        <h1 className="text-2xl font-bold mb-4">Mercado</h1>
        <OrderFilters filters={filters} onChange={(next) => setFilters(next)} />
        {loading && <p>Cargando órdenes...</p>}
        {error && <div className="text-[var(--secondary)]"><p>{error}</p><button onClick={reload}>Reintentar</button></div>}
        {!loading && !error && orders.length === 0 && <p>No hay órdenes disponibles.</p>}
        {!loading && !error && orders.length > 0 && <OrderList orders={orders} />}
        <PaginationControls page={page} totalPages={totalPages} onPageChange={goToPage} />
      </section>
      {isAuthenticated ? (
        <section>
          <OrderForm onSuccess={setLastOrder} />
          {lastOrder && <p className="mt-3 text-[var(--accent)]">Orden #{lastOrder.id} creada correctamente.</p>}
        </section>
      ) : (
        <p>Para comprar o vender, <Link className="text-[var(--accent)]" to="/login">iniciá sesión</Link>.</p>
      )}
    </main>
  );
}
