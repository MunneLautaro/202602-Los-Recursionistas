import React from "react"
import { Link } from "react-router-dom"
import { appStyles } from "../app/App.styles"
import { useAuth } from "../features/auth"

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuth()

  return (
    <div className={appStyles.heroContainer}>
      <h1 className={appStyles.title}>
        ¡Bienvenido a{" "}
        <span className={appStyles.brandHighlight}>Los Recursionistas</span>!
      </h1>
      <p className={appStyles.description}>
        Plataforma de gestión de fútbol y sincronización de datos en tiempo
        real.
      </p>
      <div className={appStyles.buttonContainer}>
        {!isAuthenticated && (
          <Link to="/register" className={appStyles.btnPrimary}>
            Registrarse
          </Link>
        )}
        <button className={appStyles.btnSecondary}>Ver Equipos</button>
      </div>
    </div>
  )
}

export default HomePage
