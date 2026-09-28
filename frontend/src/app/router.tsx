// src/app/router.tsx
import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import HomePage from "../pages/HomePage";
import RegisterPage from "../pages/RegisterPage";
import LoginPage from "../pages/LoginPage";
import { JugadoresPage } from "../pages/JugadoresPage";

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/jugadores" element={<JugadoresPage />} />
      {/* Fallback to home page */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
