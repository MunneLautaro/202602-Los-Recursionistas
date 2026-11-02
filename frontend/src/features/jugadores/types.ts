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

export interface SeasonStats {
  id: number;
  jugadorId: number;
  jugadorNombre: string;
  posicion: string;
  equipoNombre: string | null;
  torneo: string;
  torneoWhoscoredId: number | null;
  temporadaInicio: string | null;
  temporadaFin: string | null;
  ultimaActualizacion: string | null;
  apps: number | null;
  minutosTotales: number | null;
  goles: number | null;
  asistencias: number | null;
  tirosAlArco: number | null;
  pasesClave: number | null;
  regates: number | null;
  tackles: number | null;
  intercepciones: number | null;
  amarillas: number | null;
  rojas: number | null;
  ratingPromedio: number | null;
  precisionPasesPromedio: number | null;
}

export interface GlobalStats {
  apps: number;
  goles: number;
  asistencias: number;
  ratingPromedio: number | null;
}

export interface PlayerStats {
  jugadorId: number;
  nombre: string;
  posicion: string;
  equipo: string | null;
  estadisticasPorTorneo: SeasonStats[];
  totalesGlobal: GlobalStats;
}

export interface Quote {
  id: number;
  jugadorId: number;
  valor: number;
  timestampCalculo: string;
  estrategiaUsada: string;
  ventanaInicio: string;
  ventanaFin: string;
}
