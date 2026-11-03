import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { createBuyOrder, createSellOrder } from "../api/ordersApi";
import type { Order, OrderType } from "../types";
import { orderFormStyles } from "./OrderForm.styles";
import { PlayerSearch } from "./PlayerSearch";
import type { JugadorResponseDTO } from "../../jugadores/types";
import { orderSchema, type OrderFormValues } from "../schemas";
import { CampoFormulario } from "../../../components";

interface OrderFormProps {
  onSuccess: (order: Order) => void;
  onClose: () => void;
}

export function OrderForm({ onSuccess, onClose }: OrderFormProps) {
  const [player, setPlayer] = useState<JugadorResponseDTO | null>(null);
  const [tipo, setTipo] = useState<OrderType>("COMPRA");
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, setValue, formState: { errors, isSubmitting } } = useForm<OrderFormValues>({
    resolver: zodResolver(orderSchema),
    defaultValues: { jugadorId: 0, cantidad: 0 },
  });

  const submit = async (values: OrderFormValues) => {
    setError(null);
    try {
      const order = tipo === "COMPRA"
        ? await createBuyOrder(values)
        : await createSellOrder(values);
      onSuccess(order);
      onClose();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "No se pudo crear la orden.");
    }
  };

  return (
    <form className={orderFormStyles.form} onSubmit={(event) => void handleSubmit(submit)(event)}>
      <h2>Crear orden</h2>
      <PlayerSearch selected={player} onSelect={(selected) => {
        setPlayer(selected);
        setValue("jugadorId", selected?.id ?? 0, { shouldValidate: true });
      }} />
      {errors.jugadorId && <p className={orderFormStyles.error}>{errors.jugadorId.message}</p>}
      <CampoFormulario nombre="cantidad" etiqueta="Cantidad" error={errors.cantidad?.message}
        {...register("cantidad", { valueAsNumber: true })} className={orderFormStyles.input} type="number" min="1" step="1" placeholder="Cantidad" />
      <select className={orderFormStyles.input} value={tipo}
        onChange={(event) => setTipo(event.target.value as OrderType)}>
        <option value="COMPRA">Comprar</option>
        <option value="VENTA">Vender</option>
      </select>
      {error && <p className={orderFormStyles.error}>{error}</p>}
      <div className={orderFormStyles.actions}>
        <button className={orderFormStyles.button} type="button" onClick={onClose}>Cancelar</button>
        <button className={orderFormStyles.button} disabled={isSubmitting} type="submit">
          {isSubmitting ? "Enviando..." : "Confirmar"}
        </button>
      </div>
    </form>
  );
}
