<template>
  <div class="admin-dashboard">
    <h1>管理面板</h1>
    
    <!-- 导航选项卡 -->
    <div class="admin-nav">
      <button 
        v-for="tab in tabs" 
        :key="tab.id"
        :class="{ active: activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        {{ tab.name }}
      </button>
    </div>

    <!-- 数据大盘 -->
    <div v-if="activeTab === 'dashboard'" class="dashboard-section">
      <h2>全站数据大盘</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-title">总销售额</div>
          <div class="stat-value">¥{{ dashboard.totalSales.toFixed(2) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">今日销售额</div>
          <div class="stat-value">¥{{ dashboard.todaySales.toFixed(2) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">活跃用户</div>
          <div class="stat-value">{{ dashboard.activeUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">累计用户</div>
          <div class="stat-value">{{ dashboard.totalUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">月活跃用户</div>
          <div class="stat-value">{{ dashboard.monthlyActiveUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-title">付费转化率</div>
          <div class="stat-value">{{ (dashboard.conversionRate * 100).toFixed(2) }}%</div>
        </div>
      </div>

      <!-- 销售额趋势 -->
      <div class="chart-section">
        <h3>销售额趋势</h3>
        <div class="chart-container">
          <div v-for="(item, index) in dashboard.salesTrend" :key="index" class="chart-bar">
            <div class="bar-label">{{ item.date }}</div>
            <div class="bar-container">
              <div class="bar" :style="{ height: (item.amount / 2500) * 100 + '%' }"></div>
            </div>
            <div class="bar-value">¥{{ item.amount }}</div>
          </div>
        </div>
      </div>

      <!-- 热门音频 -->
      <div class="list-section">
        <h3>热门音频排行</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>排名</th>
              <th>标题</th>
              <th>播放量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in dashboard.hotAudioList" :key="item.id">
              <td>{{ index + 1 }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.playCount }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 创作者收益 -->
      <div class="list-section">
        <h3>创作者收益排行</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>排名</th>
              <th>创作者</th>
              <th>收益</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in dashboard.creatorEarnings" :key="index">
              <td>{{ index + 1 }}</td>
              <td>{{ item.creatorName }}</td>
              <td>¥{{ item.amount }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 待审音频 -->
    <div v-if="activeTab === 'audit-audio'" class="audit-section">
      <h2>待审音频</h2>
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>封面</th>
            <th>时长</th>
            <th>创作者</th>
            <th>提交时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="audio in auditAudioList.records" :key="audio.id">
            <td>{{ audio.id }}</td>
            <td class="clickable-title" @click="openAudioDetail(audio)">{{ audio.title }}</td>
            <td><img :src="audio.coverUrl" class="cover-small clickable-cover" @click="openAudioDetail(audio)" alt="封面"></td>
            <td>{{ formatDuration(audio.duration) }}</td>
            <td>{{ audio.creatorName }}</td>
            <td>{{ formatDate(audio.submitTime) }}</td>
            <td>
              <button class="btn approve" @click="auditAudio(audio.id, 'APPROVED')">通过</button>
              <button class="btn reject" @click="auditAudio(audio.id, 'REJECTED')">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 音频详情弹窗 -->
    <div v-if="showAudioDetail" class="audio-detail-modal" @click="closeAudioDetail">
      <div class="audio-detail-content" @click.stop>
        <div class="audio-detail-header">
          <h3>{{ selectedAudio.title }}</h3>
          <button class="close-btn" @click="closeAudioDetail">&times;</button>
        </div>
        <div class="audio-detail-body">
          <div class="audio-info">
            <img :src="selectedAudio.coverUrl" class="audio-cover" alt="封面">
            <div class="audio-meta">
              <p><strong>创作者：</strong>{{ selectedAudio.creatorName }}</p>
              <p><strong>时长：</strong>{{ formatDuration(selectedAudio.duration) }}</p>
              <p><strong>提交时间：</strong>{{ formatDate(selectedAudio.submitTime) }}</p>
            </div>
          </div>

          <!-- 音频简介 -->
          <div class="description-section" v-if="selectedAudio.description">
            <h4>简介</h4>
            <div class="description-content">
              {{ selectedAudio.description }}
            </div>
          </div>
          
          <!-- 音频播放器 -->
          <div class="audio-player-section">
            <audio ref="audioPlayer" controls class="audio-player">
              <source :src="audioUrl" type="audio/mpeg">
              您的浏览器不支持音频播放
            </audio>
          </div>

          <!-- 转写文本 -->
          <div class="transcript-section" v-if="selectedAudio.transcript">
            <h4>音频转写文本</h4>
            <div class="transcript-content">
              {{ selectedAudio.transcript }}
            </div>
          </div>

          <!-- 审核操作 -->
          <div class="audit-actions">
            <button class="btn approve" @click="auditAudioAndClose(selectedAudio.id, 'APPROVED')">通过</button>
            <button class="btn reject" @click="auditAudioAndClose(selectedAudio.id, 'REJECTED')">拒绝</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 待审申请 -->
    <div v-if="activeTab === 'audit-apply'" class="audit-section">
      <h2>待审创作者申请</h2>
      <table class="data-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>真实姓名</th>
            <th>电话</th>
            <th>简介</th>
            <th>附件链接</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in auditApplyList.records" :key="apply.applyId">
            <td>{{ apply.id }}</td>
            <td>{{ apply.realName }}</td>
            <td>{{ apply.phone }}</td>
            <td>{{ apply.intro }}</td>
            <td>
              <a v-if="apply.attachment" :href="apply.attachment" target="_blank" class="attachment-link">
                {{ apply.attachment }}
              </a>
              <span v-else>-</span>
            </td>
            <td>{{ formatDate(apply.applyTime) }}</td>
            <td>
              <button class="btn approve" @click="auditApply(apply.id, 'APPROVED')">通过</button>
              <button class="btn reject" @click="auditApply(apply.id, 'REJECTED')">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 退款申请 -->
    <div v-if="activeTab === 'refund'" class="refund-section">
      <h2>退款申请</h2>
      <table class="data-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>订单ID</th>
            <th>日期</th>
            <th>时间</th>
            <th>金额</th>
            <th>用户</th>
            <th>创作者</th>
            <th>原因</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in refundApplyList.records" :key="apply.id">
            <td>{{ apply.id }}</td>
            <td>{{ apply.orderId }}</td>
            <td>{{ apply.applyTime }}</td>
            <td>{{ apply.startTime }}-{{ apply.endTime }}</td>
            <td>¥{{ apply.price }}</td>
            <td>{{ apply.userNickname }}</td>
            <td>{{ apply.creatorName }}</td>
            <td>{{ apply.reason }}</td>
            <td>
              <button class="btn approve" @click="auditRefund(apply.id, true)">通过</button>
              <button class="btn reject" @click="auditRefund(apply.id, false)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage,ElMessageBox } from 'element-plus'

// 响应式数据
const activeTab = ref('dashboard')
const tabs = [
  { id: 'dashboard', name: '数据大盘' },
  { id: 'audit-audio', name: '音频审核' },
  { id: 'audit-apply', name: '申请审核' },
  { id: 'refund', name: '退款审核' }
]

// 数据大盘
const dashboard = ref({
  totalSales: 0,
  todaySales: 0,
  activeUsers: 0,
  totalUsers: 0,
  monthlyActiveUsers: 0,
  conversionRate: 0,
  salesTrend: [],
  hotAudioList: [],
  creatorEarnings: []
})

// 审核列表
const auditAudioList = ref({
  records: [],
  total: 0
})
const auditApplyList = ref({
  records: [],
  total: 0
})
const refundApplyList = ref({
  records: [],
  total: 0
})

// 音频详情
const showAudioDetail = ref(false)
const selectedAudio = ref(null)
const audioUrl = ref('')

// 加载数据
const loadDashboardData = async () => {
  try {
    //const response = await adminApi.getDashboard()
    dashboard.value = response.data
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    ElMessage.error('加载数据大盘失败')
  }
}

const loadAuditAudioList = async () => {
  try {
    //const response = await adminApi.getAuditAudioPage()
    auditAudioList.value = response.data
  } catch (error) {
    console.error('Failed to load audit audio list:', error)
    ElMessage.error('加载待审音频列表失败')
  }
}

const loadAuditApplyList = async () => {
  try {
    const response = await adminApi.getAuditApplyPage()
    auditApplyList.value = response.data
  } catch (error) {
    console.error('Failed to load audit apply list:', error)
    ElMessage.error('加载待审申请列表失败')
  }
}

const loadRefundApplyList = async () => {
  try {
    const response = await adminApi.getRefundApplyPage({
      status: 'PENDING'
    })
    refundApplyList.value = response.data
  } catch (error) {
    console.error('Failed to load refund apply list:', error)
    ElMessage.error('加载退款申请列表失败')
  }
}

// 审核操作
const auditAudio = async (audioId, status) => {
  try {
    let rejectReason = null
    if (status === 'REJECTED') {
      console.log("拒绝审核")
      ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '拒绝原因不能为空'
          }
          return true
        }
      }).then(async (result) => {
        rejectReason = result.value
        
        await adminApi.auditAudio({
          audioId,
          status,
          rejectReason
        })
        await loadAuditAudioList()
        ElMessage.success('审核操作已完成')
      }).catch(() => {
        // 用户取消输入
      })
    } else {
      await adminApi.auditAudio({
        audioId,
        status,
        rejectReason
      })
      await loadAuditAudioList()
      ElMessage.success('审核操作已完成')
    }
  } catch (error) {
    console.error('Failed to audit audio:', error)
  }
}

const auditApply = async (applyId, status) => {
  try {
    let rejectReason = null
    if (status === 'REJECTED') {
      ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '拒绝原因不能为空'
          }
          return true
        }
      }).then(async (result) => {
        rejectReason = result.value
        
        await adminApi.auditApply({
          applyId,
          status,
          rejectReason
        })
        await loadAuditApplyList()
        ElMessage.success('审核操作已完成')
      }).catch(() => {
        // 用户取消输入
      })
    } else {
      await adminApi.auditApply({
        applyId,
        status,
        rejectReason
      })
      await loadAuditApplyList()
      ElMessage.success('审核操作已完成')
    }
  } catch (error) {
    console.error('Failed to audit apply:', error)
    ElMessage.error('审核操作失败')
  }
}

const auditRefund = async (applyId, approved) => {
  try {
    let rejectReason = null
    if (!approved) {
      ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '拒绝原因不能为空'
          }
          return true
        }
      }).then(async (result) => {
        rejectReason = result.value
        
        await adminApi.auditRefund({
          applyId,
          approved,
          rejectReason
        })
        await loadRefundApplyList()
        ElMessage.success('审核操作已完成')
      }).catch(() => {
        // 用户取消输入
      })
    } else {
      await adminApi.auditRefund({
        applyId,
        approved,
        rejectReason
      })
      await loadRefundApplyList()
      ElMessage.success('审核操作已完成')
    }
  } catch (error) {
    console.error('Failed to audit refund:', error)
    ElMessage.error('审核操作失败')
  }
}

// 工具函数
const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

// 音频详情
const openAudioDetail = (audio) => {
  selectedAudio.value = audio
  audioUrl.value = audio.audioUrl || 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
  showAudioDetail.value = true
}

const closeAudioDetail = () => {
  showAudioDetail.value = false
  selectedAudio.value = null
  audioUrl.value = ''
}

const auditAudioAndClose = async (audioId, status) => {
  await auditAudio(audioId, status)
  closeAudioDetail()
}

// 生命周期
onMounted(() => {
  loadDashboardData()
  loadAuditAudioList()
  loadAuditApplyList()
  loadRefundApplyList()
})
</script>

<style scoped>
 @import '@/resource/css/adminDash.css';
</style>