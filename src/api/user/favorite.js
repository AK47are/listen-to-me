import request from '@/utils/request'

export const favoriteApi = {
  saveAudioAction(data) {
    return request({ url: '/user/audio/action', method: 'post', data })
  },
  saveFavoriteFolder(data) {
    return request({ url: '/user/favorite/folder', method: 'post', data })
  },
  getFolderList() {
    return request({ url: '/user/favorite/folder/list', method: 'get' })
  },
  getFavoritePage(params) {
    return request({ url: '/user/favorite/page', method: 'get', params })
  },
  deleteFavoriteFolder(folderId) {
    return request({ url: `/user/favorite/folder/${folderId}`, method: 'delete' })
  },
  getAudioFolders(audioId) {
    return request({ url: `/user/audio/${audioId}/folders`, method: 'get' })
  },
}
