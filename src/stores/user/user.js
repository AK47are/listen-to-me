import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLogin = computed(() => !!token.value)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 仅刷新用户信息（不刷新 token）
  // 用于更新头像临时链接等动态信息
  async function refreshUserInfo() {
    try {
      const { profileApi } = await import('@/api/user/profile')
      const res = await profileApi.getProfile()
      if (res.code === 200 && res.data) {
        setUserInfo(res.data)
        return res.data
      }
      return null
    } catch (error) {
      console.error('获取用户信息失败', error)
      // 如果是 401，让 request 拦截器处理登出
      throw error
    }
  }

  return {
    token,
    userInfo,
    isLogin,
    setToken,
    setUserInfo,
    logout,
    refreshUserInfo,
  }
})
