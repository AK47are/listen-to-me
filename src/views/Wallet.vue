<template>
  <div class="wallet-page">
    <!-- 头部和余额卡片部分保持不变 -->
    <div class="page-header">
      <h1 class="page-title">我的钱包</h1>
      <p class="page-subtitle">虚拟币余额与交易记录</p>
    </div>

    <div class="wallet-content">
      <!-- 余额卡片 -->
      <div class="balance-card">
        <div class="balance-icon">
          <el-icon><Wallet /></el-icon>
        </div>
        <div class="balance-info">
          <div class="balance-amount">
            <span class="label">当前余额</span>
            <span class="amount">{{ balanceInfo.balance }}</span>
            <span class="unit">虚拟币</span>
          </div>
          <el-button type="primary" size="large" round @click="showRechargeDialog = true">
            <el-icon><Wallet /></el-icon> 立即充值
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">累计充值</span>
            <span class="stat-value">{{ balanceInfo.totalRecharge }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">累计消费</span>
            <span class="stat-value">{{ balanceInfo.totalSpent }}</span>
          </div>
        </div>
      </div>

      <!-- 订单列表 -->
      <div class="order-section">
        <div class="order-tabs">
          <span
            :class="['tab-item', { active: activeTab === 'consult' }]"
            @click="switchTab('consult')"
            >咨询订单</span
          >
          <span
            :class="['tab-item', { active: activeTab === 'audio', disabled: true }]"
            @click="switchTab('audio')"
            >音频订单</span
          >
          <span
            :class="['tab-item', { active: activeTab === 'recharge', disabled: true }]"
            @click="switchTab('recharge')"
            >充值订单</span
          >
          <span
            :class="['tab-item', { active: activeTab === 'transaction', disabled: true }]"
            @click="switchTab('transaction')"
            >虚拟币流水</span
          >
        </div>

        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else-if="activeTab !== 'consult'" class="empty-state">
          <el-empty description="功能开发中，敬请期待" />
        </div>

        <div v-else-if="orderList.length === 0" class="empty-state">
          <el-empty description="暂无咨询订单" />
        </div>

        <div v-else class="order-list">
          <div
            v-for="order in orderList"
            :key="order.id"
            class="order-item"
            @click="showOrderDetail(order)"
          >
            <div class="order-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="order-info">
              <div class="order-header">
                <span class="order-type">咨询预约</span>
                <span class="order-sn">订单 #{{ order.id }}</span>
              </div>
              <div class="order-detail">
                <span class="order-title">{{ order.creatorName }}</span>
                <span class="order-amount">{{ order.price }} 虚拟币</span>
              </div>
              <div class="order-time">{{ formatDate(order.createTime) }}</div>
            </div>
            <div class="order-status" :class="getDisplayStatusClass(order)">
              {{ getDisplayStatusText(order) }}
            </div>
          </div>
        </div>

        <div v-if="total > 0 && !loading && activeTab === 'consult'" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            :total="total"
            :page-size="pagination.pageSize"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog
      v-model="showRechargeDialog"
      title="充值"
      width="440px"
      class="recharge-dialog"
      center
    >
      <el-form :model="rechargeForm" label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeForm.amount" :min="1" :max="10000" :step="10" />
          <span class="unit-hint">虚拟币</span>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio label="ALIPAY">支付宝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="recharge-dialog-footer">
          <el-button class="cancel-btn" @click="showRechargeDialog = false" round>取消</el-button>
          <el-button class="confirm-btn" @click="handleRecharge" round :loading="recharging"
            >确认充值</el-button
          >
        </div>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 - 优化后无滑动条 -->
    <el-dialog
      v-model="showDetailDialog"
      title="订单详情"
      width="560px"
      class="order-detail-dialog"
      center
    >
      <div v-if="currentOrder" class="order-detail-content">
        <div class="detail-section">
          <div class="section-title">预约信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">创作者</span>
              <span class="detail-value">{{ currentOrder.creatorName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">预约时间</span>
              <span class="detail-value">{{ formatDateTime(currentOrder.startTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">订单状态</span>
              <span class="detail-value status-badge" :class="getDisplayStatusClass(currentOrder)">
                {{ getDisplayStatusText(currentOrder) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">支付金额</span>
              <span class="detail-value price">{{ currentOrder.price }} 虚拟币</span>
            </div>
          </div>
        </div>

        <!-- 联系地址 -->
        <div class="detail-section">
          <div class="section-title">联系地址</div>
          <div v-if="currentOrder.address" class="detail-address">
            <el-icon><Location /></el-icon>
            <span>{{ currentOrder.address }}</span>
          </div>
          <div v-else class="detail-address-placeholder">
            <el-icon><Lock /></el-icon>
            <span>{{ getAddressPlaceholderText(currentOrder) }}</span>
          </div>
        </div>

        <!-- 咨询问题 -->
        <div class="detail-section" v-if="currentOrder.message">
          <div class="section-title">咨询问题</div>
          <div class="detail-message">{{ currentOrder.message }}</div>
        </div>

        <!-- 退款拒绝原因 -->
        <div class="detail-section" v-if="currentOrder.rejectReason">
          <div class="section-title">退款申请结果</div>
          <div class="detail-message reject">
            <el-icon><CircleClose /></el-icon>
            <span>{{ currentOrder.rejectReason }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">订单编号</span>
              <span class="detail-value">#{{ currentOrder.id }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatDate(currentOrder.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Wallet,
  Headset,
  ChatDotRound,
  ShoppingCart,
  Location,
  Lock,
  CircleClose,
} from '@element-plus/icons-vue'
import { balanceApi } from '@/api/user/balance'
import { consultApi } from '@/api/user/consult'

const activeTab = ref('consult')
const balanceInfo = reactive({
  balance: 0,
  totalRecharge: 0,
  totalSpent: 0,
})
const orderList = ref([])
const total = ref(0)
const loading = ref(false)
const recharging = ref(false)
const showRechargeDialog = ref(false)
const showDetailDialog = ref(false)
const currentOrder = ref(null)
const rechargeForm = ref({
  amount: 100,
  paymentMethod: 'ALIPAY',
})
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
})

const getDisplayStatus = (order) => {
  if (order.rejectReason) {
    return 'REFUND_REJECTED'
  }
  return order.status
}

const getDisplayStatusClass = (order) => {
  const displayStatus = getDisplayStatus(order)
  const map = {
    SUCCESS: 'success',
    COMPLETED: 'success',
    CONFIRMED: 'success',
    PENDING_CONFIRM: 'pending',
    CANCELLED: 'failed',
    REFUND_PENDING: 'pending',
    REFUNDED: 'failed',
    REFUND_REJECTED: 'rejected',
  }
  return map[displayStatus] || 'pending'
}

const getDisplayStatusText = (order) => {
  const displayStatus = getDisplayStatus(order)
  const map = {
    PENDING_CONFIRM: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUND_PENDING: '退款中',
    REFUNDED: '已退款',
    REFUND_REJECTED: '退款被拒',
    SUCCESS: '成功',
  }
  return map[displayStatus] || order.status
}

const getAddressPlaceholderText = (order) => {
  const displayStatus = getDisplayStatus(order)
  if (displayStatus === 'PENDING_CONFIRM') {
    return '创作者确认后可查看联系地址'
  }
  if (displayStatus === 'CANCELLED') {
    return '订单已取消，无法查看地址'
  }
  if (displayStatus === 'REFUND_PENDING' || displayStatus === 'REFUNDED') {
    return '订单已退款，无法查看地址'
  }
  if (displayStatus === 'REFUND_REJECTED') {
    return '退款申请被拒绝，请联系客服'
  }
  return '暂无可查看地址'
}

const switchTab = (tab) => {
  if (tab !== 'consult') {
    ElMessage.info('功能开发中，敬请期待')
    return
  }
  activeTab.value = tab
  pagination.value.pageNum = 1
  if (tab === 'consult') {
    fetchConsultOrders()
  }
}

const getBalance = async () => {
  try {
    const res = await balanceApi.getBalance()
    Object.assign(balanceInfo, res)
  } catch (error) {
    ElMessage.error('获取余额失败')
  }
}

const fetchConsultOrders = async () => {
  loading.value = true
  try {
    const res = await consultApi.getMyConsultPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    orderList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取咨询订单失败', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const showOrderDetail = (order) => {
  currentOrder.value = order
  showDetailDialog.value = true
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  fetchConsultOrders()
}

const handleRecharge = async () => {
  recharging.value = true
  try {
    const res = await balanceApi.recharge(rechargeForm.value)
    window.open(res.payUrl, '_blank')
    ElMessage.success('充值链接已打开，请在支付宝中完成支付')
    showRechargeDialog.value = false
    rechargeForm.value.amount = 100
    await getBalance()
  } catch (error) {
    ElMessage.error('充值失败')
  } finally {
    recharging.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  getBalance()
  fetchConsultOrders()
})
</script>

<style scoped>
/* 基础样式保持不变，只修改对话框相关 */
.wallet-page {
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

.wallet-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
}

.balance-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
  padding: 32px 36px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.balance-icon {
  width: 64px;
  height: 64px;
  background: #f7f6f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.balance-icon .el-icon {
  font-size: 32px;
  color: #1a1a1a;
}

.balance-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.balance-amount .label {
  font-size: 0.9rem;
  color: #8e8c84;
}

.balance-amount .amount {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a1a1a;
}

.balance-amount .unit {
  font-size: 0.85rem;
  color: #b0aea3;
}

.balance-info .el-button {
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 10px 28px;
  font-weight: 600;
}

.balance-info .el-button:hover {
  background: #2c2c2c;
  transform: scale(0.98);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s;
}

.stat-card:hover {
  border-color: #e2e0d7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: #f7f6f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: #1a1a1a;
}

.stat-info {
  flex: 1;
}

.stat-label {
  display: block;
  font-size: 0.8rem;
  color: #b0aea3;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a1a;
}

.order-section {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
  padding: 28px 32px;
}

.order-tabs {
  display: flex;
  gap: 32px;
  border-bottom: 1px solid #efeee8;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-item {
  padding: 8px 0;
  font-size: 0.95rem;
  font-weight: 500;
  color: #8e8c84;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.tab-item:hover {
  color: #1a1a1a;
}

.tab-item.active {
  color: #1a1a1a;
  border-bottom-color: #1a1a1a;
}

.tab-item.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.tab-item.disabled:hover {
  color: #8e8c84;
}

.loading-state,
.empty-state {
  padding: 60px 0;
  text-align: center;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border: 1px solid #efeee8;
  border-radius: 20px;
  transition: all 0.2s;
  cursor: pointer;
}

.order-item:hover {
  border-color: #e2e0d7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  transform: translateY(-1px);
}

.order-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  background: #f7f6f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.order-icon .el-icon {
  font-size: 20px;
  color: #1a1a1a;
}

.order-info {
  flex: 1;
}

.order-header {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 8px;
}

.order-type {
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a1a1a;
}

.order-sn {
  font-size: 0.7rem;
  color: #b0aea3;
}

.order-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-title {
  font-size: 0.9rem;
  color: #6b6a62;
}

.order-amount {
  font-size: 0.9rem;
  font-weight: 700;
  color: #1a1a1a;
}

.order-time {
  font-size: 0.7rem;
  color: #b0aea3;
}

.order-status {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.order-status.success {
  background: #f0f9f0;
  color: #67c23a;
}

.order-status.pending {
  background: #fff7e6;
  color: #e6a23c;
}

.order-status.failed {
  background: #fef0f0;
  color: #f56c6c;
}

.order-status.rejected {
  background: #fef0f0;
  color: #e5484d;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 充值对话框 */
.recharge-dialog :deep(.el-dialog) {
  border-radius: 24px;
  background: #ffffff;
}

.recharge-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 8px;
  border-bottom: 1px solid #f0efeb;
}

.recharge-dialog :deep(.el-dialog__title) {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a1a;
}

.recharge-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.recharge-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0efeb;
}

.unit-hint {
  margin-left: 8px;
  font-size: 0.8rem;
  color: #b0aea3;
}

/* 订单详情对话框 - 无滑动条优化 */
.order-detail-dialog :deep(.el-dialog) {
  border-radius: 28px;
  background: #ffffff;
  width: 560px;
}

.order-detail-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 16px;
  border-bottom: 1px solid #efeee8;
}

.order-detail-dialog :deep(.el-dialog__title) {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a1a1a;
}

.order-detail-dialog :deep(.el-dialog__body) {
  padding: 20px 28px;
  max-height: none;
  overflow: visible;
}

.order-detail-dialog :deep(.el-dialog__footer) {
  display: none;
}

.order-detail-content {
  max-height: none;
  overflow: visible;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: #8e8c84;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid #efeee8;
}

/* 使用网格布局替代原来的单行列表 */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.detail-item .detail-label {
  color: #b0aea3;
  font-size: 0.85rem;
}

.detail-item .detail-value {
  color: #1a1a1a;
  font-weight: 500;
  font-size: 0.85rem;
}

.detail-value.price {
  font-weight: 700;
  color: #1a1a1a;
}

.detail-value.status-badge {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
}

.detail-message {
  background: #fcfbf7;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 0.9rem;
  line-height: 1.5;
  color: #6b6a62;
}

.detail-message.reject {
  background: #fef0f0;
  color: #e5484d;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-address,
.detail-address-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fcfbf7;
  padding: 10px 16px;
  border-radius: 16px;
  font-size: 0.85rem;
  word-break: break-all;
}

.detail-address {
  color: #1a1a1a;
}

.detail-address-placeholder {
  color: #b0aea3;
}

.detail-address .el-icon,
.detail-address-placeholder .el-icon {
  color: #b0aea3;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .balance-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  .balance-info {
    flex-direction: column;
  }
  .balance-amount {
    flex-wrap: wrap;
    justify-content: center;
  }
  .stats-row {
    grid-template-columns: 1fr;
  }
  .order-item {
    flex-wrap: wrap;
  }
  .order-status {
    margin-left: 60px;
  }
  .order-tabs {
    gap: 16px;
  }
  .tab-item {
    font-size: 0.85rem;
  }
  .order-detail-dialog :deep(.el-dialog) {
    width: calc(100% - 40px) !important;
  }
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}

/* 充值对话框按钮样式 */
.recharge-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.recharge-dialog-footer .cancel-btn {
  background: transparent;
  border: 1px solid #e8e6df;
  color: #1a1a1a;
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.2s;
}

.recharge-dialog-footer .cancel-btn:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
  transform: translateY(-1px);
}

.recharge-dialog-footer .confirm-btn {
  background: #1a1a1a;
  border: none;
  color: #ffffff;
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.2s;
}

.recharge-dialog-footer .confirm-btn:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.recharge-dialog-footer .confirm-btn.is-loading {
  opacity: 0.8;
  transform: none;
}
</style>
