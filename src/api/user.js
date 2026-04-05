import request from '@/utils/request'

export const authApi = {
  login(data) {
    return request({
      url: '/common/auth/login',
      method: 'post',
      data,
    })
  },
  getImageCaptcha() {
    return request({
      url: '/common/auth/image-captcha',
      method: 'get',
    })
  },

  sendVerifyCode(data) {
    return request({
      url: '/common/auth/verify-code',
      method: 'post',
      data,
    })
  },

  register(data) {
    return request({
      url: '/common/auth/register',
      method: 'post',
      data,
    })
  },

  refreshToken() {
    return request({
      url: '/common/auth/refresh',
      method: 'post',
    })
  },
}

export const userApi = {
  getProfile() {
    return request({
      url: '/user/profile',
      method: 'get',
    })
  },

  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'put',
      data,
    })
  },

  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('coverFile', file)
    return request({
      url: '/creator/audio/cover/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  applyCreator(data) {
    return request({
      url: '/user/creator/apply',
      method: 'post',
      data,
    })
  },

  getCreatorApplyStatus() {
    return request({
      url: '/user/creator/apply/status',
      method: 'get',
    })
  },

  // 关注功能
  followCreator(creatorId) {
    return request({
      url: `/user/creator/${creatorId}/follow`,
      method: 'post',
    })
  },

  unfollowCreator(creatorId) {
    return request({
      url: `/user/creator/${creatorId}/follow`,
      method: 'delete',
    })
  },

  getFollowPage(params) {
    return request({
      url: '/user/creator/follow/page',
      method: 'get',
      params,
    })
  },

  getFansPage(creatorId, params) {
    return request({
      url: `/user/creator/${creatorId}/fans/page`,
      method: 'get',
      params,
    })
  },
}

