import { useCallback, useEffect, useState } from "react";
import { estrategiaApi } from "../api/estrategiaApi";
import type { EstadoEstrategia, ResultadoCambioEstrategia } from "../types";

export function useEstrategiaCotizacion() {
  const [estado, setEstado] = useState<EstadoEstrategia | null>(null);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resultado, setResultado] = useState<ResultadoCambioEstrategia | null>(null);

  const consultar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      setEstado(await estrategiaApi.consultar());
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "No se pudo consultar la estrategia.");
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    const timeout = window.setTimeout(() => void consultar(), 0);
    return () => window.clearTimeout(timeout);
  }, [consultar]);

  const cambiar = async (estrategia: string) => {
    setGuardando(true);
    setError(null);
    setResultado(null);
    try {
      const nuevoResultado = await estrategiaApi.cambiar(estrategia);
      setResultado(nuevoResultado);
      await consultar();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "No se pudo cambiar la estrategia.");
    } finally {
      setGuardando(false);
    }
  };

  return { estado, cargando, guardando, error, resultado, cambiar };
}
