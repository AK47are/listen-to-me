<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { creatorConsultApi } from '@/api/creator/consult'

const consultOrders = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchParams = ref({
  status: '',
  startDate: '',
  endDate: '',
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const statusConfig = {
  PENDING_CONFIRM: { text: '待确认', class: 'pending' },
  CONFIRMED: { text: '已确认', class: 'confirmed' },
  COMPLETED: { text: '已完成', class: 'completed' },
  CANCELLED: { text: '已取消', class: 'cancelled' },
  REFUND_PENDING: { text: '退款中', class: 'refund' },
  REFUNDED: { text: '已退款', class: 'refunded' },
}

const getConsultOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: searchParams.value.status || undefined,
      startDate: searchParams.value.startDate || undefined,
      endDate: searchParams.value.endDate || undefined,
    }
    const response = await creatorConsultApi.getConsultPage(params)
    consultOrders.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('获取预约订单列表失败:', error)
    ElMessage.error('获取预约订单列表失败')
  } finally {
    loading.value = false
  }
}

const confirmConsult = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确认接受「${order.userNickname}」的预约吗？确认后系统将通知用户。`,
      '确认预约',
      {
        confirmButtonText: '确认接受',
        cancelButtonText: '取消',
        type: 'success',
      },
    )
    await creatorConsultApi.confirmConsult(order.id)
    ElMessage.success('已确认预约')
    getConsultOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预约失败:', error)
      ElMessage.error('确认预约失败')
    }
  }
}

const rejectConsult = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要拒绝「${order.userNickname}」的预约吗？拒绝后费用将退还给用户。`,
      '拒绝预约',
      {
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await creatorConsultApi.rejectConsult(order.id)
    ElMessage.success('已拒绝预约')
    getConsultOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝预约失败:', error)
      ElMessage.error('拒绝预约失败')
    }
  }
}

const completeConsult = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确认「${order.userNickname}」的咨询已完成？标记后订单将变为已完成状态。`,
      '标记完成',
      {
        confirmButtonText: '确认完成',
        cancelButtonText: '取消',
        type: 'info',
      },
    )
    await creatorConsultApi.completeConsult(order.id)
    ElMessage.success('已标记完成')
    getConsultOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记完成失败:', error)
      ElMessage.error('标记完成失败')
    }
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const time = date.toTimeString().slice(0, 5)
  return `${month}月${day}日 ${time}`
}

const formatPrice = (price) => {
  return `¥${price}`
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    getConsultOrders()
  }
}

const resetFilters = () => {
  searchParams.value = {
    status: '',
    startDate: '',
    endDate: '',
  }
  currentPage.value = 1
  getConsultOrders()
}

onMounted(() => {
  getConsultOrders()
})
</script>

<template>
  <div class="consult-orders-page">
    <div class="page-header">
      <div class="title-section">
        <h1 class="page-title">预约订单</h1>
        <p class="page-subtitle">管理用户的咨询预约请求</p>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-controls">
        <select v-model="searchParams.status" class="status-select">
          <option value="">全部状态</option>
          <option value="PENDING_CONFIRM">待确认</option>
          <option value="CONFIRMED">已确认</option>
          <option value="COMPLETED">已完成</option>
          <option value="CANCELLED">已取消</option>
          <option value="REFUND_PENDING">退款中</option>
          <option value="REFUNDED">已退款</option>
        </select>
        <div class="date-range">
          <el-date-picker
            v-model="searchParams.startDate"
            type="date"
            placeholder="开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="custom-datepicker"
          />
          <span class="separator">—</span>
          <el-date-picker
            v-model="searchParams.endDate"
            type="date"
            placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="custom-datepicker"
          />
        </div>
        <button class="search-btn" @click="getConsultOrders">
          <el-icon><Search /></el-icon>
          查询
        </button>
        <button class="reset-btn" @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置
        </button>
      </div>
    </div>

    <div class="orders-container">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Refresh /></el-icon>
        <span>加载订单中...</span>
      </div>

      <div v-else-if="consultOrders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无预约订单</h3>
        <p>当前没有符合条件的预约记录</p>
      </div>

      <div v-else class="orders-list">
        <div v-for="order in consultOrders" :key="order.id" class="order-card">
          <div class="card-header">
            <div class="user-section">
              <img :src="order.userAvatar" :alt="order.userNickname" class="user-avatar" />
              <div class="user-info">
                <span class="user-name">{{ order.userNickname }}</span>
                <span class="order-id">#{{ order.id }}</span>
              </div>
            </div>
            <span :class="['status-badge', statusConfig[order.status]?.class || 'default']">
              {{ statusConfig[order.status]?.text || order.status }}
            </span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <span class="label">预约时间</span>
              <span class="value"
                >{{ formatDateTime(order.startTime) }} — {{ formatDateTime(order.endTime) }}</span
              >
            </div>
            <div class="info-row">
              <span class="label">咨询费用</span>
              <span class="value price">{{ formatPrice(order.price) }}</span>
            </div>
            <div
              class="info-row"
              v-if="order.address && (order.status === 'CONFIRMED' || order.status === 'COMPLETED')"
            >
              <span class="label">联系地址</span>
              <span class="value address">{{ order.address }}</span>
            </div>
            <div class="message-box" v-if="order.message">
              <span class="label">用户留言</span>
              <p class="message-content">{{ order.message }}</p>
            </div>
            <div v-if="order.rejectReason" class="reject-reason">
              <span class="label">拒绝原因</span>
              <p>{{ order.rejectReason }}</p>
            </div>
          </div>

          <div class="card-footer">
            <div class="create-time">{{ order.createTime?.split(' ')[0] }}</div>
            <div class="actions">
              <template v-if="order.status === 'PENDING_CONFIRM'">
                <button class="action-btn confirm-btn" @click="confirmConsult(order)">接受</button>
                <button class="action-btn reject-btn" @click="rejectConsult(order)">拒绝</button>
              </template>
              <template v-else-if="order.status === 'CONFIRMED'">
                <button class="action-btn complete-btn" @click="completeConsult(order)">
                  标记完成
                </button>
              </template>
              <template v-else>
                <span class="no-action">—</span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination-wrapper">
        <button class="page-btn" :disabled="currentPage === 1" @click="changePage(1)">首页</button>
        <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
          上一页
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
        <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="changePage(totalPages)"
        >
          末页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.consult-orders-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 32px 30px 60px;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 24px;
}

.title-section {
  text-align: left;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 0.9rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.filter-section {
  max-width: 1200px;
  margin: 0 auto 24px;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  background: #ffffff;
  padding: 8px 16px;
  border-radius: 48px;
  border: 1px solid #efeee8;
}

.status-select {
  padding: 8px 14px;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  background: #ffffff;
  font-size: 0.85rem;
  color: #1a1a1a;
  cursor: pointer;
  outline: none;
  min-width: 120px;
}

.status-select:focus {
  border-color: #1a1a1a;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fcfbf7;
  padding: 2px 8px;
  border-radius: 40px;
  border: 1px solid #e8e6df;
}

.custom-datepicker {
  width: 130px;
}

.custom-datepicker :deep(.el-input__wrapper) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 4px 0;
}

.custom-datepicker :deep(.el-input__inner) {
  font-size: 0.85rem;
  color: #1a1a1a;
}

.custom-datepicker :deep(.el-input__prefix) {
  display: none;
}

.custom-datepicker :deep(.el-input__suffix) {
  margin-left: 2px;
}

.separator {
  color: #b0aea3;
  font-size: 0.85rem;
}

.search-btn,
.reset-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: #ffffff;
  color: #1a1a1a;
}

.search-btn {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.search-btn:hover {
  background: #2c2c2c;
}

.reset-btn:hover {
  background: #f7f6f2;
}

.orders-container {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid #efeee8;
  color: #8e8c84;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.empty-state h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.2rem;
  color: #1a1a1a;
  margin: 0 0 6px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 20px;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.order-card:hover {
  border-color: #dcdbd3;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #efeee8;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1a1a1a;
}

.order-id {
  font-size: 0.75rem;
  color: #8e8c84;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.status-badge.pending {
  background: #fff3e0;
  color: #b85e00;
}

.status-badge.confirmed {
  background: #e8f3e8;
  color: #2e6b2e;
}

.status-badge.completed {
  background: #e8f0fe;
  color: #2c5f9a;
}

.status-badge.cancelled {
  background: #f0efe9;
  color: #5a5953;
}

.status-badge.refund {
  background: #fdeded;
  color: #c74242;
}

.status-badge.refunded {
  background: #f0efe9;
  color: #5a5953;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.info-row .label {
  width: 70px;
  font-size: 0.8rem;
  color: #8e8c84;
  flex-shrink: 0;
}

.info-row .value {
  font-size: 0.9rem;
  color: #1a1a1a;
  font-weight: 500;
}

.info-row .value.price {
  font-weight: 700;
  color: #1a1a1a;
  font-size: 1rem;
}

.info-row .value.address {
  word-break: break-all;
}

.message-box {
  margin-top: 4px;
  padding: 10px 14px;
  background: #fcfbf7;
  border-radius: 14px;
  border-left: 3px solid #1a1a1a;
}

.message-box .label {
  display: block;
  font-size: 0.75rem;
  color: #8e8c84;
  margin-bottom: 4px;
}

.message-content {
  margin: 0;
  font-size: 0.9rem;
  color: #1a1a1a;
  line-height: 1.4;
}

.reject-reason {
  margin-top: 4px;
  padding: 10px 14px;
  background: #fef8f8;
  border-radius: 14px;
  border-left: 3px solid #c74242;
}

.reject-reason .label {
  display: block;
  font-size: 0.75rem;
  color: #c74242;
  margin-bottom: 4px;
}

.reject-reason p {
  margin: 0;
  font-size: 0.9rem;
  color: #1a1a1a;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #efeee8;
}

.create-time {
  font-size: 0.8rem;
  color: #8e8c84;
}

.actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 6px 16px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #1a1a1a;
}

.confirm-btn {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.confirm-btn:hover {
  background: #2c2c2c;
}

.reject-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
  color: #c74242;
}

.complete-btn:hover {
  background: #e8f3e8;
  border-color: #b3d9b3;
  color: #2e6b2e;
}

.no-action {
  color: #b0aea3;
  font-size: 0.85rem;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 32px;
}

.page-btn {
  padding: 6px 14px;
  border-radius: 40px;
  border: 1px solid #e8e6df;
  background: #ffffff;
  color: #1a1a1a;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f7f6f2;
  border-color: #dcdbd3;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.85rem;
  color: #5a5953;
  min-width: 70px;
  text-align: center;
}

@media (max-width: 768px) {
  .consult-orders-page {
    padding: 20px 16px;
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
    border-radius: 28px;
  }

  .date-range {
    justify-content: space-between;
  }

  .custom-datepicker {
    width: 100%;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .status-badge {
    align-self: flex-start;
  }

  .info-row {
    flex-direction: column;
    gap: 2px;
  }

  .info-row .label {
    width: auto;
  }

  .card-footer {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>
