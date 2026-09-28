import React from "react"
import { Layout } from "../components/Layout"
import { RegisterForm } from "../features/auth"
import { registerPageStyles } from "./RegisterPage.styles"

export const RegisterPage: React.FC = () => {
  return (
    <Layout>
      <div className={registerPageStyles.pageContainer}>
        <RegisterForm />
      </div>
    </Layout>
  )
}

export default RegisterPage
