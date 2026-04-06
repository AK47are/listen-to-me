import request from '@/utils/request'

export const balanceApi = {
  getBalance() {
    return request({ url: '/user/balance', method: 'get' })
  },
  recharge(data) {
    return request({ url: '/user/recharge', method: 'post', data })
  },
}
