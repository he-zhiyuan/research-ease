import request from './request'
import type { BudgetItemBalanceResponse, ExpenditureRequest, ExpenditureResponse, RejectRequest } from '@/types'

export function submitExpenditure(projectId: number, data: ExpenditureRequest) {
  return request.post<unknown, ExpenditureResponse>(`/projects/${projectId}/expenditures`, data)
}

export function listExpendituresForProject(projectId: number) {
  return request.get<unknown, ExpenditureResponse[]>(`/projects/${projectId}/expenditures`)
}

export function listBudgetItemsWithBalance(projectId: number) {
  return request.get<unknown, BudgetItemBalanceResponse[]>(`/projects/${projectId}/budget-items`)
}

export function listExpendituresByStatus(status?: string) {
  return request.get<unknown, ExpenditureResponse[]>('/expenditures', { params: { status } })
}

export function approveExpenditure(id: number) {
  return request.post<unknown, void>(`/expenditures/${id}/approve`)
}

export function rejectExpenditure(id: number, data: RejectRequest) {
  return request.post<unknown, void>(`/expenditures/${id}/reject`, data)
}
