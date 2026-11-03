import { PaginationControls } from "../../jugadores/components/PaginationControls";
import { transactionHistoryStyles } from "./TransactionHistory.styles";
import type { Transaction } from "../types";

interface TransactionHistoryProps {
  transactions: Transaction[];
  page: number;
  totalPages: number;
  loading: boolean;
  onPageChange: (page: number) => void;
}

export function TransactionHistory({
  transactions,
  page,
  totalPages,
  loading,
  onPageChange,
}: TransactionHistoryProps) {
  return (
    <section className={transactionHistoryStyles.section}>
      <h2 className="text-xl font-semibold text-[var(--text-h)]">Historial de transacciones</h2>
      {loading && <p aria-live="polite">Cargando historial...</p>}
      {!loading && transactions.length === 0 && <p>No hay transacciones para mostrar.</p>}
      {!loading && transactions.length > 0 && (
        <div className={transactionHistoryStyles.tableWrapper}>
          <table className={transactionHistoryStyles.table}>
            <thead className={transactionHistoryStyles.header}>
              <tr>
                <th className={transactionHistoryStyles.cell}>Jugador</th>
                <th className={transactionHistoryStyles.cell}>Comprador</th>
                <th className={transactionHistoryStyles.cell}>Vendedor</th>
                <th className={transactionHistoryStyles.cell}>Cantidad</th>
                <th className={transactionHistoryStyles.cell}>Precio unitario</th>
                <th className={transactionHistoryStyles.cell}>Total</th>
                <th className={transactionHistoryStyles.cell}>Fecha</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => (
                <tr className={transactionHistoryStyles.row} key={transaction.id}>
                  <td className={transactionHistoryStyles.cell}>{transaction.jugadorId}</td>
                  <td className={transactionHistoryStyles.cell}>{transaction.compradorId}</td>
                  <td className={transactionHistoryStyles.cell}>{transaction.vendedorId}</td>
                  <td className={transactionHistoryStyles.cell}>{transaction.cantidad}</td>
                  <td className={transactionHistoryStyles.cell}>{transaction.precioUnitario}</td>
                  <td className={transactionHistoryStyles.cell}>{transaction.total}</td>
                  <td className={transactionHistoryStyles.cell}>
                    {new Date(transaction.ejecutadaEn).toLocaleString("es-AR")}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <PaginationControls page={page} totalPages={totalPages} onPageChange={onPageChange} />
    </section>
  );
}
