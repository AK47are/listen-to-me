<template>
  <div class="audio-audit">
    <div class="audit-header">
      <h3 class="audit-title">音频审核</h3>
      <span class="audit-count">共 {{ total }} 条待审音频</span>
    </div>

    <div class="table-wrapper">
      <table class="audit-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>封面</th>
            <th>时长</th>
            <th>创作者</th>
            <th>提交时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="audio in records" :key="audio.id">
            <td class="id-cell">{{ audio.id }}</td>
            <td class="title-cell" @click="openAudioDetail(audio)">{{ audio.title }}</td>
            <td>
              <img
                :src="audio.coverUrl"
                class="cover-thumb"
                @click="openAudioDetail(audio)"
                alt="封面"
              />
            </td>
            <td>{{ formatDuration(audio.duration) }}</td>
            <td>{{ audio.creatorName }}</td>
            <td>{{ formatDate(audio.submitTime) }}</td>
            <td class="actions-cell">
              <button class="action-btn approve-btn" @click="handleApprove(audio)">通过</button>
              <button class="action-btn reject-btn" @click="handleReject(audio)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="records.length === 0" class="empty-state">暂无待审核音频</div>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 音频详情弹窗 -->
    <el-dialog
      v-model="showAudioDetail"
      :title="selectedAudio?.title || '音频详情'"
      width="700px"
      :close-on-click-modal="false"
      class="audio-detail-dialog"
      @close="closeAudioDetail"
    >
      <div v-if="selectedAudio" class="detail-content">
        <div class="detail-header">
          <img :src="selectedAudio.coverUrl" class="detail-cover" alt="封面" />
          <div class="detail-meta">
            <div class="meta-item">
              <span class="label">创作者</span>
              <span class="value">{{ selectedAudio.creatorName }}</span>
            </div>
            <div class="meta-item">
              <span class="label">时长</span>
              <span class="value">{{ formatDuration(selectedAudio.duration) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">提交时间</span>
              <span class="value">{{ formatDate(selectedAudio.submitTime) }}</span>
            </div>
          </div>
        </div>

        <div v-if="selectedAudio.description" class="detail-section">
          <h4>简介</h4>
          <p>{{ selectedAudio.description }}</p>
        </div>

        <div class="detail-section">
          <h4>音频播放</h4>
          <audio ref="audioPlayer" controls class="audio-player">
            <source :src="audioUrl" type="audio/mpeg" />
            您的浏览器不支持音频播放
          </audio>
        </div>

        <div v-if="selectedAudio.transcript" class="detail-section">
          <h4>转写文本</h4>
          <div class="transcript-box">{{ selectedAudio.transcript }}</div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="closeAudioDetail">取消</button>
          <button class="dialog-btn approve-btn" @click="handleAuditInDialog('APPROVED')">
            通过
          </button>
          <button class="dialog-btn reject-btn" @click="handleAuditInDialog('REJECTED')">
            拒绝
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditApi } from '@/api/admin/audit'

const emit = defineEmits(['update:pendingCount'])

const records = ref([])
const total = ref(0)

const showAudioDetail = ref(false)
const selectedAudio = ref(null)
const audioUrl = ref('')
const audioPlayer = ref(null)

const pagination = ref({
  pageNum: 1,
  pageSize: 10,
})

const loadAuditAudioList = async () => {
  try {
    const response = await auditApi.getAudioAuditPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })

    if (response.records) {
      records.value = response.records
      total.value = response.total
    } else if (response.data && response.data.records) {
      records.value = response.data.records
      total.value = response.data.total
    }

    emit('update:pendingCount', total.value)
  } catch (error) {
    console.error('Failed to load audit audio list:', error)
    ElMessage.error('加载音频列表失败')
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  loadAuditAudioList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  loadAuditAudioList()
}

const handleApprove = async (audio) => {
  try {
    await ElMessageBox.confirm(`确定通过音频「${audio.title}」的审核吗？`, '审核通过', {
      confirmButtonText: '确定通过',
      cancelButtonText: '取消',
      type: 'success',
    })
    await auditApi.auditAudio({
      audioId: audio.id,
      status: 'APPROVED',
      rejectReason: null,
    })
    ElMessage.success('已通过审核')
    loadAuditAudioList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (audio) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '请填写拒绝原因',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '拒绝原因不能为空'
        }
        return true
      },
    })
    await auditApi.auditAudio({
      audioId: audio.id,
      status: 'REJECTED',
      rejectReason: reason,
    })
    ElMessage.success('已拒绝审核')
    loadAuditAudioList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const openAudioDetail = (audio) => {
  selectedAudio.value = audio
  audioUrl.value = audio.audioUrl || 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
  showAudioDetail.value = true
}

const closeAudioDetail = () => {
  if (audioPlayer.value) {
    audioPlayer.value.pause()
    audioPlayer.value.currentTime = 0
  }
  showAudioDetail.value = false
  selectedAudio.value = null
  audioUrl.value = ''
}

const handleAuditInDialog = async (status) => {
  if (!selectedAudio.value) return

  if (audioPlayer.value) {
    audioPlayer.value.pause()
  }

  if (status === 'APPROVED') {
    await handleApprove(selectedAudio.value)
  } else {
    await handleReject(selectedAudio.value)
  }
  closeAudioDetail()
}

const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

defineExpose({
  reload: loadAuditAudioList,
})

onMounted(() => {
  loadAuditAudioList()
})
</script>

<style scoped>
.audio-audit {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 24px;
}

.audit-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;
}

.audit-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.audit-count {
  font-size: 0.85rem;
  color: #8e8c84;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 16px;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}

.audit-table th {
  text-align: left;
  padding: 14px 12px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #8e8c84;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  border-bottom: 1px solid #efeee8;
}

.audit-table td {
  padding: 16px 12px;
  font-size: 0.9rem;
  color: #1a1a1a;
  border-bottom: 1px solid #f0efe9;
}

.audit-table tbody tr:hover {
  background: #fcfbf7;
}

.audit-table tbody tr:last-child td {
  border-bottom: none;
}

.id-cell {
  font-weight: 600;
  color: #5a5953;
}

.title-cell {
  font-weight: 500;
  color: #1a1a1a;
  cursor: pointer;
  text-decoration: underline;
  text-decoration-color: #dcdbd3;
  text-underline-offset: 2px;
}

.title-cell:hover {
  color: #000000;
  text-decoration-color: #1a1a1a;
}

.cover-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #efeee8;
  transition: all 0.2s;
}

.cover-thumb:hover {
  border-color: #dcdbd3;
  transform: scale(1.05);
}

.actions-cell {
  white-space: nowrap;
}

.action-btn {
  padding: 6px 16px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #1a1a1a;
  margin-right: 8px;
}

.action-btn:last-child {
  margin-right: 0;
}

.approve-btn:hover {
  background: #e8f3e8;
  border-color: #b3d9b3;
  color: #2e6b2e;
}

.reject-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
  color: #c74242;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #b0aea3;
  font-size: 0.9rem;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: #ffffff;
  --el-pagination-border-color: #efeee8;
  --el-pagination-border-radius: 40px;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 12px;
  border: 1px solid #efeee8;
  background: #ffffff;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

/* 弹窗样式 */
.audio-detail-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}

.audio-detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #efeee8;
  margin: 0;
}

.audio-detail-dialog :deep(.el-dialog__title) {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
}

.audio-detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.audio-detail-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #efeee8;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  gap: 20px;
}

.detail-cover {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid #efeee8;
}

.detail-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.meta-item .label {
  width: 70px;
  font-size: 0.85rem;
  color: #8e8c84;
}

.meta-item .value {
  font-size: 0.95rem;
  color: #1a1a1a;
  font-weight: 500;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-section h4 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.detail-section p {
  margin: 0;
  font-size: 0.95rem;
  color: #5a5953;
  line-height: 1.6;
}

.audio-player {
  width: 100%;
  height: 40px;
  border-radius: 20px;
  background: #fcfbf7;
}

.audio-player:focus {
  outline: none;
}

.transcript-box {
  padding: 16px;
  background: #fcfbf7;
  border-radius: 16px;
  font-size: 0.9rem;
  color: #5a5953;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-btn {
  padding: 10px 24px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #1a1a1a;
}

.dialog-btn.cancel-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.dialog-btn.approve-btn {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.dialog-btn.approve-btn:hover {
  background: #2c2c2c;
}

.dialog-btn.reject-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
  color: #c74242;
}

@media (max-width: 768px) {
  .audio-audit {
    padding: 16px;
  }

  .detail-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .meta-item {
    justify-content: center;
  }
}
</style>
