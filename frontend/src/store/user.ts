import { defineStore } from 'pinia'
import type { Role } from '@/types'

interface UserState {
  token: string
  userId: number | null
  username: string
  realName: string
  role: Role | null
}

const STORAGE_KEY = 'researchease-user'

function loadState(): UserState {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return { token: '', userId: null, username: '', realName: '', role: null }
  }
  return JSON.parse(raw) as UserState
}

export const useUserStore = defineStore('user', {
  state: (): UserState => loadState(),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    setLoginInfo(payload: { token: string; userId: number; username: string; realName: string; role: Role }) {
      this.token = payload.token
      this.userId = payload.userId
      this.username = payload.username
      this.realName = payload.realName
      this.role = payload.role
      this.persist()
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.role = null
      localStorage.removeItem(STORAGE_KEY)
    },
    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
    },
  },
})
