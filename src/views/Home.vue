<template>
  <div class="home-container">
    <section class="recommend-section">
      <div class="section-header">
        <div class="title-group">
          <el-icon class="header-icon"><Opportunity /></el-icon>
          <h2 class="section-title">个性化推荐</h2>
        </div>
        <p class="section-subtitle">基于您的音频偏好，发现更多精彩声音</p>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="recommendList.length === 0" class="empty-state">
        <el-empty description="暂时没有发现合适的推荐" />
      </div>

      <div v-else class="recommend-grid">
        <AudioCard
          v-for="audio in recommendList"
          :key="audio.id"
          :audio="audio"
          @click="handlePlayAudio"
        />
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
    </section>
  </div>
</template>

<script setup>
import { recommendApi } from '@/api/user/recommend'
import { Opportunity } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AudioCard from '@/components/AudioCard.vue'

const router = useRouter()
const recommendList = ref([])
const loading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 12,
  total: 0,
})

const getRecommendList = async () => {
  loading.value = true
  try {
    const res = await recommendApi.getRecommendList({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    recommendList.value = res.data?.records || res.records || []
    pagination.value.total = res.data?.total || res.total || 0
  } catch (error) {
    ElMessage.error('获取推荐列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getRecommendList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePlayAudio = (audio) => {
  router.push(`/audio/${audio.id}`)
}

onMounted(() => {
  getRecommendList()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 1400px;
  margin: 0 auto 48px;
  padding: 0 30px;
  text-align: center;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.8rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}

.page-subtitle {
  font-size: 1rem;
  color: #8e8c84;
  letter-spacing: 1px;
  margin: 0;
}

.recommend-section {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}

.section-header {
  margin-bottom: 36px;
  text-align: center;
}

.title-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-icon {
  font-size: 24px;
  color: #1a1a1a;
  background: #f0efeb;
  padding: 8px;
  border-radius: 12px;
}

.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.section-subtitle {
  font-size: 0.9rem;
  color: #8e8c84;
  margin: 0;
}

.loading-state,
.empty-state {
  padding: 80px 0;
  text-align: center;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 28px;
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
}

.pagination-wrapper :deep(.el-pagination__total) {
  color: #b0aea3;
  font-size: 13px;
  margin-right: 16px;
}

@media (max-width: 768px) {
  .home-container {
    padding: 20px 0;
  }
  .page-header {
    margin-bottom: 32px;
  }
  .page-title {
    font-size: 2rem;
  }
  .recommend-section {
    padding: 0 20px;
  }
  .recommend-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
