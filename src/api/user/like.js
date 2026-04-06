import request from '@/utils/request'

export const likeApi = {
  saveAudioLike(data) {
    return request({ url: '/user/audio/like', method: 'post', data })
  },
  getLikePage(params) {
    return request({ url: '/user/like/page', method: 'get', params })
  },
}
