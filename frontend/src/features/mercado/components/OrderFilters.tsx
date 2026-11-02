import type { FormEvent } from "react";
import type { OrderFilters as OrderFiltersValue, OrderType } from "../types";
import { orderFiltersStyles } from "./OrderFilters.styles";

interface OrderFiltersProps {
  filters: OrderFiltersValue;
  onChange: (filters: OrderFiltersValue) => void;
}

export function OrderFilters({ filters, onChange }: OrderFiltersProps) {
  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onChange(filters);
  };
  return (
    <form className={orderFiltersStyles.form} onSubmit={submit}>
      <label className={orderFiltersStyles.field}>
        Jugador ID
        <input
          className={orderFiltersStyles.input}
          type="number"
          min="1"
          value={filters.jugadorId ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              jugadorId: event.target.value ? Number(event.target.value) : undefined,
            })
          }
        />
      </label>
      <label className={orderFiltersStyles.field}>
        Tipo
        <select
          className={orderFiltersStyles.input}
          value={filters.tipo ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              tipo: (event.target.value || undefined) as OrderType | undefined,
            })
          }
        >
          <option value="">Todos</option>
          <option value="COMPRA">Compra</option>
          <option value="VENTA">Venta</option>
        </select>
      </label>
      <button className={orderFiltersStyles.button} type="submit">Aplicar</button>
    </form>
  );
}
