import React from "react"
import { Layout } from "../components/Layout"
import { LoginForm } from "../features/auth"
import { loginPageStyles } from "./LoginPage.styles"

export const LoginPage: React.FC = () => {
  return (
    <Layout>
      <div className={loginPageStyles.pageContainer}>
        <LoginForm />
      </div>
    </Layout>
  )
}

export default LoginPage
