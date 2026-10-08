export const PROJECT_STATUS_LABEL: Record<string, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  REJECTED: '已驳回',
  EXECUTING: '执行中',
  ACCEPTING: '验收中',
  COMPLETED: '已结题',
}

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

export const PROJECT_STATUS_TAG: Record<string, TagType> = {
  DRAFT: 'info',
  SUBMITTED: 'warning',
  REJECTED: 'danger',
  EXECUTING: 'primary',
  ACCEPTING: 'warning',
  COMPLETED: 'success',
}

export const BUDGET_CATEGORY_LABEL: Record<string, string> = {
  EQUIPMENT: '设备费',
  MATERIAL: '材料费',
  LABOR: '劳务费',
  TEST: '测试化验加工费',
  CONFERENCE: '会议差旅费',
  PUBLICATION: '出版文献信息传播费',
  OTHER: '其他',
}

export const BUDGET_CATEGORY_OPTIONS = Object.entries(BUDGET_CATEGORY_LABEL).map(([value, label]) => ({
  value,
  label,
}))

export const ACHIEVEMENT_TYPE_LABEL: Record<string, string> = {
  PAPER: '论文',
  PATENT: '专利',
  SOFTWARE_COPYRIGHT: '软件著作权',
  REPORT: '研究报告',
  AWARD: '获奖',
  OTHER: '其他',
}

export const ACHIEVEMENT_TYPE_OPTIONS = Object.entries(ACHIEVEMENT_TYPE_LABEL).map(([value, label]) => ({
  value,
  label,
}))

export const EXPENDITURE_STATUS_LABEL: Record<string, string> = {
  PENDING: '审核中',
  APPROVED: '已批准',
  REJECTED: '已驳回',
}

export const EXPENDITURE_STATUS_TAG: Record<string, TagType> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
}

export const APPROVAL_TYPE_LABEL: Record<string, string> = {
  PROJECT_APPROVAL: '立项审批',
  FUND_APPROVAL: '经费审核',
  ACCEPTANCE_APPROVAL: '验收审核',
}

export const ROLE_LABEL: Record<string, string> = {
  RESEARCHER: '科研人员',
  MANAGER: '管理人员',
  ADMIN: '系统管理员',
}
