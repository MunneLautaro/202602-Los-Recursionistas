import type { PaginatedResponse } from "../jugadores/api/jugadoresApi";

export type { PaginatedResponse };

export interface Balance {
  usuarioId: number;
  saldo: number;
  saldoReservado: number;
  saldoDisponible: number;
}

export interface Transaction {
  id: number;
  jugadorId: number;
  compradorId: number;
  vendedorId: number;
  cantidad: number;
  precioUnitario: number;
  total: number;
  ejecutadaEn: string;
  jugadorNombre?: string;
  compradorNombre?: string;
  vendedorNombre?: string;
}

export interface CreditReloadRequest {
  monto: number;
}
