import React from "react"
import { Routes, Route, Navigate } from "react-router-dom"
import HomePage from "../pages/HomePage"
import RegisterPage from "../pages/RegisterPage"
import LoginPage from "../pages/LoginPage"
import { JugadoresPage } from "../pages/JugadoresPage"
import { Layout } from "../components/Layout/Layout"
import { ProtectedRoute } from "../components/ProtectedRoute"
import { RankingPage } from "../pages/RankingPage"
import { MercadoPage } from "../pages/MercadoPage"
import { CuentaPage } from "../pages/CuentaPage"
import { EquiposPage } from "../pages/EquiposPage"
import { CuentaOrdenesPage, CuentaTokensPage, CuentaTransaccionesPage } from "../pages/AccountDataPages"

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/jugadores" element={<JugadoresPage />} />
        <Route path="/equipos" element={<EquiposPage />} />
        <Route path="/ranking" element={<RankingPage />} />
        <Route path="/mercado" element={<MercadoPage />} />
        <Route path="/mercado/operar" element={<ProtectedRoute><MercadoPage /></ProtectedRoute>} />
        <Route path="/cuenta" element={<ProtectedRoute><CuentaPage /></ProtectedRoute>} />
        <Route path="/cuenta/ordenes" element={<ProtectedRoute><CuentaOrdenesPage /></ProtectedRoute>} />
        <Route path="/cuenta/tokens" element={<ProtectedRoute><CuentaTokensPage /></ProtectedRoute>} />
        <Route path="/cuenta/transacciones" element={<ProtectedRoute><CuentaTransaccionesPage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
