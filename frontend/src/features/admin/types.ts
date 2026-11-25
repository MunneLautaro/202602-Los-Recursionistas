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

// --- Observabilidad (feature 013): contratos de /api/admin/observabilidad ---

export type PeriodoObservabilidad = 7 | 30 | 90;

export interface MetricasEnVivo {
  inicio: string;
  rechazosSaldoInsuficiente: number;
  recalculosExitosos: number;
  recalculosFallidos: number;
  duracionUltimoRecalculoMs: number | null;
  duracionPromedioRecalculoMs: number | null;
  jugadoresCotizadosUltimoRecalculo: number | null;
  variacionPromedioUltimoRecalculo: number | null;
}

export interface DiaMercado {
  dia: string;
  ordenesCompra: number;
  ordenesVenta: number;
  emparejamientos: number;
  volumenCreditos: number;
}

export interface DiaCotizacion {
  dia: string;
  jugadoresCotizados: number;
  variacionPromedio: number | null;
}

export interface ResumenNegocio {
  dias: PeriodoObservabilidad;
  desde: string;
  hasta: string;
  enVivo: MetricasEnVivo;
  actual: {
    tokensEnCirculacion: number;
    estrategiaActiva: { codigo: string; nombre: string };
  };
  periodo: {
    ordenesCompra: number;
    ordenesVenta: number;
    emparejamientos: number;
    volumenCreditos: number;
    cotizacionesCalculadas: number;
    variacionPromedio: number | null;
  };
  serieMercado: DiaMercado[];
  serieCotizacion: DiaCotizacion[];
}

export interface EndpointRequests {
  metodo: string;
  uri: string;
  cantidad: number;
  erroresCliente: number;
  erroresServidor: number;
  latenciaMediaMs: number | null;
}

export interface ResumenTecnico {
  instante: string;
  salud: { estado: string; componentes: { nombre: string; estado: string }[] };
  requests: {
    total: number;
    latenciaMediaMs: number | null;
    tasaErrorCliente: number | null;
    tasaErrorServidor: number | null;
    endpoints: EndpointRequests[];
  };
  jvm: {
    memoriaHeapUsadaBytes: number | null;
    memoriaHeapMaximaBytes: number | null;
    threadsVivos: number | null;
    cpuProceso: number | null;
    cpuSistema: number | null;
    uptimeSegundos: number | null;
  };
  poolConexiones: {
    activas: number | null;
    inactivas: number | null;
    pendientes: number | null;
    maximas: number | null;
  };
}

export interface MuestraTecnica {
  instante: number;
  heapUsadoMb: number | null;
  cpuProceso: number | null;
  cpuSistema: number | null;
  conexionesActivas: number | null;
  conexionesPendientes: number | null;
}