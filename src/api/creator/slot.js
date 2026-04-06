import request from '@/utils/request'

export const slotApi = {
  saveSlotBatch(data) {
    return request({ url: '/creator/slots', method: 'post', data })
  },
  getSlotPage(params) {
    return request({ url: '/creator/slots/page', method: 'get', params })
  },
  updateSlot(id, status) {
    return request({ url: `/creator/slots/${id}`, method: 'put', params: { status } })
  },
  deleteSlot(id) {
    return request({ url: `/creator/slots/${id}`, method: 'delete' })
  },
}
