<template>
  <div class="like-page">
    <div class="page-header">
      <h1 class="page-title">我喜欢的</h1>
      <p class="page-subtitle">你标记为喜欢的音频</p>
    </div>

    <div class="like-content">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="audioList.length === 0" class="empty-state">
        <el-empty description="暂无喜欢的音频" />
      </div>

      <div v-else class="audio-grid">
        <AudioCard
          v-for="audio in audioList"
          :key="audio.id"
          :audio="audio"
          @click="handlePlayAudio"
        >
          <template #action>
            <button class="custom-play-btn" @click.stop="handlePlayAudio(audio.id)">
              <el-icon><CaretRight /></el-icon> 播放
            </button>
          </template>
        </AudioCard>
      </div>

      <div v-if="pagination.total > 0" class="pagination-wrapper">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CaretRight } from '@element-plus/icons-vue'
import { likeApi } from '@/api/user/like'
import AudioCard from '@/components/AudioCard.vue'

const router = useRouter()

const audioList = ref([])
const loading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0,
})

const getLikeList = async () => {
  loading.value = true
  try {
    const res = await likeApi.getLikePage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    audioList.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    console.error(error.message)
    ElMessage.error('获取喜欢列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getLikeList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  getLikeList()
}

const handlePlayAudio = (audio) => {
  router.push(`/audio/${audio.id}`)
}

onMounted(() => {
  getLikeList()
})
</script>

<style scoped>
.like-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 1400px;
  margin: 0 auto 40px;
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

.like-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}

.loading-state,
.empty-state {
  padding: 80px 0;
  text-align: center;
}

.audio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 28px;
}

.custom-play-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 8px 12px;
  background: #f7f6f2;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
  transition: all 0.2s;
}

.custom-play-btn:hover {
  background: #efeee8;
  border-color: #dcdbd3;
  transform: translateY(-1px);
}

.custom-play-btn .el-icon {
  font-size: 14px;
}

.pagination-wrapper {
  margin-top: 56px;
  display: flex;
  justify-content: center;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: #1a1a1a;
  --el-pagination-button-disabled-bg-color: transparent;
  font-weight: 400;
}

.pagination-wrapper :deep(.btn-prev),
.pagination-wrapper :deep(.btn-next),
.pagination-wrapper :deep(.el-pager li) {
  background: transparent;
  border: none;
  border-radius: 8px;
  margin: 0 4px;
  font-size: 14px;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  transition: all 0.2s;
  color: #8e8c84;
}

.pagination-wrapper :deep(.btn-prev:hover),
.pagination-wrapper :deep(.btn-next:hover),
.pagination-wrapper :deep(.el-pager li:hover) {
  background: #f0efeb;
  color: #1a1a1a;
}

.pagination-wrapper :deep(.el-pager li.is-active) {
  background: #1a1a1a;
  color: #ffffff;
  font-weight: 500;
}

.pagination-wrapper :deep(.el-pagination__total) {
  color: #b0aea3;
  font-size: 13px;
  margin-right: 16px;
}

.pagination-wrapper :deep(.el-pagination__sizes) {
  margin: 0 12px;
}

.pagination-wrapper :deep(.el-select .el-input__wrapper) {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 20px;
  box-shadow: none;
}

.pagination-wrapper :deep(.el-pagination__jump) {
  color: #b0aea3;
  font-size: 13px;
  margin-left: 12px;
}

@media (max-width: 768px) {
  .audio-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .page-title {
    font-size: 1.8rem;
  }
}
</style>
