import { useState, useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import type { SessionStatus } from "../types";

export function useAuth() {
  const navigate = useNavigate();
  const [token, setToken] = useState(() => localStorage.getItem("token") || "");
  const [nombreUsuario, setNombreUsuario] = useState(
    () => localStorage.getItem("nombreUsuario") || "",
  );
  const [status, setStatus] = useState<SessionStatus>(() =>
    token ? "authenticated" : "unauthenticated",
  );

  const invalidate = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("usuario");
    setToken("");
    setStatus("invalidated");
    setNombreUsuario("");
  }, []);

  useEffect(() => {
    const handleInvalidation = () => {
      invalidate();
      navigate("/login", { replace: true });
    };
    window.addEventListener("auth:invalidated", handleInvalidation);
    return () => window.removeEventListener("auth:invalidated", handleInvalidation);
  }, [invalidate, navigate]);

  const logout = useCallback(() => {
    invalidate();
    navigate("/");
  }, [invalidate, navigate]);

  return {
    token,
    isAuthenticated: status === "authenticated",
    status,
    nombreUsuario,
    logout,
    invalidate,
  };
}
