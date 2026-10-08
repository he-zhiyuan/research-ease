import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/user'
import type { Result } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response): any => {
    const result = response.data as Result<unknown>
    if (result.code !== 200) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络错误'
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request
