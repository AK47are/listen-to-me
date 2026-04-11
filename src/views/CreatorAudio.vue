<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Loading, Headset, Star, Plus } from '@element-plus/icons-vue'
import { creatorAudioApi } from '@/api/creator/audio'

const audioList = ref([])
const loading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})

const dialogVisible = ref(false)
const dialogTitle = ref('编辑稿件')
const submitting = ref(false)
const uploadingCover = ref(false)
const uploadingAudio = ref(false)

const audioForm = reactive({
  id: null,
  title: '',
  coverUrl: '',
  description: '',
  trialDuration: 30,
  isPaid: false,
  price: 0,
  visibility: 'PUBLIC',
})

const getAudioList = async () => {
  loading.value = true
  try {
    const res = await creatorAudioApi.getAudioPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    audioList.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取稿件列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getAudioList()
}

const handleDeleteAudio = async (audio) => {
  try {
    await ElMessageBox.confirm('确定要删除该稿件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const index = audioList.value.findIndex((item) => item.id === audio.id)
    if (index !== -1) {
      audioList.value.splice(index, 1)
    }

    await creatorAudioApi.deleteAudio(audio.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      await getAudioList()
    }
  }
}

const handleEditAudio = async (audio) => {
  dialogTitle.value = '编辑稿件'
  try {
    const res = await creatorAudioApi.getAudioDetail(audio.id)
    const data = res.data
    audioForm.id = data.id
    audioForm.title = data.title || ''
    audioForm.coverUrl = data.coverUrl || ''
    audioForm.description = data.description || ''
    audioForm.trialDuration = data.trialDuration ?? 30
    audioForm.isPaid = data.isPaid ?? false
    audioForm.price = data.price ?? 0
    audioForm.visibility = data.visibility || 'PUBLIC'
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取稿件详情失败')
  }
}

const beforeUploadCover = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 图片')
    return false
  }
  if (!isLt20M) {
    ElMessage.error('图片大小不能超过 20MB')
    return false
  }
  return true
}

const handleCoverUpload = async (options) => {
  const file = options.file
  if (!file) {
    ElMessage.error('请选择文件')
    return
  }
  uploadingCover.value = true
  try {
    const res = await creatorAudioApi.uploadCover(file)
    audioForm.coverUrl = res.data
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '封面上传失败')
  } finally {
    uploadingCover.value = false
  }
}

const beforeUploadAudio = (file) => {
  const isMp3 = file.type === 'audio/mpeg'
  const isLt200M = file.size / 1024 / 1024 < 200

  if (!isMp3) {
    ElMessage.error('只能上传 MP3 格式')
    return false
  }
  if (!isLt200M) {
    ElMessage.error('文件大小不能超过 200MB')
    return false
  }
  return true
}

const handleAudioUpload = async (options) => {
  const file = options.file
  if (!file) {
    ElMessage.error('请选择文件')
    return
  }
  uploadingAudio.value = true
  try {
    const res = await creatorAudioApi.uploadAudio(file)
    audioForm.audioUrl = res.data
    ElMessage.success('音频上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '音频上传失败')
  } finally {
    uploadingAudio.value = false
  }
}

const handleSubmit = async () => {
  if (!audioForm.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  if (!audioForm.coverUrl) {
    ElMessage.warning('请上传封面')
    return
  }

  if (audioForm.isPaid && (!audioForm.price || audioForm.price <= 0)) {
    ElMessage.warning('付费音频请设置价格')
    return
  }

  submitting.value = true
  try {
    await creatorAudioApi.updateAudio({
      id: audioForm.id,
      title: audioForm.title,
      coverUrl: audioForm.coverUrl,
      description: audioForm.description,
      trialDuration: audioForm.trialDuration,
      isPaid: audioForm.isPaid,
      price: audioForm.price,
      visibility: audioForm.visibility,
    })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await getAudioList()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleDialogClose = () => {
  audioForm.id = null
  audioForm.title = ''
  audioForm.coverUrl = ''
  audioForm.description = ''
  audioForm.trialDuration = 30
  audioForm.isPaid = false
  audioForm.price = 0
  audioForm.visibility = 'PUBLIC'
  audioForm.audioUrl = ''
}

const formatDuration = (seconds) => {
  if (!seconds) return '0 分钟'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return secs > 0 ? `${mins} 分 ${secs} 秒` : `${mins} 分钟`
}

const getStatusConfig = (status) => {
  const statusMap = {
    PENDING_TRANSCODE: { text: '转码中', class: 'status-pending' },
    TRANSCODING: { text: '转码中', class: 'status-pending' },
    ONLINE: { text: '已发布', class: 'status-approved' },
    FAILED: { text: '失败', class: 'status-rejected' },
  }
  return statusMap[status] || { text: '处理中', class: 'status-pending' }
}

onMounted(() => {
  getAudioList()
})
</script>

<template>
  <div class="my-audio-page">
    <div class="page-header">
      <div class="header-text">
        <h1 class="page-title">我的稿件</h1>
        <p class="page-subtitle">管理你的音频作品</p>
      </div>
    </div>

    <div class="page-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载稿件列表中...</span>
      </div>

      <div v-else-if="audioList.length === 0" class="empty-state">
        <div class="empty-icon">🎵</div>
        <h3>暂无稿件</h3>
        <p>点击右上角「发布新稿件」，开始你的创作之旅</p>
      </div>

      <div v-else class="audio-list">
        <div v-for="audio in audioList" :key="audio.id" class="audio-card">
          <div class="card-cover">
            <img :src="audio.coverUrl" :alt="audio.title" />
          </div>

          <div class="card-info">
            <div class="info-header">
              <h3 class="audio-title">{{ audio.title }}</h3>
              <span :class="['status-badge', getStatusConfig(audio.status).class]">
                {{ getStatusConfig(audio.status).text }}
              </span>
            </div>

            <div class="info-meta">
              <div class="meta-stats">
                <div class="stat-item">
                  <el-icon><Headset /></el-icon>
                  <span>{{ audio.playCount || 0 }} 次播放</span>
                </div>
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ audio.collectCount || 0 }} 收藏</span>
                </div>
              </div>
            </div>

            <div v-if="audio.rejectReason" class="reject-reason">
              <span>{{ audio.rejectReason }}</span>
            </div>

            <div class="visibility-tag">
              <span v-if="audio.visibility === 'PRIVATE'" class="private-badge">私密</span>
              <span v-else class="public-badge">公开</span>
            </div>
          </div>

          <div class="card-actions">
            <button class="action-btn edit-btn" @click="handleEditAudio(audio)">
              <el-icon><Edit /></el-icon>
              <span>编辑</span>
            </button>
            <button class="action-btn delete-btn" @click="handleDeleteAudio(audio)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </button>
          </div>
        </div>
      </div>

      <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form :model="audioForm" label-width="100px" class="dialog-form">
        <el-form-item label="标题" required>
          <el-input
            v-model="audioForm.title"
            placeholder="请输入标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面" required>
          <div class="upload-area">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUploadCover"
              :http-request="handleCoverUpload"
              accept="image/*"
              class="cover-upload"
            >
              <div v-if="audioForm.coverUrl" class="cover-preview">
                <img :src="audioForm.coverUrl" alt="封面" />
                <div class="cover-mask">
                  <el-icon><Edit /></el-icon>
                  <span>更换封面</span>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
            <div v-if="uploadingCover" class="uploading-status">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="简介">
          <el-input
            v-model="audioForm.description"
            type="textarea"
            placeholder="请输入简介"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="试听时长">
          <div class="trial-setting">
            <el-input-number v-model="audioForm.trialDuration" :min="0" :max="300" :step="10" />
            <span class="hint">秒（0 表示无试听）</span>
          </div>
        </el-form-item>

        <el-form-item label="付费设置">
          <div class="switch-wrapper">
            <el-switch v-model="audioForm.isPaid" />
            <span class="hint">开启后用户需付费收听完整版</span>
          </div>
        </el-form-item>

        <el-form-item v-if="audioForm.isPaid" label="价格" required>
          <div class="price-setting">
            <el-input-number
              v-model="audioForm.price"
              :min="0.01"
              :max="9999"
              :precision="2"
              :step="0.01"
            />
            <span class="hint">元（虚拟币）</span>
          </div>
        </el-form-item>

        <el-form-item label="可见性">
          <div class="radio-wrapper">
            <el-radio-group v-model="audioForm.visibility">
              <el-radio value="PUBLIC">公开</el-radio>
              <el-radio value="PRIVATE">私密</el-radio>
            </el-radio-group>
            <span class="hint">私密稿件仅自己可见</span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="cancel-btn" @click="dialogVisible = false">取消</el-button>
          <el-button class="submit-btn" :loading="submitting" @click="handleSubmit">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my-audio-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 1100px;
  margin: 0 auto 32px;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.header-text {
  text-align: left;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 0.95rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.page-content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 30px;
}

.loading-state,
.empty-state {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
  padding: 80px 20px;
  text-align: center;
  color: #8e8c84;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-state h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.5rem;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.empty-state p {
  margin: 0 0 24px 0;
}

.audio-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.audio-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 24px;
  padding: 24px;
  display: flex;
  gap: 24px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.audio-card:hover {
  border-color: #dcdbd3;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.card-cover {
  position: relative;
  width: 160px;
  height: 160px;
  flex-shrink: 0;
  border-radius: 16px;
  overflow: hidden;
  background: #f0efe9;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.info-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.audio-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.3rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  line-height: 1.4;
  flex: 1;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.3px;
  background: #f0efe9;
  color: #5a5953;
  white-space: nowrap;
}

.status-approved {
  background: #e8f3e8;
  color: #2e6b2e;
}

.status-rejected {
  background: #fdeded;
  color: #c74242;
}

.status-pending {
  background: #fff3e0;
  color: #b85e00;
}

.info-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.meta-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: #7a7972;
}

.stat-item .el-icon {
  font-size: 16px;
  color: #a09f97;
}

.reject-reason {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef8f8;
  border-radius: 12px;
  font-size: 0.85rem;
  color: #c74242;
  border-left: 3px solid #c74242;
}

.visibility-tag {
  margin-top: 10px;
}

.private-badge,
.public-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 30px;
  font-size: 0.75rem;
  font-weight: 500;
}

.private-badge {
  background: #f0efe9;
  color: #5a5953;
}

.public-badge {
  background: #e8f3e8;
  color: #2e6b2e;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
  padding-left: 8px;
  border-left: 1px solid #efeee8;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 40px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  white-space: nowrap;
}

.edit-btn {
  color: #1a1a1a;
  border: 1px solid #e8e6df;
}

.edit-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.delete-btn {
  color: #c74242;
  border: 1px solid #e8e6df;
}

.delete-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 12px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: #ffffff !important;
  border-radius: 40px !important;
  border: 1px solid #efeee8 !important;
}

.dialog-form {
  padding: 10px 0;
}

.dialog-form :deep(.el-form-item__label) {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a1a1a;
}

.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-textarea__inner) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 16px;
  box-shadow: none;
}

.dialog-form :deep(.el-input__wrapper:hover),
.dialog-form :deep(.el-textarea__inner:hover) {
  border-color: #dcdbd3;
}

.dialog-form :deep(.el-input__wrapper.is-focus),
.dialog-form :deep(.el-textarea__inner:focus) {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 2px rgba(26, 26, 26, 0.05);
}

.upload-area {
  width: 100%;
}

.cover-upload {
  display: inline-block;
}

.cover-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.cover-preview:hover .cover-mask {
  opacity: 1;
}

.cover-mask .el-icon {
  font-size: 18px;
}

.cover-mask span {
  font-size: 0.75rem;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #e8e6df;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #b0aea3;
  cursor: pointer;
  transition: all 0.2s;
  background: #fcfbf7;
}

.upload-placeholder:hover {
  border-color: #1a1a1a;
  color: #1a1a1a;
}

.upload-placeholder .el-icon {
  font-size: 24px;
}

.upload-placeholder span {
  font-size: 0.8rem;
}

.uploading-status {
  margin-top: 12px;
  font-size: 0.85rem;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.trial-setting,
.price-setting {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.switch-wrapper,
.radio-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.hint {
  font-size: 0.85rem;
  color: #8e8c84;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #1a1a1a;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.submit-btn {
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #ffffff;
  transition: all 0.2s;
}

.submit-btn:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .audio-card {
    flex-direction: column;
  }

  .card-cover {
    width: 100%;
    height: 200px;
  }

  .card-actions {
    flex-direction: row;
    border-left: none;
    border-top: 1px solid #efeee8;
    padding-top: 16px;
    padding-left: 0;
    margin-top: 8px;
  }

  .action-btn {
    flex: 1;
  }

  .info-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
