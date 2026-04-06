import request from '@/utils/request'

export const aiApi = {
  getAiTask(taskId) {
    return request({ url: `/common/ai/task/${taskId}`, method: 'get' })
  },
}
