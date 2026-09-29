import React from "react"
import { RegisterForm } from "../features/auth"
import { registerPageStyles } from "./RegisterPage.styles"

export const RegisterPage: React.FC = () => {
  return (
    <div className={registerPageStyles.pageContainer}>
      <RegisterForm />
    </div>
  )
}

export default RegisterPage
