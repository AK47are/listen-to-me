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
        <button class="control-btn icon-btn close-btn" @click="emit('close')">
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </div>

    <audio
      ref="audioPlayer"
      :src="audioUrl"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoadedMetadata"
      @ended="handleEnded"
    ></audio>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, VideoPause, Headset, Mute, Close } from '@element-plus/icons-vue'
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
})

const emit = defineEmits(['close'])

const audioUrl = ref('')
const isPlaying = ref(false)
const currentTime = ref(props.progress)
const volume = ref(80)
const isMuted = ref(false)
const isDragging = ref(false)
const dragPercent = ref(0)

const audioPlayer = ref(null)
const progressBarRef = ref(null)

const progressPercent = computed(() => {
  if (props.duration === 0) return 0
  if (isDragging.value) return dragPercent.value
  return (currentTime.value / props.duration) * 100
})

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getStreamSign = async () => {
  try {
    const res = await fileApi.getStreamSign(props.audioId)
    audioUrl.value = res
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
  if (props.progress > 0 && audioPlayer.value) {
    audioPlayer.value.currentTime = props.progress
    currentTime.value = props.progress
  }
}

const handleEnded = () => {
  isPlaying.value = false
  currentTime.value = 0
}

const handleProgressMouseDown = (e) => {
  if (!audioPlayer.value) return
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
  if (isDragging.value && audioPlayer.value) {
    isDragging.value = false
    audioPlayer.value.currentTime = (dragPercent.value / 100) * props.duration
    currentTime.value = audioPlayer.value.currentTime
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
  if (!progressBarRef.value || !audioPlayer.value) return
  const rect = progressBarRef.value.getBoundingClientRect()
  const percent = ((e.clientX - rect.left) / rect.width) * 100
  const newTime = (percent / 100) * props.duration
  audioPlayer.value.currentTime = newTime
  currentTime.value = newTime
}

const toggleMute = () => {
  if (!audioPlayer.value) return
  isMuted.value = !isMuted.value
  audioPlayer.value.muted = isMuted.value
}

const handleVolumeChange = (value) => {
  volume.value = value
  if (audioPlayer.value) {
    audioPlayer.value.volume = value / 100
    if (value > 0 && isMuted.value) {
      isMuted.value = false
      audioPlayer.value.muted = false
    }
  }
}

const syncProgress = async () => {
  if (currentTime.value > 0 && (!props.isPaid || props.isPurchased)) {
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

onMounted(() => {
  if (props.progress > 0 && (props.isPaid ? props.isPurchased : true)) {
    getStreamSign()
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

watch(
  () => props.isPurchased,
  (newVal) => {
    if (newVal && !audioUrl.value) {
      getStreamSign()
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
  height: 4px;
  cursor: pointer;
  margin: 0 20px;
}

.progress-track {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #e8e6df;
  border-radius: 2px;
  overflow: hidden;
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

@media (max-width: 768px) {
  .player-content {
    padding: 10px 16px;
    gap: 12px;
  }
  .volume-slider-wrapper {
    width: 60px;
  }
}
</style>
