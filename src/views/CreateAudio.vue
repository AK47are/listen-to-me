<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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

const handleAudioUpload = async (file) => {
  uploadingAudio.value = true
  try {
    const res = await creatorAudioApi.uploadAudio(file.raw)
    audioForm.audioUrl = res
    ElMessage.success('音频上传成功')
  } catch (error) {
    ElMessage.error('音频上传失败')
  } finally {
    uploadingAudio.value = false
  }
}

const handleCoverUpload = async (file) => {
  uploadingCover.value = true
  try {
    const res = await creatorAudioApi.uploadCover(file.raw)
    audioForm.coverUrl = res
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败')
  } finally {
    uploadingCover.value = false
  }
}

const beforeUpload = (file) => {
  const isValidSize = file.size / 1024 / 1024 < 500
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 500MB')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!audioForm.title) {
    ElMessage.warning('请输入标题')
    return
  }

  if (!audioForm.coverUrl) {
    ElMessage.warning('请上传封面')
    return
  }

  if (!audioForm.audioUrl) {
    ElMessage.warning('请上传音频')
    return
  }

  if (audioForm.isPaid && (!audioForm.price || audioForm.price <= 0)) {
    ElMessage.warning('请设置价格')
    return
  }

  submitting.value = true
  try {
    const res = await creatorAudioApi.saveAudio(audioForm)
    ElMessage.success('发布成功，稿件正在处理中')
    router.push('/creator/audio')
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>

<template>
  <div class="create-audio-container">
    <div class="header">
      <h2>发布新稿件</h2>
    </div>

    <div class="content">
      <el-form :model="audioForm" label-width="120px">
        <el-form-item label="标题" required>
          <el-input
            v-model="audioForm.title"
            placeholder="请输入标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面" required>
          <el-upload
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleCoverUpload"
            accept="image/*"
          >
            <div v-if="audioForm.coverUrl" class="cover-preview">
              <img :src="audioForm.coverUrl" alt="封面" />
              <div class="cover-mask">
                <el-icon><Plus /></el-icon>
                <span>更换封面</span>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传封面</span>
            </div>
          </el-upload>
          <div v-if="uploadingCover" class="uploading">
            <el-icon class="is-loading"><Loading /></el-icon>
            上传中...
          </div>
        </el-form-item>

        <el-form-item label="音频文件" required>
          <el-upload
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleAudioUpload"
            accept="audio/*"
          >
            <el-button :loading="uploadingAudio">
              <el-icon><Upload /></el-icon>
              {{ uploadingAudio ? '上传中...' : '选择音频文件' }}
            </el-button>
          </el-upload>
          <div v-if="audioForm.audioUrl" class="audio-info">
            <el-icon><Check /></el-icon>
            音频已上传
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

        <el-form-item label="试听设置">
          <el-input-number v-model="audioForm.trialDuration" :min="0" :max="300" :step="10" />
          <span style="margin-left: 10px; color: #999">秒（0表示无试听）</span>
        </el-form-item>

        <el-form-item label="付费设置">
          <el-switch v-model="audioForm.isPaid" />
          <span style="margin-left: 10px; color: #999">开启付费</span>
        </el-form-item>

        <el-form-item v-if="audioForm.isPaid" label="价格" required>
          <el-input-number
            v-model="audioForm.price"
            :min="0.01"
            :max="9999"
            :precision="2"
            :step="0.01"
          />
          <span style="margin-left: 10px; color: #999">元</span>
        </el-form-item>

        <el-form-item label="可见性">
          <el-radio-group v-model="audioForm.visibility">
            <el-radio label="PUBLIC">公开</el-radio>
            <el-radio label="PRIVATE">私密</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit"> 发布 </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.create-audio-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 30px;
}

.content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cover-preview {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 8px;
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
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-mask {
  opacity: 1;
}

.cover-mask .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.upload-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #667eea;
  color: #667eea;
}

.upload-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.uploading {
  margin-top: 10px;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 5px;
}

.audio-info {
  margin-top: 10px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
