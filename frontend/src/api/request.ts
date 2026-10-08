import axios from 'axios'
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
      const requestToken = error.config?.headers?.Authorization
      const currentToken = userStore.token ? `Bearer ${userStore.token}` : ''
      // 只有当这次 401 用的还是当前登录态的 token 时才登出，避免一次更早发出、更晚返回的
      // 请求（比如重复点击登录触发的旧请求）用过期结果顶掉刚刚登录成功的新会话
      if (!currentToken || requestToken === currentToken) {
        userStore.logout()
        if (router.currentRoute.value.name !== 'login') {
          router.push('/login')
        }
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request
