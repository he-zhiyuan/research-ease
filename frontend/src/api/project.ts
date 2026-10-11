import request from './request'
import type {
  AcceptanceRequest,
  ApprovalRecordResponse,
  ApproveProjectRequest,
  MyApprovalRecordResponse,
  ProjectCreateRequest,
  ProjectResponse,
  ProjectUpdateRequest,
  RejectRequest,
} from '@/types'

export function createProject(data: ProjectCreateRequest) {
  return request.post<unknown, ProjectResponse>('/projects', data)
}

export function updateProject(id: number, data: ProjectUpdateRequest) {
  return request.put<unknown, void>(`/projects/${id}`, data)
}

export function submitProject(id: number) {
  return request.post<unknown, void>(`/projects/${id}/submit`)
}

export function listMyProjects() {
  return request.get<unknown, ProjectResponse[]>('/projects/my')
}

export function listProjectsForManager(params: { status?: string; keyword?: string }) {
  return request.get<unknown, ProjectResponse[]>('/projects', { params })
}

export function listMyApprovalRecords() {
  return request.get<unknown, MyApprovalRecordResponse[]>('/projects/my/approval-records')
}

export function getProject(id: number) {
  return request.get<unknown, ProjectResponse>(`/projects/${id}`)
}

export function listApprovalRecords(id: number) {
  return request.get<unknown, ApprovalRecordResponse[]>(`/projects/${id}/approval-records`)
}

export function approveProject(id: number, data?: ApproveProjectRequest) {
  return request.post<unknown, void>(`/projects/${id}/approve`, data)
}

export function rejectProject(id: number, data: RejectRequest) {
  return request.post<unknown, void>(`/projects/${id}/reject`, data)
}

export function submitAcceptance(id: number, data: AcceptanceRequest) {
  return request.post<unknown, void>(`/projects/${id}/acceptance`, data)
}

export function approveAcceptance(id: number) {
  return request.post<unknown, void>(`/projects/${id}/acceptance/approve`)
}

export function rejectAcceptance(id: number, data: RejectRequest) {
  return request.post<unknown, void>(`/projects/${id}/acceptance/reject`, data)
}
