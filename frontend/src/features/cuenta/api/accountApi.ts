import { fetchClient } from "../../../lib/fetchClient";
import type { Balance, CreditReloadRequest, PaginatedResponse, Transaction } from "../types";

export function reloadCredits(userId: number, monto: number): Promise<Balance> {
  const request: CreditReloadRequest = { monto };
  return fetchClient<Balance>(`/users/${userId}/credits`, {
    method: "POST",
    body: JSON.stringify(request),
  });
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
