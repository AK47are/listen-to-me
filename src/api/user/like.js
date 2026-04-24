import request from '@/utils/request'

export const likeApi = {
  likeAudio(audioId) {
    return request({ url: `/user/audio/${audioId}/like`, method: 'post' })
  },
  unlikeAudio(audioId) {
    return request({ url: `/user/audio/${audioId}/like`, method: 'delete' })
  },
  getLikePage(params) {
    return request({ url: '/user/like/page', method: 'get', params })
  },
}
