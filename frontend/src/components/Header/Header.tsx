import React, { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { LogIn, LogOut, Menu, X } from "lucide-react";
import { useAuth } from "../../features/auth";
import { headerStyles } from "./Header.styles";

interface HeaderProps {
  title?: string;
}

export const Header: React.FC<HeaderProps> = ({
  title = "Los Recursionistas",
}) => {
  const { isAuthenticated, isAdmin, nombreUsuario, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const links = [
    { to: "/", label: "Inicio" },
    { to: "/equipos", label: "Equipos" },
    { to: "/jugadores", label: "Jugadores" },
    { to: "/ranking", label: "Ranking" },
    { to: "/mercado", label: "Mercado" },
  ];
  const closeMenu = () => setMenuOpen(false);

  return (
    <header className={headerStyles.header}>
      <div className={headerStyles.container}>
        <Link to="/" className={headerStyles.brandContainer}>
          <img src="/logo2.png" alt="Logo" className={headerStyles.logo} />
          <span className={headerStyles.title}>{title}</span>
        </Link>

        <nav className={`${headerStyles.nav} ${menuOpen ? headerStyles.mobileNav : ""}`} aria-label="Navegación principal">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} onClick={closeMenu} className={({ isActive }) => `${headerStyles.navLink} ${isActive ? headerStyles.activeLink : ""}`}>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button type="button" className={headerStyles.menuButton} aria-label={menuOpen ? "Cerrar menú" : "Abrir menú"} aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>
          {menuOpen ? <X aria-hidden="true" /> : <Menu aria-hidden="true" />}
        </button>

        <div className={headerStyles.actionsContainer}>
          {isAuthenticated ? (
            <>
              <span className={headerStyles.userName}>{nombreUsuario}</span>
              <Link to="/cuenta" onClick={closeMenu} className={headerStyles.navLink}>
                Cuenta
              </Link>
              {isAdmin && (
                <Link to="/admin" className={headerStyles.adminLink}>
                  Administración
                </Link>
              )}
              <button
                type="button"
                onClick={logout}
                className={headerStyles.btnLogout}
              >
                <LogOut aria-hidden="true" size={16} /> Cerrar sesión
              </button>
            </>
          ) : (
            <>
              <Link to="/login" onClick={closeMenu} className={headerStyles.btnLogin}>
                <LogIn aria-hidden="true" size={16} />
                Ingresar
              </Link>
              <Link to="/register" onClick={closeMenu} className={headerStyles.btnRegister}>
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
