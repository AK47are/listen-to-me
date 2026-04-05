import request from '@/utils/request'
import mockApi from '@/data/mockApi'

// 管理面板API
export const adminApi = {
  // 获取数据大盘
  async getDashboard() {
    const data = await mockApi.admin.getDashboard()
    return { data }
  },

  // 获取待审音频列表
  async getAuditAudioPage(params) {
    const data = await mockApi.admin.getAuditAudioPage(params)
    return { data }
  },

  // 审核音频
  async auditAudio(data) {
    const result = await mockApi.admin.auditAudio(data)
    return { data: result }
  },

  // 获取待审创作者申请列表
  getAuditApplyPage(params) {
    return request({
      url: '/admin/creator/apply/audit/page',
      method: 'get',
      params
    })
  },

  // 审核创作者申请
  auditApply(data) {
    return request({
      url: '/admin/creator/apply/audit',
      method: 'put',
      data
    })
  },

  // 获取退款申请列表
  getRefundApplyPage(params) {
    return request({
      url: '/admin/consult/refund/page',
      method: 'get',
      params
    })
  },

  // 审核退款申请
  auditRefund(data) {
    return request({
        url: '/admin/consult/refund/audio',
        method: 'put',
        data
    })
  },

  // 用户管理
  getUserPage(params) {
    return request({
      url: '/admin/user/page',
      method: 'get',
      params
    })
  },

  banUser(userId) {
    return request({
      url: `/admin/user/${userId}/ban`,
      method: 'put'
    })
  },

  unbanUser(userId) {
    return request({
      url: `/admin/user/${userId}/unban`,
      method: 'put'
    })
  }
}