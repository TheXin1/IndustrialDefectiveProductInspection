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

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  time: string
}

export interface ChatSessionSummary {
  id: number
  title: string
  updatedAt: string
}

export interface ChatSession {
  id: number
  userId: number
  title: string
  prompt: string
  maxTgtLen: number
  topP: number
  temperature: number
  imageDataUrl?: string
  normalDataUrl?: string
  result?: InferenceDetectResponse
  createdAt?: string
  updatedAt?: string
  messages: ChatMessage[]
}

export interface DashboardOverview {
  todayCount: number
  defectRate: number
  alertCount: number
  modelVersion: string
  inferenceStatus: string
  nodeStatus: string
  alertPolicy: string
  dataSync: string
}

export interface PageResult<T> {
  page: number
  size: number
  total: number
  records: T[]
}

export interface InspectionRecord {
  id: number
  userId?: number
  sourceType?: string
  imageUrl?: string
  localizationImageUrl?: string
  description?: string
  hasAnomaly?: boolean
  modelVersion?: string
  reviewStatus?: number
  reviewResult?: number
  reviewNote?: string
  reviewedBy?: number
  reviewedAt?: string
  createdAt?: string
}
