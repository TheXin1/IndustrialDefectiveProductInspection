import { http } from './http'
import type {
  ApiResponse,
  LoginResponse,
  User,
  Role,
  Permission,
  InferenceDetectResponse
} from './types'

export const api = {
  login(payload: { username: string; password: string }) {
    return http.post<ApiResponse<LoginResponse>>('/v1/auth/login', payload)
  },
  listUsers(status?: number) {
    return http.get<ApiResponse<User[]>>('/v1/users', { params: { status } })
  },
  listRoles() {
    return http.get<ApiResponse<Role[]>>('/v1/roles')
  },
  listPermissions() {
    return http.get<ApiResponse<Permission[]>>('/v1/permissions')
  },
  detect(form: FormData) {
    return http.post<ApiResponse<InferenceDetectResponse>>('/v1/inference/detect', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  inferenceHealth() {
    return http.get<ApiResponse<Record<string, unknown>>>('/v1/inference/health')
  },
  inferenceStartup() {
    return http.get<ApiResponse<Record<string, unknown>>>('/v1/inference/startup_status')
  }
}
