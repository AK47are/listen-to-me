<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { consultApi } from '@/api/consult'
import { useUserStore } from '@/stores/user/user'

const router = useRouter()
const userStore = useUserStore()

// 过滤后的创作者列表（排除当前用户）
const filteredCreatorList = computed(() => {
  const currentUserId = userStore.userInfo?.id
  return creatorList.value.filter(creator => creator.creatorId !== currentUserId)
})

const loading = ref(false)
const creatorList = ref([])
const total = ref(0)
const searchQuery = ref('')

const pagination = ref({
  pageNum: 1,
  pageSize: 12
})

const getCreatorList = async () => {
  loading.value = true
  try {
    const res = await consultApi.getCreatorList({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchQuery.value
    })
    creatorList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取创作者列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  getCreatorList()
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getCreatorList()
}

const handleCreatorClick = (creator) => {
  router.push(`/consult/creator/${creator.creatorId}`)
}

onMounted(() => {
  getCreatorList()
})
</script>

<template>
  <div class="creator-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><UserFilled /></el-icon>
          咨询预约
        </h1>
        <p class="page-subtitle">选择心仪的创作者，预约一对一咨询服务</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索创作者名称..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 创作者列表 -->
    <div class="creator-section">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="creatorList.length === 0" class="empty-state">
        <el-empty description="暂无创作者">
          <template #image>
            <el-icon :size="60" color="#ccc"><User /></el-icon>
          </template>
        </el-empty>
      </div>

      <div v-else class="creator-grid">
        <div
          v-for="creator in filteredCreatorList"
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
            <div class="price-range">
              <span class="price-label">咨询价格</span>
              <span class="price-value">¥{{ creator.minPrice }} 起</span>
            </div>
          </div>

          <div class="card-hover">
            <el-button type="primary" size="large" round>
              <el-icon><Calendar /></el-icon>
              立即预约
            </el-button>
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
          @size-change="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '@/resource/css/creatorList.css';
</style>