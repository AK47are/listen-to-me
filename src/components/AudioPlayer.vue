<template>
  <div class="mini-player">
    <div
      class="progress-bar"
      ref="progressBarRef"
      @click="handleProgressClick"
      @mousedown="handleProgressMouseDown"
    >
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        <div v-if="showTrialMark" class="trial-mark" :style="{ left: trialPercent + '%' }">
          <div class="trial-line"></div>
          <span class="trial-label">试听</span>
        </div>
      </div>
      <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
    </div>

    <div class="player-content">
      <div class="audio-info-section">
        <div class="cover-wrapper">
          <img :src="coverUrl" alt="封面" class="cover" :class="{ 'is-playing': isPlaying }" />
          <div class="playing-indicator" v-if="isPlaying">
            <span></span><span></span><span></span>
          </div>
        </div>
        <div class="info">
          <h3 class="title">{{ title }}</h3>
          <p class="creator">{{ creatorName }}</p>
          <div v-if="showTrialMark" class="trial-info">
            <el-icon><Clock /></el-icon>
            <span>试听 {{ formatTime(trialDuration) }}</span>
          </div>
        </div>
      </div>

      <div class="player-controls-section">
        <button class="control-btn play-btn" @click="togglePlay">
          <el-icon :size="24">
            <VideoPause v-if="isPlaying" />
            <VideoPlay v-else />
          </el-icon>
        </button>
        <div class="time-display">
          <span>{{ formatTime(currentTime) }}</span>
          <span class="separator">/</span>
          <span>{{ formatTime(duration) }}</span>
        </div>
      </div>

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
        <button class="control-btn icon-btn close-btn" @click="handleClose">
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </div>

    <audio
      v-if="audioUrl"
      ref="audioPlayer"
      :src="audioUrl"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoadedMetadata"
      @ended="handleEnded"
      @error="handleAudioError"
    ></audio>

    <div v-if="showTrialTip" class="trial-tip" @click="handleGoToPurchase">
      <el-icon><InfoFilled /></el-icon>
      <span>试听已结束，购买后可收听完整版</span>
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  VideoPause,
  Headset,
  Mute,
  Close,
  Clock,
  InfoFilled,
  ArrowRight,
} from '@element-plus/icons-vue'
import { fileApi } from '@/api/common/file'
import { historyApi } from '@/api/user/history'

const props = defineProps({
  audioId: { type: Number, required: true },
  title: { type: String, required: true },
  creatorName: { type: String, required: true },
  coverUrl: { type: String, required: true },
  duration: { type: Number, required: true },
  progress: { type: Number, default: 0 },
  isPaid: { type: Boolean, default: false },
  isPurchased: { type: Boolean, default: false },
  trialDuration: { type: Number, default: 0 },
  price: { type: Number, default: 0 },
})

const emit = defineEmits(['close', 'goToPurchase'])

const audioUrl = ref('')
const isPlaying = ref(false)
const currentTime = ref(props.progress)
const volume = ref(80)
const isMuted = ref(false)
const isDragging = ref(false)
const dragPercent = ref(0)
const showTrialTip = ref(false)
const hasPromptedPurchase = ref(false)
const isAudioReady = ref(false)

const audioPlayer = ref(null)
const progressBarRef = ref(null)

const showTrialMark = computed(() => {
  return props.isPaid && !props.isPurchased && props.trialDuration > 0
})

const trialPercent = computed(() => {
  if (props.duration === 0) return 0
  return (props.trialDuration / props.duration) * 100
})

const progressPercent = computed(() => {
  if (props.duration === 0) return 0
  if (isDragging.value) return dragPercent.value
  return (currentTime.value / props.duration) * 100
})

const formatTime = (seconds) => {
  if (!seconds || seconds < 0) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getStreamSign = async () => {
  try {
    const res = await fileApi.getStreamSign(props.audioId)
    let url = res
    if (typeof res === 'object') {
      url = res.data || res.url || res
    }
    if (url && typeof url === 'string' && url.startsWith('http')) {
      audioUrl.value = url
      await nextTick()
      isAudioReady.value = true
    } else {
      ElMessage.error('获取播放地址失败')
    }
  } catch (error) {
    if (error.response?.status === 403) {
      ElMessage.warning('该音频需要购买后才能收听完整版')
    } else {
      ElMessage.error('获取播放地址失败')
    }
  }
}

const togglePlay = async () => {
  if (!audioUrl.value) {
    await getStreamSign()
  }
  if (!audioUrl.value || !audioPlayer.value) {
    ElMessage.warning('音频尚未准备好')
    return
  }
  try {
    if (isPlaying.value) {
      audioPlayer.value.pause()
      isPlaying.value = false
    } else {
      await audioPlayer.value.play()
      isPlaying.value = true
    }
  } catch (error) {
    ElMessage.error('播放失败，请重试')
    isPlaying.value = false
  }
}

const handleTimeUpdate = () => {
  const player = audioPlayer.value
  if (!player) return
  try {
    if (!isDragging.value) {
      currentTime.value = player.currentTime || 0
    }
    if (
      showTrialMark.value &&
      !hasPromptedPurchase.value &&
      currentTime.value >= props.trialDuration
    ) {
      hasPromptedPurchase.value = true
      player.pause()
      isPlaying.value = false
      showTrialTip.value = true
      setTimeout(() => {
        showTrialTip.value = false
      }, 5000)
    }
  } catch (error) {}
}

const handleLoadedMetadata = () => {
  const player = audioPlayer.value
  if (!player) return
  try {
    if (currentTime.value > 0) {
      const seekTime = Math.min(
        currentTime.value,
        props.isPurchased ? props.duration : props.trialDuration,
      )
      player.currentTime = seekTime
    }
  } catch (error) {}
}

const handleEnded = () => {
  isPlaying.value = false
  currentTime.value = 0
  hasPromptedPurchase.value = false
}

const handleAudioError = () => {
  isPlaying.value = false
  isAudioReady.value = false
  ElMessage.error('音频加载失败，请稍后重试')
}

const handleProgressMouseDown = (e) => {
  if (!audioPlayer.value || !isAudioReady.value) return
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
  if (!isDragging.value) return
  isDragging.value = false
  const player = audioPlayer.value
  if (player && isAudioReady.value) {
    try {
      let targetPercent = dragPercent.value
      if (showTrialMark.value && !props.isPurchased) {
        targetPercent = Math.min(targetPercent, trialPercent.value)
      }
      const newTime = (targetPercent / 100) * props.duration
      player.currentTime = newTime
      currentTime.value = newTime
    } catch (error) {}
  }
  document.removeEventListener('mousemove', handleProgressMouseMove)
  document.removeEventListener('mouseup', handleProgressMouseUp)
}

const updateDragPercent = (e) => {
  if (!progressBarRef.value) return
  const rect = progressBarRef.value.getBoundingClientRect()
  let percent = ((e.clientX - rect.left) / rect.width) * 100
  percent = Math.max(0, Math.min(100, percent))
  if (showTrialMark.value && !props.isPurchased) {
    percent = Math.min(percent, trialPercent.value)
  }
  dragPercent.value = percent
}

const handleProgressClick = (e) => {
  const player = audioPlayer.value
  if (!progressBarRef.value || !player || !isAudioReady.value) return
  try {
    const rect = progressBarRef.value.getBoundingClientRect()
    let percent = ((e.clientX - rect.left) / rect.width) * 100
    percent = Math.max(0, Math.min(100, percent))
    if (showTrialMark.value && !props.isPurchased && percent > trialPercent.value) {
      showTrialTip.value = true
      setTimeout(() => {
        showTrialTip.value = false
      }, 3000)
      return
    }
    const newTime = (percent / 100) * props.duration
    player.currentTime = newTime
    currentTime.value = newTime
  } catch (error) {}
}

const toggleMute = () => {
  const player = audioPlayer.value
  if (!player) return
  isMuted.value = !isMuted.value
  player.muted = isMuted.value
}

const handleVolumeChange = (value) => {
  volume.value = value
  const player = audioPlayer.value
  if (!player) return
  player.volume = value / 100
  if (value > 0 && isMuted.value) {
    isMuted.value = false
    player.muted = false
  }
}

const handleGoToPurchase = () => {
  showTrialTip.value = false
  emit('goToPurchase', props.audioId)
}

const handleClose = () => {
  emit('close')
}

const syncProgress = async () => {
  if (currentTime.value > 0) {
    try {
      await historyApi.saveProgress({
        audioId: props.audioId,
        lastPosition: Math.floor(currentTime.value),
      })
    } catch (error) {}
  }
}

const initHistoryProgress = async () => {
  try {
    const res = await historyApi.getProgress(props.audioId)
    const savedProgress = res.data ?? res

    if (savedProgress !== null && savedProgress !== undefined) {
      currentTime.value = savedProgress
    } else {
      try {
        await historyApi.saveProgress({
          audioId: props.audioId,
          lastPosition: 0,
        })
        currentTime.value = 0
      } catch (error) {}
    }
  } catch (error) {}
}

let progressTimer = null

onMounted(async () => {
  isAudioReady.value = false
  audioUrl.value = ''

  await initHistoryProgress()
  await getStreamSign()

  progressTimer = setInterval(syncProgress, 10000)
})

onUnmounted(() => {
  const player = audioPlayer.value
  if (player) {
    player.pause()
    player.removeEventListener('timeupdate', handleTimeUpdate)
    player.removeEventListener('loadedmetadata', handleLoadedMetadata)
    player.removeEventListener('ended', handleEnded)
    player.removeEventListener('error', handleAudioError)
    player.src = ''
  }
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
  syncProgress()
  document.removeEventListener('mousemove', handleProgressMouseMove)
  document.removeEventListener('mouseup', handleProgressMouseUp)
})

watch(
  () => props.isPurchased,
  async (newVal) => {
    if (newVal && !audioUrl.value) {
      await getStreamSign()
    }
  },
)
</script>

<style scoped>
.mini-player {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-top: 1px solid #e8e6df;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.progress-bar {
  position: relative;
  height: 24px;
  cursor: pointer;
  margin: 0 20px;
  padding: 10px 0;
}

.progress-track {
  position: absolute;
  top: 10px;
  left: 0;
  right: 0;
  height: 4px;
  background: #e8e6df;
  border-radius: 2px;
  overflow: visible;
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: #1a1a1a;
  border-radius: 2px;
  transition: width 0.1s linear;
}

.trial-mark {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  pointer-events: none;
}

.trial-line {
  position: absolute;
  top: -8px;
  width: 2px;
  height: 20px;
  background: #f56c6c;
  z-index: 10;
}

.trial-label {
  position: absolute;
  top: -28px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.65rem;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 10;
}

.progress-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  background: #1a1a1a;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.progress-bar:hover .progress-thumb {
  opacity: 1;
}

.player-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  gap: 20px;
}

.audio-info-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 2;
}

.cover-wrapper {
  position: relative;
}

.cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.playing-indicator {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.playing-indicator span {
  width: 3px;
  height: 12px;
  background: white;
  border-radius: 1px;
  animation: playing 0.8s ease infinite;
}

.playing-indicator span:nth-child(1) {
  animation-delay: 0s;
}
.playing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}
.playing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes playing {
  0%,
  100% {
    height: 6px;
  }
  50% {
    height: 16px;
  }
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.creator {
  font-size: 0.7rem;
  color: #b0aea3;
  margin: 0;
}

.trial-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.7rem;
  color: #f56c6c;
  margin-top: 2px;
}

.player-controls-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.play-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #1a1a1a;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.2s;
}

.play-btn:hover {
  background: #2c2c2c;
  transform: scale(1.05);
}

.time-display {
  font-size: 0.7rem;
  color: #b0aea3;
  display: flex;
  gap: 4px;
}

.extra-controls-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 2;
  justify-content: flex-end;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.volume-slider-wrapper {
  width: 80px;
}

.volume-slider-wrapper :deep(.el-slider__runway) {
  background: #e8e6df;
}

.volume-slider-wrapper :deep(.el-slider__bar) {
  background: #1a1a1a;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b6a62;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f7f6f2;
  color: #1a1a1a;
}

.close-btn:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.trial-tip {
  position: absolute;
  top: -48px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2c2c2c 100%);
  border-radius: 40px;
  color: #ffffff;
  font-size: 0.9rem;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  animation: slideDown 0.3s ease;
  white-space: nowrap;
  z-index: 1001;
}

.trial-tip .el-icon {
  font-size: 18px;
}

.trial-tip span {
  font-weight: 500;
}

.trial-tip:hover {
  background: linear-gradient(135deg, #2c2c2c 0%, #3a3a3a 100%);
  transform: translateX(-50%) scale(1.02);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@media (max-width: 768px) {
  .player-content {
    padding: 10px 16px;
    gap: 12px;
  }
  .volume-slider-wrapper {
    width: 60px;
  }
  .trial-tip {
    padding: 10px 16px;
    font-size: 0.8rem;
    white-space: normal;
    width: 90%;
    text-align: center;
  }
}
</style>
