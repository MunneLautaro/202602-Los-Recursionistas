import React from "react"
import { Link } from "react-router-dom"
import { appStyles } from "../app/App.styles"
import { useAuth } from "../features/auth"

// Posiciones decorativas de los jugadores en la cancha (porcentajes).
const pitchPlayers = [
  { left: "28%", top: "40%", tone: "lavender", delay: "0s" },
  { left: "34%", top: "68%", tone: "lavender", delay: "1.2s" },
  { left: "60%", top: "62%", tone: "mint", delay: "0.6s" },
  { left: "66%", top: "30%", tone: "mint", delay: "1.8s" },
  { left: "78%", top: "50%", tone: "mint", delay: "0.9s" },
  { left: "87%", top: "53%", tone: "ball", delay: "0.3s" },
] as const

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuth()

  return (
    <div className={appStyles.heroContainer}>
      <div className={appStyles.heroGlow} aria-hidden="true" />
      <span className={appStyles.eyebrow}>
        <span className="ui-dot" aria-hidden="true" />
        Fantasy football · Mercado de tokens
      </span>
      <h1 className={appStyles.title}>
        ¡Bienvenido a{" "}
        <span className={appStyles.brandHighlight}>Los Recursionistas</span>!
      </h1>
      <p className={appStyles.description}>
        Descubrí jugadores, seguí el ranking y participá del mercado de tokens
        de fantasy football.
      </p>
      <div className={appStyles.pitchStage} aria-hidden="true">
        <div className={appStyles.pitch}>
          <span className="ui-pitch-box ui-pitch-box--left" />
          <span className="ui-pitch-box ui-pitch-box--right" />
          {pitchPlayers.map((player) => (
            <span
              key={`${player.left}-${player.top}`}
              className={`ui-pitch-player ui-pitch-player--${player.tone}`}
              style={{ left: player.left, top: player.top, animationDelay: player.delay }}
            />
          ))}
        </div>
      </div>
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
