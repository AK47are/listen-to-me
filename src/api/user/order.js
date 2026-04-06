import request from '@/utils/request'

export const orderApi = {
  purchaseAudio(audioId) {
    return request({ url: `/user/audio/${audioId}/purchase`, method: 'post' })
  },
  getAudioOrder(sn) {
    return request({ url: `/user/audio/order/${sn}`, method: 'get' })
  },
  getAudioOrderPage(params) {
    return request({ url: '/user/audio/order/page', method: 'get', params })
  },
}
