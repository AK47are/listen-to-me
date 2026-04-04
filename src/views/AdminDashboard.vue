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
            <th>用户名</th>
            <th>真实姓名</th>
            <th>电话</th>
            <th>简介</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in auditApplyList.records" :key="apply.applyId">
            <td>{{ apply.applyId }}</td>
            <td>{{ apply.username }}</td>
            <td>{{ apply.realName }}</td>
            <td>{{ apply.phone }}</td>
            <td>{{ apply.intro }}</td>
            <td>{{ formatDate(apply.applyTime) }}</td>
            <td>
              <button class="btn approve" @click="auditApply(apply.applyId, 'APPROVED')">通过</button>
              <button class="btn reject" @click="auditApply(apply.applyId, 'REJECTED')">拒绝</button>
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
            <td>{{ apply.date }}</td>
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

<script>
import mockApi from '@/data/mockApi'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      activeTab: 'dashboard',
      tabs: [
        { id: 'dashboard', name: '数据大盘' },
        { id: 'audit-audio', name: '音频审核' },
        { id: 'audit-apply', name: '申请审核' },
        { id: 'refund', name: '退款审核' }
      ],
      dashboard: {
        totalSales: 0,
        todaySales: 0,
        activeUsers: 0,
        totalUsers: 0,
        monthlyActiveUsers: 0,
        conversionRate: 0,
        salesTrend: [],
        hotAudioList: [],
        creatorEarnings: []
      },
      auditAudioList: {
        records: [],
        total: 0
      },
      auditApplyList: {
        records: [],
        total: 0
      },
      refundApplyList: {
        records: [],
        total: 0
      },
      showAudioDetail: false,
      selectedAudio: null,
      audioUrl: ''
    }
  },
  mounted() {
    this.loadDashboardData()
    this.loadAuditAudioList()
    this.loadAuditApplyList()
    this.loadRefundApplyList()
  },
  methods: {
    async loadDashboardData() {
      try {
        const response = await mockApi.admin.getDashboard()
        this.dashboard = response
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      }
    },
    
    async loadAuditAudioList() {
      try {
        const response = await mockApi.admin.getAuditAudioPage()
        this.auditAudioList = response
      } catch (error) {
        console.error('Failed to load audit audio list:', error)
      }
    },
    
    async loadAuditApplyList() {
      try {
        const response = await mockApi.admin.getAuditApplyPage()
        this.auditApplyList = response
      } catch (error) {
        console.error('Failed to load audit apply list:', error)
      }
    },
    
    async loadRefundApplyList() {
      try {
        const response = await mockApi.admin.getRefundApplyPage()
        this.refundApplyList = response
      } catch (error) {
        console.error('Failed to load refund apply list:', error)
      }
    },
    
    async auditAudio(audioId, status) {
      try {
        await mockApi.admin.auditAudio({
          audioId,
          status,
          rejectReason: status === 'REJECTED' ? '不符合平台规范' : null
        })
        this.loadAuditAudioList()
        alert('审核操作已完成')
      } catch (error) {
        console.error('Failed to audit audio:', error)
      }
    },
    
    async auditApply(applyId, status) {
      try {
        await mockApi.admin.auditApply({
          applyId,
          status,
          rejectReason: status === 'REJECTED' ? '不符合创作者要求' : null
        })
        this.loadAuditApplyList()
        alert('审核操作已完成')
      } catch (error) {
        console.error('Failed to audit apply:', error)
      }
    },
    
    async auditRefund(applyId, approved) {
      try {
        await mockApi.admin.auditRefund({
          applyId,
          approved,
          rejectReason: !approved ? '不符合退款条件' : null
        })
        this.loadRefundApplyList()
        alert('审核操作已完成')
      } catch (error) {
        console.error('Failed to audit refund:', error)
      }
    },
    
    formatDuration(seconds) {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
    },
    
    formatDate(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN')
    },
    
    openAudioDetail(audio) {
      this.selectedAudio = audio
      this.audioUrl = audio.audioUrl || 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
      this.showAudioDetail = true
    },
    
    closeAudioDetail() {
      this.showAudioDetail = false
      this.selectedAudio = null
      this.audioUrl = ''
    },
    
    async auditAudioAndClose(audioId, status) {
      await this.auditAudio(audioId, status)
      this.closeAudioDetail()
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

h1 {
  margin-bottom: 30px;
  color: #333;
}

.admin-nav {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #e0e0e0;
}

.admin-nav button {
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.admin-nav button:hover {
  color: #1890ff;
}

.admin-nav button.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.chart-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.chart-container {
  display: flex;
  align-items: end;
  height: 300px;
  gap: 10px;
  padding: 20px 0;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bar-container {
  flex: 1;
  width: 100%;
  background: #f0f0f0;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
  margin: 10px 0;
  position: relative;
}

.bar {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: #1890ff;
  transition: height 0.5s;
}

.bar-label {
  font-size: 12px;
  color: #666;
}

.bar-value {
  font-size: 12px;
  color: #333;
  margin-top: 5px;
}

.list-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.data-table th {
  background: #f5f5f5;
  font-weight: bold;
  color: #333;
}

.data-table tr:hover {
  background: #f9f9f9;
}

.cover-small {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 5px;
  transition: all 0.3s;
}

.btn.approve {
  background: #52c41a;
  color: white;
}

.btn.approve:hover {
  background: #73d13d;
}

.btn.reject {
  background: #ff4d4f;
  color: white;
}

.btn.reject:hover {
  background: #ff7875;
}

.audit-section,
.refund-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 音频详情弹窗样式 */
.audio-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.audio-detail-content {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.audio-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.audio-detail-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.audio-detail-body {
  padding: 20px;
}

.audio-info {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.audio-cover {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.audio-meta {
  flex: 1;
}

.audio-meta p {
  margin: 8px 0;
  color: #666;
}

.audio-player-section {
  margin: 20px 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.audio-player {
  width: 100%;
  height: 40px;
}

.transcript-section {
  margin: 20px 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.transcript-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.description-section {
  margin: 20px 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.description-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.description-content {
  padding: 15px;
  background: #fff;
  border-radius: 4px;
  line-height: 1.6;
  color: #666;
}

.transcript-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 15px;
  background: #fff;
  border-radius: 4px;
  line-height: 1.6;
  color: #666;
}

.audit-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.clickable-title {
  cursor: pointer;
  color: #1890ff;
  text-decoration: underline;
}

.clickable-title:hover {
  color: #40a9ff;
}

.clickable-cover {
  cursor: pointer;
  transition: transform 0.3s;
}

.clickable-cover:hover {
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-container {
    height: 200px;
  }
  
  .data-table {
    font-size: 14px;
  }
  
  .data-table th,
  .data-table td {
    padding: 8px;
  }
}
</style>