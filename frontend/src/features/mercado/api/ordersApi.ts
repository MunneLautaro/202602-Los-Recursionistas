import { fetchClient } from "../../../lib/fetchClient";
import type {
  Order,
  OrderFilters,
  OrderRequest,
  PaginatedResponse,
} from "../types";

export async function fetchOrders(
  filters: OrderFilters = {},
  page = 0,
  size = 12,
): Promise<PaginatedResponse<Order>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      params.set(key, String(value));
    }
  });
  return fetchClient<PaginatedResponse<Order>>(`/orders?${params}`);
}

export function createBuyOrder(request: OrderRequest): Promise<Order> {
  return fetchClient<Order>("/orders/buy", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function createSellOrder(request: OrderRequest): Promise<Order> {
  return fetchClient<Order>("/orders/sell", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
