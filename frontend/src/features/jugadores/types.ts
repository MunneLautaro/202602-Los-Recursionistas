export interface JugadorFiltroRequestDTO {
  nombre?: string;
  posicion?: string;
  ligaId?: number;
  equipoId?: number;
}

export interface LigaResponseDTO {
  id: number;
  idExterno: number;
  nombre: string;
  codigo: string;
  fechaCreacion: string;
}

export interface EquipoResponseDTO {
  id: number;
  idExterno: number;
  nombre: string;
  nombreCorto: string;
  sigla: string;
  escudoUrl: string;
  fundacion: number;
  colores: string;
  estadio: string;
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
