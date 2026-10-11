import request from './request'
import type { UserCreateRequest, UserResponse } from '@/types'

export function listUsers() {
  return request.get<unknown, UserResponse[]>('/users')
}

export function createUser(data: UserCreateRequest) {
  return request.post<unknown, UserResponse>('/users', data)
}

export function setUserEnabled(id: number, enabled: boolean) {
  return request.put<unknown, void>(`/users/${id}/enabled`, { enabled })
}

export function deleteUser(id: number) {
  return request.delete<unknown, void>(`/users/${id}`)
}
