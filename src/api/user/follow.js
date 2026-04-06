import request from '@/utils/request'

export const followApi = {
  followCreator(creatorId) {
    return request({ url: `/user/creator/${creatorId}/follow`, method: 'post' })
  },
  unfollowCreator(creatorId) {
    return request({ url: `/user/creator/${creatorId}/follow`, method: 'delete' })
  },
  getFollowPage(params) {
    return request({ url: '/user/creator/follow/page', method: 'get', params })
  },
  getFansPage(creatorId, params) {
    return request({ url: `/user/creator/${creatorId}/fans/page`, method: 'get', params })
  },
}
