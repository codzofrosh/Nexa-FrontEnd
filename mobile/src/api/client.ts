import AsyncStorage from '@react-native-async-storage/async-storage'
import { BASE_URL } from './config'

const TOKEN_KEY = 'nexa_token'

export async function getToken(): Promise<string | null> {
  return AsyncStorage.getItem(TOKEN_KEY)
}

export async function saveToken(token: string): Promise<void> {
  await AsyncStorage.setItem(TOKEN_KEY, token)
}

export async function clearToken(): Promise<void> {
  await AsyncStorage.removeItem(TOKEN_KEY)
}

async function req<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = await getToken()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers })

  if (!res.ok) {
    const err = await res.json().catch(() => ({ detail: res.statusText }))
    throw Object.assign(new Error(err.detail ?? 'Request failed'), {
      status: res.status,
    })
  }
  return res.json()
}

// ── Types ────────────────────────────────────────────────────────────────────

export interface User {
  id: number
  name: string
  email: string
}

export interface AuthResponse {
  success: boolean
  user: User
  token?: string
}

// ── API ──────────────────────────────────────────────────────────────────────

export const api = {
  register: (name: string, email: string, password: string, phone?: string) =>
    req<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, ...(phone && { phone }) }),
    }),

  login: (email: string, password: string) =>
    req<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  me: () =>
    req<{ authenticated: boolean; user?: User }>('/api/auth/me'),

  logout: () =>
    req<{ success: boolean }>('/api/auth/logout', { method: 'POST' }),
}
