import { defineStore } from 'pinia'
import { listMyApprovalRecords, listProjectsForManager } from '@/api/project'
import { listExpendituresByStatus } from '@/api/expenditure'
import { useUserStore } from '@/store/user'
import type { MyApprovalRecordResponse } from '@/types'

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

async function buildManagerItems(): Promise<NotificationItem[]> {
  const [pendingProjects, acceptingProjects, pendingExpenditures] = await Promise.all([
    listProjectsForManager({ status: 'SUBMITTED' }),
    listProjectsForManager({ status: 'ACCEPTING' }),
    listExpendituresByStatus('PENDING'),
  ])

  const items: NotificationItem[] = []
  pendingProjects.forEach((p) => {
    items.push({
      key: `project-${p.id}-${p.submittedAt}`,
      text: `${p.leaderName} 提交了项目「${p.projectName}」的立项申报，等待审批`,
      route: `/manager/projects/${p.id}`,
    })
  })
  acceptingProjects.forEach((p) => {
    items.push({
      key: `acceptance-${p.id}-${p.updatedAt}`,
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
  return items
}

function toResearcherItem(record: MyApprovalRecordResponse): NotificationItem | null {
  const name = record.projectName ?? `#${record.projectId}`
  const approved = record.result === 'APPROVED'
  const detailRoute = `/researcher/projects/${record.projectId}`
  const editRoute = `/researcher/projects/${record.projectId}/edit`
  const key = `approval-${record.id}`

  switch (record.type) {
    case 'PROJECT_APPROVAL':
      return approved
        ? { key, text: `您的项目「${name}」已通过审批，进入执行阶段`, route: detailRoute }
        : { key, text: `您的项目「${name}」被管理人员驳回${record.comment ? '，意见：' + record.comment : ''}，请修改后重新提交`, route: editRoute }
    case 'FUND_APPROVAL':
      return approved
        ? { key, text: `您在项目「${name}」中提交的经费支出申请已通过`, route: detailRoute }
        : { key, text: `您在项目「${name}」中提交的经费支出申请被驳回${record.comment ? '，意见：' + record.comment : ''}`, route: detailRoute }
    case 'ACCEPTANCE_APPROVAL':
      return approved
        ? { key, text: `您的项目「${name}」验收通过，项目已结题`, route: detailRoute }
        : { key, text: `您的项目「${name}」结题申请未通过${record.comment ? '，意见：' + record.comment : ''}，项目已回到执行阶段`, route: detailRoute }
    default:
      return null
  }
}

async function buildResearcherItems(): Promise<NotificationItem[]> {
  const records = await listMyApprovalRecords()
  return records.map(toResearcherItem).filter((item): item is NotificationItem => item !== null)
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

      const items = userStore.role === 'RESEARCHER' ? await buildResearcherItems() : await buildManagerItems()

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
