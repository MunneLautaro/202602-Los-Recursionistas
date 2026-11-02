import { balanceCardStyles } from "./BalanceCard.styles";
import type { Balance } from "../types";

interface BalanceCardProps {
  balance: Balance | null;
  loading: boolean;
}

const formatAmount = (amount: number) =>
  new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
  }).format(amount);

export function BalanceCard({ balance, loading }: BalanceCardProps) {
  if (loading) {
    return <p aria-live="polite">Cargando saldo...</p>;
  }

  if (!balance) {
    return <p>No hay información de saldo disponible.</p>;
  }

  return (
    <section className={balanceCardStyles.card} aria-label="Saldo de la cuenta">
      <h2 className={balanceCardStyles.title}>Saldo</h2>
      <div className={balanceCardStyles.values}>
        <div className={balanceCardStyles.value}>
          <span className={balanceCardStyles.label}>Total</span>
          <strong className={balanceCardStyles.amount}>{formatAmount(balance.saldo)}</strong>
        </div>
        <div className={balanceCardStyles.value}>
          <span className={balanceCardStyles.label}>Reservado</span>
          <strong className={balanceCardStyles.amount}>{formatAmount(balance.saldoReservado)}</strong>
        </div>
        <div className={balanceCardStyles.value}>
          <span className={balanceCardStyles.label}>Disponible</span>
          <strong className={balanceCardStyles.amount}>{formatAmount(balance.saldoDisponible)}</strong>
        </div>
      </div>
    </section>
  );
}
