import request from '@/utils/request'

export const creatorApi = {
  getCreatorPage(params) {
    return request({ url: '/user/creator/page', method: 'get', params })
  },
  getCreatorDetail(creatorId) {
    return request({ url: `/user/creator/${creatorId}`, method: 'get' })
  },
  getCreatorSlots(params) {
    // params 包含 creatorId, startTime, endTime, pageNum, pageSize
    return request({
      url: `/user/creator/${params.creatorId}/slots/page`,
      method: 'get',
      params,
    })
  },
}
