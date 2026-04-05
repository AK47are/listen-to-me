<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { audioApi } from '@/api/audio'

const router = useRouter()

const audioList = ref([])
const loading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0,
})

const getHistoryList = async () => {
  loading.value = true
  try {
    const res = await audioApi.getHistoryPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    audioList.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取历史记录失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getHistoryList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  getHistoryList()
}

const handlePlayAudio = (audioId) => {
  router.push(`/audio/${audioId}`)
}

onMounted(() => {
  getHistoryList()
})
</script>

<template>
  <div class="history-container">
    <div class="header">
      <h2>播放历史</h2>
    </div>

    <div class="content">
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      <div v-else-if="audioList.length === 0" class="empty">
        <el-empty description="暂无播放历史" />
      </div>
      <div v-else class="audio-grid">
        <div
          v-for="audio in audioList"
          :key="audio.id"
          class="audio-card"
        >
          <img :src="audio.coverUrl" alt="封面" class="cover" />
          <div class="audio-info">
            <h4>{{ audio.title }}</h4>
            <p>{{ audio.creatorName }}</p>
            <div class="stats">
              <span><el-icon><Headset /></el-icon> {{ audio.playCount }}</span>
              <span><el-icon><Star /></el-icon> {{ audio.collectCount }}</span>
            </div>
          </div>
          <div class="actions">
            <el-button type="primary" size="small" @click="handlePlayAudio(audio.id)">
              继续播放
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="pagination.total > 0" class="pagination">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[12, 24, 36]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.history-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 500px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  gap: 10px;
  color: #999;
}

.audio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.audio-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.audio-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.audio-info {
  padding: 15px;
}

.audio-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audio-info p {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.actions {
  padding: 10px 15px;
  border-top: 1px solid #e0e0e0;
}

.actions .el-button {
  width: 100%;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>