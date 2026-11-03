import type { ComponentPropsWithoutRef, ReactNode } from "react"

export interface CampoFormularioProps
  extends Omit<ComponentPropsWithoutRef<"input">, "name"> {
  nombre: string
  etiqueta: string
  error?: string
  ayuda?: string
  sufijo?: ReactNode
}
