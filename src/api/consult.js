import mockApi from '@/data/mockApi'
import request from '@/utils/request'

export const consultApi = {
  // 获取创作者列表
  getCreatorList(params) {
    return request({
      url: '/user/creator/page',
      method: 'get',
      params
    })
    //return mockApi.userConsult.getCreatorList(params)
  },

  // 获取创作者详情
  getCreatorDetail(creatorId) {
    return mockApi.userConsult.getCreatorDetail(creatorId)
  },

  // 获取创作者时间槽列表
  getCreatorSlots(params) {
    return request({
      url: `/user/creator/${params.creatorId}/slots/page`,
      method: 'get',
      params
    })
    //return mockApi.userConsult.getCreatorSlots(params)
  },

  // 获取时间槽详情
  getSlotDetail(slotId) {
    return mockApi.userConsult.getSlotDetail(slotId)
  },

  // 发起预约
  saveConsult(data) {
    return request({
      url: '/user/consult',
      method: 'post',
      data
    })
  },

  // 查询我的预约
  getMyConsultPage(params) {
    return request({
      url: '/user/consult/page',
      method: 'get',
      params
    })
  },

  // 取消预约
  cancelConsult(id) {
    return request({
      url: `/user/consult/${id}/cancel`,
      method: 'put'
    })
  },

  // 申请退款
  applyRefund(id, reason) {
    return request({
      url: `/user/consult/consult/${id}/refund`,
      method: 'put',
      data: {
        reason
      }
    })
  },

  // 查询可用时间槽列表（听众视角）- 根据时间范围查询
  getAvailableSlots(params) {
    return mockApi.userConsult.getAvailableSlots(params)
  },
}