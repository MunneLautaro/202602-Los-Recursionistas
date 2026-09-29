import React from "react"
import { LoginForm } from "../features/auth"
import { loginPageStyles } from "./LoginPage.styles"

export const LoginPage: React.FC = () => {
  return (
    <div className={loginPageStyles.pageContainer}>
      <LoginForm />
    </div>
  )
}

export default LoginPage
