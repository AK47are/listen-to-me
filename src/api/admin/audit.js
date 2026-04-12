import request from '@/utils/request'

export const auditApi = {
  getAudioAuditPage(params) {
    return request({ url: '/admin/audio/audit/page', method: 'get', params })
  },
  auditAudio(data) {
    return request({ url: '/admin/audio/audit', method: 'put', data })
  },
  getAuditApplyPage(params) {
    return request({ url: '/admin/creator/apply/audit/page', method: 'get', params })
  },
  auditApply(data) {
    return request({ url: '/admin/creator/apply/audit', method: 'put', data })
  },
}
