<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElDialog } from 'element-plus'
import { audioApi } from '@/api/user/audio'
import { likeApi } from '@/api/user/like'
import { favoriteApi } from '@/api/user/favorite'
import { followApi } from '@/api/user/follow'
import AudioPlayer from '@/components/AudioPlayer.vue'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()

const audioId = ref(Number(route.params.id))
const audioDetail = ref(null)
const showPlayer = ref(false)
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)

// 收藏夹相关
const showFolderDialog = ref(false)
const folders = ref([])
const selectedFolderId = ref(null)

const getAudioDetail = async () => {
  try {
    const res = await audioApi.getAudioDetail(audioId.value)
    audioDetail.value = res
  } catch (error) {
    ElMessage.error('获取音频详情失败')
  }
}

// 获取收藏夹列表
const getFolderList = async () => {
  try {
    const res = await favoriteApi.getFolderList()
    folders.value = res.data
    if (folders.value.length > 0) {
      selectedFolderId.value = folders.value[0].id
    }
  } catch (error) {
    console.error('获取收藏夹列表失败:', error)
  }
}

const handlePlay = () => {
  showPlayer.value = true
}

const handleLike = async () => {
  try {
    await likeApi.saveAudioLike({
      audioId: audioId.value,
      action: isLiked.value ? 'UNLIKE' : 'LIKE',
    })
    isLiked.value = !isLiked.value
    if (audioDetail.value) {
      audioDetail.value.stats.likeCount += isLiked.value ? 1 : -1
    }
    ElMessage.success(isLiked.value ? '已添加到喜欢' : '已取消喜欢')
  } catch (error) {
    console.error(error.message)
  }
}

const handleCollect = async () => {
  if (isCollected.value) {
    // 取消收藏
    try {
      await favoriteApi.saveAudioAction({
        audioId: audioId.value,
        action: 'UNCOLLECT',
      })
      isCollected.value = false
      if (audioDetail.value) {
        audioDetail.value.stats.collectCount -= 1
      }
      ElMessage.success('已取消收藏')
    } catch (error) {
      console.error(error.message)
      ElMessage.error('取消收藏失败')
    }
  } else {
    // 打开收藏夹选择对话框
    await getFolderList()
    showFolderDialog.value = true
  }
}

// 关注/取消关注创作者
const handleFollow = async () => {
  // 注意：需要根据实际返回的数据结构获取 creatorId
  // 假设 audioDetail.value.creator.id 存在，否则使用临时值
  if (!audioDetail.value || !audioDetail.value.creator) {
    return
  }

  const creatorId = audioDetail.value.creator.id

  try {
    if (isFollowing.value) {
      await followApi.unfollowCreator(creatorId)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await followApi.followCreator(creatorId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  }
}

// 确认收藏到选定的收藏夹
const confirmCollect = async () => {
  if (!selectedFolderId.value) {
    ElMessage.warning('请选择一个收藏夹')
    return
  }

  try {
    await favoriteApi.saveAudioAction({
      audioId: audioId.value,
      folderId: selectedFolderId.value,
      action: 'COLLECT',
    })
    isCollected.value = true
    if (audioDetail.value) {
      audioDetail.value.stats.collectCount += 1
    }
    showFolderDialog.value = false
    ElMessage.success('已添加到收藏夹')
  } catch (error) {
    console.error(error.message)
    ElMessage.error('添加到收藏夹失败')
  }
}

onMounted(() => {
  getAudioDetail()
})
</script>

<template>
  <div class="audio-detail-container">
    <div v-if="audioDetail" class="audio-detail">
      <div class="header">
        <img :src="audioDetail.coverUrl" alt="封面" class="cover" />
        <div class="info">
          <h1>{{ audioDetail.title }}</h1>
          <div class="creator">
            <img :src="audioDetail.creator.avatar" alt="头像" class="creator-avatar" />
            <span>{{ audioDetail.creator.nickname }}</span>
          </div>
          <div class="stats">
            <span
              ><el-icon><Headset /></el-icon> {{ audioDetail.stats.playCount }}</span
            >
            <span
              ><el-icon><Star /></el-icon> {{ audioDetail.stats.collectCount }}</span
            >
            <span
              ><el-icon><Pointer /></el-icon> {{ audioDetail.stats.likeCount }}</span
            >
          </div>
          <div class="meta">
            <span v-if="audioDetail.isPaid" class="price">¥{{ audioDetail.price }}</span>
            <span class="duration">{{ Math.floor(audioDetail.duration / 60) }} 分钟</span>
          </div>
        </div>
      </div>

      <div class="description">
        <h3>简介</h3>
        <p>{{ audioDetail.description || '暂无简介' }}</p>
      </div>

      <div class="actions">
        <el-button type="primary" size="large" @click="handlePlay">
          <el-icon><VideoPlay /></el-icon> 立即播放
        </el-button>
        <el-button size="large" :type="isLiked ? 'danger' : 'default'" @click="handleLike">
          <el-icon><Star /></el-icon> {{ isLiked ? '已喜欢' : '喜欢' }}
        </el-button>
        <el-button size="large" :type="isCollected ? 'warning' : 'default'" @click="handleCollect">
          <el-icon><FolderOpened /></el-icon> {{ isCollected ? '已收藏' : '收藏' }}
        </el-button>
        <el-button size="large" :type="isFollowing ? 'default' : 'info'" @click="handleFollow">
          <el-icon><UserFilled /></el-icon> {{ isFollowing ? '已关注' : '关注' }}
        </el-button>
      </div>

      <!-- 收藏夹选择对话框 -->
      <el-dialog v-model="showFolderDialog" title="选择收藏夹" width="400px">
        <div v-if="folders.length === 0" class="empty-folders">
          <el-empty description="暂无收藏夹" />
          <el-button type="primary" style="margin-top: 20px" @click="showFolderDialog = false">
            关闭
          </el-button>
        </div>
        <div v-else class="folder-list">
          <div
            v-for="folder in folders"
            :key="folder.id"
            :class="['folder-item', { active: selectedFolderId === folder.id }]"
            @click="selectedFolderId = folder.id"
          >
            <el-icon><FolderOpened /></el-icon>
            <span>{{ folder.name }}</span>
            <span class="audio-count">{{ folder.audioCount }} 个音频</span>
          </div>
        </div>
        <template #footer>
          <el-button @click="showFolderDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmCollect" :disabled="folders.length === 0">
            确认收藏
          </el-button>
        </template>
      </el-dialog>

      <div v-if="audioDetail.isPaid && !audioDetail.isPurchased" class="trial-info">
        <el-alert type="info" :closable="false">
          该音频为付费内容，试听时长 {{ audioDetail.trialDuration }} 秒
        </el-alert>
      </div>
    </div>

    <AudioPlayer v-if="showPlayer" :audio-id="audioId" @close="showPlayer = false" />
    <CommentSection v-if="audioDetail" :audio-id="audioId" />
  </div>
</template>

<style scoped>
@import '@/resource/css/audioDetail.css';

.folder-list {
  max-height: 300px;
  overflow-y: auto;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.folder-item:hover {
  background-color: #f5f5f5;
}

.folder-item.active {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.folder-item .audio-count {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.empty-folders {
  padding: 40px 0;
  text-align: center;
}
</style>

