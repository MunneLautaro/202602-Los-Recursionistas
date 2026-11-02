import { useState, type FormEvent } from "react";
import { createBuyOrder, createSellOrder } from "../api/ordersApi";
import type { Order, OrderType } from "../types";
import { orderFormStyles } from "./OrderForm.styles";

interface OrderFormProps {
  onSuccess: (order: Order) => void;
}

export function OrderForm({ onSuccess }: OrderFormProps) {
  const [jugadorId, setJugadorId] = useState("");
  const [cantidad, setCantidad] = useState("");
  const [tipo, setTipo] = useState<OrderType>("COMPRA");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const parsedPlayer = Number(jugadorId);
    const parsedQuantity = Number(cantidad);
    if (!Number.isInteger(parsedPlayer) || parsedPlayer <= 0 ||
        !Number.isInteger(parsedQuantity) || parsedQuantity <= 0) {
      setError("Jugador y cantidad deben ser enteros positivos.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const order = tipo === "COMPRA"
        ? await createBuyOrder({ jugadorId: parsedPlayer, cantidad: parsedQuantity })
        : await createSellOrder({ jugadorId: parsedPlayer, cantidad: parsedQuantity });
      onSuccess(order);
      setCantidad("");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "No se pudo crear la orden.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className={orderFormStyles.form} onSubmit={submit}>
      <h2>Crear orden</h2>
      <input className={orderFormStyles.input} type="number" min="1" placeholder="ID del jugador"
        value={jugadorId} onChange={(event) => setJugadorId(event.target.value)} />
      <input className={orderFormStyles.input} type="number" min="1" step="1" placeholder="Cantidad"
        value={cantidad} onChange={(event) => setCantidad(event.target.value)} />
      <select className={orderFormStyles.input} value={tipo}
        onChange={(event) => setTipo(event.target.value as OrderType)}>
        <option value="COMPRA">Comprar</option>
        <option value="VENTA">Vender</option>
      </select>
      {error && <p className={orderFormStyles.error}>{error}</p>}
      <div className={orderFormStyles.actions}>
        <button className={orderFormStyles.button} disabled={loading} type="submit">
          {loading ? "Enviando..." : "Confirmar"}
        </button>
      </div>
    </form>
  );
}
