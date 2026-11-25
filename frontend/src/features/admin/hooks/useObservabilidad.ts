import { useCallback, useEffect, useRef, useState } from "react";
import { ApiError } from "../../../lib/fetchClient";
import { observabilidadApi } from "../api/observabilidadApi";
import type {
  MuestraTecnica,
  PeriodoObservabilidad,
  ResumenNegocio,
  ResumenTecnico,
} from "../types";

const INTERVALO_MS = 30_000;
const MAXIMO_MUESTRAS = 60;
const BYTES_POR_MB = 1024 * 1024;

function mensajeDeError(reason: unknown, porDefecto: string): string {
  return reason instanceof Error ? reason.message : porDefecto;
}

function esSesionInvalida(reason: unknown): boolean {
  return reason instanceof ApiError && reason.status === 401;
}

function muestraDesde(tecnico: ResumenTecnico): MuestraTecnica {
  const heap = tecnico.jvm.memoriaHeapUsadaBytes;
  return {
    instante: Date.parse(tecnico.instante) || Date.now(),
    heapUsadoMb: heap === null ? null : heap / BYTES_POR_MB,
    cpuProceso: tecnico.jvm.cpuProceso,
    cpuSistema: tecnico.jvm.cpuSistema,
    conexionesActivas: tecnico.poolConexiones.activas,
    conexionesPendientes: tecnico.poolConexiones.pendientes,
  };
}

/**
 * Estado del panel de observabilidad con refresco cada 30 s mientras la pestaña está visible.
 * Antes de pedir los datos protegidos consulta la salud pública: si el servicio no está UP
 * (por ejemplo, base de datos caída) no los pide, para no provocar un 401 que cierre la sesión.
 */
export function useObservabilidad() {
  const [periodo, setPeriodo] = useState<PeriodoObservabilidad>(30);
  const [negocio, setNegocio] = useState<ResumenNegocio | null>(null);
  const [errorNegocio, setErrorNegocio] = useState<string | null>(null);
  const [cargandoNegocio, setCargandoNegocio] = useState(true);
  const [tecnico, setTecnico] = useState<ResumenTecnico | null>(null);
  const [errorTecnico, setErrorTecnico] = useState<string | null>(null);
  const [cargandoTecnico, setCargandoTecnico] = useState(true);
  const [muestrasTecnicas, setMuestrasTecnicas] = useState<MuestraTecnica[]>([]);
  const [estadoServicio, setEstadoServicio] = useState<string | null>(null);
  const [ultimaActualizacion, setUltimaActualizacion] = useState<Date | null>(null);
  const detenido = useRef(false);
  const montado = useRef(true);

  const actualizar = useCallback(async (dias: PeriodoObservabilidad) => {
    if (detenido.current) {
      return;
    }
    let estado: string;
    try {
      estado = await observabilidadApi.saludPublica();
    } catch {
      estado = "DOWN";
    }
    if (!montado.current) {
      return;
    }
    if (estado !== "UP") {
      setEstadoServicio(estado);
      setCargandoNegocio(false);
      setCargandoTecnico(false);
      return;
    }
    setEstadoServicio(null);

    const [resultadoNegocio, resultadoTecnico] = await Promise.allSettled([
      observabilidadApi.negocio(dias),
      observabilidadApi.tecnico(),
    ]);
    if (!montado.current) {
      return;
    }
    if (resultadoNegocio.status === "fulfilled") {
      setNegocio(resultadoNegocio.value);
      setErrorNegocio(null);
    } else {
      setErrorNegocio(mensajeDeError(resultadoNegocio.reason, "No se pudieron obtener las métricas de negocio."));
    }
    if (resultadoTecnico.status === "fulfilled") {
      setTecnico(resultadoTecnico.value);
      setErrorTecnico(null);
      setMuestrasTecnicas((previas) =>
        [...previas, muestraDesde(resultadoTecnico.value)].slice(-MAXIMO_MUESTRAS),
      );
    } else {
      setErrorTecnico(mensajeDeError(resultadoTecnico.reason, "No se pudieron obtener las métricas técnicas."));
    }
    if (esSesionInvalida(resultadoNegocio.status === "rejected" ? resultadoNegocio.reason : null)
      || esSesionInvalida(resultadoTecnico.status === "rejected" ? resultadoTecnico.reason : null)) {
      // La sesión expiró: fetchClient ya emitió "auth:invalidated" y AdminRoute redirige al login.
      detenido.current = true;
    }
    setCargandoNegocio(false);
    setCargandoTecnico(false);
    setUltimaActualizacion(new Date());
  }, []);

  useEffect(() => {
    montado.current = true;
    const consultarSiVisible = () => {
      if (document.visibilityState === "visible") {
        void actualizar(periodo);
      }
    };
    const inicial = window.setTimeout(consultarSiVisible, 0);
    const intervalo = window.setInterval(consultarSiVisible, INTERVALO_MS);
    document.addEventListener("visibilitychange", consultarSiVisible);
    return () => {
      montado.current = false;
      window.clearTimeout(inicial);
      window.clearInterval(intervalo);
      document.removeEventListener("visibilitychange", consultarSiVisible);
    };
  }, [actualizar, periodo]);

  const cambiarPeriodo = (nuevo: PeriodoObservabilidad) => {
    if (nuevo !== periodo) {
      setCargandoNegocio(true);
      setPeriodo(nuevo);
    }
  };

  return {
    periodo,
    cambiarPeriodo,
    negocio,
    errorNegocio,
    cargandoNegocio,
    tecnico,
    errorTecnico,
    cargandoTecnico,
    muestrasTecnicas,
    estadoServicio,
    ultimaActualizacion,
  };
}
