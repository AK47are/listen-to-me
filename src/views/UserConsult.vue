<template>
  <div class="user-consult-page">
    <div class="page-header">
      <h1 class="page-title">我的预约</h1>
      <p class="page-subtitle">查看和管理您的咨询预约记录</p>
    </div>

    <div class="consult-content">
      <div class="section-header">
        <div class="filter-bar">
          <el-select
            v-model="searchParams.status"
            placeholder="全部状态"
            clearable
            @change="handleStatusChange"
            style="min-width: 120px"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待确认" value="PENDING_CONFIRM" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退款中" value="REFUND_PENDING" />
            <el-option label="已退款" value="REFUNDED" />
            <el-option label="退款被拒" value="REFUND_REJECTED" />
          </el-select>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="consultOrders.length === 0" class="empty-state">
        <el-empty description="暂无预约记录" />
      </div>

      <div v-else class="consult-list">
        <div v-for="order in consultOrders" :key="order.id" class="consult-card">
          <div class="consult-card-header">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span
                >{{ formatDate(order.startTime) }}
                {{ formatTimeRange(order.startTime, order.endTime) }}</span
              >
            </div>
            <el-tag :type="getStatusType(order)" effect="plain">
              {{ getStatusText(order) }}
            </el-tag>
          </div>

          <div class="consult-card-body">
            <div class="creator-info">
              <el-avatar :size="40" :src="order.creatorAvatar" />
              <div class="creator-detail">
                <span class="creator-name">{{ order.creatorName }}</span>
                <span class="consult-price">
                  <el-icon><Coin /></el-icon> {{ order.price }} 虚拟币
                </span>
              </div>
            </div>
            <div v-if="order.message" class="message-preview">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ order.message }}</span>
            </div>
            <div v-if="order.address" class="address-info">
              <el-icon><Location /></el-icon>
              <span>联系地址：{{ order.address }}</span>
            </div>
            <div
              v-else-if="order.status === 'CONFIRMED' || order.status === 'COMPLETED'"
              class="address-placeholder"
            >
              <el-icon><Lock /></el-icon>
              <span>联系地址需在创作者确认后显示</span>
            </div>
          </div>

          <div class="consult-card-footer">
            <el-button
              v-if="order.status === 'PENDING_CONFIRM'"
              type="danger"
              size="small"
              plain
              @click="cancelConsult(order.id)"
            >
              取消预约
            </el-button>
            <el-button
              v-if="['CONFIRMED', 'COMPLETED'].includes(order.status)"
              type="warning"
              size="small"
              plain
              @click="showRefundDialog(order.id)"
            >
              申请退款
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          @current-change="handleConsultPageChange"
        />
      </div>
    </div>

    <!-- 退款申请弹窗 -->
    <el-dialog
      v-model="showRefundModal"
      title="申请退款"
      width="500px"
      class="refund-dialog"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="退款原因" required>
          <el-input
            v-model="refundReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退款原因..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRefundModal = false">取消</el-button>
          <el-button
            type="primary"
            @click="applyRefund"
            :loading="submitting"
            :disabled="!refundReason.trim()"
          >
            提交申请
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, ChatDotRound, Coin, Location, Lock } from '@element-plus/icons-vue'
import { consultApi } from '@/api/user/consult'

const consultOrders = ref([])
const total = ref(0)
const searchParams = ref({ status: '' })
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const submitting = ref(false)
const showRefundModal = ref(false)
const currentOrderId = ref(null)
const refundReason = ref('')

const getMyConsultOrders = async () => {
  loading.value = true
  try {
    const res = await consultApi.getMyConsultPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: searchParams.value.status || undefined,
    })
    consultOrders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取预约订单失败:', error)
    ElMessage.error('获取预约订单失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  currentPage.value = 1
  getMyConsultOrders()
}

const cancelConsult = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？取消后将全额退款。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await consultApi.cancelConsult(id)
    ElMessage.success('预约取消成功')
    getMyConsultOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  }
}

const showRefundDialog = (id) => {
  currentOrderId.value = id
  refundReason.value = ''
  showRefundModal.value = true
}

const applyRefund = async () => {
  if (!refundReason.value.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }
  submitting.value = true
  try {
    await consultApi.applyRefund(currentOrderId.value, { reason: refundReason.value })
    ElMessage.success('退款申请已提交，请等待审核')
    showRefundModal.value = false
    getMyConsultOrders()
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error(error.response?.data?.msg || '申请退款失败')
  } finally {
    submitting.value = false
  }
}

const handleConsultSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getMyConsultOrders()
}

const handleConsultPageChange = (page) => {
  currentPage.value = page
  getMyConsultOrders()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatTimeRange = (startStr, endStr) => {
  if (!startStr || !endStr) return ''
  const start = new Date(startStr)
  const end = new Date(endStr)
  return `${String(start.getHours()).padStart(2, '0')}:${String(start.getMinutes()).padStart(2, '0')} - ${String(end.getHours()).padStart(2, '0')}:${String(end.getMinutes()).padStart(2, '0')}`
}

const getStatusType = (order) => {
  const status = order.rejectReason ? 'REFUND_REJECTED' : order.status
  const map = {
    PENDING_CONFIRM: 'warning',
    CONFIRMED: 'success',
    COMPLETED: 'info',
    CANCELLED: 'danger',
    REFUND_PENDING: 'warning',
    REFUNDED: 'info',
    REFUND_REJECTED: 'danger',
  }
  return map[status] || 'info'
}

const getStatusText = (order) => {
  const status = order.rejectReason ? 'REFUND_REJECTED' : order.status
  const map = {
    PENDING_CONFIRM: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUND_PENDING: '退款中',
    REFUNDED: '已退款',
    REFUND_REJECTED: '退款被拒',
  }
  return map[status] || status
}

onMounted(() => {
  getMyConsultOrders()
})
</script>

<style scoped>
.user-consult-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 30px;
  text-align: center;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 0.95rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.consult-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
}

.section-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 28px;
}

.loading-state,
.empty-state {
  padding: 80px 0;
  text-align: center;
}

.consult-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.consult-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 24px;
  padding: 20px 24px;
  transition: all 0.2s;
}

.consult-card:hover {
  border-color: #e2e0d7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.consult-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0efeb;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #6b6a62;
}

.consult-card-body {
  margin-bottom: 16px;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.creator-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.creator-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1a1a1a;
}

.consult-price {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: #b0aea3;
}

.message-preview,
.address-info,
.address-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #6b6a62;
  margin-top: 12px;
  padding: 8px 12px;
  background: #fcfbf7;
  border-radius: 16px;
}

.address-placeholder {
  color: #b0aea3;
}

.consult-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0efeb;
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: #1a1a1a;
}

.refund-dialog :deep(.el-dialog) {
  border-radius: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

@media (max-width: 768px) {
  .consult-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .consult-card-footer {
    justify-content: flex-start;
  }
}
</style>
