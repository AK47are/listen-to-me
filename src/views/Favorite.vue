<template>
  <div class="favorite-page">
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <p class="page-subtitle">管理你收藏的音频</p>
    </div>

    <div class="favorite-content">
      <div class="folder-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">收藏夹</span>
          <el-button
            class="create-folder-btn"
            :icon="Plus"
            circle
            @click="showCreateFolderDialog = true"
          />
        </div>
        <div class="folder-list">
          <div
            v-for="folder in folders"
            :key="folder.id"
            :class="['folder-item', { active: currentFolderId === folder.id }]"
            @click="selectFolder(folder.id)"
          >
            <div class="folder-info">
              <el-icon><FolderOpened /></el-icon>
              <span class="folder-name">{{ folder.name }}</span>
            </div>
            <div class="folder-actions">
              <span class="folder-count">{{ folder.audioCount }}</span>
              <el-button
                link
                class="delete-folder-btn"
                :icon="Delete"
                @click.stop="handleDeleteFolder(folder.id)"
              />
            </div>
          </div>
          <div v-if="folders.length === 0" class="empty-folders">
            <el-empty description="暂无收藏夹" :image-size="60" />
          </div>
        </div>
      </div>

      <div class="audio-main">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="audioList.length === 0" class="empty-state">
          <el-empty description="暂无收藏音频" />
        </div>
        <div v-else>
          <div class="audio-grid">
            <div v-for="audio in audioList" :key="audio.id" class="audio-card">
              <img :src="audio.coverUrl" class="cover-img" @click="handlePlayAudio(audio.id)" />
              <div class="card-info">
                <h3 class="audio-title" @click="handlePlayAudio(audio.id)">{{ audio.title }}</h3>
                <p class="creator-name">{{ audio.creatorName }}</p>
                <div class="stats">
                  <span class="stat-item">
                    <el-icon><Headset /></el-icon>
                    {{ formatNumber(audio.playCount) }}
                  </span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ formatNumber(audio.collectCount) }}
                  </span>
                </div>
              </div>
              <div class="card-action">
                <el-button
                  class="remove-btn"
                  :icon="Delete"
                  @click="handleRemoveFavorite(audio.id)"
                >
                  移除
                </el-button>
              </div>
            </div>
          </div>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              :total="total"
              :page-size="pagination.pageSize"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 新建收藏夹对话框 -->
    <el-dialog
      v-model="showCreateFolderDialog"
      title="新建收藏夹"
      width="420px"
      class="folder-dialog"
      destroy-on-close
    >
      <el-form :model="folderForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input
            v-model="folderForm.name"
            placeholder="请输入收藏夹名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="folderForm.description"
            type="textarea"
            :rows="2"
            placeholder="选填，简短描述这个收藏夹（限30字）"
            maxlength="30"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="cancel-btn" @click="showCreateFolderDialog = false">取消</el-button>
          <el-button class="confirm-btn" :loading="creatingFolder" @click="handleCreateFolder">
            创建
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, FolderOpened, Headset, Star } from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/user/favorite'

const router = useRouter()

const folders = ref([])
const currentFolderId = ref(null)
const audioList = ref([])
const total = ref(0)
const loading = ref(false)
const creatingFolder = ref(false)
const showCreateFolderDialog = ref(false)
const folderForm = ref({
  name: '',
  description: '',
})

const pagination = ref({
  pageNum: 1,
  pageSize: 12,
})

const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num.toString()
}

const getFolderList = async () => {
  try {
    const res = await favoriteApi.getFolderList()
    folders.value = (res.data || []).map((f) => ({
      ...f,
      id: Number(f.id || f.folderId),
    }))
    if (folders.value.length > 0 && !currentFolderId.value) {
      currentFolderId.value = folders.value[0].id
      getFavoriteList()
    }
  } catch (error) {
    console.error('获取收藏夹列表失败:', error)
  }
}

const selectFolder = (folderId) => {
  currentFolderId.value = folderId
  pagination.value.pageNum = 1
  getFavoriteList()
}

const getFavoriteList = async () => {
  if (!currentFolderId.value) return
  loading.value = true
  try {
    const res = await favoriteApi.getFavoritePage({
      folderId: currentFolderId.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    audioList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreateFolder = async () => {
  if (!folderForm.value.name.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  creatingFolder.value = true
  try {
    await favoriteApi.saveFavoriteFolder({
      name: folderForm.value.name,
      description: folderForm.value.description,
    })
    ElMessage.success('创建成功')
    showCreateFolderDialog.value = false
    folderForm.value = { name: '', description: '' }
    await getFolderList()
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  } finally {
    creatingFolder.value = false
  }
}

const handleDeleteFolder = async (folderId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该收藏夹吗？删除后其中的音频也会从该收藏夹中移除',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await favoriteApi.deleteFavoriteFolder(folderId)
    ElMessage.success('删除成功')
    if (currentFolderId.value === folderId) {
      currentFolderId.value = null
      audioList.value = []
    }
    await getFolderList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleRemoveFavorite = async (audioId) => {
  try {
    await ElMessageBox.confirm('确定要移除该音频吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await favoriteApi.uncollectAudio(audioId, currentFolderId.value)
    ElMessage.success('移除成功')
    await getFavoriteList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error('移除失败')
    }
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getFavoriteList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePlayAudio = (audioId) => {
  router.push(`/audio/${audioId}`)
}

onMounted(() => {
  getFolderList()
})
</script>

<style scoped>
.favorite-page {
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

.favorite-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  gap: 28px;
}

.folder-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 24px;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0efeb;
}

.sidebar-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a1a1a;
}

.create-folder-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 50%;
  color: #1a1a1a;
}

.create-folder-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.folder-list {
  padding: 8px;
}

.folder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.folder-item:hover {
  background: #f7f6f2;
}

.folder-item.active {
  background: #1a1a1a;
  color: #ffffff;
}

.folder-item.active .folder-info,
.folder-item.active .folder-actions {
  color: #ffffff;
}

.folder-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.folder-info .el-icon {
  font-size: 16px;
}

.folder-name {
  font-size: 0.85rem;
  font-weight: 500;
}

.folder-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.7rem;
  color: #b0aea3;
}

.delete-folder-btn {
  padding: 0;
  color: #b0aea3;
  opacity: 0;
  transition: opacity 0.2s;
}

.folder-item:hover .delete-folder-btn {
  opacity: 1;
}

.delete-folder-btn:hover {
  color: #f56c6c !important;
}

.empty-folders {
  padding: 40px 20px;
}

.audio-main {
  flex: 1;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 24px;
  padding: 24px;
  min-height: 500px;
}

.loading-state,
.empty-state {
  padding: 80px 0;
  text-align: center;
}

.audio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

.audio-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
}

.audio-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 28px -12px rgba(0, 0, 0, 0.1);
  border-color: #e2e0d7;
}

.cover-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  cursor: pointer;
}

.card-info {
  padding: 14px 16px 12px;
}

.audio-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.audio-title:hover {
  color: #2c2c2c;
}

.creator-name {
  font-size: 0.75rem;
  color: #b0aea3;
  margin: 0 0 10px 0;
}

.stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.7rem;
  color: #b0aea3;
}

.card-action {
  padding: 10px 16px 16px;
  border-top: 1px solid #f0efeb;
}

.remove-btn {
  width: 100%;
  border-radius: 40px;
  background: transparent;
  border: 1px solid #e8e6df;
  color: #1a1a1a;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.remove-btn:hover {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
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

.folder-dialog :deep(.el-dialog) {
  border-radius: 24px;
  background: #ffffff;
}

.folder-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 12px;
  border-bottom: 1px solid #f0efeb;
}

.folder-dialog :deep(.el-dialog__title) {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a1a1a;
}

.folder-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.folder-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0efeb;
}

.folder-dialog :deep(.el-input__wrapper) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 16px;
  box-shadow: none;
}

.folder-dialog :deep(.el-input__wrapper:hover) {
  border-color: #dcdbd3;
}

.folder-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 2px rgba(26, 26, 26, 0.05);
}

.folder-dialog :deep(.el-textarea__inner) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 16px;
  box-shadow: none;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.dialog-footer .cancel-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  padding: 8px 24px;
  font-weight: 500;
  color: #1a1a1a;
  transition: all 0.2s;
}

.dialog-footer .cancel-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.dialog-footer .confirm-btn {
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 8px 24px;
  font-weight: 500;
  color: #ffffff;
  transition: all 0.2s;
}

.dialog-footer .confirm-btn:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .favorite-content {
    flex-direction: column;
    gap: 20px;
  }
  .folder-sidebar {
    width: 100%;
  }
  .audio-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .page-title {
    font-size: 1.8rem;
  }
}
</style>
