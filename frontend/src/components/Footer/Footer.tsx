import React from "react"
import { footerStyles } from "./Footer.styles"

export const Footer: React.FC = () => {
  return (
    <footer className={footerStyles.footer}>
      <div className={footerStyles.container}>
        <div className={footerStyles.brandSection}>
          <span className={footerStyles.brandLockup}>
            <img src="/logo.svg" alt="" className={footerStyles.brandLogo} />
            <span className={footerStyles.brandText}>
              <span className={footerStyles.brandTitle}>Los Recursionistas</span>
              <span className={footerStyles.brandTagline}>Mercado de fútbol</span>
            </span>
          </span>
          <span className={footerStyles.copyright}>
            © {new Date().getFullYear()} - Todos los derechos reservados.
          </span>
        </div>

        <div className={footerStyles.nav}>
          <a href="#" className={footerStyles.navLink}>
            Términos
          </a>
          <a href="#" className={footerStyles.navLink}>
            Privacidad
          </a>
          <a href="#" className={footerStyles.navLink}>
            Contacto
          </a>
        </div>
      </div>
    </footer>
  )
}
