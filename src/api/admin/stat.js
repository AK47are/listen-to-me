import request from '@/utils/request'

export const statApi = {
  getDashboard(params) {
    return request({ url: '/admin/stat/dashboard', method: 'get', params })
  },
}
