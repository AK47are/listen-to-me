import request from '@/utils/request'

export const audioApi = {
  getAudioDetail(id) {
    return request({ url: `/user/audio/${id}`, method: 'get' })
  },
}
