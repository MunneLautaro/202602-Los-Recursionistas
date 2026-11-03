import { useState, type FormEvent } from "react";
import { createBuyOrder, createSellOrder } from "../api/ordersApi";
import type { Order, OrderType } from "../types";
import { orderFormStyles } from "./OrderForm.styles";
import { PlayerSearch } from "./PlayerSearch";
import type { JugadorResponseDTO } from "../../jugadores/types";

interface OrderFormProps {
  onSuccess: (order: Order) => void;
  onClose: () => void;
}

export function OrderForm({ onSuccess, onClose }: OrderFormProps) {
  const [player, setPlayer] = useState<JugadorResponseDTO | null>(null);
  const [cantidad, setCantidad] = useState("");
  const [tipo, setTipo] = useState<OrderType>("COMPRA");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const parsedQuantity = Number(cantidad);
    if (!player || !Number.isInteger(parsedQuantity) || parsedQuantity <= 0) {
      setError("Seleccioná un jugador y una cantidad entera positiva.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const order = tipo === "COMPRA"
        ? await createBuyOrder({ jugadorId: player.id, cantidad: parsedQuantity })
        : await createSellOrder({ jugadorId: player.id, cantidad: parsedQuantity });
      onSuccess(order);
      setCantidad("");
      onClose();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "No se pudo crear la orden.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className={orderFormStyles.form} onSubmit={submit}>
      <h2>Crear orden</h2>
      <PlayerSearch selected={player} onSelect={setPlayer} />
      <input className={orderFormStyles.input} type="number" min="1" step="1" placeholder="Cantidad"
        value={cantidad} onChange={(event) => setCantidad(event.target.value)} />
      <select className={orderFormStyles.input} value={tipo}
        onChange={(event) => setTipo(event.target.value as OrderType)}>
        <option value="COMPRA">Comprar</option>
        <option value="VENTA">Vender</option>
      </select>
      {error && <p className={orderFormStyles.error}>{error}</p>}
      <div className={orderFormStyles.actions}>
        <button className={orderFormStyles.button} type="button" onClick={onClose}>Cancelar</button>
        <button className={orderFormStyles.button} disabled={loading} type="submit">
          {loading ? "Enviando..." : "Confirmar"}
        </button>
      </div>
    </form>
  );
}
