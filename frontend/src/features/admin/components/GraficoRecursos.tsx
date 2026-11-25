import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { MuestraTecnica, ResumenTecnico } from "../types";
import { graficoRecursosStyles as styles } from "./GraficoRecursos.styles";
import { TarjetaMetrica } from "./TarjetaMetrica";
import {
  COLOR_AMBAR,
  COLOR_CORAL,
  COLOR_GRILLA,
  COLOR_LAVANDA,
  COLOR_MINT,
  ESTILO_EJE,
  ESTILO_TOOLTIP,
  formatearHora,
  formatearNumero,
  formatearPorcentaje,
  formatearUptime,
} from "./temaGraficos";

const BYTES_POR_MB = 1024 * 1024;
const MINIMO_MUESTRAS = 2;

export interface GraficoRecursosProps {
  tecnico: ResumenTecnico;
  muestras: MuestraTecnica[];
}

function megas(bytes: number | null): string {
  return bytes === null ? "Sin datos" : `${formatearNumero(bytes / BYTES_POR_MB)} MB`;
}

export function GraficoRecursos({ tecnico, muestras }: GraficoRecursosProps) {
  const { jvm, poolConexiones } = tecnico;
  const recolectando = muestras.length < MINIMO_MUESTRAS;

  return (
    <div className={styles.raiz}>
      <div className={styles.tarjetas}>
        <TarjetaMetrica etiqueta="Tiempo activo" valor={formatearUptime(jvm.uptimeSegundos)} />
        <TarjetaMetrica etiqueta="Threads vivos" valor={formatearNumero(jvm.threadsVivos)} />
        <TarjetaMetrica etiqueta="Heap usado / máximo" valor={`${megas(jvm.memoriaHeapUsadaBytes)} / ${megas(jvm.memoriaHeapMaximaBytes)}`} />
        <TarjetaMetrica
          etiqueta="Pool (inactivas / máx.)"
          valor={`${formatearNumero(poolConexiones.inactivas)} / ${formatearNumero(poolConexiones.maximas)}`}
          tono={poolConexiones.pendientes ? "alerta" : "neutro"}
        />
      </div>

      <div className={styles.grilla}>
        <figure className={styles.marco}>
          <figcaption className={styles.titulo}>Memoria y CPU</figcaption>
          {recolectando ? (
            <p className={styles.vacio} role="status">Recolectando datos…</p>
          ) : (
            <div className={styles.lienzo} role="img" aria-label="Evolución de memoria heap y uso de CPU durante esta sesión">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={muestras} margin={{ top: 4, right: 0, left: -8, bottom: 0 }}>
                  <CartesianGrid stroke={COLOR_GRILLA} vertical={false} />
                  <XAxis dataKey="instante" tickFormatter={formatearHora} tick={ESTILO_EJE} tickLine={false} axisLine={false} minTickGap={24} />
                  <YAxis yAxisId="mb" tick={ESTILO_EJE} tickLine={false} axisLine={false} />
                  <YAxis
                    yAxisId="cpu"
                    orientation="right"
                    tickFormatter={(valor: number) => formatearPorcentaje(valor)}
                    tick={ESTILO_EJE}
                    tickLine={false}
                    axisLine={false}
                  />
                  <Tooltip
                    contentStyle={ESTILO_TOOLTIP}
                    labelFormatter={(instante) => formatearHora(Number(instante))}
                    formatter={(valor, nombre) =>
                      nombre === "Heap (MB)" ? `${formatearNumero(Number(valor))} MB` : formatearPorcentaje(Number(valor))
                    }
                  />
                  <Legend wrapperStyle={{ fontSize: 12 }} />
                  <Line yAxisId="mb" type="monotone" dataKey="heapUsadoMb" name="Heap (MB)" stroke={COLOR_LAVANDA} strokeWidth={2} dot={false} />
                  <Line yAxisId="cpu" type="monotone" dataKey="cpuProceso" name="CPU proceso" stroke={COLOR_MINT} strokeWidth={2} dot={false} />
                  <Line yAxisId="cpu" type="monotone" dataKey="cpuSistema" name="CPU sistema" stroke={COLOR_AMBAR} strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </figure>

        <figure className={styles.marco}>
          <figcaption className={styles.titulo}>Conexiones del pool de base de datos</figcaption>
          {recolectando ? (
            <p className={styles.vacio} role="status">Recolectando datos…</p>
          ) : (
            <div className={styles.lienzo} role="img" aria-label="Evolución de conexiones activas y pendientes del pool durante esta sesión">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={muestras} margin={{ top: 4, right: 8, left: -12, bottom: 0 }}>
                  <CartesianGrid stroke={COLOR_GRILLA} vertical={false} />
                  <XAxis dataKey="instante" tickFormatter={formatearHora} tick={ESTILO_EJE} tickLine={false} axisLine={false} minTickGap={24} />
                  <YAxis allowDecimals={false} tick={ESTILO_EJE} tickLine={false} axisLine={false} />
                  <Tooltip contentStyle={ESTILO_TOOLTIP} labelFormatter={(instante) => formatearHora(Number(instante))} />
                  <Legend wrapperStyle={{ fontSize: 12 }} />
                  <Line type="stepAfter" dataKey="conexionesActivas" name="Activas" stroke={COLOR_MINT} strokeWidth={2} dot={false} />
                  <Line type="stepAfter" dataKey="conexionesPendientes" name="Pendientes" stroke={COLOR_CORAL} strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </figure>
      </div>
      <p className={styles.nota}>Evolución durante esta sesión (se reinicia al recargar la página).</p>
    </div>
  );
}
