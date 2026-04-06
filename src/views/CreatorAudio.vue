<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { creatorAudioApi } from '@/api/creator/audio'

const router = useRouter()

const audioList = ref([])
const loading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 20,
  total: 0,
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

const handleDeleteAudio = async (audioId) => {
  try {
    await ElMessageBox.confirm('确定要删除该稿件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await creatorAudioApi.removeAudio(audioId)
    ElMessage.success('删除成功')
    await getAudioList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleEditAudio = (audioId) => {
  router.push(`/creator/edit/${audioId}`)
}

const handleCreateAudio = () => {
  router.push('/creator/create')
}

onMounted(() => {
  getAudioList()
})
</script>

<template>
  <div class="creator-audio-container">
    <div class="header">
      <h2>我的稿件</h2>
      <el-button type="primary" @click="handleCreateAudio">
        <el-icon><Plus /></el-icon> 发布新稿件
      </el-button>
    </div>

    <div class="content">
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      <div v-else-if="audioList.length === 0" class="empty">
        <el-empty description="暂无稿件">
          <el-button type="primary" @click="handleCreateAudio">发布第一个稿件</el-button>
        </el-empty>
      </div>
      <div v-else class="audio-list">
        <div v-for="audio in audioList" :key="audio.id" class="audio-item">
          <img :src="audio.coverUrl" alt="封面" class="cover" />
          <div class="audio-info">
            <h3>{{ audio.title }}</h3>
            <div class="meta">
              <span v-if="audio.isPaid" class="price">¥{{ audio.price }}</span>
              <span class="duration">{{ Math.floor(audio.duration / 60) }} 分钟</span>
              <el-tag
                :type="
                  audio.status === 'APPROVED'
                    ? 'success'
                    : audio.status === 'REJECTED'
                      ? 'danger'
                      : 'warning'
                "
              >
                {{
                  audio.status === 'APPROVED'
                    ? '已通过'
                    : audio.status === 'REJECTED'
                      ? '已拒绝'
                      : '审核中'
                }}
              </el-tag>
            </div>
            <div class="stats">
              <span
                ><el-icon><Headset /></el-icon> {{ audio.playCount }}</span
              >
              <span
                ><el-icon><Star /></el-icon> {{ audio.collectCount }}</span
              >
              <span
                ><el-icon><Pointer /></el-icon> {{ audio.likeCount }}</span
              >
            </div>
            <div class="time">创建于 {{ new Date(audio.createTime).toLocaleString() }}</div>
          </div>
          <div class="actions">
            <el-button size="large" type="primary" @click="handleEditAudio(audio.id)"
              ><el-icon><Edit /></el-icon
            ></el-button>
            <el-button size="large" type="danger" @click="handleDeleteAudio(audio.id)"
              ><el-icon><Delete /></el-icon
            ></el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="pagination.total > 0" class="pagination">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.creator-audio-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.audio-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.audio-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s;
}

.audio-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cover {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  object-fit: cover;
}

.audio-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.audio-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.duration {
  color: #999;
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  color: #999;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.time {
  margin-top: auto;
  color: #999;
  font-size: 12px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
