import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../features/auth";
import { headerStyles } from "./Header.styles";

interface HeaderProps {
  title?: string;
}

export const Header: React.FC<HeaderProps> = ({
  title = "Los Recursionistas",
}) => {
  const { isAuthenticated, nombreUsuario, logout } = useAuth();

  return (
    <header className={headerStyles.header}>
      <div className={headerStyles.container}>
        <Link to="/" className={headerStyles.brandContainer}>
          <div className={headerStyles.logo}>LR</div>
          <span className={headerStyles.title}>{title}</span>
        </Link>

        <nav className={headerStyles.nav}>
          <Link to="/" className={headerStyles.navLink}>
            Inicio
          </Link>
          <Link to="/equipos" className={headerStyles.navLink}>
            Equipos
          </Link>
          <Link to="/jugadores" className={headerStyles.navLink}>
            Jugadores
          </Link>
        </nav>

        <div className={headerStyles.actionsContainer}>
          {isAuthenticated ? (
            <>
              <span className={headerStyles.userName}>{nombreUsuario}</span>
              <button
                type="button"
                onClick={logout}
                className={headerStyles.btnLogout}
              >
                Cerrar sesión
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={headerStyles.btnLogin}>
                Ingresar
              </Link>
              <Link to="/register" className={headerStyles.btnRegister}>
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
