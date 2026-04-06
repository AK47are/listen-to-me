<template>
  <div class="audio-card" @click="handleClick">
    <div class="cover-wrapper">
      <img :src="audio.coverUrl" class="cover-img" />
      <div class="play-overlay">
        <el-icon><CaretRight /></el-icon>
      </div>
    </div>
    <div class="card-info">
      <h3 class="audio-title">{{ audio.title }}</h3>
      <p class="creator-name">{{ audio.creatorName }}</p>
      <div class="stats">
        <div class="stat-item">
          <el-icon><Headset /></el-icon>
          <span>{{ formatNumber(audio.playCount) }}</span>
        </div>
        <div class="stat-item">
          <el-icon><Star /></el-icon>
          <span>{{ formatNumber(audio.collectCount) }}</span>
        </div>
      </div>
    </div>
    <div class="card-action">
      <span class="action-btn">查看详情</span>
    </div>
  </div>
</template>

<script setup>
import { Headset, Star, CaretRight } from '@element-plus/icons-vue'

const props = defineProps({
  audio: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click', props.audio)
}

const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num?.toString() || '0'
}
</script>

<style scoped>
.audio-card {
  background: #ffffff;
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid #efeee8;
  display: flex;
  flex-direction: column;
}

.audio-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 28px -12px rgba(0, 0, 0, 0.1);
  border-color: #e2e0d7;
}

.cover-wrapper {
  position: relative;
  overflow: hidden;
  aspect-ratio: 16 / 9;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.audio-card:hover .cover-img {
  transform: scale(1.05);
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
  backdrop-filter: blur(2px);
}

.play-overlay .el-icon {
  font-size: 42px;
  color: white;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.2));
}

.audio-card:hover .play-overlay {
  opacity: 1;
}

.card-info {
  padding: 14px 16px 12px;
  flex: 1;
}

.audio-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.creator-name {
  font-size: 0.85rem;
  font-weight: 500;
  color: #8e8c84;
  margin: 0 0 12px 0;
}

.stats {
  display: flex;
  gap: 20px;
  align-items: center;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-item .el-icon {
  font-size: 18px;
  color: #8e8c84;
}

.card-action {
  padding: 8px 16px 16px;
  border-top: 1px solid #f0efeb;
}

.action-btn {
  display: inline-block;
  width: 100%;
  text-align: center;
  padding: 8px 12px;
  background: #f7f6f2;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a1a1a;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #efeee8;
  border-color: #dcdbd3;
  transform: translateY(-1px);
}
</style>
