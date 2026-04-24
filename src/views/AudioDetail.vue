<template>
  <div class="audio-detail-page">
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>音频详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-content">
      <div v-if="audioDetail" class="audio-detail-card">
        <!-- 左侧：封面 + 简介 -->
        <div class="cover-section">
          <img :src="audioDetail.coverUrl" class="cover-image" />

          <div class="content-section collapsible-section cover-description">
            <div class="section-header" @click="toggleSection('description')">
              <h3>简介</h3>
              <el-icon :class="{ rotated: !sectionCollapsed.description }">
                <ArrowDown />
              </el-icon>
            </div>
            <el-collapse-transition>
              <div v-show="!sectionCollapsed.description" class="section-content">
                <p>{{ audioDetail.description || '暂无简介' }}</p>
              </div>
            </el-collapse-transition>
          </div>
        </div>

        <!-- 右侧：信息区 -->
        <div class="info-section">
          <h1 class="audio-title">{{ audioDetail.title }}</h1>

          <div class="creator-row">
            <div class="creator-info" @click="goToCreator">
              <img :src="audioDetail.creator?.avatar || defaultAvatar" class="creator-avatar" />
              <span class="creator-name">{{ audioDetail.creator?.nickname || '未知创作者' }}</span>
            </div>
            <el-button :class="['follow-btn', { followed: isFollowing }]" @click="handleFollow">
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </el-button>
          </div>

          <div class="stats-row">
            <span class="stat-item">
              <el-icon><Headset /></el-icon>
              {{ formatNumber(audioDetail.stats?.playCount) }}
            </span>
            <span class="stat-item">
              <el-icon><Star /></el-icon>
              {{ formatNumber(audioDetail.stats?.collectCount) }}
            </span>
            <span class="stat-item">
              <el-icon><Pointer /></el-icon>
              {{ formatNumber(audioDetail.stats?.likeCount) }}
            </span>
          </div>

          <div class="meta-row">
            <div
              v-if="audioDetail.isPaid"
              class="price-wrapper"
              :class="{ purchasable: !audioDetail.isPurchased }"
              @click="!audioDetail.isPurchased && (showPurchaseDialog = true)"
            >
              <span class="price-label">价格</span>
              <div class="price-content">
                <span class="price-value">¥{{ audioDetail.price }}</span>
                <span v-if="!audioDetail.isPurchased" class="purchase-hint">
                  <el-icon><ShoppingCart /></el-icon> 点击购买
                </span>
                <span v-else class="purchased-badge">
                  <el-icon><Check /></el-icon> 已购买
                </span>
              </div>
            </div>
            <div class="duration">
              <el-icon><Clock /></el-icon>
              {{ formatDuration(audioDetail.duration) }}
            </div>
          </div>

          <div class="action-buttons">
            <el-button class="play-btn" :icon="VideoPlay" @click="handlePlay"> 立即播放 </el-button>
            <el-button
              :class="['action-btn', { active: isLiked }]"
              :icon="Star"
              @click="handleLike"
            >
              {{ isLiked ? '已喜欢' : '喜欢' }}
            </el-button>
            <el-button class="action-btn" :icon="FolderOpened" @click="openFolderManager">
              收藏
            </el-button>
            <!-- AI 转写按钮 -->
            <el-button
              v-if="audioDetail.transcript"
              class="action-btn ai-transcript-btn"
              @click="showTranscriptDialog = true"
            >
              <el-icon><Document /></el-icon>
              AI 转写
            </el-button>
          </div>

          <div v-if="audioDetail.isPaid && !audioDetail.isPurchased" class="trial-alert">
            <el-alert type="info" :closable="false">
              该音频为付费内容，试听时长 {{ audioDetail.trialDuration }} 秒
            </el-alert>
          </div>

          <!-- AI 摘要区块（可折叠） -->
          <div v-if="audioDetail.summary" class="content-section collapsible-section">
            <div class="section-header" @click="toggleSection('summary')">
              <h3>
                <el-icon class="ai-icon"><MagicStick /></el-icon>
                AI 摘要
              </h3>
              <el-icon :class="{ rotated: !sectionCollapsed.summary }">
                <ArrowDown />
              </el-icon>
            </div>
            <el-collapse-transition>
              <div v-show="!sectionCollapsed.summary" class="section-content ai-content">
                <p>{{ audioDetail.summary }}</p>
              </div>
            </el-collapse-transition>
          </div>
        </div>
      </div>

      <CommentSection v-if="audioDetail" :audio-id="audioId" />
    </div>

    <AudioPlayer
      v-if="showPlayer"
      :audio-id="audioId"
      :title="audioDetail.title"
      :creator-name="audioDetail.creator?.nickname || '未知创作者'"
      :cover-url="audioDetail.coverUrl"
      :duration="audioDetail.duration"
      :progress="audioDetail.progress || 0"
      :is-paid="audioDetail.isPaid"
      :is-purchased="audioDetail.isPurchased"
      @close="showPlayer = false"
    />

    <!-- AI 转写弹窗 -->
    <el-dialog
      v-model="showTranscriptDialog"
      title="AI 转写"
      width="680px"
      class="transcript-dialog"
      destroy-on-close
    >
      <div class="transcript-dialog-content">
        <div class="transcript-header">
          <el-icon class="ai-icon-large"><MagicStick /></el-icon>
          <span>以下内容由 AI 生成，仅供参考</span>
        </div>
        <div class="transcript-body">
          <p>{{ audioDetail?.transcript || '暂无转写内容' }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="close-btn" @click="showTranscriptDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 收藏夹管理弹窗 -->
    <el-dialog
      v-model="showFolderManager"
      title="管理收藏夹"
      width="520px"
      class="folder-manager-dialog"
      destroy-on-close
    >
      <div class="folder-list">
        <div v-for="folder in allFolders" :key="folder.id" class="folder-manager-item">
          <el-checkbox v-model="folderCheckStatus[folder.id]">
            <span class="folder-name">{{ folder.name }}</span>
            <span class="folder-count">{{ folder.audioCount }} 个音频</span>
            <span v-if="folder.description" class="folder-description">{{
              folder.description
            }}</span>
          </el-checkbox>
        </div>
      </div>
      <div class="folder-create-tip" @click="openCreateFolderDialog">
        <el-icon><Plus /></el-icon>
        <span>新建收藏夹</span>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="cancel-btn" @click="showFolderManager = false">取消</el-button>
          <el-button class="confirm-btn" :loading="savingFolders" @click="saveFolderChanges">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建收藏夹对话框 -->
    <el-dialog
      v-model="showCreateFolderDialog"
      title="新建收藏夹"
      width="420px"
      class="folder-create-dialog"
      destroy-on-close
    >
      <el-form :model="newFolderForm">
        <el-form-item label="收藏夹名称">
          <el-input
            v-model="newFolderForm.name"
            placeholder="请输入收藏夹名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="newFolderForm.description"
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

    <!-- 购买弹窗 -->
    <el-dialog
      v-model="showPurchaseDialog"
      title="购买音频"
      width="420px"
      class="purchase-dialog"
      align-center
    >
      <div class="purchase-content">
        <div class="purchase-icon">
          <el-icon :size="48" color="#f7ba2a"><Lock /></el-icon>
        </div>
        <p class="purchase-title">购买后即可收听完整版</p>
        <div class="purchase-info">
          <div class="info-row">
            <span class="label">音频价格</span>
            <span class="value price">{{ audioDetail?.price }} 虚拟币</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPurchaseDialog = false">取消</el-button>
          <el-button type="primary" @click="handlePurchase">立即购买</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Headset,
  Star,
  Pointer,
  VideoPlay,
  FolderOpened,
  Plus,
  ShoppingCart,
  Check,
  Clock,
  ArrowDown,
  Document,
  MagicStick,
} from '@element-plus/icons-vue'
import { audioApi } from '@/api/user/audio'
import { likeApi } from '@/api/user/like'
import { favoriteApi } from '@/api/user/favorite'
import { followApi } from '@/api/user/follow'
import { orderApi } from '@/api/user/order'
import { profileApi } from '@/api/user/profile'
import { useUserStore } from '@/stores/user/user'
import AudioPlayer from '@/components/AudioPlayer.vue'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const audioId = ref(Number(route.params.id))
const audioDetail = ref(null)
const showPlayer = ref(false)
const isLiked = ref(false)
const isFollowing = ref(false)
const showPurchaseDialog = ref(false)
const showTranscriptDialog = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 折叠状态管理
const sectionCollapsed = reactive({
  description: false, // 简介默认展开
  summary: true, // 摘要默认折叠
})

const toggleSection = (section) => {
  sectionCollapsed[section] = !sectionCollapsed[section]
}

const allFolders = ref([])
const collectedFolderIds = ref(new Set())
const folderCheckStatus = reactive({})
const showFolderManager = ref(false)
const showCreateFolderDialog = ref(false)
const creatingFolder = ref(false)
const savingFolders = ref(false)
const newFolderForm = ref({ name: '', description: '' })
let originalCollectedSet = new Set()

const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num.toString()
}

const formatDuration = (seconds) => {
  if (!seconds) return '0分钟'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (mins === 0) return `${secs}秒`
  return `${mins}分${secs}秒`
}

const getAudioDetail = async () => {
  try {
    const res = await audioApi.getAudioDetail(audioId.value)
    audioDetail.value = res.data
    isLiked.value = res.data.isLike || false
    isFollowing.value = res.data.creator?.isFollow || false
    await loadCollectedFolderIds()
  } catch (error) {
    console.error('获取音频详情失败:', error)
    ElMessage.error('获取音频详情失败')
  }
}

const handlePurchase = async () => {
  try {
    await orderApi.purchaseAudio(audioId.value)
    ElMessage.success('购买成功')
    showPurchaseDialog.value = false
    await getAudioDetail()
    const profileRes = await profileApi.getProfile()
    userStore.setUserInfo(profileRes.data)
  } catch (error) {
    console.error('购买失败', error)
    ElMessage.error(error.response?.data?.msg || '购买失败')
  }
}

const loadCollectedFolderIds = async () => {
  try {
    const res = await favoriteApi.getAudioFolders(audioId.value)
    const folders = res.data || []
    collectedFolderIds.value = new Set(folders.map((f) => Number(f.id)))
  } catch (error) {
    console.error('获取音频收藏夹失败:', error)
    collectedFolderIds.value = new Set()
  }
}

const loadAllFolders = async () => {
  try {
    const res = await favoriteApi.getFolderList()
    allFolders.value = (res.data || []).map((f) => ({
      ...f,
      id: Number(f.id),
    }))
    for (const folder of allFolders.value) {
      folderCheckStatus[folder.id] = collectedFolderIds.value.has(folder.id)
    }
    originalCollectedSet = new Set(collectedFolderIds.value)
  } catch (error) {
    console.error('获取收藏夹列表失败:', error)
    allFolders.value = []
  }
}

const openFolderManager = async () => {
  await loadAllFolders()
  showFolderManager.value = true
}

const saveFolderChanges = async () => {
  const toAdd = []
  const toRemove = []
  for (const folder of allFolders.value) {
    const isNowChecked = folderCheckStatus[folder.id] === true
    const wasChecked = originalCollectedSet.has(folder.id)
    if (isNowChecked && !wasChecked) {
      toAdd.push(folder.id)
    } else if (!isNowChecked && wasChecked) {
      toRemove.push(folder.id)
    }
  }
  if (toAdd.length === 0 && toRemove.length === 0) {
    showFolderManager.value = false
    return
  }

  savingFolders.value = true
  try {
    for (const folderId of toAdd) {
      await favoriteApi.collectAudio(audioId.value, folderId)
    }
    for (const folderId of toRemove) {
      await favoriteApi.uncollectAudio(audioId.value, folderId)
    }
    await loadCollectedFolderIds()
    if (audioDetail.value?.stats) {
      const delta = toAdd.length - toRemove.length
      audioDetail.value.stats.collectCount += delta
    }
    ElMessage.success('收藏夹已更新')
    showFolderManager.value = false
  } catch (error) {
    console.error('保存收藏夹失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    savingFolders.value = false
  }
}

const openCreateFolderDialog = () => {
  newFolderForm.value = { name: '', description: '' }
  showCreateFolderDialog.value = true
}

const handleCreateFolder = async () => {
  if (!newFolderForm.value.name.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  creatingFolder.value = true
  try {
    await favoriteApi.saveFavoriteFolder({
      name: newFolderForm.value.name,
      description: newFolderForm.value.description,
    })
    ElMessage.success('创建成功')
    showCreateFolderDialog.value = false
    await loadAllFolders()
    const newFolder = allFolders.value.find((f) => f.name === newFolderForm.value.name)
    if (newFolder) {
      folderCheckStatus[newFolder.id] = true
    }
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  } finally {
    creatingFolder.value = false
  }
}

const handlePlay = () => {
  showPlayer.value = true
}

const handleLike = async () => {
  const originState = isLiked.value
  isLiked.value = !isLiked.value
  if (audioDetail.value?.stats) {
    audioDetail.value.stats.likeCount += isLiked.value ? 1 : -1
  }
  try {
    if (isLiked.value) {
      await likeApi.likeAudio(audioId.value)
    } else {
      await likeApi.unlikeAudio(audioId.value)
    }
    ElMessage.success(isLiked.value ? '已添加到喜欢' : '已取消喜欢')
  } catch (error) {
    isLiked.value = originState
    if (audioDetail.value?.stats) {
      audioDetail.value.stats.likeCount += isLiked.value ? 1 : -1
    }
    ElMessage.error('操作失败')
  }
}

const handleFollow = async () => {
  if (!audioDetail.value?.creator?.id) return
  const creatorId = audioDetail.value.creator.id
  const originState = isFollowing.value
  isFollowing.value = !isFollowing.value
  try {
    if (originState) {
      await followApi.unfollowCreator(creatorId)
      ElMessage.success('已取消关注')
    } else {
      await followApi.followCreator(creatorId)
      ElMessage.success('关注成功')
    }
  } catch (error) {
    isFollowing.value = originState
    ElMessage.error('操作失败')
  }
}

const goToCreator = () => {
  if (audioDetail.value?.creator?.id) {
    router.push(`/creator/${audioDetail.value.creator.id}`)
  }
}

onMounted(() => {
  getAudioDetail()
})
</script>

<style scoped>
.audio-detail-page {
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

.detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
}

.audio-detail-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 32px;
  padding: 32px;
  display: flex;
  gap: 36px;
  flex-wrap: wrap;
  margin-bottom: 28px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.cover-section {
  flex-shrink: 0;
  width: 220px;
}

.cover-image {
  width: 220px;
  height: 220px;
  border-radius: 24px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

/* 封面下方的简介区块 */
.cover-description {
  margin-top: 16px;
  border-top: 1px solid #f0efeb;
  padding-top: 12px;
}

.cover-description .section-header {
  padding: 4px 0;
}

.cover-description .section-content {
  padding: 8px 0 4px;
}

.info-section {
  flex: 1;
  min-width: 0;
}

.audio-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.creator-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0efeb;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.creator-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.creator-name {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a1a;
}

.follow-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  padding: 8px 24px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #1a1a1a;
  transition: all 0.2s;
  cursor: pointer;
}

.follow-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-1px);
}

.follow-btn.followed {
  background: #f0efeb;
  border-color: #e2e0d7;
  color: #8e8c84;
}

.stats-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #8e8c84;
}

.stat-item .el-icon {
  font-size: 16px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0efeb;
}

.price-wrapper {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.price-label {
  font-size: 0.85rem;
  color: #b0aea3;
}

.price-content {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
}

.purchase-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: #ff5a5f;
  cursor: pointer;
  transition: all 0.2s;
  padding: 4px 8px;
  border-radius: 20px;
  background: rgba(255, 90, 95, 0.1);
}

.purchase-hint:hover {
  background: #ff5a5f;
  color: #ffffff;
  transform: translateY(-1px);
}

.purchased-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
  padding: 4px 8px;
  border-radius: 20px;
}

.price-wrapper.purchasable {
  cursor: pointer;
}

.price-wrapper.purchasable .price-value {
  transition: color 0.2s;
}

.price-wrapper.purchasable:hover .price-value {
  color: #ff5a5f;
}

.duration {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  color: #b0aea3;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.action-buttons .el-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.play-btn {
  background: #1a1a1a;
  border: none;
  color: #ffffff;
}

.play-btn:hover {
  background: #2c2c2c;
  transform: translateY(-2px);
}

.action-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  color: #1a1a1a;
}

.action-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-2px);
}

.action-btn.active {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.ai-transcript-btn {
  color: #7c6a9c;
  border-color: #e0d8ec;
}

.ai-transcript-btn:hover {
  background: #f5f0fa;
  border-color: #c8b8dc;
  color: #5a4a72;
}

.trial-alert {
  margin-top: 16px;
}

.trial-alert :deep(.el-alert) {
  background: #f7f6f2;
  border-radius: 16px;
  border: none;
}

/* 统一折叠卡片样式 - 紧凑版 */
.content-section {
  margin-top: 12px;
  border-top: 1px solid #f0efeb;
  padding-top: 12px;
}

.content-section:first-of-type {
  margin-top: 0;
  border-top: none;
  padding-top: 0;
}

.collapsible-section .section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 4px 0;
  transition: opacity 0.2s;
}

.collapsible-section .section-header:hover {
  opacity: 0.7;
}

.collapsible-section .section-header h3 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #8e8c84;
  margin: 0;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.collapsible-section .section-header .el-icon {
  font-size: 16px;
  color: #b0aea3;
  transition: transform 0.3s;
}

.collapsible-section .section-header .el-icon.rotated {
  transform: rotate(180deg);
}

.section-content {
  padding: 8px 0 4px;
}

.section-content p {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #6b6a62;
  margin: 0;
}

.ai-content {
  background: #fcfbf7;
  padding: 12px;
  border-radius: 12px;
  margin-top: 2px;
}

.ai-icon {
  color: #a28bc2;
}

/* AI 转写弹窗样式 */
.transcript-dialog :deep(.el-dialog) {
  border-radius: 28px;
  background: #ffffff;
}

.transcript-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 16px;
  border-bottom: 1px solid #f0efeb;
}

.transcript-dialog :deep(.el-dialog__title) {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.transcript-dialog :deep(.el-dialog__body) {
  padding: 20px 28px;
}

.transcript-dialog-content {
  display: flex;
  flex-direction: column;
}

.transcript-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px dashed #e8e6df;
  font-size: 0.8rem;
  color: #b0aea3;
}

.ai-icon-large {
  font-size: 18px;
  color: #a28bc2;
}

.transcript-body {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.transcript-body p {
  font-size: 0.95rem;
  line-height: 1.8;
  color: #4a4a44;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.transcript-body::-webkit-scrollbar {
  width: 4px;
}

.transcript-body::-webkit-scrollbar-thumb {
  background: #e0d8ec;
  border-radius: 4px;
}

.transcript-dialog .dialog-footer {
  display: flex;
  justify-content: center;
}

.transcript-dialog .close-btn {
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 10px 32px;
  font-weight: 500;
  color: #ffffff;
  transition: all 0.2s;
}

.transcript-dialog .close-btn:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

/* 收藏夹相关样式 */
.folder-manager-item {
  display: flex;
  align-items: center;
  padding: 14px 12px;
  border-bottom: 1px solid #f0efeb;
}

.folder-manager-item :deep(.el-checkbox) {
  width: 100%;
}

.folder-manager-item :deep(.el-checkbox__label) {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.folder-name {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1a1a1a;
  flex-shrink: 0;
}

.folder-count {
  font-size: 0.7rem;
  color: #8e8c84;
  flex-shrink: 0;
}

.folder-description {
  font-size: 0.75rem;
  color: #b0aea3;
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-create-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  margin-top: 8px;
  border-radius: 20px;
  background: #f7f6f2;
  color: #1a1a1a;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.folder-create-tip:hover {
  background: #efeee8;
  transform: translateY(-1px);
}

.folder-manager-dialog :deep(.el-dialog) {
  border-radius: 28px;
}

.folder-manager-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.folder-create-dialog :deep(.el-dialog) {
  border-radius: 28px;
  background: #ffffff;
}

.folder-create-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 16px;
  border-bottom: 1px solid #f0efeb;
}

.folder-create-dialog :deep(.el-dialog__title) {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a1a;
}

.folder-create-dialog :deep(.el-dialog__body) {
  padding: 24px 28px;
}

.folder-create-dialog :deep(.el-dialog__footer) {
  padding: 16px 28px 24px;
  border-top: 1px solid #f0efeb;
}

.folder-create-dialog :deep(.el-form-item) {
  margin-bottom: 20px;
}

.folder-create-dialog :deep(.el-form-item__label) {
  font-size: 0.85rem;
  font-weight: 500;
  color: #1a1a1a;
  padding-bottom: 6px;
}

.folder-create-dialog :deep(.el-input__wrapper) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 16px;
  box-shadow: none;
}

.folder-create-dialog :deep(.el-input__wrapper:hover) {
  border-color: #dcdbd3;
}

.folder-create-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 2px rgba(26, 26, 26, 0.05);
}

.folder-create-dialog :deep(.el-textarea__inner) {
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

.dialog-footer .confirm-btn:disabled {
  background: #e8e6df;
  color: #b0aea3;
  cursor: not-allowed;
}

.purchase-dialog :deep(.el-dialog) {
  border-radius: 24px;
}

.purchase-content {
  text-align: center;
  padding: 20px;
}

.purchase-icon {
  margin-bottom: 16px;
}

.purchase-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.purchase-info {
  background: #fcfbf7;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.info-row .label {
  color: #b0aea3;
  font-size: 0.85rem;
}

.info-row .value {
  font-weight: 600;
  color: #1a1a1a;
}

.info-row .value.price {
  color: #1a1a1a;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .audio-detail-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 24px;
  }
  .cover-section {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .cover-image {
    width: 180px;
    height: 180px;
  }
  .cover-description {
    width: 100%;
    max-width: 260px;
  }
  .creator-row {
    flex-direction: column;
    gap: 12px;
  }
  .stats-row {
    justify-content: center;
  }
  .meta-row {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  .action-buttons {
    justify-content: center;
  }
  .collapsible-section .section-header {
    justify-content: center;
    gap: 8px;
  }
  .folder-manager-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .folder-manager-item :deep(.el-checkbox__label) {
    flex-wrap: wrap;
  }
  .folder-description {
    white-space: normal;
  }
  .folder-manager-dialog :deep(.el-dialog) {
    width: calc(100% - 40px) !important;
  }
  .folder-create-dialog :deep(.el-dialog) {
    width: calc(100% - 40px) !important;
  }
  .purchase-dialog :deep(.el-dialog) {
    width: calc(100% - 40px) !important;
  }
  .transcript-dialog :deep(.el-dialog) {
    width: calc(100% - 40px) !important;
  }
}
</style>
