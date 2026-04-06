<script setup>
import { recommendApi } from '@/api/user/recommend'
import { Headset, Loading, Opportunity, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const recommendList = ref([])
const loading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})

const getRecommendList = async () => {
  loading.value = true
  try {
    const res = await recommendApi.getRecommendList({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    // 适配后端分页对象 PageVO
    recommendList.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    ElMessage.error('获取推荐列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 分页切换处理
 */
const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getRecommendList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 跳转音频详情页
 */
const handlePlayAudio = (audioId) => {
  router.push(`/audio/${audioId}`)
}

onMounted(() => {
  getRecommendList()
})
</script>

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

      <div v-if="loading" class="state-placeholder">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在为您挑选音频...</span>
      </div>

      <template v-else>
        <div v-if="recommendList.length > 0" class="recommend-grid">
          <div
            v-for="audio in recommendList"
            :key="audio.id"
            class="audio-card"
            @click="handlePlayAudio(audio.id)"
          >
            <div class="card-cover-wrapper">
              <img :src="audio.coverUrl" class="card-cover" loading="lazy" />
              <div class="play-overlay">
                <el-icon><Headset /></el-icon>
              </div>
              <div class="collect-badge">
                <el-icon><Star /></el-icon>
                {{ audio.collectCount }}
              </div>
            </div>

            <div class="card-content">
              <h4 class="audio-title" :title="audio.title">{{ audio.title }}</h4>
              <div class="audio-footer">
                <span class="creator-name">{{ audio.creatorName }}</span>
                <div class="play-stats">
                  <el-icon><Headset /></el-icon>
                  {{ audio.playCount }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="state-placeholder">
          <el-empty description="暂时没有发现合适的推荐" />
        </div>

        <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
}

.recommend-section {
  width: 100%;
}

.section-header {
  margin-bottom: 36px;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-icon {
  font-size: 24px;
  color: #409eff;
  background: #f0f7ff;
  padding: 8px;
  border-radius: 12px;
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0;
}

.section-subtitle {
  font-size: 14px;
  color: #8e8c84;
  margin: 0;
  padding-left: 44px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 32px;
}

.audio-card {
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.audio-card:hover {
  transform: translateY(-8px);
}

.card-cover-wrapper {
  position: relative;
  aspect-ratio: 1 / 1;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
  background: #f7f6f2;
}

.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.audio-card:hover .card-cover {
  transform: scale(1.1);
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: #fff;
  font-size: 36px;
}

.audio-card:hover .play-overlay {
  opacity: 1;
}

.collect-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-content {
  padding: 0 4px;
}

.audio-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.audio-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.creator-name {
  font-size: 13px;
  color: #8e8c84;
}

.play-stats {
  font-size: 12px;
  color: #b0aea3;
  display: flex;
  align-items: center;
  gap: 4px;
}

.state-placeholder {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #b0aea3;
}

.pagination-wrapper {
  margin-top: 60px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .home-container {
    padding: 20px;
  }
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  .card-cover-wrapper {
    border-radius: 16px;
  }
}
</style>
