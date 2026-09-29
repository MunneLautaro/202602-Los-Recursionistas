import React from "react"
import { Header } from "../Header"
import { Footer } from "../Footer"
import { layoutStyles } from "./Layout.styles"
import { Outlet } from "react-router-dom"

export const Layout: React.FC = () => {
  return (
    <div className={layoutStyles.container}>
      <Header />
      <main className={layoutStyles.main}>
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
