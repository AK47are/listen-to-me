<template>
  <div class="fans-list-page">
    <div class="page-header">
      <h2>我的粉丝</h2>
    </div>

    <div class="content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>

      <div v-else-if="fansList.length === 0" class="empty-state">
        <el-empty description="暂无粉丝" />
      </div>

      <div v-else class="fans-grid">
        <div v-for="fan in fansList" :key="fan.userId" class="fan-card">
          <div class="card-header">
            <div class="avatar-wrapper">
              <img :src="fan.avatar" :alt="fan.nickname" class="avatar" />
            </div>
            <div class="header-info">
              <h3 class="fan-name">{{ fan.nickname }}</h3>
              <p class="follow-time">关注时间: {{ formatDate(fan.followTime) }}</p>
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
import { ElMessage } from 'element-plus'
import { followApi } from '@/api/user/follow'
import { useUserStore } from '@/stores/user/user'

const userStore = useUserStore()
const loading = ref(false)
const fansList = ref([])
const total = ref(0)

const pagination = ref({
  pageNum: 1,
  pageSize: 12,
})

const getFansList = async () => {
  const creatorId = userStore.userInfo?.id
  if (!creatorId) {
    ElMessage.error('用户未登录')
    return
  }

  loading.value = true
  try {
    const res = await followApi.getFansPage(creatorId, {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    fansList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取粉丝列表失败:', error)
    ElMessage.error('获取粉丝列表失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getFansList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  getFansList()
}

onMounted(() => {
  getFansList()
})
</script>

<style scoped>
.fans-list-page {
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

.fans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.fan-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  background: white;
}

.fan-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  padding: 15px;
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
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.follow-time {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
