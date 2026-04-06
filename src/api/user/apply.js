import request from '@/utils/request'

export const applyApi = {
  applyCreator(data) {
    return request({ url: '/user/creator/apply', method: 'post', data })
  },
  getApplyStatus() {
    return request({ url: '/user/creator/apply/status', method: 'get' })
  },
}
