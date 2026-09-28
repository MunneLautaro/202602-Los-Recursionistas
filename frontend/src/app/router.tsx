import React from "react"
import { Routes, Route, Navigate } from "react-router-dom"
import HomePage from "../pages/HomePage"
import RegisterPage from "../pages/RegisterPage"

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/register" element={<RegisterPage />} />
      {/* Fallback to home page */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
