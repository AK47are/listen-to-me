import request from '@/utils/request'

export const historyApi = {
  saveProgress(data) {
    return request({ url: '/user/history', method: 'post', data })
  },
  getProgress(audioId) {
    return request({ url: `/user/history/${audioId}`, method: 'get' })
  },
  getHistoryPage(params) {
    return request({ url: '/user/history/page', method: 'get', params })
  },
}
