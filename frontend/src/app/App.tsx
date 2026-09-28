import { Layout } from "../components/Layout"
import { appStyles } from "./App.styles"

function App() {
  return (
    <Layout>
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
          <button className={appStyles.btnPrimary}>Comenzar</button>
          <button className={appStyles.btnSecondary}>Ver Equipos</button>
        </div>
      </div>
    </Layout>
  )
}

export default App
