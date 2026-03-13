import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import type { ApiResponse } from './types'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

export const http = axios.create({
  baseURL,
  timeout: 30000
})

export const request = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return http.get<ApiResponse<T>>(url, config).then((res) => res.data)
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return http.post<ApiResponse<T>>(url, data, config).then((res) => res.data)
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return http.put<ApiResponse<T>>(url, data, config).then((res) => res.data)
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return http.delete<ApiResponse<T>>(url, config).then((res) => res.data)
  }
}