<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1 class="page-title">管理中心</h1>
      <p class="page-subtitle">数据总览与业务审核</p>
    </div>

    <div class="tabs-wrapper">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="overview">
          <template #label>
            <span class="tab-label">
              <el-icon><DataAnalysis /></el-icon>
              数据总览
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="audio">
          <template #label>
            <span class="tab-label">
              <el-icon><Headset /></el-icon>
              音频审核
              <span class="pending-count" v-if="pendingCounts.audio">{{
                pendingCounts.audio
              }}</span>
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="apply">
          <template #label>
            <span class="tab-label">
              <el-icon><UserFilled /></el-icon>
              创作者审核
              <span class="pending-count" v-if="pendingCounts.apply">{{
                pendingCounts.apply
              }}</span>
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="refund">
          <template #label>
            <span class="tab-label">
              <el-icon><Money /></el-icon>
              退款审核
              <span class="pending-count" v-if="pendingCounts.refund">{{
                pendingCounts.refund
              }}</span>
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="user">
          <template #label>
            <span class="tab-label">
              <el-icon><User /></el-icon>
              用户管理
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 数据总览 -->
    <div v-show="activeTab === 'overview'" class="overview-content">
      <div class="key-metrics">
        <div class="metric-card">
          <div class="metric-icon">💰</div>
          <div class="metric-content">
            <span class="metric-label">总收入</span>
            <span class="metric-value">¥{{ formatMoney(totalRevenue) }}</span>
          </div>
        </div>
        <div class="metric-card">
          <div class="metric-icon">👥</div>
          <div class="metric-content">
            <span class="metric-label">总用户数</span>
            <span class="metric-value">{{ formatNumber(dashboard.totalUsers) }}</span>
          </div>
        </div>
        <div class="metric-card">
          <div class="metric-icon">✍️</div>
          <div class="metric-content">
            <span class="metric-label">总创作者</span>
            <span class="metric-value">{{ formatNumber(dashboard.totalCreators) }}</span>
          </div>
        </div>
        <div class="metric-card">
          <div class="metric-icon">▶️</div>
          <div class="metric-content">
            <span class="metric-label">总播放量</span>
            <span class="metric-value">{{ formatNumber(dashboard.totalPlays) }}</span>
          </div>
        </div>
      </div>

      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Calendar /></el-icon>
          时段数据
        </h3>
        <div class="section-actions">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="—"
            start-placeholder="开始"
            end-placeholder="结束"
            :shortcuts="dateShortcuts"
            @change="handleDateChange"
            class="date-picker"
            size="small"
          />
          <button class="toggle-btn" @click="showPeriodStats = !showPeriodStats">
            <el-icon><ArrowUp v-if="showPeriodStats" /><ArrowDown v-else /></el-icon>
          </button>
        </div>
      </div>

      <transition name="slide-fade">
        <div v-show="showPeriodStats" class="period-stats">
          <div class="period-stat-item">
            <span class="period-stat-label">新增用户</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newUsers) }}</span>
          </div>
          <div class="period-stat-item">
            <span class="period-stat-label">新增创作者</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newCreators) }}</span>
          </div>
          <div class="period-stat-item">
            <span class="period-stat-label">新增音频</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newAudios) }}</span>
          </div>
          <div class="period-stat-item">
            <span class="period-stat-label">新增审核通过</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newApprovedAudios) }}</span>
          </div>
          <div class="period-stat-item">
            <span class="period-stat-label">新增收藏</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newCollects) }}</span>
          </div>
          <div class="period-stat-item">
            <span class="period-stat-label">新增点赞</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newLikes) }}</span>
          </div>
          <div class="period-stat-item">
            <span class="period-stat-label">新增评论</span>
            <span class="period-stat-value">{{ formatNumber(dashboard.newComments) }}</span>
          </div>
        </div>
      </transition>

      <div class="data-sections">
        <div class="data-group">
          <div class="group-header" @click="showUserContent = !showUserContent">
            <h3>用户与内容</h3>
            <el-icon class="collapse-icon"
              ><ArrowUp v-if="showUserContent" /><ArrowDown v-else
            /></el-icon>
          </div>
          <transition name="slide-fade">
            <div v-show="showUserContent" class="data-grid">
              <div class="data-item">
                <div class="data-icon">👥</div>
                <div class="data-info">
                  <span class="data-label">总用户</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalUsers) }}</span>
                </div>
              </div>
              <div class="data-item">
                <div class="data-icon">✍️</div>
                <div class="data-info">
                  <span class="data-label">总创作者</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalCreators) }}</span>
                </div>
              </div>
              <div class="data-item">
                <div class="data-icon">🎵</div>
                <div class="data-info">
                  <span class="data-label">总音频</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalAudios) }}</span>
                </div>
              </div>
              <div class="data-item">
                <div class="data-icon">✅</div>
                <div class="data-info">
                  <span class="data-label">已审核音频</span>
                  <span class="data-value">{{ formatNumber(dashboard.approvedAudios) }}</span>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <div class="data-group">
          <div class="group-header" @click="showInteraction = !showInteraction">
            <h3>互动数据</h3>
            <el-icon class="collapse-icon"
              ><ArrowUp v-if="showInteraction" /><ArrowDown v-else
            /></el-icon>
          </div>
          <transition name="slide-fade">
            <div v-show="showInteraction" class="data-grid">
              <div class="data-item">
                <div class="data-icon">▶️</div>
                <div class="data-info">
                  <span class="data-label">总播放量</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalPlays) }}</span>
                </div>
              </div>
              <div class="data-item">
                <div class="data-icon">⭐</div>
                <div class="data-info">
                  <span class="data-label">总收藏</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalCollects) }}</span>
                </div>
              </div>
              <div class="data-item">
                <div class="data-icon">❤️</div>
                <div class="data-info">
                  <span class="data-label">总点赞</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalLikes) }}</span>
                </div>
              </div>
              <div class="data-item">
                <div class="data-icon">💬</div>
                <div class="data-info">
                  <span class="data-label">总评论</span>
                  <span class="data-value">{{ formatNumber(dashboard.totalComments) }}</span>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <div class="data-group financial">
          <div class="group-header" @click="showFinancial = !showFinancial">
            <h3>财务数据</h3>
            <el-icon class="collapse-icon"
              ><ArrowUp v-if="showFinancial" /><ArrowDown v-else
            /></el-icon>
          </div>
          <transition name="slide-fade">
            <div v-show="showFinancial" class="financial-grid">
              <div class="financial-item">
                <span class="financial-label">音频销售额</span>
                <span class="financial-value">¥{{ formatMoney(dashboard.totalAudioSales) }}</span>
              </div>
              <div class="financial-item">
                <span class="financial-label">充值总额</span>
                <span class="financial-value">¥{{ formatMoney(dashboard.totalRecharge) }}</span>
              </div>
              <div class="financial-item">
                <span class="financial-label">咨询销售额</span>
                <span class="financial-value">¥{{ formatMoney(dashboard.totalConsultSales) }}</span>
              </div>
              <div class="financial-item total">
                <span class="financial-label">总收入</span>
                <span class="financial-value">¥{{ formatMoney(totalRevenue) }}</span>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- 音频审核 -->
    <div v-show="activeTab === 'audio'" class="tab-content">
      <AudioAudit @update:pending-count="pendingCounts.audio = $event" />
    </div>

    <!-- 创作者审核 -->
    <div v-show="activeTab === 'apply'" class="tab-content">
      <ApplyAudit @update:pending-count="pendingCounts.apply = $event" />
    </div>

    <!-- 退款审核 -->
    <div v-show="activeTab === 'refund'" class="tab-content">
      <RefundAudit @update:pending-count="pendingCounts.refund = $event" />
    </div>

    <!-- 用户管理 -->
    <div v-show="activeTab === 'user'" class="tab-content">
      <UserManagement />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  DataAnalysis,
  Headset,
  UserFilled,
  Money,
  User,
  ArrowUp,
  ArrowDown,
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import ApplyAudit from '@/components/admin/ApplyAudit.vue'
import AudioAudit from '@/components/admin/AudioAudit.vue'
import RefundAudit from '@/components/admin/RefundAudit.vue'
import UserManagement from '@/components/admin/UserManagement.vue'

const activeTab = ref('overview')

const showPeriodStats = ref(true)
const showUserContent = ref(true)
const showInteraction = ref(true)
const showFinancial = ref(true)

const pendingCounts = ref({
  audio: 0,
  apply: 0,
  refund: 0,
})

const dateRange = ref([])
const dateShortcuts = [
  {
    text: '近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
]

const dashboard = ref({
  totalUsers: 0,
  totalCreators: 0,
  totalAudios: 0,
  approvedAudios: 0,
  totalPlays: 0,
  totalAudioSales: 0,
  totalRecharge: 0,
  totalConsultSales: 0,
  totalCollects: 0,
  totalLikes: 0,
  totalComments: 0,
  newUsers: 0,
  newCreators: 0,
  newAudios: 0,
  newApprovedAudios: 0,
  newCollects: 0,
  newLikes: 0,
  newComments: 0,
})

const totalRevenue = computed(() => {
  return (
    dashboard.value.totalAudioSales +
    dashboard.value.totalConsultSales +
    dashboard.value.totalRecharge
  )
})

const formatDateTime = (date, isEnd = false) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const time = isEnd ? '23:59:59' : '00:00:00'
  return `${year}-${month}-${day}T${time}`
}

const loadDashboardData = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = formatDateTime(dateRange.value[0], false)
      params.endDate = formatDateTime(dateRange.value[1], true)
    }
    const res = await request.get('/admin/stat/dashboard', { params })
    dashboard.value = res.data
  } catch (error) {
    console.error('获取大盘数据失败', error)
    ElMessage.error('数据加载失败，请稍后重试')
  }
}

const handleDateChange = () => {
  loadDashboardData()
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return num.toLocaleString()
}

const formatMoney = (num) => {
  if (num === null || num === undefined) return '0.00'
  return num.toFixed(2)
}

onMounted(() => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
  dateRange.value = [start, end]
  loadDashboardData()
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 32px 30px 60px;
}

.dashboard-header {
  max-width: 1400px;
  margin: 0 auto 24px;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 0.95rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.tabs-wrapper {
  max-width: 1400px;
  margin: 0 auto 28px;
}

.custom-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-tabs :deep(.el-tabs__item) {
  padding: 12px 0 !important;
  margin-right: 28px;
  font-weight: 500;
  color: #8e8c84;
  height: auto;
  line-height: 1;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #1a1a1a;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  height: 2px;
  background: #1a1a1a;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.95rem;
}

.pending-count {
  display: inline-block;
  background: #e8e6df;
  color: #5a5953;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 6px;
  line-height: 1;
}

.overview-content {
  max-width: 1400px;
  margin: 0 auto;
}

.key-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  gap: 16px;
  transition: all 0.2s;
}

.metric-card:hover {
  border-color: #dcdbd3;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.metric-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fcfbf7;
  border-radius: 16px;
  flex-shrink: 0;
}

.metric-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.metric-label {
  font-size: 0.85rem;
  color: #8e8c84;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Noto Serif SC', serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-picker {
  width: 200px;
}

.date-picker :deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  box-shadow: none;
  padding: 4px 12px;
}

.toggle-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #e8e6df;
  background: #ffffff;
  color: #5a5953;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.period-stats {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16px;
  margin-bottom: 28px;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
}

.period-stat-item {
  text-align: center;
}

.period-stat-label {
  display: block;
  font-size: 0.75rem;
  color: #8e8c84;
  margin-bottom: 6px;
}

.period-stat-value {
  display: block;
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a1a;
}

.data-sections {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.data-group {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 20px 24px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}

.group-header h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.collapse-icon {
  color: #8e8c84;
  transition: transform 0.2s;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #fcfbf7;
  border-radius: 16px;
  transition: all 0.2s;
}

.data-item:hover {
  background: #f7f6f2;
}

.data-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  background: #ffffff;
  border-radius: 12px;
  flex-shrink: 0;
}

.data-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.data-label {
  font-size: 0.8rem;
  color: #8e8c84;
}

.data-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.financial-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.financial-item {
  padding: 16px 20px;
  background: #fcfbf7;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.financial-item.total {
  background: #f0efe9;
  border: 1px solid #e0ded6;
}

.financial-label {
  font-size: 0.85rem;
  color: #8e8c84;
}

.financial-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a1a1a;
}

.tab-content {
  max-width: 1400px;
  margin: 0 auto;
}

.tab-placeholder {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 80px 32px;
  text-align: center;
}

.placeholder-icon {
  font-size: 56px;
  color: #dcdbd3;
  margin-bottom: 20px;
}

.tab-placeholder h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.3rem;
  color: #1a1a1a;
  margin: 0 0 12px;
}

.tab-placeholder p {
  color: #8e8c84;
  margin: 0;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.25s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 1200px) {
  .key-metrics {
    grid-template-columns: repeat(2, 1fr);
  }

  .period-stats {
    grid-template-columns: repeat(4, 1fr);
  }

  .data-grid,
  .financial-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    padding: 20px 16px;
  }

  .key-metrics {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .period-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .data-grid,
  .financial-grid {
    grid-template-columns: 1fr;
  }
}
</style>
