import request from '@/utils/request'

export const searchApi = {
  searchAudio(params) {
    return request({ url: '/user/audio/search', method: 'get', params })
  },
}
