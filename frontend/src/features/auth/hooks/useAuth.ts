import { useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";

export function useAuth() {
  const navigate = useNavigate();

  const [isAuthenticated, setIsAuthenticated] = useState(
    () => !!localStorage.getItem("token"),
  );
  const [nombreUsuario, setNombreUsuario] = useState(
    () => localStorage.getItem("nombreUsuario") || "",
  );

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("nombreUsuario");
    setIsAuthenticated(false);
    setNombreUsuario("");
    navigate("/");
    window.location.reload();
  }, [navigate]);

  return {
    isAuthenticated,
    nombreUsuario,
    logout,
  };
}
