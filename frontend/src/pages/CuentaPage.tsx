import { useMemo } from "react";
import { BalanceCard } from "../features/cuenta/components/BalanceCard";
import { CreditReloadForm } from "../features/cuenta/components/CreditReloadForm";
import { TransactionHistory } from "../features/cuenta/components/TransactionHistory";
import { useAccount } from "../features/cuenta/hooks/useAccount";

export function CuentaPage() {
  const userId = useMemo(() => {
    const storedId = localStorage.getItem("userId");
    if (!storedId || !/^\d+$/.test(storedId)) {
      return undefined;
    }
    return Number(storedId);
  }, []);
  const account = useAccount(userId);

  if (userId === undefined) {
    return (
      <main className="container mx-auto grid gap-4 p-4">
        <h1 className="text-2xl font-bold text-[var(--text-h)]">Mi cuenta</h1>
        <p role="status">
          El login actual solo devuelve un token y no permite identificar la cuenta de forma
          confiable. No se puede cargar saldo ni historial sin un userId entregado por el backend.
        </p>
      </main>
    );
  }

  return (
    <main className="container mx-auto grid gap-6 p-4">
      <h1 className="text-2xl font-bold text-[var(--text-h)]">Mi cuenta</h1>
      {account.error && (
        <div role="alert" className="grid gap-2 text-[var(--secondary)]">
          <p>{account.errorStatus === 403 ? "No tenés permisos para consultar esta cuenta." : account.error}</p>
          <button type="button" onClick={account.reloadTransactions}>Reintentar historial</button>
        </div>
      )}
      <BalanceCard balance={account.balance} loading={account.loading && account.balance === null} />
      <CreditReloadForm
        disabled={account.reloadingCredits}
        onSubmit={account.reload}
      />
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
