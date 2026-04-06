import request from '@/utils/request'

export const authApi = {
  login(data) {
    return request({ url: '/common/auth/login', method: 'post', data })
  },
  getImageCaptcha() {
    return request({ url: '/common/auth/image-captcha', method: 'get' })
  },
  sendVerifyCode(data) {
    return request({ url: '/common/auth/verify-code', method: 'post', data })
  },
  register(data) {
    return request({ url: '/common/auth/register', method: 'post', data })
  },
  refreshToken() {
    return request({ url: '/common/auth/refresh', method: 'post' })
  },
}
