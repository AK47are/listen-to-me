<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="ai-manage-content">
      <div class="status-section" v-if="!result && !loading">
        <el-icon class="hint-icon"><Document /></el-icon>
        <p>点击下方按钮生成 AI {{ typeName }}</p>
      </div>

      <div class="loading-section" v-if="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>{{ loadingText }}</p>
      </div>

      <div class="result-section" v-if="result && !loading">
        <div class="result-header">
          <span class="result-label">生成结果</span>
          <el-tag v-if="!confirmed" type="warning" size="small">待确认</el-tag>
          <el-tag v-else type="success" size="small">已确认</el-tag>
        </div>
        <div class="result-content">{{ result }}</div>
        <p class="result-hint">结果由 AI 生成，仅供参考</p>
      </div>

      <div class="error-section" v-if="error">
        <el-icon color="#f56c6c"><CircleClose /></el-icon>
        <p>{{ error }}</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button v-if="!result" class="generate-btn" :loading="loading" @click="handleGenerate">
          <el-icon><MagicStick /></el-icon>
          生成{{ typeName }}
        </el-button>
        <template v-else>
          <el-button class="regenerate-btn" :loading="loading" @click="handleGenerate">
            <el-icon><Refresh /></el-icon>
            重新生成
          </el-button>
          <el-button
            v-if="!confirmed"
            class="confirm-btn"
            :loading="confirming"
            @click="handleConfirm"
          >
            确认并保存
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, MagicStick, Refresh, Loading, CircleClose } from '@element-plus/icons-vue'
import { creatorAiApi } from '@/api/creator/ai'
import { aiApi } from '@/api/common/ai'

const props = defineProps({
  audioId: {
    type: Number,
    required: true,
  },
  type: {
    type: String,
    required: true,
  },
  audioTitle: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const confirming = ref(false)
const result = ref('')
const error = ref('')
const confirmed = ref(false)
const currentTaskId = ref('')
let pollingTimer = null
let isPollingActive = false

const savedResult = ref('')
const savedTaskId = ref('')

const typeName = computed(() => (props.type === 'transcript' ? '转写' : '摘要'))
const dialogTitle = computed(() => `AI ${typeName.value} - ${props.audioTitle || '未命名稿件'}`)
const loadingText = computed(() => `正在生成${typeName.value}，请稍候...`)

const stopPolling = () => {
  isPollingActive = false
  if (pollingTimer) {
    clearTimeout(pollingTimer)
    pollingTimer = null
  }
}

const startPolling = (taskId) => {
  stopPolling()
  isPollingActive = true

  let attempts = 0
  const maxAttempts = 30

  const poll = async () => {
    if (!isPollingActive) {
      return
    }

    if (attempts >= maxAttempts) {
      stopPolling()
      loading.value = false
      error.value = '处理超时，请稍后重试'
      ElMessage.warning(`「${props.audioTitle || '稿件'}」的 AI ${typeName.value}处理超时`)
      return
    }

    try {
      const res = await aiApi.getAiTask(taskId)
      const task = res.data

      if (task.status === 'SUCCESS') {
        stopPolling()
        let extractedResult = ''

        const rawResult = task.result || task.transcript || task.summary || task

        if (props.type === 'transcript') {
          let data = rawResult
          if (typeof rawResult === 'string') {
            try {
              data = JSON.parse(rawResult)
            } catch (e) {
              extractedResult = rawResult
            }
          }

          if (typeof data === 'object' && data !== null) {
            if (data.transcripts && data.transcripts.length > 0) {
              extractedResult = data.transcripts[0].text || ''
            } else if (data.text) {
              extractedResult = data.text
            } else {
              extractedResult = JSON.stringify(data)
            }
          }
        } else {
          extractedResult =
            typeof rawResult === 'string'
              ? rawResult
              : rawResult.summary || rawResult.text || JSON.stringify(rawResult)
        }

        savedResult.value = extractedResult
        savedTaskId.value = taskId

        if (visible.value) {
          result.value = extractedResult
          loading.value = false
          confirmed.value = false
        }
        ElMessage.success(`「${props.audioTitle || '稿件'}」的 AI ${typeName.value}生成成功`)
        return
      }

      if (task.status === 'FAILED') {
        stopPolling()
        const failMsg = task.failReason || '生成失败'
        if (visible.value) {
          loading.value = false
          error.value = failMsg
        }
        ElMessage.error(`「${props.audioTitle || '稿件'}」的 AI ${typeName.value}失败：${failMsg}`)
        return
      }

      attempts++
      if (isPollingActive) {
        pollingTimer = setTimeout(poll, 10000)
      }
    } catch (err) {
      console.error('轮询失败', err)
      attempts++
      if (isPollingActive) {
        pollingTimer = setTimeout(poll, 10000)
      }
    }
  }

  poll()
}

const open = () => {
  visible.value = true

  if (savedResult.value) {
    result.value = savedResult.value
    currentTaskId.value = savedTaskId.value
    loading.value = false
    error.value = ''
    confirmed.value = false
  } else {
    result.value = ''
    error.value = ''
    confirmed.value = false
    loading.value = false
  }
}

const handleGenerate = async () => {
  stopPolling()
  loading.value = true
  error.value = ''
  result.value = ''
  confirmed.value = false

  savedResult.value = ''
  savedTaskId.value = ''

  try {
    const apiMethod =
      props.type === 'transcript' ? creatorAiApi.saveAiTranscript : creatorAiApi.saveAiNote

    const res = await apiMethod(props.audioId)
    const taskId = res.data.taskId
    currentTaskId.value = taskId

    startPolling(taskId)
  } catch (err) {
    loading.value = false
    error.value = err.response?.data?.msg || '提交失败'
    ElMessage.error(err.response?.data?.msg || '提交失败')
  }
}

const handleConfirm = async () => {
  const taskId = currentTaskId.value || savedTaskId.value

  if (!taskId) {
    ElMessage.warning('没有可确认的任务')
    return
  }

  confirming.value = true
  try {
    const confirmMethod =
      props.type === 'transcript' ? creatorAiApi.confirmTranscript : creatorAiApi.confirmSummary

    const data =
      props.type === 'transcript' ? { transcript: result.value } : { summary: result.value }

    await confirmMethod(taskId, data)

    savedResult.value = ''
    savedTaskId.value = ''
    confirmed.value = true
    ElMessage.success(`「${props.audioTitle || '稿件'}」的${typeName.value}结果已保存`)
    emit('success')
    visible.value = false
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '确认失败')
  } finally {
    confirming.value = false
  }
}

onUnmounted(() => {
  stopPolling()
})

defineExpose({ open })
</script>

<style scoped>
.ai-manage-content {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.status-section,
.loading-section,
.error-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #8e8c84;
}

.hint-icon {
  font-size: 48px;
  color: #e0d8ec;
}

.loading-section .el-icon {
  font-size: 32px;
  color: #7c6a9c;
}

.result-section {
  width: 100%;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.result-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a1a1a;
}

.result-content {
  background: #fcfbf7;
  border: 1px solid #efeee8;
  border-radius: 16px;
  padding: 20px;
  font-size: 0.95rem;
  line-height: 1.8;
  color: #4a4a44;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.result-hint {
  margin-top: 8px;
  font-size: 0.75rem;
  color: #b0aea3;
  text-align: right;
}

.error-section {
  color: #f56c6c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.dialog-footer .el-button:first-child {
  background: transparent;
  border: 1px solid #e8e6df;
  color: #1a1a1a;
}

.dialog-footer .el-button:first-child:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.generate-btn,
.regenerate-btn {
  background: #f5f0fa !important;
  border: 1px solid #e0d8ec !important;
  color: #7c6a9c !important;
}

.generate-btn:hover,
.regenerate-btn:hover {
  background: #e8ddf5 !important;
  border-color: #c8b8dc !important;
  color: #5a4a72 !important;
}

.confirm-btn {
  background: #1a1a1a !important;
  border: none !important;
  color: #ffffff !important;
}

.confirm-btn:hover {
  background: #2c2c2c !important;
  transform: translateY(-1px);
}
</style>
