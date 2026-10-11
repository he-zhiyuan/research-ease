import { defineStore } from 'pinia'
import { listProjectsForManager } from '@/api/project'
import { listExpendituresByStatus } from '@/api/expenditure'
import { useUserStore } from '@/store/user'

export interface NotificationItem {
  key: string
  text: string
  route: string
}

const STORAGE_KEY = 'researchease-dismissed-notifications'

function loadDismissedMap(): Record<string, string[]> {
  const raw = localStorage.getItem(STORAGE_KEY)
  return raw ? JSON.parse(raw) : {}
}

function saveDismissedMap(map: Record<string, string[]>) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(map))
}

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    items: [] as NotificationItem[],
  }),
  actions: {
    async refresh() {
      const userStore = useUserStore()
      const userId = userStore.userId
      if (!userId) {
        this.items = []
        return
      }
      const dismissedMap = loadDismissedMap()
      const dismissed = new Set(dismissedMap[userId] || [])

      const [pendingProjects, acceptingProjects, pendingExpenditures] = await Promise.all([
        listProjectsForManager({ status: 'SUBMITTED' }),
        listProjectsForManager({ status: 'ACCEPTING' }),
        listExpendituresByStatus('PENDING'),
      ])

      const items: NotificationItem[] = []
      pendingProjects.forEach((p) => {
        items.push({
          key: `project-${p.id}`,
          text: `${p.leaderName} 提交了项目「${p.projectName}」的立项申报，等待审批`,
          route: `/manager/projects/${p.id}`,
        })
      })
      acceptingProjects.forEach((p) => {
        items.push({
          key: `acceptance-${p.id}`,
          text: `${p.leaderName} 提交了项目「${p.projectName}」的结题申请，等待验收`,
          route: `/manager/projects/${p.id}`,
        })
      })
      pendingExpenditures.forEach((e) => {
        items.push({
          key: `expenditure-${e.id}`,
          text: `${e.creatorName} 提交了一笔经费支出申请（¥${e.amount}），等待审核`,
          route: `/manager/expenditures`,
        })
      })

      this.items = items.filter((item) => !dismissed.has(item.key))
    },
    dismiss(key: string) {
      const userStore = useUserStore()
      const userId = userStore.userId
      if (userId) {
        const map = loadDismissedMap()
        const list = map[userId] || []
        if (!list.includes(key)) {
          map[userId] = [...list, key]
          saveDismissedMap(map)
        }
      }
      this.items = this.items.filter((item) => item.key !== key)
    },
    reset() {
      this.items = []
    },
  },
})
