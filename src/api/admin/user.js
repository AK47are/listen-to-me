import request from '@/utils/request'

export const adminUserApi = {
  getUserPage(params) {
    return request({ url: '/admin/user/page', method: 'get', params })
  },
  banUser(userId) {
    return request({ url: `/admin/user/${userId}/ban`, method: 'put' })
  },
  unbanUser(userId) {
    return request({ url: `/admin/user/${userId}/unban`, method: 'put' })
  },
}
