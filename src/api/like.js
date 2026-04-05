import mockApi from '@/data/mockApi'
import request from '@/utils/request'

export const likeApi = {
  saveAudioLike(data) {
    return request({
      url: '/user/audio/like',
      method: 'post',
      data,
    })
    //return mockApi.like.saveAudioLike(data)
  },

  getLikePage(params) {
    return request({
      url: '/user/like/page',
      method: 'get',
      params,
    })
  },
}
