import React from "react"
import { useRegister } from "../hooks/useRegister"
import { registerFormStyles } from "./RegisterForm.styles"

interface RegisterFormProps {
  onSuccess?: () => void
}

export const RegisterForm: React.FC<RegisterFormProps> = () => {
  const { register, errors, generalError, isLoading, isSuccess, handleSubmit } =
    useRegister()

  return (
    <div className={registerFormStyles.container}>
      <div className={registerFormStyles.header}>
        <h2 className={registerFormStyles.title}>Crear Cuenta</h2>
        <p className={registerFormStyles.subtitle}>
          Regístrate para comenzar a gestionar tu equipo y jugadores
        </p>
      </div>

      {generalError && (
        <div className={registerFormStyles.alertError}>{generalError}</div>
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
            {...register("nombreUsuario")}
            type="text"
            placeholder="Ej: recursionista10"
            disabled={isLoading || isSuccess}
            aria-invalid={Boolean(errors.nombreUsuario)}
            aria-describedby={
              errors.nombreUsuario ? "register-usuario-error" : undefined
            }
            className={`${registerFormStyles.input} ${
              errors.nombreUsuario ? registerFormStyles.inputError : ""
            }`}
          />
          {errors.nombreUsuario && (
            <span className={registerFormStyles.errorText}>
              <span id="register-usuario-error">
                {errors.nombreUsuario.message}
              </span>
            </span>
          )}
        </div>

        <div className={registerFormStyles.fieldGroup}>
          <label htmlFor="contrasena" className={registerFormStyles.label}>
            Contraseña
          </label>
          <input
            id="contrasena"
            {...register("contrasena")}
            type="password"
            placeholder="••••••••"
            disabled={isLoading || isSuccess}
            aria-invalid={Boolean(errors.contrasena)}
            aria-describedby={
              errors.contrasena ? "register-contrasena-error" : undefined
            }
            className={`${registerFormStyles.input} ${
              errors.contrasena ? registerFormStyles.inputError : ""
            }`}
          />
          {errors.contrasena && (
            <span className={registerFormStyles.errorText}>
              <span id="register-contrasena-error">
                {errors.contrasena.message}
              </span>
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
            {...register("confirmarContrasena")}
            type="password"
            placeholder="••••••••"
            disabled={isLoading || isSuccess}
            aria-invalid={Boolean(errors.confirmarContrasena)}
            aria-describedby={
              errors.confirmarContrasena
                ? "register-confirmar-error"
                : undefined
            }
            className={`${registerFormStyles.input} ${
              errors.confirmarContrasena ? registerFormStyles.inputError : ""
            }`}
          />
          {errors.confirmarContrasena && (
            <span className={registerFormStyles.errorText}>
              <span id="register-confirmar-error">
                {errors.confirmarContrasena.message}
              </span>
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
