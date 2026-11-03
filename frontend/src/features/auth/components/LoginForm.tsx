import React from "react"
import { useLogin } from "../hooks/useLogin"
import { loginFormStyles } from "./LoginForm.styles"

export const LoginForm: React.FC = () => {
  const { register, errors, generalError, isLoading, handleSubmit } = useLogin()

  return (
    <div className={loginFormStyles.container}>
      <div className={loginFormStyles.header}>
        <h2 className={loginFormStyles.title}>Iniciar Sesión</h2>
        <p className={loginFormStyles.subtitle}>
          Ingresá tus credenciales para acceder a tu cuenta
        </p>
      </div>

      {generalError && (
        <div className={loginFormStyles.alertError}>{generalError}</div>
      )}

      <form onSubmit={handleSubmit} className={loginFormStyles.form} noValidate>
        <div className={loginFormStyles.fieldGroup}>
          <label htmlFor="nombreUsuario" className={loginFormStyles.label}>
            Nombre de Usuario
          </label>
          <input
            id="nombreUsuario"
            {...register("nombreUsuario")}
            type="text"
            placeholder="Ej: recursionista10"
            disabled={isLoading}
            aria-invalid={Boolean(errors.nombreUsuario)}
            aria-describedby={errors.nombreUsuario ? "login-usuario-error" : undefined}
            className={`${loginFormStyles.input} ${
              errors.nombreUsuario ? loginFormStyles.inputError : ""
            }`}
          />
          {errors.nombreUsuario && (
            <span className={loginFormStyles.errorText}>
              <span id="login-usuario-error">{errors.nombreUsuario.message}</span>
            </span>
          )}
        </div>

        <div className={loginFormStyles.fieldGroup}>
          <label htmlFor="contrasena" className={loginFormStyles.label}>
            Contraseña
          </label>
          <input
            id="contrasena"
            {...register("contrasena")}
            type="password"
            placeholder="••••••••"
            disabled={isLoading}
            aria-invalid={Boolean(errors.contrasena)}
            aria-describedby={errors.contrasena ? "login-contrasena-error" : undefined}
            className={`${loginFormStyles.input} ${
              errors.contrasena ? loginFormStyles.inputError : ""
            }`}
          />
          {errors.contrasena && (
            <span className={loginFormStyles.errorText}>
              <span id="login-contrasena-error">{errors.contrasena.message}</span>
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
