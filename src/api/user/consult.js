import request from '@/utils/request'

export const consultApi = {
  saveConsult(data) {
    return request({ url: '/user/consult', method: 'post', data })
  },
  getMyConsultPage(params) {
    return request({ url: '/user/consult/page', method: 'get', params })
  },
  cancelConsult(id) {
    return request({ url: `/user/consult/${id}/cancel`, method: 'put' })
  },
  applyRefund(id, data) {
    return request({ url: `/user/consult/${id}/refund`, method: 'put', data })
  },
}
