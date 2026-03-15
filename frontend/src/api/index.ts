import { request } from './http'
import type {
  LoginResponse,
  User,
  Role,
  Permission,
  InferenceDetectResponse,
  ChatSession,
  ChatSessionSummary,
  DashboardOverview,
  InspectionRecord,
  PageResult
} from './types'

export const api = {
  login(payload: { username: string; password: string }) {
    return request.post<LoginResponse>('/v1/auth/login', payload)
  },
  register(payload: {
    username: string
    password: string
    displayName?: string
    email?: string
    phone?: string
  }) {
    return request.post<User>('/v1/auth/register', payload)
  },
  listUsers(status?: number) {
    return request.get<User[]>('/v1/users', { params: { status } })
  },
  createUser(payload: {
    username: string
    password: string
    displayName?: string
    email?: string
    phone?: string
    status?: number
    roleIds?: number[]
  }) {
    return request.post<User>('/v1/users', payload)
  },
  userRoles(id: number) {
    return request.get<Role[]>(`/v1/users/${id}/roles`)
  },
  deleteUser(id: number) {
    return request.delete<void>(`/v1/users/${id}`)
  },
  updateUser(id: number, payload: {
    displayName?: string
    email?: string
    phone?: string
    password?: string
    status?: number
    roleIds?: number[]
  }) {
    return request.put<User>(`/v1/users/${id}`, payload)
  },
  listRoles() {
    return request.get<Role[]>('/v1/roles')
  },
  createRole(payload: {
    code: string
    name: string
    description?: string
    permissionIds?: number[]
  }) {
    return request.post<Role>('/v1/roles', payload)
  },
  updateRole(id: number, payload: {
    name: string
    description?: string
    permissionIds?: number[]
  }) {
    return request.put<Role>(`/v1/roles/${id}`, payload)
  },
  deleteRole(id: number) {
    return request.delete<void>(`/v1/roles/${id}`)
  },
  rolePermissions(id: number) {
    return request.get<number[]>(`/v1/roles/${id}/permissions`)
  },
  listPermissions() {
    return request.get<Permission[]>('/v1/permissions')
  },
  createPermission(payload: {
    code: string
    name: string
    type: 'MODULE' | 'ACTION'
    parentId?: number
    path?: string
    method?: string
    description?: string
    sortOrder?: number
  }) {
    return request.post<Permission>('/v1/permissions', payload)
  },
  deletePermission(id: number) {
    return request.delete<void>(`/v1/permissions/${id}`)
  },
  detect(form: FormData) {
    return request.post<InferenceDetectResponse>('/v1/inference/detect', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  chatDetect(form: FormData) {
    return request.post<InferenceDetectResponse>('/v1/inference/chat', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  inferenceHealth() {
    return request.get<Record<string, unknown>>('/v1/inference/health')
  },
  inferenceStartup() {
    return request.get<Record<string, unknown>>('/v1/inference/startup_status')
  },
  dashboardOverview() {
    return request.get<DashboardOverview>('/v1/dashboard/overview')
  },
  recordsList(params: {
    userId?: number
    hasAnomaly?: number
    reviewStatus?: number
    reviewResult?: number
    keyword?: string
    startAt?: string
    endAt?: string
    page?: number
    size?: number
  }) {
    return request.get<PageResult<InspectionRecord>>('/v1/records', { params })
  },
  recordReview(id: number, payload: {
    reviewResult: number
    reviewNote?: string
    reviewerId?: number
  }) {
    return request.put<void>(`/v1/records/${id}/review`, payload)
  },
  chatList(userId: number) {
    return request.get<ChatSessionSummary[]>('/v1/chat/sessions', { params: { userId } })
  },
  chatGet(id: number) {
    return request.get<ChatSession>(`/v1/chat/sessions/${id}`)
  },
  chatCreate(payload: Partial<ChatSession>) {
    return request.post<ChatSession>('/v1/chat/sessions', payload)
  },
  chatUpdate(id: number, payload: Partial<ChatSession>) {
    return request.put<ChatSession>(`/v1/chat/sessions/${id}`, payload)
  },
  chatDelete(id: number) {
    return request.delete<void>(`/v1/chat/sessions/${id}`)
  }
}
