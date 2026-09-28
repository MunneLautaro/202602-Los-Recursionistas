import React from "react"
import { useRegister } from "../hooks/useRegister"
import { registerFormStyles } from "./RegisterForm.styles"

interface RegisterFormProps {
  onSuccess?: () => void
}

export const RegisterForm: React.FC<RegisterFormProps> = () => {
  const { formData, errors, isLoading, isSuccess, handleChange, handleSubmit } =
    useRegister()

  console.log(errors)
  return (
    <div className={registerFormStyles.container}>
      <div className={registerFormStyles.header}>
        <h2 className={registerFormStyles.title}>Crear Cuenta</h2>
        <p className={registerFormStyles.subtitle}>
          Regístrate para comenzar a gestionar tu equipo y jugadores
        </p>
      </div>

      {errors.general && (
        <div className={registerFormStyles.alertError}>{errors.general}</div>
      )}

      {isSuccess && (
        <div className={registerFormStyles.alertSuccess}>
          ¡Cuenta creada con éxito! Redirigiendo a inicio de sesión...
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        className={registerFormStyles.form}
        noValidate
      >
        <div className={registerFormStyles.fieldGroup}>
          <label htmlFor="nombreUsuario" className={registerFormStyles.label}>
            Nombre de Usuario
          </label>
          <input
            id="nombreUsuario"
            name="nombreUsuario"
            type="text"
            placeholder="Ej: recursionista10"
            value={formData.nombreUsuario}
            onChange={handleChange}
            disabled={isLoading || isSuccess}
            className={`${registerFormStyles.input} ${
              errors.nombreUsuario ? registerFormStyles.inputError : ""
            }`}
          />
          {errors.nombreUsuario && (
            <span className={registerFormStyles.errorText}>
              {errors.nombreUsuario}
            </span>
          )}
        </div>

        <div className={registerFormStyles.fieldGroup}>
          <label htmlFor="contrasena" className={registerFormStyles.label}>
            Contraseña
          </label>
          <input
            id="contrasena"
            name="contrasena"
            type="password"
            placeholder="••••••••"
            value={formData.contrasena}
            onChange={handleChange}
            disabled={isLoading || isSuccess}
            className={`${registerFormStyles.input} ${
              errors.contrasena ? registerFormStyles.inputError : ""
            }`}
          />
          {errors.contrasena && (
            <span className={registerFormStyles.errorText}>
              {errors.contrasena}
            </span>
          )}
        </div>

        <div className={registerFormStyles.fieldGroup}>
          <label
            htmlFor="confirmarContrasena"
            className={registerFormStyles.label}
          >
            Confirmar Contraseña
          </label>
          <input
            id="confirmarContrasena"
            name="confirmarContrasena"
            type="password"
            placeholder="••••••••"
            value={formData.confirmarContrasena}
            onChange={handleChange}
            disabled={isLoading || isSuccess}
            className={`${registerFormStyles.input} ${
              errors.confirmarContrasena ? registerFormStyles.inputError : ""
            }`}
          />
          {errors.confirmarContrasena && (
            <span className={registerFormStyles.errorText}>
              {errors.confirmarContrasena}
            </span>
          )}
        </div>

        <button
          type="submit"
          disabled={isLoading || isSuccess}
          className={registerFormStyles.submitBtn}
        >
          {isLoading ? "Registrando..." : "Registrarse"}
        </button>
      </form>

      <div className={registerFormStyles.footer}>
        <span>¿Ya tienes una cuenta?</span>
        <a href="/login" className={registerFormStyles.footerLink}>
          Iniciar Sesión
        </a>
      </div>
    </div>
  )
}
