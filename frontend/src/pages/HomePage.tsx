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
        Descubrí jugadores, seguí el ranking y participá del mercado de tokens
        de fantasy football.
      </p>
      <div className={appStyles.buttonContainer}>
        {!isAuthenticated && (
          <Link to="/register" className={appStyles.btnPrimary}>
            Registrarse
          </Link>
        )}
        <Link to="/equipos" className={appStyles.btnSecondary}>Ver Equipos</Link>
        <Link to="/jugadores" className={appStyles.btnSecondary}>Explorar jugadores</Link>
        <Link to="/ranking" className={appStyles.btnSecondary}>Ver ranking</Link>
        <Link to="/mercado" className={appStyles.btnSecondary}>Ir al mercado</Link>
      </div>
    </div>
  )
}

export default HomePage
