import { defineStore } from 'pinia'

const STORAGE_KEY = 'researchease-elder-mode'

export const useAccessibilityStore = defineStore('accessibility', {
  state: () => ({
    elderMode: document.documentElement.classList.contains('elder-mode'),
  }),
  actions: {
    toggle() {
      this.elderMode = !this.elderMode
      document.documentElement.classList.toggle('elder-mode', this.elderMode)
      localStorage.setItem(STORAGE_KEY, this.elderMode ? '1' : '0')
    },
  },
})
