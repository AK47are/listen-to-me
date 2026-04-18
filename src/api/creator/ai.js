import request from '@/utils/request'

export const creatorAiApi = {
  // 申请 AI 转写
  saveAiTranscript(audioId) {
    return request({
      url: `/creator/ai/transcript/${audioId}`,
      method: 'post',
    })
  },
  // 确认转写结果
  confirmTranscript(taskId, data) {
    return request({
      url: `/creator/ai/transcript/${taskId}/confirm`,
      method: 'put',
      data,
    })
  },
  // AI 智能排期
  aiGenerateSlots(description) {
    return request({
      url: '/creator/ai/slots',
      method: 'post',
      data: description,
    })
  },
  // 申请 AI 摘要
  saveAiNote(audioId) {
    return request({
      url: `/creator/ai/note/${audioId}`,
      method: 'post',
    })
  },
  // 确认摘要结果
  confirmSummary(taskId, data) {
    return request({
      url: `/creator/ai/note/${taskId}/confirm`,
      method: 'put',
      data,
    })
  },
}
