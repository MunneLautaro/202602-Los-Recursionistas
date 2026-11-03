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
      <main className="container mx-auto grid gap-4 p-4">
        <h1 className="text-2xl font-bold text-[var(--text-h)]">Mi cuenta</h1>
        <p role="status">Cargando datos del usuario...</p>
      </main>
    );
  }

  if (userId === undefined || user === null) {
    return (
      <main className="container mx-auto grid gap-4 p-4">
        <h1 className="text-2xl font-bold text-[var(--text-h)]">Mi cuenta</h1>
        <p role="alert">{profileError ?? "No se pudo identificar al usuario autenticado."}</p>
      </main>
    );
  }

  return (
    <main className="container mx-auto grid gap-6 p-4">
      <h1 className="text-2xl font-bold text-[var(--text-h)]">Mi cuenta</h1>
      <section className="rounded border border-[var(--border)] p-4 text-left">
        <h2>Datos del usuario</h2>
        <p>Usuario: {user.nombreUsuario}</p>
        <p>ID: {userId}</p>
        <p>Estado: {user.habilitado ? "Habilitado" : "No disponible"}</p>
        <p>Saldo informado: {user.saldo}</p>
        {user.fechaCreacion && <p>Creada: {new Date(user.fechaCreacion).toLocaleString("es-AR")}</p>}
      </section>
      {account.error && (
        <div role="alert" className="grid gap-2 text-[var(--secondary)]">
          <p>{account.errorStatus === 403 ? "No tenés permisos para consultar esta cuenta." : account.error}</p>
          <button type="button" className="rounded-lg border border-[var(--accent)] px-3 py-2 font-semibold text-[var(--accent)] hover:bg-[var(--accent-bg)]" onClick={account.reloadTransactions}>Reintentar historial</button>
        </div>
      )}
      <BalanceCard balance={account.balance} loading={account.loading && account.balance === null} />
      <button type="button" className="w-fit rounded-lg bg-[var(--primary)] px-4 py-2 font-semibold text-[var(--pantone-darkest-hour)] transition hover:brightness-95" onClick={() => setReloadOpen(true)}>
        Cargar saldo
      </button>
      {reloadOpen && (
        <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-[var(--bg)] p-5">
            <CreditReloadForm disabled={account.reloadingCredits} onSubmit={async (amount) => { await account.reload(amount); setReloadOpen(false); }} />
            <button type="button" className="rounded-lg border border-[var(--border)] px-4 py-2 font-semibold text-[var(--text)] hover:bg-[var(--accent-bg)]" onClick={() => setReloadOpen(false)}>Cancelar</button>
          </div>
        </div>
      )}
      <nav className="flex flex-wrap gap-3">
        <Link className="rounded bg-[var(--accent)] px-4 py-2 text-[var(--pantone-cloud-dancer)]" to="/cuenta/ordenes">Ver todas mis órdenes</Link>
        <Link className="rounded bg-[var(--accent)] px-4 py-2 text-[var(--pantone-cloud-dancer)]" to="/cuenta/tokens">Mis tokens</Link>
        <Link className="rounded bg-[var(--accent)] px-4 py-2 text-[var(--pantone-cloud-dancer)]" to="/cuenta/transacciones">Ver transacciones</Link>
      </nav>
      <section className="grid gap-3 text-left">
        <h2>Mis órdenes</h2>
        {ordersError && <p role="alert" className="text-[var(--secondary)]">{ordersError}</p>}
        {!ordersError && orders.length === 0 && <p>No tenés órdenes registradas.</p>}
        {orders.map((order) => (
          <article key={order.id} className="rounded border border-[var(--border)] p-3">
            <strong>{order.tipo} - {order.jugadorNombre ?? `Jugador #${order.jugadorId}`}</strong>
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
