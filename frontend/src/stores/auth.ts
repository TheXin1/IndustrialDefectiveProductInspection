import { defineStore } from 'pinia'
import { api } from '../api'
import type { LoginResponse, Permission, Role, User } from '../api/types'

interface AuthState {
  user: User | null
  roles: Role[]
  permissions: Permission[]
}

const STORAGE_KEY = 'industrial_auth'

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    roles: [],
    permissions: []
  }),
  getters: {
    isLoggedIn: (state) => !!state.user,
    permissionCodes: (state) => state.permissions.map((p) => p.code)
  },
  actions: {
    hydrate() {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (!raw) return
      try {
        const parsed = JSON.parse(raw) as LoginResponse
        this.user = parsed.user
        this.roles = parsed.roles
        this.permissions = parsed.permissions
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    },
    async login(username: string, password: string) {
      const res = await api.login({ username, password })
      if (!res.success) {
        throw new Error(res.message || 'login_failed')
      }
      this.user = res.data.user
      this.roles = res.data.roles
      this.permissions = res.data.permissions
      localStorage.setItem(STORAGE_KEY, JSON.stringify(res.data))
    },
    logout() {
      this.user = null
      this.roles = []
      this.permissions = []
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
