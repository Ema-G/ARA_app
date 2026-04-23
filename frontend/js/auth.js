// ─── Auth Utilities ───────────────────────────────────────────
const API_BASE = '/api';

const auth = {
  TOKEN_KEY: 'ara_token',
  USER_KEY:  'ara_user',

  getToken() { return localStorage.getItem(this.TOKEN_KEY); },
  setToken(t) { localStorage.setItem(this.TOKEN_KEY, t); },
  clearToken() { localStorage.removeItem(this.TOKEN_KEY); localStorage.removeItem(this.USER_KEY); },

  getUser() {
    try { return JSON.parse(localStorage.getItem(this.USER_KEY) || 'null'); } catch { return null; }
  },
  setUser(u) { localStorage.setItem(this.USER_KEY, JSON.stringify(u)); },

  isAuthenticated() { return !!this.getToken(); },

  /** Redirect to login if no token is present. Call at top of protected pages. */
  guard() {
    if (!this.isAuthenticated()) {
      window.location.replace('/pages/login.html');
      return false;
    }
    return true;
  },

  /** Redirect away from auth pages if already logged in. */
  redirectIfAuthenticated(dest = '/app.html') {
    if (this.isAuthenticated()) {
      window.location.replace(dest);
      return true;
    }
    return false;
  },

  logout() {
    this.clearToken();
    window.location.replace('/pages/login.html');
  },

  /** fetch wrapper that injects Authorization header and handles 401 globally */
  async fetchWithAuth(url, options = {}) {
    const res = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`,
        ...(options.headers || {})
      }
    });
    if (res.status === 401 || res.status === 403) {
      this.logout();
      return res;
    }
    return res;
  },

  // ── API calls ──────────────────────────────────────────────────

  async register(fullName, email, password) {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, password })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Registration failed.');
    this.setToken(data.token);
    this.setUser({ email: data.email, fullName: data.fullName });
    return data;
  },

  async login(email, password) {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Login failed.');
    this.setToken(data.token);
    this.setUser({ email: data.email, fullName: data.fullName });
    return data;
  },

  async forgotPassword(email) {
    const res = await fetch(`${API_BASE}/auth/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    if (!res.ok) throw new Error('Something went wrong. Please try again.');
    return res.json();
  },

  async resetPassword(token, newPassword) {
    const res = await fetch(`${API_BASE}/auth/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, newPassword })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Reset failed.');
    return data;
  }
};
