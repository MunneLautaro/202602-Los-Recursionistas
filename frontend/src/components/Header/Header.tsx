import React from "react"
import { headerStyles } from "./Header.styles"

interface HeaderProps {
  title?: string
}

export const Header: React.FC<HeaderProps> = ({
  title = "Los Recursionistas",
}) => {
  return (
    <header className={headerStyles.header}>
      <div className={headerStyles.container}>
        {/* Brand / Logo */}
        <div className={headerStyles.brandContainer}>
          <div className={headerStyles.logo}>LR</div>
          <span className={headerStyles.title}>{title}</span>
        </div>

        {/* Navigation */}
        <nav className={headerStyles.nav}>
          <a href="/" className={headerStyles.navLink}>
            Inicio
          </a>
          <a href="/equipos" className={headerStyles.navLink}>
            Equipos
          </a>
          <a href="/jugadores" className={headerStyles.navLink}>
            Jugadores
          </a>
          <a href="/sincronizacion" className={headerStyles.navLink}>
            Sincronización
          </a>
        </nav>

        {/* Actions / CTA */}
        <div className={headerStyles.actionsContainer}>
          <button type="button" className={headerStyles.btnLogin}>
            Ingresar
          </button>
          <button type="button" className={headerStyles.btnRegister}>
            Registrarse
          </button>
        </div>
      </div>
    </header>
  )
}
