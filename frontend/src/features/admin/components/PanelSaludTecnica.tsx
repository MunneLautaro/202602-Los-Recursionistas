import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { EndpointRequests, ResumenTecnico } from "../types";
import { panelSaludTecnicaStyles as styles } from "./PanelSaludTecnica.styles";
import { TarjetaMetrica } from "./TarjetaMetrica";
import {
  COLOR_CURSOR,
  COLOR_GRILLA,
  COLOR_LAVANDA,
  ESTILO_EJE,
  ESTILO_TOOLTIP,
  formatearNumero,
  formatearPorcentaje,
} from "./temaGraficos";

export interface PanelSaludTecnicaProps {
  tecnico: ResumenTecnico;
}

interface FilaEndpoint extends EndpointRequests {
  etiqueta: string;
}

function tonoTasa(tasa: number | null): "neutro" | "alerta" {
  return tasa !== null && tasa > 0.05 ? "alerta" : "neutro";
}

export function PanelSaludTecnica({ tecnico }: PanelSaludTecnicaProps) {
  const { salud, requests } = tecnico;
  const filas: FilaEndpoint[] = requests.endpoints.map((endpoint) => ({
    ...endpoint,
    etiqueta: `${endpoint.metodo} ${endpoint.uri}`,
  }));

  return (
    <div className={styles.raiz}>
      <div className={styles.saludFila}>
        <span className={salud.estado === "UP" ? styles.badgeUp : styles.badgeOtro} role="status">
          Estado global: {salud.estado}
        </span>
        <ul className={styles.componentes} aria-label="Estado por componente">
          {salud.componentes.map((componente) => (
            <li
              key={componente.nombre}
              className={componente.estado === "UP" ? styles.componenteUp : styles.componenteOtro}
            >
              {componente.nombre}: {componente.estado}
            </li>
          ))}
        </ul>
      </div>

      <div className={styles.tarjetas}>
        <TarjetaMetrica etiqueta="Requests" valor={formatearNumero(requests.total)} nota="desde el último reinicio" />
        <TarjetaMetrica
          etiqueta="Latencia media"
          valor={requests.latenciaMediaMs === null ? "Sin datos" : `${formatearNumero(requests.latenciaMediaMs)} ms`}
        />
        <TarjetaMetrica
          etiqueta="Errores cliente (4xx)"
          valor={formatearPorcentaje(requests.tasaErrorCliente)}
          tono={tonoTasa(requests.tasaErrorCliente)}
        />
        <TarjetaMetrica
          etiqueta="Errores servidor (5xx)"
          valor={formatearPorcentaje(requests.tasaErrorServidor)}
          tono={requests.tasaErrorServidor ? "alerta" : "neutro"}
        />
      </div>

      <figure className={styles.marco}>
        <figcaption className={styles.titulo}>Endpoints con más requests</figcaption>
        {filas.length === 0 ? (
          <p className={styles.vacio}>Todavía no hay requests registrados</p>
        ) : (
          <div className={styles.lienzo} role="img" aria-label="Gráfico de barras horizontales con los endpoints que más requests recibieron">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={filas} layout="vertical" margin={{ top: 0, right: 16, left: 8, bottom: 0 }}>
                <CartesianGrid stroke={COLOR_GRILLA} horizontal={false} />
                <XAxis type="number" allowDecimals={false} tick={ESTILO_EJE} tickLine={false} axisLine={false} />
                <YAxis type="category" dataKey="etiqueta" width={190} tick={ESTILO_EJE} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={ESTILO_TOOLTIP}
                  cursor={{ fill: COLOR_CURSOR }}
                  formatter={(valor, _nombre, item) => {
                    const fila = item.payload as FilaEndpoint;
                    const latencia = fila.latenciaMediaMs === null ? "sin datos" : `${formatearNumero(fila.latenciaMediaMs)} ms`;
                    return [`${String(valor)} (4xx: ${fila.erroresCliente}, 5xx: ${fila.erroresServidor}, latencia ${latencia})`, "Requests"];
                  }}
                />
                <Bar dataKey="cantidad" name="Requests" fill={COLOR_LAVANDA} radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </figure>
    </div>
  );
}
