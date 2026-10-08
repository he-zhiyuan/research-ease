import request from './request'
import type { DashboardOverviewResponse } from '@/types'

export function getDashboardOverview() {
  return request.get<unknown, DashboardOverviewResponse>('/dashboard/overview')
}
