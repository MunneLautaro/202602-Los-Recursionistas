import type { CampoFormularioProps } from "../../types"
import { campoFormularioStyles } from "./CampoFormulario.styles"

export function CampoFormulario({
  nombre,
  etiqueta,
  error,
  ayuda,
  sufijo,
  id,
  className = "",
  ...props
}: CampoFormularioProps) {
  const inputId = id ?? nombre
  const helpId = ayuda ? `${inputId}-ayuda` : undefined
  const errorId = error ? `${inputId}-error` : undefined
  const describedBy = [helpId, errorId].filter(Boolean).join(" ") || undefined

  return (
    <div className={campoFormularioStyles.container}>
      <label htmlFor={inputId} className={campoFormularioStyles.label}>
        {etiqueta}
      </label>
      <div className={campoFormularioStyles.suffix}>
        <input
          {...props}
          id={inputId}
          name={nombre}
          aria-describedby={describedBy}
          aria-invalid={error ? true : undefined}
          className={`${campoFormularioStyles.input} ${error ? campoFormularioStyles.invalid : ""} ${className}`.trim()}
        />
        {sufijo && <span aria-hidden="true">{sufijo}</span>}
      </div>
      {ayuda && (
        <span id={helpId} className={campoFormularioStyles.help}>
          {ayuda}
        </span>
      )}
      {error && (
        <span id={errorId} role="alert" className={campoFormularioStyles.error}>
          {error}
        </span>
      )}
    </div>
  )
}
