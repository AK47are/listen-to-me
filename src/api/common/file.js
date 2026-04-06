import request from '@/utils/request'

export const fileApi = {
  getStreamSign(audioId) {
    return request({ url: '/common/file/sign', method: 'get', params: { audioId } })
  },
}
