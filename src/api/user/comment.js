import request from '@/utils/request'

export const commentApi = {
  saveComment(data) {
    return request({ url: '/user/comment', method: 'post', data })
  },
  getCommentPage(params) {
    return request({ url: '/user/comment/page', method: 'get', params })
  },
  likeComment(data) {
    return request({ url: '/user/comment/like', method: 'post', data })
  },
}
