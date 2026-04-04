import mockApi from '@/data/mockApi'
import request from '@/utils/request'

export const creatorApi = {
  uploadAudio(file) {
    return mockApi.creator.uploadAudio(file)
  },

  uploadCover(file) {
    return mockApi.creator.uploadCover(file)
  },

  saveAudio(data) {
    return mockApi.creator.saveAudio(data)
  },

  getAudioPage(params) {
    return request({
      url: '/creator/audio/page',
      method: 'get',
      params
    })
    //return mockApi.creator.getAudioPage(params)
  },

  getAudio(id) {
    return mockApi.creator.getAudio(id)
  },

  getAudioStatus(id) {
    return mockApi.creator.getAudioStatus(id)
  },

  updateAudio(data) {
    return mockApi.creator.updateAudio(data)
  },

  removeAudio(id) {
    return mockApi.creator.removeAudio(id)
  },

  // 时间槽管理
  saveSlotBatch(slots) {
    return request({
      url: '/creator/slots',
      method: 'post',
      data: slots
    })
    //return mockApi.creator.saveSlotBatch(slots)
  },

  getSlotPage(params) {
    return request({
      url: 'creator/slots/page',
      method: 'get',
      params
    })
  },

  updateSlot(id, status) {
    return request({
      url: `/creator/slots/${id}`,
      method: 'put',
      params: {
        status
      }
    })
  },

  deleteSlot(id) {
    return request({
      url: `/creator/slots/${id}`,
      method: 'delete'
    })
  },

  // 预约订单管理
  getConsultPage(params) {
    return request({
      url: '/creator/consult/page',
      method: 'get',
      params
    })
  },

  confirmConsult(id, address) {
    return request({
      url: `/creator/consult/${id}/confirm`,
      method: 'put',
      data: {
        address
      }
    })
  },

  rejectConsult(id) {
    return request({
      url: `/creator/consult/${id}/reject`,
      method: 'put'
    })
  },

  completeConsult(id) {
    return request({
      url: `/creator/consult/${id}/complete`,
      method: 'put'
    })
  },
}