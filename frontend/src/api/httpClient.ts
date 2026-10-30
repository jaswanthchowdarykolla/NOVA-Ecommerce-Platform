const BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

/** Called on any 401 — wipes stale credentials and redirects to login. */
function handleUnauthorized() {
  localStorage.removeItem("nova-token");
  localStorage.removeItem("nova-user");
  // Avoid redirect loop if already on an auth page
  if (!window.location.pathname.startsWith("/login") &&
      !window.location.pathname.startsWith("/register")) {
    const next = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.href = `/login?next=${next}&expired=1`;
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem("nova-token");
  const headers = new Headers(init.headers);

  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${BASE}${path}`, { ...init, headers });

  if (!response.ok) {
    // Auto-logout on expired / invalid JWT
    if (response.status === 401) {
      handleUnauthorized();
    }

    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || body.detail || body.error || message;
    } catch {
      // Ignore parse failures and keep the HTTP status fallback.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
