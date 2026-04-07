import request from '@/utils/request'

export const audioApi = {
  getAudioDetail(id) {
    return request({ url: `/user/audio/${id}`, method: 'get' })
  },

  getCreatorAudioPage(creatorId, params) {
    return request({
      url: `/user/audio/creator/${creatorId}/page`,
      method: 'get',
      params,
    })
  },
}
