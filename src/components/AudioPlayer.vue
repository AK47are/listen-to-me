<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { audioApi } from '@/api/user/audio'
import { likeApi } from '@/api/user/like'
import { favoriteApi } from '@/api/user/favorite'
import { fileApi } from '@/api/common/file'
import { balanceApi } from '@/api/user/balance'
import { orderApi } from '@/api/user/order'
import { historyApi } from '@/api/user/history'

const props = defineProps({
  audioId: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['close'])

const audioDetail = ref(null)
const audioUrl = ref('')
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.8)
const isMuted = ref(false)
const isLiked = ref(false)
const isCollected = ref(false)
const showPurchaseDialog = ref(false)
const balance = ref(0)
const isDragging = ref(false)
const dragPercent = ref(0)
const isExpanded = ref(false)

const audioPlayer = ref(null)
const progressBarRef = ref(null)

const progressPercent = computed(() => {
  if (duration.value === 0) return 0
  if (isDragging.value) return dragPercent.value
  return (currentTime.value / duration.value) * 100
})

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getAudioDetail = async () => {
  try {
    const res = await audioApi.getAudioDetail(props.audioId)
    audioDetail.value = res
    duration.value = res.duration
    currentTime.value = res.progress || 0
  } catch (error) {
    ElMessage.error('获取音频详情失败')
  }
}

const getStreamSign = async () => {
  try {
    const res = await fileApi.getStreamSign(props.audioId)
    audioUrl.value = res
  } catch (error) {
    if (error.response?.status === 403) {
      showPurchaseDialog.value = true
      getBalance()
    } else {
      ElMessage.error('获取播放地址失败')
    }
  }
}

const getBalance = async () => {
  try {
    const res = await balanceApi.getBalance()
    balance.value = res.balance
  } catch (error) {
    ElMessage.error('获取余额失败')
  }
}

const togglePlay = async () => {
  if (!audioUrl.value) {
    await getStreamSign()
  }
  if (audioUrl.value) {
    if (isPlaying.value) {
      audioPlayer.value.pause()
      isPlaying.value = false
    } else {
      audioPlayer.value.play()
      isPlaying.value = true
    }
  }
}

const handleTimeUpdate = () => {
  if (!isDragging.value) {
    currentTime.value = audioPlayer.value.currentTime
  }
}

const handleLoadedMetadata = () => {
  duration.value = audioPlayer.value.duration
}

const handleEnded = () => {
  isPlaying.value = false
  currentTime.value = 0
}

const handleProgressMouseDown = (e) => {
  isDragging.value = true
  updateDragPercent(e)
  document.addEventListener('mousemove', handleProgressMouseMove)
  document.addEventListener('mouseup', handleProgressMouseUp)
}

const handleProgressMouseMove = (e) => {
  if (isDragging.value) {
    updateDragPercent(e)
  }
}

const handleProgressMouseUp = () => {
  if (isDragging.value) {
    isDragging.value = false
    audioPlayer.value.currentTime = (dragPercent.value / 100) * duration.value
    document.removeEventListener('mousemove', handleProgressMouseMove)
    document.removeEventListener('mouseup', handleProgressMouseUp)
  }
}

const updateDragPercent = (e) => {
  if (!progressBarRef.value) return
  const rect = progressBarRef.value.getBoundingClientRect()
  const percent = ((e.clientX - rect.left) / rect.width) * 100
  dragPercent.value = Math.max(0, Math.min(100, percent))
}

const handleProgressClick = (e) => {
  if (!progressBarRef.value) return
  const rect = progressBarRef.value.getBoundingClientRect()
  const percent = ((e.clientX - rect.left) / rect.width) * 100
  const newTime = (percent / 100) * duration.value
  audioPlayer.value.currentTime = newTime
  currentTime.value = newTime
}

const toggleMute = () => {
  isMuted.value = !isMuted.value
  audioPlayer.value.muted = isMuted.value
}

const handleVolumeChange = (value) => {
  volume.value = value
  audioPlayer.value.volume = value / 100
  if (value > 0 && isMuted.value) {
    isMuted.value = false
    audioPlayer.value.muted = false
  }
}

const toggleLike = async () => {
  try {
    await likeApi.saveAudioLike({
      audioId: props.audioId,
      action: isLiked.value ? 'UNLIKE' : 'LIKE',
    })
    isLiked.value = !isLiked.value
    if (audioDetail.value) {
      audioDetail.value.stats.likeCount += isLiked.value ? 1 : -1
    }
    ElMessage.success(isLiked.value ? '已添加到喜欢' : '已取消喜欢')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleCollect = async () => {
  try {
    await favoriteApi.saveAudioAction({
      audioId: props.audioId,
      action: isCollected.value ? 'UNCOLLECT' : 'COLLECT',
    })
    isCollected.value = !isCollected.value
    if (audioDetail.value) {
      audioDetail.value.stats.collectCount += isCollected.value ? 1 : -1
    }
    ElMessage.success(isCollected.value ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handlePurchase = async () => {
  try {
    const res = await orderApi.purchaseAudio(props.audioId)
    if (res.status === 'SUCCESS') {
      ElMessage.success('购买成功')
      showPurchaseDialog.value = false
      await getStreamSign()
      await getAudioDetail()
    } else {
      ElMessage.error('购买失败，余额不足')
    }
  } catch (error) {
    ElMessage.error('购买失败')
  }
}

const syncProgress = async () => {
  if (currentTime.value > 0) {
    try {
      await historyApi.saveProgress({
        audioId: props.audioId,
        lastPosition: Math.floor(currentTime.value),
      })
    } catch (error) {
      console.error('同步进度失败', error)
    }
  }
}

let progressTimer = null

onMounted(async () => {
  await getAudioDetail()
  if (audioDetail.value?.progress > 0) {
    await getStreamSign()
  }

  progressTimer = setInterval(syncProgress, 10000)
})

onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
  syncProgress()
  document.removeEventListener('mousemove', handleProgressMouseMove)
  document.removeEventListener('mouseup', handleProgressMouseUp)
})
</script>

<template>
  <!-- 迷你底部播放器 - 不遮挡页面内容 -->
  <div class="mini-player" :class="{ expanded: isExpanded }">
    <!-- 进度条（在播放器顶部） -->
    <div
      class="progress-bar"
      ref="progressBarRef"
      @click="handleProgressClick"
      @mousedown="handleProgressMouseDown"
    >
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
    </div>

    <div class="player-content">
      <!-- 左侧：音频信息 -->
      <div class="audio-info-section" @click="isExpanded = !isExpanded">
        <div class="cover-wrapper">
          <img
            :src="audioDetail?.coverUrl"
            alt="封面"
            class="cover"
            :class="{ 'is-playing': isPlaying }"
          />
          <div class="playing-indicator" v-if="isPlaying">
            <span></span><span></span><span></span>
          </div>
        </div>
        <div class="info">
          <h3 class="title">{{ audioDetail?.title }}</h3>
          <p class="creator">{{ audioDetail?.creator?.nickname }}</p>
        </div>
        <el-icon class="expand-icon"><ArrowUp v-if="isExpanded" /><ArrowDown v-else /></el-icon>
      </div>

      <!-- 中间：播放控制 -->
      <div class="player-controls-section">
        <div class="control-buttons">
          <button class="control-btn icon-btn" :class="{ active: isLiked }" @click="toggleLike">
            <el-icon><StarFilled v-if="isLiked" /><Star v-else /></el-icon>
          </button>

          <button class="control-btn play-btn" @click="togglePlay">
            <el-icon :size="24">
              <VideoPause v-if="isPlaying" />
              <VideoPlay v-else />
            </el-icon>
          </button>

          <button
            class="control-btn icon-btn"
            :class="{ active: isCollected }"
            @click="toggleCollect"
          >
            <el-icon><FolderOpened /></el-icon>
          </button>
        </div>
        <div class="time-display">
          <span>{{ formatTime(currentTime) }}</span>
          <span class="separator">/</span>
          <span>{{ formatTime(duration) }}</span>
        </div>
      </div>

      <!-- 右侧：音量和关闭 -->
      <div class="extra-controls-section">
        <div class="volume-control">
          <button class="control-btn icon-btn" @click="toggleMute">
            <el-icon>
              <Mute v-if="isMuted || volume === 0" />
              <Headset v-else />
            </el-icon>
          </button>
          <div class="volume-slider-wrapper">
            <el-slider
              v-model="volume"
              :min="0"
              :max="100"
              :step="1"
              @input="handleVolumeChange"
              size="small"
            />
          </div>
        </div>

        <button class="control-btn icon-btn close-btn" @click="emit('close')">
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </div>

    <!-- 展开后的详细信息 -->
    <div v-if="isExpanded" class="expanded-content">
      <div class="expanded-info">
        <img
          :src="audioDetail?.coverUrl"
          alt="封面"
          class="expanded-cover"
          :class="{ 'is-playing': isPlaying }"
        />
        <div class="expanded-details">
          <h2 class="expanded-title">{{ audioDetail?.title }}</h2>
          <p class="expanded-creator">
            <el-icon><User /></el-icon>
            {{ audioDetail?.creator?.nickname }}
          </p>
          <div class="expanded-stats">
            <span class="stat-item">
              <el-icon><Headset /></el-icon>
              {{ audioDetail?.stats?.playCount }}
            </span>
            <span class="stat-item">
              <el-icon><Star /></el-icon>
              {{ audioDetail?.stats?.collectCount }}
            </span>
            <span class="stat-item">
              <el-icon><Pointer /></el-icon>
              {{ audioDetail?.stats?.likeCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 音频元素 -->
    <audio
      ref="audioPlayer"
      :src="audioUrl"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoadedMetadata"
      @ended="handleEnded"
    ></audio>

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
        <p class="purchase-title">该音频需要购买后才能收听完整版</p>
        <div class="purchase-info">
          <div class="info-row">
            <span class="label">音频价格</span>
            <span class="value price">{{ audioDetail?.price }} 虚拟币</span>
          </div>
          <div class="info-row">
            <span class="label">当前余额</span>
            <span class="value" :class="{ insufficient: balance < audioDetail?.price }">
              {{ balance }} 虚拟币
            </span>
          </div>
        </div>
        <el-alert
          v-if="balance < audioDetail?.price"
          title="余额不足，请先充值"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
      <template #footer>
        <el-button @click="showPurchaseDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePurchase" :disabled="balance < audioDetail?.price">
          立即购买
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
@import '@/resource/css/audioPlayer.css';
</style>
