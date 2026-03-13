import { request } from './http'
import type {
  LoginResponse,
  User,
  Role,
  Permission,
  InferenceDetectResponse,
  ChatSession,
  ChatSessionSummary
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
  listRoles() {
    return request.get<Role[]>('/v1/roles')
  },
  listPermissions() {
    return request.get<Permission[]>('/v1/permissions')
  },
  detect(form: FormData) {
    return request.post<InferenceDetectResponse>('/v1/inference/detect', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  inferenceHealth() {
    return request.get<Record<string, unknown>>('/v1/inference/health')
  },
  inferenceStartup() {
    return request.get<Record<string, unknown>>('/v1/inference/startup_status')
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