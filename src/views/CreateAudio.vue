<template>
  <div class="create-audio-page">
    <div class="page-header">
      <h1 class="page-title">发布新稿件</h1>
      <p class="page-subtitle">上传音频内容，让更多人听见</p>
    </div>

    <div class="create-content">
      <el-form :model="audioForm" label-width="100px" class="create-form">
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
              :before-upload="beforeUpload"
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
                <p class="upload-hint">建议尺寸 1:1，不超过 20MB</p>
              </div>
            </el-upload>
            <div v-if="uploadingCover" class="uploading-status">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="音频文件" required>
          <div class="upload-area">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUploadAudio"
              :http-request="handleAudioUpload"
              accept="audio/mpeg"
              class="audio-upload"
            >
              <el-button :loading="uploadingAudio" size="large">
                <el-icon><Upload /></el-icon>
                {{ uploadingAudio ? '上传中...' : '选择音频文件' }}
              </el-button>
            </el-upload>
            <div v-if="audioForm.audioUrl" class="audio-info">
              <el-icon><SuccessFilled /></el-icon>
              <span>音频已上传，格式 MP3，大小不超过 200MB</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="简介">
          <el-input
            v-model="audioForm.description"
            type="textarea"
            placeholder="请输入简介"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="试听时长">
          <div class="trial-setting">
            <el-input-number v-model="audioForm.trialDuration" :min="0" :max="300" :step="10" />
            <span class="hint">秒（0 表示无试听，付费音频建议设置 30-60 秒）</span>
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

        <el-form-item>
          <div class="form-actions">
            <el-button class="cancel-btn" @click="handleCancel">取消</el-button>
            <el-button class="submit-btn" :loading="submitting" @click="handleSubmit">
              发布稿件
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Edit, Loading, SuccessFilled } from '@element-plus/icons-vue'
import { creatorAudioApi } from '@/api/creator/audio'

const router = useRouter()

const audioForm = reactive({
  title: '',
  coverUrl: '',
  description: '',
  audioUrl: '',
  trialDuration: 30,
  isPaid: false,
  price: 0,
  visibility: 'PUBLIC',
})

const uploadingAudio = ref(false)
const uploadingCover = ref(false)
const submitting = ref(false)

const beforeUpload = (file) => {
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

const handleCoverUpload = async (options) => {
  const file = options.file
  if (!file) {
    ElMessage.error('请选择文件')
    return
  }
  uploadingCover.value = true
  try {
    const res = await creatorAudioApi.uploadCover(file)
    const tempUrl = res.data
    audioForm.coverUrl = tempUrl
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error(error.response?.data?.msg || '封面上传失败')
  } finally {
    uploadingCover.value = false
  }
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
    const tempUrl = res.data
    audioForm.audioUrl = tempUrl
    ElMessage.success('音频上传成功')
  } catch (error) {
    console.error('音频上传失败:', error)
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

  if (!audioForm.audioUrl) {
    ElMessage.warning('请上传音频文件')
    return
  }

  if (audioForm.isPaid && (!audioForm.price || audioForm.price <= 0)) {
    ElMessage.warning('付费音频请设置价格')
    return
  }

  submitting.value = true
  try {
    await creatorAudioApi.saveAudio(audioForm)
    ElMessage.success('发布成功，稿件正在处理中')
    router.push('/creator/audio')
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(error.response?.data?.msg || '发布失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.create-audio-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 900px;
  margin: 0 auto 32px;
  padding: 0 30px;
  text-align: center;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 0.95rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.create-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 30px;
}

.create-form {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
  padding: 32px 36px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.create-form :deep(.el-form-item__label) {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a1a1a;
}

.create-form :deep(.el-input__wrapper),
.create-form :deep(.el-textarea__inner) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 16px;
  box-shadow: none;
}

.create-form :deep(.el-input__wrapper:hover),
.create-form :deep(.el-textarea__inner:hover) {
  border-color: #dcdbd3;
}

.create-form :deep(.el-input__wrapper.is-focus),
.create-form :deep(.el-textarea__inner:focus) {
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
  width: 160px;
  height: 160px;
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
  font-size: 22px;
}

.cover-mask span {
  font-size: 0.8rem;
}

.upload-placeholder {
  width: 160px;
  height: 160px;
  border: 2px dashed #e8e6df;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
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
  font-size: 32px;
}

.upload-placeholder span {
  font-size: 0.85rem;
}

.upload-hint {
  font-size: 0.7rem;
  margin: 0;
  color: #c4c2ba;
}

.uploading-status {
  margin-top: 12px;
  font-size: 0.85rem;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.audio-upload {
  display: inline-block;
}

.audio-upload .el-button {
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 10px 28px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #ffffff;
  transition: all 0.2s;
}

.audio-upload .el-button:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.audio-info {
  margin-top: 14px;
  font-size: 0.85rem;
  color: #67c23a;
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px;
}

.cancel-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  padding: 10px 28px;
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
  padding: 10px 28px;
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
  .create-content {
    padding: 0 20px;
  }
  .create-form {
    padding: 24px;
  }
  .cover-preview,
  .upload-placeholder {
    width: 120px;
    height: 120px;
  }
  .trial-setting,
  .price-setting,
  .switch-wrapper,
  .radio-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
