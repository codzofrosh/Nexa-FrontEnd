import React, { createContext, useContext, useEffect, useState } from 'react'
import { api, clearToken, getToken, saveToken, User } from '../api/client'

interface AuthState {
  user: User | null
  loading: boolean
  signIn: (email: string, password: string) => Promise<void>
  signUp: (name: string, email: string, password: string, phone?: string) => Promise<void>
  signOut: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Restore session on app start
    ;(async () => {
      try {
        const token = await getToken()
        if (token) {
          const { authenticated, user } = await api.me()
          if (authenticated && user) setUser(user)
          else await clearToken()
        }
      } catch {
        await clearToken()
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  async function signIn(email: string, password: string) {
    const { user, token } = await api.login(email, password)
    if (token) await saveToken(token)
    setUser(user)
  }

  async function signUp(name: string, email: string, password: string, phone?: string) {
    const { user, token } = await api.register(name, email, password, phone)
    if (token) await saveToken(token)
    setUser(user)
  }

  async function signOut() {
    await api.logout().catch(() => {})
    await clearToken()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, signIn, signUp, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
