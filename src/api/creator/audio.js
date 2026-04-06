import request from '@/utils/request'

export const creatorAudioApi = {
  uploadAudio(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/creator/audio/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  uploadCover(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/creator/audio/cover/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  saveAudio(data) {
    return request({ url: '/creator/audio', method: 'post', data })
  },
  getAudioPage(params) {
    return request({ url: '/creator/audio/page', method: 'get', params })
  },
  getAudioDetail(id) {
    return request({ url: `/creator/audio/${id}`, method: 'get' })
  },
  getAudioStatus(id) {
    return request({ url: `/creator/audio/${id}/status`, method: 'get' })
  },
  updateAudio(data) {
    return request({ url: '/creator/audio', method: 'put', data })
  },
  deleteAudio(id) {
    return request({ url: `/creator/audio/${id}`, method: 'delete' })
  },
}
