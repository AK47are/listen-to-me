<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { recommendApi } from '@/api/recommend'
import { Headset, Star, Trophy, Loading, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const hotList = ref([])
const loading = ref(false)

const fetchHotRankData = async () => {
  loading.value = true
  try {
    const res = await recommendApi.getHotList()
    console.log('Hot Rank Original Response:', res)
    const targetData = res?.data || res

    if (targetData && Array.isArray(targetData)) {
      hotList.value = targetData.map((item) => ({
        ...item,
        playCount: Number(item.playCount || 0),
        collectCount: Number(item.collectCount || 0),
        coverUrl: item.coverUrl || '',
        title: item.title || '未知音频',
        creatorName: item.creatorName || '匿名创作者',
      }))
      console.log('Processed Hot List:', hotList.value)
    } else {
      hotList.value = []
    }
  } catch (error) {
    console.error('Fetch hot list failed:', error)
    ElMessage.error('无法连接到热榜服务器')
  } finally {
    loading.value = false
  }
}

const handlePlayAudio = (id) => {
  if (id) router.push(`/audio/${id}`)
}

onMounted(() => {
  fetchHotRankData()
})
</script>

<template>
  <div class="hot-rank-container">
    <header class="rank-header">
      <div class="icon-badge">
        <el-icon><Trophy /></el-icon>
      </div>
      <h1 class="rank-title">全站热度榜</h1>
      <p class="rank-intro">根据近一月的播放、点赞、收藏及评论数据综合计算，每小时自动更新</p>
    </header>

    <div v-if="loading" class="state-placeholder">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在同步实时排名...</span>
    </div>

    <div v-else class="rank-main">
      <div
        v-for="(audio, index) in hotList"
        :key="audio.id"
        class="rank-row"
        @click="handlePlayAudio(audio.id)"
      >
        <div class="rank-index" :class="'top-' + (index + 1)">
          {{ index + 1 }}
        </div>

        <div class="cover-wrapper">
          <img v-if="audio.coverUrl" :src="audio.coverUrl" class="audio-cover" alt="封面" />
          <div v-else class="audio-cover-placeholder">
            <el-icon><Headset /></el-icon>
          </div>
        </div>

        <div class="audio-content">
          <h3 class="audio-title" :title="audio.title">{{ audio.title }}</h3>
          <p class="creator-name">{{ audio.creatorName }}</p>
        </div>

        <div class="audio-stats">
          <div class="stat-item">
            <el-icon><Headset /></el-icon>
            <span class="stat-value">{{ audio.playCount }}</span>
            <span class="stat-label">播放量</span>
          </div>
          <div class="stat-item">
            <el-icon><Star /></el-icon>
            <span class="stat-value">{{ audio.collectCount }}</span>
            <span class="stat-label">收藏数</span>
          </div>
        </div>

        <div class="action-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <el-empty v-if="hotList.length === 0" description="榜单计算中，请稍后再试" />
    </div>
  </div>
</template>

<style scoped>
.hot-rank-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 60px 40px;
}

.rank-header {
  text-align: center;
  margin-bottom: 50px;
}

.icon-badge {
  display: inline-flex;
  padding: 16px;
  background: #fff9eb;
  color: #ffae00;
  border-radius: 20px;
  font-size: 32px;
  margin-bottom: 20px;
}

.rank-title {
  font-size: 32px;
  font-weight: 900;
  color: #1a1a1a;
  margin: 0 0 12px;
}

.rank-intro {
  font-size: 14px;
  color: #8e8c84;
}

.rank-main {
  background: #ffffff;
  border-radius: 28px;
  border: 1px solid #efeee8;
  padding: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;
}

.rank-row {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  margin-bottom: 4px;
}

.rank-row:hover {
  background: #f9f8f4;
  transform: scale(1.01) translateX(5px);
}

.rank-index {
  width: 60px;
  font-size: 22px;
  font-weight: 900;
  color: #d1cfc7;
  font-style: italic;
  text-align: center;
}

.top-1 {
  color: #ffb800;
  font-size: 36px;
}
.top-2 {
  color: #adb5bd;
  font-size: 30px;
}
.top-3 {
  color: #cd7f32;
  font-size: 30px;
}

.cover-wrapper {
  margin: 0 28px;
  flex-shrink: 0;
}

.audio-cover {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: #f0f0f0;
}

.audio-cover-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 30px;
}

.audio-content {
  flex: 1;
  min-width: 0;
}

.audio-title {
  font-size: 20px;
  font-weight: 800;
  color: #2d2d2d;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.creator-name {
  font-size: 15px;
  color: #999;
  font-weight: 500;
}

.audio-stats {
  display: flex;
  gap: 48px;
  margin: 0 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 70px;
}

.stat-item .el-icon {
  font-size: 18px;
  color: #c4c2ba;
}

.stat-value {
  font-size: 16px;
  font-weight: 800;
  color: #1a1a1a;
}

.stat-label {
  font-size: 12px;
  color: #bbb;
  letter-spacing: 1px;
}

.action-arrow {
  color: #eee;
  font-size: 24px;
  transition: all 0.3s;
  padding-right: 10px;
}

.rank-row:hover .action-arrow {
  color: #ffb800;
  transform: translateX(5px);
}

.state-placeholder {
  height: 450px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  color: #adb5bd;
  font-size: 16px;
}

@media (max-width: 850px) {
  .audio-stats {
    display: none;
  }
  .rank-row {
    padding: 16px;
  }
  .audio-cover,
  .audio-cover-placeholder {
    width: 64px;
    height: 64px;
  }
  .hot-rank-container {
    padding: 40px 16px;
  }
  .cover-wrapper {
    margin: 0 16px;
  }
}
</style>
