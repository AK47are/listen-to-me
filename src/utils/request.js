import axios from 'axios'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => {
    if (response.data.code && response.data.code !== 200) {
      ElMessage.error(response.data.msg || '操作失败')
      return Promise.reject(new Error(response.data.msg || '操作失败'))
    }
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      // 统一处理不同状态码
      switch (status) {
        case 400:
          ElMessage.error(data?.msg || '请求参数错误')
          break
        case 401:
          const userStore = useUserStore()
          userStore.logout()
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
          break
        case 403:
          ElMessage.error(data?.msg || '无权限访问')
          break
        case 404:
          ElMessage.error(data?.msg || '资源不存在')
          break
        case 409:
          ElMessage.warning(data?.msg || '访问资源冲突')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data?.msg || '接口访问异常')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络连接异常，请检查网络设置')
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  },
)

export default request
