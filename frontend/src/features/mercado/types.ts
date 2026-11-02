import type { PaginatedResponse } from "../jugadores/api/jugadoresApi";

export type { PaginatedResponse };

export interface RankingPlayer {
  jugadorId: number;
  nombre: string;
  posicion: string;
  equipo: string | null;
  liga: string | null;
  valorActual: number;
  valorAnterior: number | null;
  variacion: number | null;
}

export interface RankingFilters {
  ligaId?: number;
  posicion?: string;
}

export type OrderType = "COMPRA" | "VENTA";
export type OrderStatus = "ABIERTA" | "CERRADA" | "CANCELADA" | string;

export interface Order {
  id: number;
  jugadorId: number;
  tipo: OrderType;
  cantidadOriginal: number;
  cantidadDisponible: number;
  precioUnitario: number;
  precioTotalRemanente: number;
  estado: OrderStatus;
  fechaCreacion: string;
}

export interface OrderFilters {
  jugadorId?: number;
  tipo?: OrderType;
}

export interface OrderRequest {
  jugadorId: number;
  cantidad: number;
}
