import type { Order } from "../types";
import { orderListStyles } from "./OrderList.styles";

export function OrderList({ orders }: { orders: Order[] }) {
  return (
    <div className={orderListStyles.grid}>
      {orders.map((order) => (
        <article className={orderListStyles.card} key={order.id}>
          <strong>{order.tipo} #{order.id}</strong>
          <div className={orderListStyles.meta}>
            <span>Jugador: {order.jugadorId}</span>
            <span>Estado: {order.estado}</span>
            <span>Cantidad: {order.cantidadDisponible}/{order.cantidadOriginal}</span>
            <span>Precio unitario: {order.precioUnitario}</span>
            <span>Total remanente: {order.precioTotalRemanente}</span>
            <span>{new Date(order.fechaCreacion).toLocaleString()}</span>
          </div>
        </article>
      ))}
    </div>
  );
}
