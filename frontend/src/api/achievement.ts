import request from './request'
import type { AchievementResponse } from '@/types'

export function listAchievements(projectId: number) {
  return request.get<unknown, AchievementResponse[]>(`/projects/${projectId}/achievements`)
}
