export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface User {
  id: number
  username: string
  displayName?: string
  email?: string
  phone?: string
  status: number
  lastLoginAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface Role {
  id: number
  code: string
  name: string
  description?: string
  builtIn?: number
}

export interface Permission {
  id: number
  code: string
  name: string
  type: 'MODULE' | 'ACTION'
  parentId?: number
  path?: string
  method?: string
  description?: string
  sortOrder?: number
}

export interface LoginResponse {
  user: User
  roles: Role[]
  permissions: Permission[]
}

export interface InferenceDetectData {
  description: string
  localization_image_base64: string
  has_anomaly: boolean
}

export interface InferenceDetectResponse {
  success: boolean
  data: InferenceDetectData
}
