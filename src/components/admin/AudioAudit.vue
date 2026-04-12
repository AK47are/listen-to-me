<template>
  <div class="admin-dashboard">
    <h1>音频审核</h1>
    
    <div class="audit-section">
      <table class="data-table">
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
          <tr v-for="audio in auditAudioList.records" :key="audio.id">
            <td>{{ audio.id }}</td>
            <td class="clickable-title" @click="openAudioDetail(audio)">{{ audio.title }}</td>
            <td><img :src="audio.coverUrl" class="cover-small clickable-cover" @click="openAudioDetail(audio)" alt="封面"></td>
            <td>{{ formatDuration(audio.duration) }}</td>
            <td>{{ audio.creatorName }}</td>
            <td>{{ formatDate(audio.submitTime) }}</td>
            <td>
              <button class="btn approve" @click="auditAudio(audio.id, 'APPROVED')">通过</button>
              <button class="btn reject" @click="auditAudio(audio.id, 'REJECTED')">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 音频详情弹窗 -->
    <div v-if="showAudioDetail" class="audio-detail-modal" @click="closeAudioDetail">
      <div class="audio-detail-content" @click.stop>
        <div class="audio-detail-header">
          <h3>{{ selectedAudio.title }}</h3>
          <button class="close-btn" @click="closeAudioDetail">&times;</button>
        </div>
        <div class="audio-detail-body">
          <div class="audio-info">
            <img :src="selectedAudio.coverUrl" class="audio-cover" alt="封面">
            <div class="audio-meta">
              <p><strong>创作者：</strong>{{ selectedAudio.creatorName }}</p>
              <p><strong>时长：</strong>{{ formatDuration(selectedAudio.duration) }}</p>
              <p><strong>提交时间：</strong>{{ formatDate(selectedAudio.submitTime) }}</p>
            </div>
          </div>

          <!-- 音频简介 -->
          <div class="description-section" v-if="selectedAudio.description">
            <h4>简介</h4>
            <div class="description-content">
              {{ selectedAudio.description }}
            </div>
          </div>
          
          <!-- 音频播放器 -->
          <div class="audio-player-section">
            <audio ref="audioPlayer" controls class="audio-player">
              <source :src="audioUrl" type="audio/mpeg">
              您的浏览器不支持音频播放
            </audio>
          </div>

          <!-- 转写文本 -->
          <div class="transcript-section" v-if="selectedAudio.transcript">
            <h4>音频转写文本</h4>
            <div class="transcript-content">
              {{ selectedAudio.transcript }}
            </div>
          </div>

          <!-- 审核操作 -->
          <div class="audit-actions">
            <button class="btn approve" @click="auditAudioAndClose(selectedAudio.id, 'APPROVED')">通过</button>
            <button class="btn reject" @click="auditAudioAndClose(selectedAudio.id, 'REJECTED')">拒绝</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="auditAudioList.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="auditAudioList.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import mockApi from '@/data/mockApi'

const auditAudioList = ref({
  records: [],
  total: 0
})
const showAudioDetail = ref(false)
const selectedAudio = ref(null)
const audioUrl = ref('')
const audioPlayer = ref(null)

const pagination = ref({
  pageNum: 1,
  pageSize: 10
})

const loadAuditAudioList = async () => {
  try {
    const response = await mockApi.admin.getAuditAudioPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    auditAudioList.value = response
  } catch (error) {
    console.error('Failed to load audit audio list:', error)
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

const auditAudio = async (audioId, status) => {
  try {
    await mockApi.admin.auditAudio({
      audioId,
      status,
      rejectReason: status === 'REJECTED' ? '不符合平台规范' : null
    })
    loadAuditAudioList()
    alert('审核操作已完成')
  } catch (error) {
    console.error('Failed to audit audio:', error)
  }
}

const openAudioDetail = (audio) => {
  selectedAudio.value = audio
  audioUrl.value = audio.audioUrl || 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
  showAudioDetail.value = true
}

const closeAudioDetail = () => {
  showAudioDetail.value = false
  selectedAudio.value = null
  audioUrl.value = ''
}

const auditAudioAndClose = async (audioId, status) => {
  await auditAudio(audioId, status)
  closeAudioDetail()
}

const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadAuditAudioList()
})
</script>

<style scoped>
@import '@/resource/css/adminDash.css';
</style>