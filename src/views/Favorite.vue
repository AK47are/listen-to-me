<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteApi } from '@/api/user/favorite'

const router = useRouter()

const folders = ref([])
const currentFolderId = ref(null)
const audioList = ref([])
const total = ref(0)
const loading = ref(false)
const showCreateFolderDialog = ref(false)
const folderForm = ref({
  name: '',
  description: '',
})

const pagination = ref({
  pageNum: 1,
  pageSize: 12,
})

const getFolderList = async () => {
  try {
    const res = await favoriteApi.getFolderList()
    folders.value = res.data
    if (folders.value.length > 0 && !currentFolderId.value) {
      currentFolderId.value = folders.value[0].id
      getFavoriteList()
    }
  } catch (error) {
    console.error(error.message)
  }
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
    console.error(error.message)
  } finally {
    loading.value = false
  }
}

const handleCreateFolder = async () => {
  if (!folderForm.value.name) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  try {
    await favoriteApi.saveFavoriteFolder(folderForm.value)
    ElMessage.success('创建成功')
    showCreateFolderDialog.value = false
    folderForm.value = { name: '', description: '' }
    await getFolderList()
  } catch (error) {
    console.error(error.message)
  }
}

const handleDeleteFolder = async (folderId) => {
  try {
    await ElMessageBox.confirm('确定要删除该收藏夹吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await favoriteApi.deleteFavoriteFolder(folderId)
    ElMessage.success('删除成功')
    if (currentFolderId.value === folderId) {
      currentFolderId.value = null
      audioList.value = []
    }
    await getFolderList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error.message)
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
    await favoriteApi.saveAudioAction({
      audioId,
      folderId: currentFolderId.value,
      action: 'UNCOLLECT',
    })
    ElMessage.success('移除成功')
    await getFavoriteList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error.message)
    }
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getFavoriteList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  getFavoriteList()
}

const handlePlayAudio = (audioId) => {
  router.push(`/audio/${audioId}`)
}

onMounted(() => {
  getFolderList()
})
</script>

<template>
  <div class="favorite-container">
    <div class="header">
      <h2>我的收藏</h2>
      <el-button type="primary" @click="showCreateFolderDialog = true">
        <el-icon><Plus /></el-icon> 新建收藏夹
      </el-button>
    </div>

    <div class="content">
      <div class="folder-list">
        <div
          v-for="folder in folders"
          :key="folder.id"
          :class="['folder-item', { active: currentFolderId === folder.id }]"
          @click="
            currentFolderId = folder.id
            getFavoriteList()
          "
        >
          <div class="folder-info">
            <el-icon><FolderOpened /></el-icon>
            <span>{{ folder.name }}</span>
          </div>
          <div class="folder-meta">
            <span>{{ folder.audioCount }} 个音频</span>
            <span>{{ folder.createTime }}</span>
            <el-button link type="danger" @click.stop="handleDeleteFolder(folder.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <div class="audio-list">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        <div v-else-if="audioList.length === 0" class="empty">
          <el-empty description="暂无收藏" />
        </div>
        <div v-else>
          <div class="audio-grid">
            <div v-for="audio in audioList" :key="audio.id" class="audio-card">
              <img :src="audio.coverUrl" alt="封面" class="cover" />
              <div class="audio-info">
                <h4>{{ audio.title }}</h4>
                <p>{{ audio.creatorName }}</p>
                <div class="stats">
                  <span
                    ><el-icon><Headset /></el-icon> {{ audio.playCount }}</span
                  >
                  <span
                    ><el-icon><Star /></el-icon> {{ audio.collectCount }}</span
                  >
                </div>
              </div>
              <div class="actions">
                <el-button type="primary" size="small" @click="handlePlayAudio(audio.id)">
                  播放
                </el-button>
                <el-button size="small" type="danger" @click="handleRemoveFavorite(audio.id)">
                  移除
                </el-button>
              </div>
            </div>
          </div>

          <!-- 分页组件 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[1, 12, 24, 36]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showCreateFolderDialog" title="新建收藏夹" width="400px">
      <el-form :model="folderForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="folderForm.name" placeholder="请输入收藏夹名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="folderForm.description"
            type="textarea"
            placeholder="请输入描述（选填）"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateFolder">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.favorite-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content {
  display: flex;
  gap: 20px;
}

.folder-list {
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 10px;
}

.folder-item {
  padding: 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.folder-item:hover {
  background: #f5f5f5;
}

.folder-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.folder-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.folder-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  opacity: 0.8;
}

.audio-list {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 500px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  gap: 10px;
  color: #999;
}

.audio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.audio-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.audio-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.audio-info {
  padding: 15px;
}

.audio-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audio-info p {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.actions {
  padding: 10px 15px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.actions .el-button {
  flex: 1;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>

