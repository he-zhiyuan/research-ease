import request from './request'
import type {
  OverdueProjectResponse,
  ProgressReportRequest,
  ProgressReportResponse,
  RecentProgressReportResponse,
} from '@/types'

export function submitProgressReport(projectId: number, data: ProgressReportRequest) {
  return request.post<unknown, ProgressReportResponse>(`/projects/${projectId}/progress-reports`, data)
}

export function listProgressReports(projectId: number) {
  return request.get<unknown, ProgressReportResponse[]>(`/projects/${projectId}/progress-reports`)
}

export function listOverdueProjects() {
  return request.get<unknown, OverdueProjectResponse[]>('/progress-reports/overdue')
}

export function listRecentProgressReports(limit = 20) {
  return request.get<unknown, RecentProgressReportResponse[]>('/progress-reports/recent', { params: { limit } })
}
