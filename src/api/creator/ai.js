import request from '@/utils/request'

export const creatorAiApi = {
  saveAiTranscript(audioId) {
    return request({ url: `/creator/ai/transcript/${audioId}`, method: 'post' })
  },
  saveAiNote(audioId) {
    return request({ url: `/creator/ai/note/${audioId}`, method: 'post' })
  },
  aiGenerateSlots(description) {
    return request({ url: '/creator/ai/slots', method: 'post', data: description })
  },
}
