export interface JugadorFiltroRequestDTO {
  nombre?: string;
  posicion?: string;
  ligaId?: number;
  equipoId?: number;
}

export interface JugadorResponseDTO {
  id: number;
  nombre: string;
  posicion: string;
  edad: number;
  equipo?: {
    id: number;
    nombre: string;
  };
  liga?: {
    id: number;
    nombre: string;
  };
}

export interface PaginatedJugadores {
  content: JugadorResponseDTO[];
  totalPages: number;
  number: number;
  size: number;
  totalElements: number;
}
