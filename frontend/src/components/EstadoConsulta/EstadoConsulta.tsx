import type { EstadoConsultaProps } from "../../types"
import { estadoConsultaStyles } from "./EstadoConsulta.styles"

export function EstadoConsulta({
  estado,
  mensaje,
  titulo,
  accion,
  children,
}: EstadoConsultaProps) {
  if (estado === "exito") {
    return <>{children}</>
  }

  const defaultTitle = {
    cargando: "Cargando",
    vacio: "Sin resultados",
    error: "No se pudo completar la consulta",
  }[estado]

  return (
    <div
      className={estadoConsultaStyles.container}
      role={estado === "error" ? "alert" : "status"}
      aria-live="polite"
    >
      {estado === "cargando" && (
        <div className={estadoConsultaStyles.skeleton} aria-hidden="true" />
      )}
      <strong className={estadoConsultaStyles.title}>{titulo ?? defaultTitle}</strong>
      {mensaje && (
        <p className={estado === "error" ? estadoConsultaStyles.error : estadoConsultaStyles.message}>
          {mensaje}
        </p>
      )}
      {accion && (
        <button type="button" className={estadoConsultaStyles.action} onClick={accion.ejecutar}>
          {accion.etiqueta}
        </button>
      )}
    </div>
  )
}
