import React from "react"
import { Header } from "../Header"
import { Footer } from "../Footer"
import { layoutStyles } from "./Layout.styles"

interface LayoutProps {
  children?: React.ReactNode
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <div className={layoutStyles.container}>
      <Header />
      <main className={layoutStyles.main}>{children}</main>
      <Footer />
    </div>
  )
}
