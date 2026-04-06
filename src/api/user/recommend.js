import request from '@/utils/request'

export const recommendApi = {
  getRecommendList(params) {
    return request({
      url: '/user/audio/recommend',
      method: 'get',
      params,
    })
  },
  getHotList() {
    return request({
      url: '/user/audio/hot',
      method: 'get',
    })
  },
}
