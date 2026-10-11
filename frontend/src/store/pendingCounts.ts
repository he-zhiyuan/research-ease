import { defineStore } from 'pinia'
import { getDashboardOverview } from '@/api/dashboard'

interface PendingCountsState {
  projectCount: number
  acceptanceCount: number
  expenditureCount: number
}

export const usePendingCountsStore = defineStore('pendingCounts', {
  state: (): PendingCountsState => ({ projectCount: 0, acceptanceCount: 0, expenditureCount: 0 }),
  actions: {
    async refresh() {
      const overview = await getDashboardOverview()
      this.projectCount = overview.pendingProjectCount
      this.acceptanceCount = overview.pendingAcceptanceCount
      this.expenditureCount = overview.pendingExpenditureCount
    },
    reset() {
      this.projectCount = 0
      this.acceptanceCount = 0
      this.expenditureCount = 0
    },
  },
})
