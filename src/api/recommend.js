import mockApi from '@/data/mockApi'
import request from '@/utils/request'

export const recommendApi = {
  getHotList() {
    return request({
      url: '/user/audio/hot',
      method: 'get',
    })
  },

  getRecommendList(params) {
    return mockApi.recommend.getRecommendList(params)
  },
}
