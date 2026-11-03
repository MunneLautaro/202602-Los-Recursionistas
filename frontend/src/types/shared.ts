import type { ComponentPropsWithoutRef, ReactNode } from "react"
import type { z } from "zod"

export type EstadoConsultaTipo = "cargando" | "exito" | "vacio" | "error"

export interface AccionConsulta {
  etiqueta: string
  ejecutar: () => void
}

export interface FiltroActivo {
  clave: string
  etiqueta: string
  valor: string
  remover: () => void
}

export interface CampoError {
  mensaje: string
  campo?: string
}

export interface FormularioState {
  enviando: boolean
  valido: boolean
  errorGeneral?: string
}

export interface EstadoConsultaModel {
  estado: EstadoConsultaTipo
  mensaje?: string
  titulo?: string
  accion?: AccionConsulta
  children?: ReactNode
}

export interface CampoFormularioProps
  extends Omit<ComponentPropsWithoutRef<"input">, "name"> {
  nombre: string
  etiqueta: string
  error?: string
  ayuda?: string
  sufijo?: ReactNode
}

export type EstadoConsultaProps = EstadoConsultaModel

export interface UseDebounceOptions {
  delayMs?: number
}

export type UseDebounce = <T>(valor: T, opciones?: UseDebounceOptions) => T

export interface EsquemaFormulario<T extends z.ZodType> {
  esquema: T
  valoresIniciales: z.input<T>
}
