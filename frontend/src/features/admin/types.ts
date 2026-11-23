export interface EstrategiaDisponible {
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface UltimoCambioEstrategia {
  estrategiaAnterior: string | null;
  estrategiaNueva: string;
  usuarioAdmin: string;
  timestampCambio: string;
}

export interface EstadoEstrategia {
  estrategiaActiva: string;
  nombreActivo: string;
  descripcionActiva: string;
  estrategias: EstrategiaDisponible[];
  origen: string;
  ultimoCambio: UltimoCambioEstrategia | null;
}

export interface ResultadoCambioEstrategia {
  mensaje: string;
  estrategiaActiva: string;
  nombreActivo: string;
  cambioAplicado: boolean;
  ultimoCambio: UltimoCambioEstrategia | null;
}
