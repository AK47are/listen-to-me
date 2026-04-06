import request from '@/utils/request'

export const profileApi = {
  getProfile() {
    return request({ url: '/user/profile', method: 'get' })
  },
  updateProfile(data) {
    return request({ url: '/user/profile', method: 'put', data })
  },
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('coverFile', file)
    return request({
      url: '/creator/audio/cover/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
