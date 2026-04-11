<template>
  <div class="search-page">
    <div class="page-header">
      <h1 class="page-title">搜索结果</h1>
      <p class="page-subtitle" v-if="keyword">找到 {{ total }} 个与“{{ keyword }}”相关的结果</p>
      <p class="page-subtitle" v-else>请输入关键词搜索</p>
    </div>

    <div class="search-content">
      <!-- 类型切换标签 -->
      <div class="search-type-tabs">
        <span :class="['type-tab', { active: searchType === 'TITLE' }]" @click="changeType('TITLE')"
          >按标题</span
        >
        <span
          :class="['type-tab', { active: searchType === 'CREATOR' }]"
          @click="changeType('CREATOR')"
          >按创作者</span
        >
        <span
          :class="['type-tab', { active: searchType === 'TRANSCRIPT' }]"
          @click="changeType('TRANSCRIPT')"
          >按转写内容</span
        >
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="keyword && resultList.length === 0" class="empty-state">
        <el-empty description="没有找到相关音频" />
      </div>

      <div v-else-if="!keyword" class="empty-state">
        <el-empty description="在顶部搜索框输入关键词开始搜索" />
      </div>

      <div v-else class="result-grid">
        <AudioCard v-for="audio in resultList" :key="audio.id" :audio="audio" @click="goToAudio" />
      </div>

      <div v-if="total > 0 && !loading" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchApi } from '@/api/user/search'
import AudioCard from '@/components/AudioCard.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const searchType = ref('TITLE')
const resultList = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)

const performSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) {
    resultList.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const res = await searchApi.searchAudio({
      keyword: kw,
      searchType: searchType.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })
    resultList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error(error.response?.data?.msg || '搜索失败')
    resultList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const changeType = (type) => {
  searchType.value = type
  currentPage.value = 1
  performSearch()
  router.replace({
    query: {
      q: keyword.value,
      type: type,
    },
  })
}

const handlePageChange = (page) => {
  currentPage.value = page
  performSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToAudio = (audio) => {
  router.push(`/audio/${audio.id}`)
}

// 监听路由参数变化
watch(
  () => route.query.q,
  (newQ) => {
    if (newQ) {
      keyword.value = newQ
      currentPage.value = 1
      performSearch()
    }
  },
  { immediate: true },
)

// 监听类型参数变化
watch(
  () => route.query.type,
  (newType) => {
    if (newType && ['TITLE', 'CREATOR', 'TRANSCRIPT'].includes(newType)) {
      searchType.value = newType
    }
  },
)

onMounted(() => {
  const queryKeyword = route.query.q
  const queryType = route.query.type
  if (queryKeyword) {
    keyword.value = queryKeyword
    if (queryType && ['TITLE', 'CREATOR', 'TRANSCRIPT'].includes(queryType)) {
      searchType.value = queryType
    }
    performSearch()
  }
})
</script>

<style scoped>
.search-page {
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

.search-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}

.search-type-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 32px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0efeb;
}

.type-tab {
  padding: 6px 0;
  font-size: 0.95rem;
  font-weight: 500;
  color: #8e8c84;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.type-tab:hover {
  color: #1a1a1a;
}

.type-tab.active {
  color: #1a1a1a;
  border-bottom-color: #1a1a1a;
}

.loading-state,
.empty-state {
  padding: 80px 0;
  text-align: center;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 28px;
}

.pagination-wrapper {
  margin-top: 48px;
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
  .result-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .page-title {
    font-size: 1.8rem;
  }
  .search-type-tabs {
    gap: 20px;
  }
  .type-tab {
    font-size: 0.85rem;
  }
}
</style>
