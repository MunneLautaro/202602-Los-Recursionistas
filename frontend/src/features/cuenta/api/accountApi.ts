import { fetchClient } from "../../../lib/fetchClient";
import type { Balance, CreditReloadRequest, PaginatedResponse, Transaction } from "../types";
import type { Order } from "../../mercado/types";

export function reloadCredits(userId: number, monto: number): Promise<Balance> {
  const request: CreditReloadRequest = { monto };
  return fetchClient<Balance>(`/users/${userId}/credits`, {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function fetchBalance(userId: number): Promise<Balance> {
  return fetchClient<Balance>(`/users/${userId}/credits`);
}

export function fetchTransactions(
  userId: number,
  page = 0,
  size = 12,
): Promise<PaginatedResponse<Transaction>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  return fetchClient<PaginatedResponse<Transaction>>(
    `/users/${userId}/transactions?${params}`,
  );
}

export function fetchUserOrders(userId: number, page = 0, size = 12): Promise<PaginatedResponse<Order>> {
  return fetchClient<PaginatedResponse<Order>>(`/users/${userId}/orders?page=${page}&size=${size}`);
}

export interface TokenHolding {
  id: number;
  jugadorId: number;
  jugadorNombre: string;
  equipoNombre: string;
  cantidadTotal: number;
  cantidadReservada: number;
  cantidadDisponible: number;
}

export function fetchTokens(userId: number, page = 0, size = 12): Promise<PaginatedResponse<TokenHolding>> {
  return fetchClient<PaginatedResponse<TokenHolding>>(`/users/${userId}/tokens?page=${page}&size=${size}`);
}
