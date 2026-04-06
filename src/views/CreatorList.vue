<template>
  <div class="creator-list-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><UserFilled /></el-icon>
          咨询预约
        </h1>
        <p class="page-subtitle">选择心仪的创作者，预约一对一咨询服务</p>
      </div>
      <div class="header-right">
        <div class="search-trigger-btn" @click.stop="openSearchPanel">
          <el-icon><Search /></el-icon>
          <span>搜索创作者</span>
        </div>
      </div>
    </div>

    <transition name="panel-fade">
      <div v-if="showSearchPanel" class="search-panel" ref="panelRef">
        <div class="panel-container">
          <div class="search-input-area">
            <el-icon class="panel-search-icon"><Search /></el-icon>
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              placeholder="输入创作者名称..."
              class="panel-input"
              @keyup.enter="handleSearch"
            />
            <el-icon v-if="searchQuery" class="panel-clear" @click.stop="clearSearch"
              ><Close
            /></el-icon>
          </div>
          <div class="panel-actions">
            <button class="panel-btn cancel" @click="closeSearchPanel">取消</button>
            <button class="panel-btn submit" @click="handleSearch">搜索</button>
          </div>
        </div>
      </div>
    </transition>

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
            <p class="creator-bio">{{ creator.intro || '这个人很懒，还没有填写简介' }}</p>
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

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          :total="total"
          :page-size="12"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  UserFilled,
  User,
  Headset,
  Star,
  ChatDotRound,
  Calendar,
  Close,
} from '@element-plus/icons-vue'
import { consultApi } from '@/api/consult'
import { useUserStore } from '@/stores/user/user'

const router = useRouter()
const userStore = useUserStore()

const currentUserId = computed(() => userStore.userInfo?.id)

const filteredCreatorList = computed(() => {
  if (!currentUserId.value) return creatorList.value
  return creatorList.value.filter((creator) => creator.creatorId !== currentUserId.value)
})

const loading = ref(false)
const creatorList = ref([])
const total = ref(0)
const searchQuery = ref('')
const showSearchPanel = ref(false)
const searchInputRef = ref(null)
const panelRef = ref(null)

const pagination = ref({
  pageNum: 1,
  pageSize: 12,
})

const getCreatorList = async () => {
  loading.value = true
  try {
    const res = await consultApi.getCreatorList({
      pageNum: pagination.value.pageNum,
      pageSize: 12,
      keyword: searchQuery.value,
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
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  pagination.value.pageNum = 1
  getCreatorList()
  closeSearchPanel()
}

const openSearchPanel = () => {
  showSearchPanel.value = true
  setTimeout(() => {
    if (searchInputRef.value) {
      searchInputRef.value.focus()
    }
  }, 50)
}

const closeSearchPanel = () => {
  showSearchPanel.value = false
}

const clearSearch = () => {
  searchQuery.value = ''
  if (!searchQuery.value) {
    pagination.value.pageNum = 1
    getCreatorList()
  }
}

const handleClickOutside = (e) => {
  if (!showSearchPanel.value) return
  if (
    panelRef.value &&
    !panelRef.value.contains(e.target) &&
    !e.target.closest('.search-trigger-btn')
  ) {
    closeSearchPanel()
  }
}

const handleEscape = (e) => {
  if (e.key === 'Escape' && showSearchPanel.value) {
    closeSearchPanel()
  }
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
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleEscape)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleEscape)
})
</script>

<style scoped>
.creator-list-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 1400px;
  margin: 0 auto 40px;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.4rem;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 1rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.header-right {
  flex-shrink: 0;
}

.search-trigger-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: #ffffff;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  font-size: 15px;
  color: #1a1a1a;
  cursor: pointer;
  transition: all 0.2s;
}

.search-trigger-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-1px);
}

.search-trigger-btn .el-icon {
  font-size: 18px;
}

.search-panel {
  position: fixed;
  top: 100px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1001;
  width: 90%;
  max-width: 480px;
  animation: panelSlideDown 0.2s ease;
}

@keyframes panelSlideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.panel-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 28px;
  border: 1px solid #e2e0d7;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
}

.search-input-area {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #efeee8;
  padding-bottom: 12px;
  margin-bottom: 20px;
}

.panel-search-icon {
  font-size: 22px;
  color: #b0aea3;
}

.panel-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 17px;
  padding: 8px 0;
  background: transparent;
  color: #1a1a1a;
  font-weight: 500;
}

.panel-input::placeholder {
  color: #c4c2ba;
  font-weight: 400;
}

.panel-clear {
  font-size: 18px;
  color: #b0aea3;
  cursor: pointer;
  transition: color 0.2s;
}

.panel-clear:hover {
  color: #1a1a1a;
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.panel-btn {
  background: transparent;
  border: none;
  padding: 8px 20px;
  border-radius: 32px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.panel-btn.cancel {
  color: #8e8c84;
}

.panel-btn.cancel:hover {
  color: #1a1a1a;
  background: #f7f6f2;
}

.panel-btn.submit {
  background: #1a1a1a;
  color: #ffffff;
}

.panel-btn.submit:hover {
  background: #2c2c2c;
  transform: scale(0.96);
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.2s ease;
}
.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
}

.creator-section {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}

.loading-state,
.empty-state {
  padding: 80px 0;
  text-align: center;
}

.creator-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 32px;
}

.creator-card {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  border: 1px solid #efeee8;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.creator-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.08);
  border-color: #e2e0d7;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 24px 16px;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fcfbf7;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.04);
}

.creator-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 6px 0;
}

.card-body {
  padding: 0 24px 12px;
}

.creator-bio {
  font-size: 0.95rem;
  line-height: 1.5;
  color: #6b6a62;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0efeb;
  margin-top: 8px;
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 18px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: #8e8c84;
}

.stat-item .el-icon {
  font-size: 15px;
}

.price-range {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.price-label {
  font-size: 0.8rem;
  color: #b0aea3;
  letter-spacing: 0.5px;
}

.price-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a1a1a;
}

.card-hover {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
  pointer-events: none;
}

.creator-card:hover .card-hover {
  opacity: 1;
  pointer-events: auto;
}

.card-hover .el-button {
  background: #1a1a1a;
  border: none;
  padding: 12px 32px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: transform 0.2s;
  font-size: 16px;
}

.card-hover .el-button:hover {
  background: #2c2c2c;
  transform: scale(0.98);
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
  font-size: 14px;
  margin-right: 16px;
}

.pagination-wrapper :deep(.el-pagination__jump) {
  color: #b0aea3;
  font-size: 14px;
  margin-left: 16px;
}

.pagination-wrapper :deep(.el-pagination__editor .el-input__wrapper) {
  border: 1px solid #e8e6df;
  border-radius: 20px;
  background: transparent;
  box-shadow: none;
}

@media (max-width: 768px) {
  .creator-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .page-title {
    font-size: 1.8rem;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-panel {
    width: calc(100% - 40px);
  }
}
</style>
