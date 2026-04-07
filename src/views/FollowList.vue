<template>
  <div class="follow-list-page">
    <div class="page-header">
      <h2>我的关注</h2>
    </div>

    <div class="content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>

      <div v-else-if="creatorList.length === 0" class="empty-state">
        <el-empty description="暂无关注的创作者" />
      </div>

      <div v-else class="creator-grid">
        <div
          v-for="creator in creatorList"
          :key="creator.creatorId"
          class="creator-card"
          @click="handleCreatorClick(creator)"
        >
          <div class="card-header">
            <div class="avatar-wrapper">
              <img :src="creator.avatar" :alt="creator.nickname" class="avatar" />
            </div>
            <div class="header-info">
              <h3 class="creator-name">{{ creator.nickname }}</h3>
            </div>
          </div>

          <div class="card-body">
            <p class="creator-bio">{{ creator.intro }}</p>
          </div>

          <div class="card-footer">
            <div class="stats">
              <span class="stat-item">
                <el-icon><Headset /></el-icon>
                {{ creator.audioCount }} 作品
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ creator.fansCount }} 关注
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ creator.consultCount }} 咨询
              </span>
            </div>
            <div class="card-actions">
              <div class="price-range">
                <span class="price-label">咨询价格</span>
                <span class="price-value">¥{{ creator.minPrice }} 起</span>
              </div>
              <el-button type="default" size="small" @click="handleFollow(creator, $event)">
                <el-icon><UserFilled /></el-icon>
                已关注
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[12, 24, 36]"
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
import { followApi } from '@/api/user/follow'

const router = useRouter()
const loading = ref(false)
const creatorList = ref([])
const total = ref(0)

const pagination = ref({
  pageNum: 1,
  pageSize: 12,
})

const getFollowList = async () => {
  loading.value = true
  try {
    const res = await followApi.getFollowPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    creatorList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取关注列表失败:', error)
    ElMessage.error('获取关注列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreatorClick = (creator) => {
  router.push(`/consult/creator/${creator.creatorId}`)
}

const handleFollow = async (creator, event) => {
  event.stopPropagation()
  try {
    await followApi.unfollowCreator(creator.creatorId)
    ElMessage.success('已取消关注')
    getFollowList()
  } catch (error) {
    console.error('取消关注失败:', error)
    ElMessage.error('取消关注失败')
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getFollowList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  getFollowList()
}

onMounted(() => {
  getFollowList()
})
</script>

<style scoped>
.follow-list-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  color: #333;
  font-size: 24px;
}

.content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 500px;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  gap: 10px;
  color: #999;
}

.empty-state {
  padding: 60px 0;
}

.creator-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.creator-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  background: white;
}

.creator-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-info h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.card-body {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.creator-bio {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  padding: 15px;
}

.stats {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 5px;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.price-value {
  font-size: 16px;
  font-weight: bold;
  color: #ff4d4f;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
