import type { ReactNode } from "react"

export type EstadoConsultaTipo = "cargando" | "exito" | "vacio" | "error"

export interface EstadoConsultaProps {
  estado: EstadoConsultaTipo
  mensaje?: string
  titulo?: string
  accion?: {
    etiqueta: string
    ejecutar: () => void
  }
  children?: ReactNode
}
