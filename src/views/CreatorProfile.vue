<template>
  <div class="creator-profile-page">
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>创作者主页</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="profile-content">
      <div class="creator-card">
        <div class="creator-avatar">
          <img :src="creatorInfo.avatar || defaultAvatar" />
        </div>
        <div class="creator-info">
          <div class="creator-name">
            {{ creatorInfo.nickname }}
            <el-tag v-if="creatorInfo.isFollowing" type="success" size="small">已关注</el-tag>
          </div>
          <div class="creator-stats">
            <div class="stat-item">
              <span class="stat-value">{{ creatorInfo.audioCount || 0 }}</span>
              <span class="stat-label">作品</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ creatorInfo.fansCount || 0 }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ creatorInfo.consultCount || 0 }}</span>
              <span class="stat-label">咨询</span>
            </div>
          </div>
          <div class="creator-bio">{{ creatorInfo.intro || '这个人很懒，还没有填写简介' }}</div>
          <div class="creator-actions">
            <el-button
              v-if="creatorInfo.isFollowing"
              class="followed-btn"
              :icon="Check"
              @click="handleUnfollow"
            >
              已关注
            </el-button>
            <el-button
              v-if="creatorInfo.isFollowing"
              class="followed-btn"
              :icon="Check"
              @click="handleUnfollow"
            >
              已关注
            </el-button>
            <el-button v-else class="follow-btn" :icon="Plus" @click="handleFollow">
              关注
            </el-button>
            <el-button class="consult-btn" :icon="ChatDotRound" @click="goToConsult">
              预约咨询
            </el-button>
          </div>
        </div>
      </div>

      <div class="audio-section">
        <div class="section-header">
          <h3>全部作品</h3>
          <div class="section-filters">
            <el-button
              :type="audioFilter === 'all' ? 'primary' : 'default'"
              size="small"
              round
              @click="setFilter('all')"
              >全部</el-button
            >
            <el-button
              :type="audioFilter === 'free' ? 'primary' : 'default'"
              size="small"
              round
              @click="setFilter('free')"
              >免费</el-button
            >
            <el-button
              :type="audioFilter === 'paid' ? 'primary' : 'default'"
              size="small"
              round
              @click="setFilter('paid')"
              >付费</el-button
            >
          </div>
        </div>

        <div v-if="audioLoading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else-if="audioList.length === 0" class="empty-state">
          <el-empty description="暂无作品" />
        </div>

        <div v-else class="audio-grid">
          <AudioCard v-for="audio in audioList" :key="audio.id" :audio="audio" @click="goToAudio" />
        </div>

        <div v-if="audioTotal > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="audioPageNum"
            :total="audioTotal"
            :page-size="audioPageSize"
            layout="total, prev, pager, next"
            @current-change="handleAudioPageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Plus, ChatDotRound } from '@element-plus/icons-vue'
import { creatorApi } from '@/api/user/creator'
import { followApi } from '@/api/user/follow'
import { audioApi } from '@/api/user/audio'
import AudioCard from '@/components/AudioCard.vue'

const route = useRoute()
const router = useRouter()

const creatorId = ref(Number(route.params.id))
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const creatorInfo = ref({
  creatorId: null,
  nickname: '',
  avatar: '',
  intro: '',
  audioCount: 0,
  fansCount: 0,
  consultCount: 0,
  minPrice: 0,
  isFollowing: false,
})

const allAudioList = ref([])
const audioList = ref([])
const audioLoading = ref(false)
const audioTotal = ref(0)
const audioPageNum = ref(1)
const audioPageSize = ref(12)
const audioFilter = ref('all')

const getCreatorInfo = async () => {
  try {
    const res = await creatorApi.getCreatorDetail(creatorId.value)
    creatorInfo.value = res.data
  } catch (error) {
    console.error('获取创作者信息失败', error)
    ElMessage.error('获取创作者信息失败')
  }
}

const getAllAudioList = async () => {
  audioLoading.value = true
  try {
    const res = await audioApi.getCreatorAudioPage(creatorId.value, {
      pageNum: 1,
      pageSize: 100,
    })
    allAudioList.value = res.data.records || []
    updateDisplayList()
  } catch (error) {
    console.error('获取作品列表失败', error)
    ElMessage.error('获取作品列表失败')
  } finally {
    audioLoading.value = false
  }
}

const updateDisplayList = () => {
  let filtered = [...allAudioList.value]
  if (audioFilter.value === 'free') {
    filtered = filtered.filter((item) => !item.isPaid)
  } else if (audioFilter.value === 'paid') {
    filtered = filtered.filter((item) => item.isPaid)
  }
  const start = (audioPageNum.value - 1) * audioPageSize.value
  const end = start + audioPageSize.value
  audioList.value = filtered.slice(start, end)
  audioTotal.value = filtered.length
}

const setFilter = (filter) => {
  audioFilter.value = filter
  audioPageNum.value = 1
  updateDisplayList()
}

const handleFilterChange = () => {
  audioPageNum.value = 1
  updateDisplayList()
}

const handleAudioPageChange = (page) => {
  audioPageNum.value = page
  updateDisplayList()
}

const handleFollow = async () => {
  try {
    await followApi.followCreator(creatorId.value)
    ElMessage.success('关注成功')
    creatorInfo.value.isFollowing = true
    creatorInfo.value.fansCount++
  } catch (error) {
    console.error('关注失败', error)
    ElMessage.error('关注失败')
  }
}

const handleUnfollow = async () => {
  try {
    await followApi.unfollowCreator(creatorId.value)
    ElMessage.success('已取消关注')
    creatorInfo.value.isFollowing = false
    creatorInfo.value.fansCount--
  } catch (error) {
    console.error('取消关注失败', error)
    ElMessage.error('取消关注失败')
  }
}

const goToConsult = () => {
  router.push(`/consult/creator/${creatorId.value}`)
}

const goToAudio = (audio) => {
  router.push(`/audio/${audio.id}`)
}

onMounted(() => {
  getCreatorInfo()
  getAllAudioList()
})
</script>

<style scoped>
.creator-profile-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 32px;
  padding: 0 30px;
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #8e8c84;
  font-weight: 400;
}

.breadcrumb :deep(.el-breadcrumb__inner a) {
  color: #8e8c84;
  text-decoration: none;
}

.breadcrumb :deep(.el-breadcrumb__inner a:hover) {
  color: #1a1a1a;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1a1a1a;
  font-weight: 600;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
}

.creator-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 32px;
  padding: 32px 36px;
  margin-bottom: 32px;
  display: flex;
  gap: 36px;
  flex-wrap: wrap;
}

.creator-avatar img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fcfbf7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.creator-info {
  flex: 1;
}

.creator-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.creator-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 0.75rem;
  color: #b0aea3;
  margin-top: 4px;
}

.creator-bio {
  font-size: 0.95rem;
  line-height: 1.6;
  color: #6b6a62;
  margin-bottom: 24px;
  max-width: 500px;
}

.creator-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.follow-btn {
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 10px 32px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #ffffff;
  transition: all 0.2s;
}

.follow-btn:hover {
  background: #2c2c2c;
  transform: translateY(-2px);
}

.followed-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  padding: 10px 32px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #1a1a1a;
  transition: all 0.2s;
}

.followed-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-2px);
}

.consult-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  padding: 10px 32px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #1a1a1a;
  transition: all 0.2s;
}

.consult-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-2px);
}

.audio-section {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
  padding: 24px 28px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.section-filters {
  display: flex;
  gap: 1px;
}

.section-filters .el-button {
  border-radius: 40px;
  padding: 8px 24px;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s;
}

.section-filters .el-button--default {
  background: transparent;
  border: 1px solid #e8e6df;
  color: #1a1a1a;
}

.section-filters .el-button--default:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-1px);
}

.section-filters .el-button--primary {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.section-filters .el-button--primary:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.loading-state,
.empty-state {
  padding: 60px 0;
  text-align: center;
}

.audio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

.pagination-wrapper {
  margin-top: 32px;
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

@media (max-width: 768px) {
  .creator-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  .creator-stats {
    justify-content: center;
  }
  .creator-bio {
    max-width: 100%;
  }
  .creator-actions {
    justify-content: center;
  }
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }
  .section-filters {
    justify-content: center;
  }
  .audio-grid {
    grid-template-columns: 1fr;
  }
}
</style>
