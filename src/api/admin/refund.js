import request from '@/utils/request'

export const refundApi = {
  getRefundApplyPage(params) {
    return request({ url: '/admin/consult/refund/page', method: 'get', params })
  },
  auditRefund(data) {
    return request({ url: '/admin/consult/refund/audit', method: 'put', data })
  },
}
