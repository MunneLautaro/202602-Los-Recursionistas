import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { BalanceCard } from "../features/cuenta/components/BalanceCard";
import { CreditReloadForm } from "../features/cuenta/components/CreditReloadForm";
import { TransactionHistory } from "../features/cuenta/components/TransactionHistory";
import { useAccount } from "../features/cuenta/hooks/useAccount";
import { fetchUserOrders } from "../features/cuenta/api/accountApi";
import { fetchCurrentUser } from "../features/auth/api/profileApi";
import type { LoginUser } from "../features/auth/types";
import type { Order } from "../features/mercado/types";

export function CuentaPage() {
  const storedUserId = useMemo(() => {
    const storedId = localStorage.getItem("userId");
    if (!storedId || !/^\d+$/.test(storedId)) {
      return undefined;
    }
    return Number(storedId);
  }, []);
  const [user, setUser] = useState<LoginUser | null>(() => {
    const stored = localStorage.getItem("usuario");
    if (!stored) {
      return null;
    }
    try {
      return JSON.parse(stored) as LoginUser;
    } catch {
      return null;
    }
  });
  const userId = user?.id ?? storedUserId;
  const account = useAccount(userId);
  const [reloadOpen, setReloadOpen] = useState(false);
  const [profileLoading, setProfileLoading] = useState(true);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [ordersError, setOrdersError] = useState<string | null>(null);

  useEffect(() => {
    void fetchCurrentUser()
      .then((currentUser) => {
        setUser(currentUser);
        localStorage.setItem("usuario", JSON.stringify(currentUser));
        localStorage.setItem("userId", String(currentUser.id));
        localStorage.setItem("nombreUsuario", currentUser.nombreUsuario);
        setProfileError(null);
      })
      .catch((reason: unknown) => {
        setProfileError(
          reason instanceof Error ? reason.message : "No se pudo cargar el perfil.",
        );
      })
      .finally(() => setProfileLoading(false));
  }, []);

  useEffect(() => {
    if (userId === undefined) {
      return;
    }
    void fetchUserOrders(userId)
      .then((response) => {
        setOrders(response.content);
        setOrdersError(null);
      })
      .catch((reason: unknown) => {
        setOrdersError(
          reason instanceof Error ? reason.message : "No se pudieron cargar tus órdenes.",
        );
      });
  }, [userId]);

  if (profileLoading) {
    return (
      <main className="grid gap-6">
        <h1 className="ui-page-title">Mi cuenta</h1>
        <p role="status" className="text-sm text-white/55">Cargando datos del usuario...</p>
      </main>
    );
  }

  if (userId === undefined || user === null) {
    return (
      <main className="grid gap-6">
        <h1 className="ui-page-title">Mi cuenta</h1>
        <p role="alert" className="ui-alert-error">{profileError ?? "No se pudo identificar al usuario autenticado."}</p>
      </main>
    );
  }

  return (
    <main className="grid gap-6">
      <header className="grid gap-3">
        <span className="ui-eyebrow flex items-center gap-3"><span className="ui-dot" aria-hidden="true" />Cartera</span>
        <h1 className="ui-page-title">Mi cuenta</h1>
      </header>
      <section className="ui-card grid gap-2 p-6 text-sm text-white/65 sm:grid-cols-2 sm:p-8">
        <h2 className="ui-eyebrow mb-3 sm:col-span-2">Datos del usuario</h2>
        <p>Usuario: {user.nombreUsuario}</p>
        <p>ID: {userId}</p>
        <p>Estado: {user.habilitado ? "Habilitado" : "No disponible"}</p>
        <p>Saldo informado: {user.saldo}</p>
        {user.fechaCreacion && <p>Creada: {new Date(user.fechaCreacion).toLocaleString("es-AR")}</p>}
      </section>
      {account.error && (
        <div role="alert" className="ui-alert-error grid gap-3">
          <p>{account.errorStatus === 403 ? "No tenés permisos para consultar esta cuenta." : account.error}</p>
          <button type="button" className="ui-btn-ghost w-fit px-4 py-2 text-sm" onClick={account.reloadTransactions}>Reintentar historial</button>
        </div>
      )}
      <BalanceCard balance={account.balance} loading={account.loading && account.balance === null} />
      <button type="button" className="ui-btn-primary w-fit px-6" onClick={() => setReloadOpen(true)}>
        Cargar saldo
      </button>
      {reloadOpen && (
        <div className="ui-overlay fixed inset-0 z-[60] grid place-items-center p-4">
          <div className="ui-modal grid w-full max-w-md gap-3 p-6 sm:p-8">
            <CreditReloadForm disabled={account.reloadingCredits} onSubmit={async (amount) => { await account.reload(amount); setReloadOpen(false); }} />
            <button type="button" className="ui-btn-ghost w-full py-3" onClick={() => setReloadOpen(false)}>Cancelar</button>
          </div>
        </div>
      )}
      <nav className="flex flex-wrap gap-3">
        <Link className="ui-btn-ghost px-5 py-2.5 text-sm" to="/cuenta/ordenes">Ver todas mis órdenes</Link>
        <Link className="ui-btn-ghost px-5 py-2.5 text-sm" to="/cuenta/tokens">Mis tokens</Link>
        <Link className="ui-btn-ghost px-5 py-2.5 text-sm" to="/cuenta/transacciones">Ver transacciones</Link>
      </nav>
      <section className="grid gap-3">
        <h2 className="ui-eyebrow">Mis órdenes</h2>
        {ordersError && <p role="alert" className="ui-alert-error">{ordersError}</p>}
        {!ordersError && orders.length === 0 && <p className="text-sm text-white/50">No tenés órdenes registradas.</p>}
        {orders.map((order) => (
          <article key={order.id} className="ui-tile grid gap-1.5 p-5 text-sm text-white/60 tabular-nums">
            <strong className="text-base font-semibold text-white">{order.tipo} - {order.jugadorNombre ?? `Jugador #${order.jugadorId}`}</strong>
            <p>Estado: {order.estado} · Cantidad: {order.cantidadDisponible}/{order.cantidadOriginal}</p>
            <p>Precio unitario: {order.precioUnitario} · Total restante: {order.precioTotalRemanente}</p>
          </article>
        ))}
      </section>
      <TransactionHistory
        transactions={account.transactions}
        page={account.page}
        totalPages={account.totalPages}
        loading={account.loading}
        onPageChange={account.goToPage}
      />
    </main>
  );
}
