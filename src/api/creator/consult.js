import request from '@/utils/request'

export const creatorConsultApi = {
  getConsultPage(params) {
    return request({ url: '/creator/consult/page', method: 'get', params })
  },
  confirmConsult(id, address) {
    return request({
      url: `/creator/consult/${id}/confirm`,
      method: 'put',
      params: address ? { address } : {},
    })
  },
  rejectConsult(id) {
    return request({ url: `/creator/consult/${id}/reject`, method: 'put' })
  },
  completeConsult(id) {
    return request({ url: `/creator/consult/${id}/complete`, method: 'put' })
  },
}
