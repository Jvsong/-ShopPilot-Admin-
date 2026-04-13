import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuthState } from './auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data ?? {}

    if (typeof code !== 'undefined') {
      if (code === 200) {
        return data
      }

      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }

    return response.data
  },
  (error) => {
    const { response } = error

    if (response) {
      const { status, data } = response

      switch (status) {
        case 400:
          ElMessage.error(data.message || '请求参数错误')
          break
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          clearAuthState()
          if (window.location.pathname !== '/admin/login') {
            window.location.href = '/admin/login'
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data.message || '服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败: ${status}`)
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络设置')
    }

    return Promise.reject(error)
  }
)

export const http = {
  get: <T = any>(url: string, params?: any, config?: AxiosRequestConfig) =>
    request.get<any, T>(url, { ...config, params }),
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    request.post<any, T>(url, data, config),
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    request.put<any, T>(url, data, config),
  delete: <T = any>(url: string, params?: any, config?: AxiosRequestConfig) =>
    request.delete<any, T>(url, { ...config, params }),
  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) =>
    request.patch<any, T>(url, data, config)
}

export default request
