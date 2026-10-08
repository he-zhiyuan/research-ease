export type Role = 'RESEARCHER' | 'MANAGER' | 'ADMIN'

export type ProjectStatus = 'DRAFT' | 'SUBMITTED' | 'REJECTED' | 'EXECUTING' | 'ACCEPTING' | 'COMPLETED'

export type BudgetCategory =
  | 'EQUIPMENT'
  | 'MATERIAL'
  | 'LABOR'
  | 'TEST'
  | 'CONFERENCE'
  | 'PUBLICATION'
  | 'OTHER'

export type AchievementType = 'PAPER' | 'PATENT' | 'SOFTWARE_COPYRIGHT' | 'REPORT' | 'AWARD' | 'OTHER'

export type ApprovalType = 'PROJECT_APPROVAL' | 'FUND_APPROVAL' | 'ACCEPTANCE_APPROVAL'

export type ApprovalResult = 'APPROVED' | 'REJECTED'

export type ExpenditureStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  realName: string
  role: Role
}

export interface BudgetItemRequest {
  category: BudgetCategory
  requestedAmount: number
}

export interface BudgetItemResponse {
  id: number
  category: BudgetCategory
  requestedAmount: number
  approvedAmount: number | null
}

export interface BudgetApprovalItem {
  budgetItemId: number
  approvedAmount: number
}

export interface ProjectCreateRequest {
  projectName: string
  researchContent?: string
  teamMembers?: string
  fundingSource?: string
  startDate: string
  endDate: string
  budgetItems: BudgetItemRequest[]
}

export type ProjectUpdateRequest = ProjectCreateRequest

export interface ApproveProjectRequest {
  budgetItems?: BudgetApprovalItem[]
}

export interface RejectRequest {
  comment: string
}

export interface ProjectResponse {
  id: number
  projectName: string
  researchContent: string | null
  leaderId: number
  leaderName: string | null
  teamMembers: string | null
  fundingSource: string | null
  startDate: string
  endDate: string
  status: ProjectStatus
  submittedAt: string | null
  createdAt: string
  updatedAt: string
  budgetItems: BudgetItemResponse[]
}

export interface ApprovalRecordResponse {
  id: number
  type: ApprovalType
  targetId: number | null
  result: ApprovalResult
  comment: string | null
  operatorId: number
  operatorName: string | null
  createdAt: string
}

export interface ProgressReportRequest {
  content: string
  reportDate: string
}

export interface ProgressReportResponse {
  id: number
  projectId: number
  content: string
  reportDate: string
  createdBy: number
  creatorName: string | null
  createdAt: string
}

export interface OverdueProjectResponse {
  projectId: number
  projectName: string
  leaderName: string | null
  lastReportDate: string
  daysSinceLastReport: number
}

export interface ExpenditureRequest {
  budgetItemId: number
  amount: number
  description?: string
}

export interface ExpenditureResponse {
  id: number
  projectId: number
  budgetItemId: number
  budgetItemCategory: BudgetCategory
  amount: number
  description: string | null
  status: ExpenditureStatus
  createdBy: number
  creatorName: string | null
  createdAt: string
}

export interface BudgetItemBalanceResponse {
  id: number
  category: BudgetCategory
  requestedAmount: number
  approvedAmount: number | null
  balance: number
}

export interface AchievementRequest {
  type: AchievementType
  title: string
  detail?: string
  achievedDate?: string
}

export interface AchievementResponse {
  id: number
  type: AchievementType
  title: string
  detail: string | null
  achievedDate: string | null
  createdAt: string
}

export interface AcceptanceRequest {
  achievements: AchievementRequest[]
}

export interface DashboardOverviewResponse {
  totalProjects: number
  statusCounts: Record<string, number>
  totalRequestedBudget: number
  totalApprovedBudget: number
  totalSpent: number
  totalPending: number
  totalBalance: number
}

export interface UserCreateRequest {
  username: string
  password: string
  realName: string
  role: Role
}

export interface UserResponse {
  id: number
  username: string
  realName: string
  role: Role
  enabled: boolean
  createdAt: string
}
