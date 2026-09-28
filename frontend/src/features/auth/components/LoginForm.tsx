import React from "react"
import { useLogin } from "../hooks/useLogin"
import { loginFormStyles } from "./LoginForm.styles"

export const LoginForm: React.FC = () => {
  const { formData, errors, isLoading, handleChange, handleSubmit } = useLogin()

  return (
    <div className={loginFormStyles.container}>
      <div className={loginFormStyles.header}>
        <h2 className={loginFormStyles.title}>Iniciar Sesión</h2>
        <p className={loginFormStyles.subtitle}>
          Ingresá tus credenciales para acceder a tu cuenta
        </p>
      </div>

      {errors.general && (
        <div className={loginFormStyles.alertError}>{errors.general}</div>
      )}

      <form onSubmit={handleSubmit} className={loginFormStyles.form} noValidate>
        <div className={loginFormStyles.fieldGroup}>
          <label htmlFor="nombreUsuario" className={loginFormStyles.label}>
            Nombre de Usuario
          </label>
          <input
            id="nombreUsuario"
            name="nombreUsuario"
            type="text"
            placeholder="Ej: recursionista10"
            value={formData.nombreUsuario}
            onChange={handleChange}
            disabled={isLoading}
            className={`${loginFormStyles.input} ${
              errors.nombreUsuario ? loginFormStyles.inputError : ""
            }`}
          />
          {errors.nombreUsuario && (
            <span className={loginFormStyles.errorText}>
              {errors.nombreUsuario}
            </span>
          )}
        </div>

        <div className={loginFormStyles.fieldGroup}>
          <label htmlFor="contrasena" className={loginFormStyles.label}>
            Contraseña
          </label>
          <input
            id="contrasena"
            name="contrasena"
            type="password"
            placeholder="••••••••"
            value={formData.contrasena}
            onChange={handleChange}
            disabled={isLoading}
            className={`${loginFormStyles.input} ${
              errors.contrasena ? loginFormStyles.inputError : ""
            }`}
          />
          {errors.contrasena && (
            <span className={loginFormStyles.errorText}>
              {errors.contrasena}
            </span>
          )}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className={loginFormStyles.submitBtn}
        >
          {isLoading ? "Iniciando sesión..." : "Iniciar Sesión"}
        </button>
      </form>

      <div className={loginFormStyles.footer}>
        <span>¿No tenés una cuenta?</span>
        <a href="/register" className={loginFormStyles.footerLink}>
          Registrarse
        </a>
      </div>
    </div>
  )
}
