/** API base URL including /api (e.g. http://localhost:8080/api or https://api.example.com/api). */
const raw = import.meta.env.VITE_API_URL;
export const API_BASE = (raw && String(raw).replace(/\/$/, "")) || "http://localhost:8080/api";
